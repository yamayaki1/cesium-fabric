package de.yamayaki.cesium.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.common.db.DatabaseItem;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(EntityStorage.class)
public class MixinEntityStorage {
    protected LMDBInstance storage;

    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void reinit(ServerLevel serverLevel, Path path, DataFixer dataFixer, boolean bl, Executor executor, CallbackInfo ci) {
        this.storage = ((DatabaseItem) serverLevel).getStorage();

        try {
            this.worker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worker = null;
    }

    @Redirect(method = "loadEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/IOWorker;loadAsync(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Optional<CompoundTag>> redirectReLoadEntities(IOWorker storageIoWorker, ChunkPos pos) {
        return CompletableFuture.supplyAsync(() -> {
            CompoundTag compoundTag = this.storage
                    .getDatabase(WorldDatabaseSpecs.ENTITY)
                    .getValue(pos);

            return Optional.ofNullable(compoundTag);
        }, Util.backgroundExecutor());
    }

    @Redirect(method = "storeEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/IOWorker;store(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> redirectStoreEntities(IOWorker instance, ChunkPos pos, @Nullable CompoundTag nbt) {
        return CompletableFuture.supplyAsync(() -> {
            this.storage
                    .getTransaction(WorldDatabaseSpecs.ENTITY)
                    .add(pos, nbt);

            return null;
        }, Util.backgroundExecutor());
    }

    @Redirect(method = "flush", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/IOWorker;synchronize(Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> redirectFlush(IOWorker instance, boolean bl) {
        return CompletableFuture.completedFuture(null);
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/IOWorker;close()V"))
    private void redirectClose(IOWorker instance) {
    }
}
