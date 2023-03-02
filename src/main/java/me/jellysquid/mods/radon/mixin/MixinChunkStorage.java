package me.jellysquid.mods.radon.mixin;

import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.ChunkDatabaseAccess;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.impl.WorldDatabaseSpecs;
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

import java.io.File;
import java.io.IOException;

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
    private void reinit(File file, DataFixer dataFixer, boolean bl, CallbackInfo ci) {
        try {
            this.worker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worker = null;
    }

    @Overwrite
    public @Nullable CompoundTag read(ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA)
                .getValue(chunkPos);
    }

    @Overwrite
    public void write(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.database
                .getTransaction(WorldDatabaseSpecs.CHUNK_DATA)
                .add(chunkPos, compoundTag);

        if (this.legacyStructureHandler != null) {
            this.legacyStructureHandler.removeIndex(chunkPos.toLong());
        }
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
