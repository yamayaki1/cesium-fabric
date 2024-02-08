package de.yamayaki.cesium.mixin.core.storage;

import com.mojang.datafixers.util.Either;
import de.yamayaki.cesium.accessor.DatabaseActions;
import de.yamayaki.cesium.accessor.DatabaseSetter;
import de.yamayaki.cesium.accessor.SpecificationSetter;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(IOWorker.class)
public abstract class MixinIOWorker implements DatabaseSetter, SpecificationSetter, DatabaseActions {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private RegionFileStorage storage;

    @Shadow
    @Final
    private Map<ChunkPos, IOWorker.PendingStore> pendingWrites;

    @Shadow
    protected abstract <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> supplier);

    @Unique
    private LMDBInstance lmdbStorage;

    @Unique
    private DatabaseSpec<ChunkPos, CompoundTag> databaseSpec;

    @Unique
    private boolean isCesium = false;

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
                    CompoundTag compoundTag;
                    if (this.isCesium) {
                        compoundTag = this.lmdbStorage
                                .getDatabase(this.databaseSpec)
                                .getValue(chunkPos);
                    } else {
                        compoundTag = this.storage.read(chunkPos);
                    }

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
        CompletableFuture<Void> completableFuture = this.submitTask(() -> Either.left(
                CompletableFuture.allOf(
                        this.pendingWrites.values().stream()
                                .map((pendingStore) -> pendingStore.result)
                                .toArray(CompletableFuture[]::new))
        )).thenCompose(Function.identity());

        return bl ? completableFuture.thenCompose((void_) -> this.submitTask(() -> {
            try {
                if (!this.isCesium) {
                    this.storage.flush();
                }

                return Either.left(null);
            } catch (Exception var2) {
                LOGGER.warn("Failed to synchronize chunks", var2);
                return Either.right(var2);
            }
        })) : completableFuture.thenCompose((void_) -> this.submitTask(() -> Either.left(null)));
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
                    if (this.isCesium) {
                        this.lmdbStorage.getDatabase(this.databaseSpec).scan(chunkPos, streamTagVisitor);
                    } else {
                        this.storage.scanChunk(chunkPos, streamTagVisitor);
                    }
                }

                return Either.left(null);
            } catch (Exception var4) {
                LOGGER.warn("Failed to bulk scan chunk {}", chunkPos, var4);
                return Either.right(var4);
            }
        });
    }

    @Redirect(method = "runStore", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"))
    private void cesium$write(RegionFileStorage instance, ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
        if (this.isCesium) {
            this.lmdbStorage.getTransaction(this.databaseSpec).add(chunkPos, compoundTag);
        } else {
            this.storage.write(chunkPos, compoundTag);
        }
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;close()V"))
    private void cesium$close(RegionFileStorage instance) throws IOException {
        if (!this.isCesium) {
            this.storage.close();
        }
    }

    public void cesium$flush() {
        this.lmdbStorage.flushChanges();
    }

    public void cesium$close() {
        this.lmdbStorage.close();
    }

    @Override
    public void cesium$setStorage(LMDBInstance lmdbInstance) {
        this.isCesium = true;
        this.lmdbStorage = lmdbInstance;

        try {
            this.storage.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void cesium$setSpec(DatabaseSpec<?, ?> databaseSpec) {
        this.databaseSpec = (DatabaseSpec<ChunkPos, CompoundTag>) databaseSpec;
    }
}
