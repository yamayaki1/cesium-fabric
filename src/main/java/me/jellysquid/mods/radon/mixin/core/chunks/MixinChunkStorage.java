package me.jellysquid.mods.radon.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.ChunkDatabaseAccess;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(ChunkStorage.class)
public class MixinChunkStorage implements ChunkDatabaseAccess {
    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    @Shadow
    @Nullable
    private LegacyStructureDataHandler legacyStructureHandler;

    private LMDBInstance database;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(Path path, DataFixer dataFixer, boolean bl, CallbackInfo ci) {
        try {
            this.worker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worker = null;
    }

    @Overwrite
    public boolean isOldChunkAround(ChunkPos chunkPos, int i) {
        //TODO implement
        return false;
    }

    @Overwrite
    public CompletableFuture<Optional<CompoundTag>> read(ChunkPos chunkPos) {
        return CompletableFuture.supplyAsync(() -> {
            CompoundTag compoundTag = this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA)
                    .getValue(chunkPos);

            return Optional.ofNullable(compoundTag);
        }, Util.backgroundExecutor());
    }

    @Overwrite
    public void write(ChunkPos chunkPos, CompoundTag compoundTag) {
        CompletableFuture.supplyAsync(() -> {
            this.database
                    .getTransaction(WorldDatabaseSpecs.CHUNK_DATA)
                    .add(chunkPos, compoundTag);

            if (this.legacyStructureHandler != null) {
                this.legacyStructureHandler.removeIndex(chunkPos.toLong());
            }

            return null;
        }, Util.backgroundExecutor());
    }

    @Overwrite
    public void flushWorker() {

    }

    @Overwrite
    public void close() {

    }

    @Override
    public void setDatabase(LMDBInstance database) {
        this.database = database;
    }
}
