package de.yamayaki.cesium.mixin.core.storage;

import com.mojang.datafixers.util.Either;
import de.yamayaki.cesium.common.IOWorkerExtended;
import de.yamayaki.cesium.common.KVProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(IOWorker.class)
public abstract class MixinIOWorker implements IOWorkerExtended {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private RegionFileStorage storage;
    @Shadow
    @Final
    private Map<ChunkPos, IOWorker.PendingStore> pendingWrites;
    private KVProvider provider;

    @Shadow
    protected abstract <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> supplier);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void reinit(Path path, boolean bl, String string, CallbackInfo ci) throws IOException {
        this.storage.close();
    }

    /**
     * @author Yamayaki
     * @reason Stupid lambda
     */
    @Overwrite
    public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos chunkPos) {
        return this.submitTask(() -> {
            IOWorker.PendingStore pendingStore = this.pendingWrites.get(chunkPos);
            if (pendingStore != null) {
                return Either.left(Optional.ofNullable(pendingStore.data));
            } else {
                try {
                    CompoundTag compoundTag = this.provider.getDatabase().getValue(chunkPos);
                    return Either.left(Optional.ofNullable(compoundTag));
                } catch (Exception var4) {
                    LOGGER.warn("Failed to read chunk {}", chunkPos, var4);
                    return Either.right(var4);
                }
            }
        });
    }

    /**
     * @author Yamayaki
     * @reason Stupid lambda
     */
    @Overwrite
    public CompletableFuture<Void> synchronize(boolean bl) {
        CompletableFuture<Void> completableFuture = this.submitTask(
                        () -> Either.left(
                                CompletableFuture.allOf(
                                        this.pendingWrites.values().stream().map(pendingStore -> pendingStore.result).toArray(CompletableFuture[]::new)
                                )
                        )
                )
                .thenCompose(Function.identity());
        return bl ? completableFuture.thenCompose(void_ -> this.submitTask(() -> {
            try {
                return Either.left(null);
            } catch (Exception var2x) {
                LOGGER.warn("Failed to synchronize chunks", var2x);
                return Either.right(var2x);
            }
        })) : completableFuture.thenCompose(void_ -> this.submitTask(() -> Either.left(null)));
    }

    /**
     * @author Yamayaki
     * @reason Stupid lambda
     */
    @Overwrite
    public CompletableFuture<Void> scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) {
        return this.submitTask(() -> {
            try {
                IOWorker.PendingStore pendingStore = this.pendingWrites.get(chunkPos);
                if (pendingStore != null) {
                    if (pendingStore.data != null) {
                        pendingStore.data.acceptAsRoot(streamTagVisitor);
                    }
                } else {
                    this.provider.getDatabase().scan(chunkPos, streamTagVisitor);
                }

                return Either.left(null);
            } catch (Exception var4) {
                LOGGER.warn("Failed to bulk scan chunk {}", chunkPos, var4);
                return Either.right(var4);
            }
        });
    }

    @Redirect(method = "runStore", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"))
    private void runStore$write(RegionFileStorage instance, ChunkPos chunkPos, CompoundTag compoundTag) {
        this.provider.getTransaction().add(chunkPos, compoundTag);
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;close()V"))
    private void close$close(RegionFileStorage instance) {
        // Do nothing
    }

    @Override
    public void setKVProvider(KVProvider provider) {
        this.provider = provider;
    }
}
