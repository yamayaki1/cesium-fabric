package me.jellysquid.mods.radon.converter.formats.radon;

import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.lightning.LmdbException;
import me.jellysquid.mods.radon.common.db.spec.DatabaseSpec;
import me.jellysquid.mods.radon.common.db.spec.impl.WorldDatabaseSpecs;
import me.jellysquid.mods.radon.converter.IChunkStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class RadonChunkStorage implements IChunkStorage {
    private final Logger logger;

    private final LMDBInstance database;

    public RadonChunkStorage(Logger logger, Path basePath) {
        this.logger = logger;

        this.database = new LMDBInstance(basePath.toFile(), "chunks", new DatabaseSpec[]{
                WorldDatabaseSpecs.CHUNK_DATA,
                WorldDatabaseSpecs.POI,
                WorldDatabaseSpecs.ENTITY
        });
    }


    @Override
    public List<ChunkPos> getAllChunks() {
        return null;
    }

    @Override
    public void flush() {
        try {
            this.database.flushChanges();
        } catch (LmdbException lmdbException) {
            this.logger.info(lmdbException);
        }
    }

    @Override
    public void close() {
        this.flush();
        this.database.close();
    }

    @Override
    public void setChunkData(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.database.getTransaction(WorldDatabaseSpecs.CHUNK_DATA).add(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getChunkData(ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA).getValue(chunkPos);
    }

    @Override
    public void setPOIData(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.database.getTransaction(WorldDatabaseSpecs.POI).add(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getPOIData(ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.POI).getValue(chunkPos);
    }

    @Override
    public void setEntityData(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.database.getTransaction(WorldDatabaseSpecs.ENTITY).add(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getEntityData(ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.ENTITY).getValue(chunkPos);
    }
}
