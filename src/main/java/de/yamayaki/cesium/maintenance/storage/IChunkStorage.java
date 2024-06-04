package de.yamayaki.cesium.maintenance.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public interface IChunkStorage extends AutoCloseable {
    List<ChunkPos> getAllChunks();

    void flush();

    /**
     * ChunkData
     **/
    void setChunkData(ChunkPos chunkPos, CompoundTag compoundTag);

    CompoundTag getChunkData(ChunkPos chunkPos);

    /**
     * POIData
     **/
    void setPOIData(ChunkPos chunkPos, CompoundTag compoundTag);

    CompoundTag getPOIData(ChunkPos chunkPos);

    /**
     * EntityData
     **/
    void setEntityData(ChunkPos chunkPos, CompoundTag compoundTag);

    CompoundTag getEntityData(ChunkPos chunkPos);
}
