package de.yamayaki.cesium.converter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public interface IChunkStorage {
    List<ChunkPos> getAllChunks();

    void flush();

    void close();

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
