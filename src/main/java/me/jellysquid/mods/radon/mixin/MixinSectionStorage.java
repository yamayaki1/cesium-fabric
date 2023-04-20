package me.jellysquid.mods.radon.mixin;

import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.ChunkDatabaseAccess;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SectionStorage;
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
import java.util.function.Function;

@Mixin(SectionStorage.class)
public class MixinSectionStorage<R> implements ChunkDatabaseAccess {
    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    private LMDBInstance database;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(Path path, Function<?, ?> function, Function<?, ?> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl, RegistryAccess registryAccess, LevelHeightAccessor levelHeightAccessor, CallbackInfo ci) {
        try {
            this.worker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worker = null;
    }

    @Override
    public void setDatabase(LMDBInstance storage) {
        this.database = storage;
    }

    @Redirect(method = "tryRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/IOWorker;loadAsync(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Optional<CompoundTag>> redirectTryRead(IOWorker storageIoWorker, ChunkPos pos) {
        CompoundTag compoundTag = this.database
                .getDatabase(WorldDatabaseSpecs.POI)
                .getValue(pos);

        return CompletableFuture.completedFuture(Optional.ofNullable(compoundTag));
    }

    @Redirect(method = "writeColumn(Lnet/minecraft/world/level/ChunkPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/IOWorker;store(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> redirectWriteColumn(IOWorker storageIoWorker, ChunkPos pos, CompoundTag nbt) {
        this.database
                .getTransaction(WorldDatabaseSpecs.POI)
                .add(pos, nbt);

        return null;
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/IOWorker;close()V"))
    private void redirectClose(IOWorker storageIoWorker) {

    }
}
