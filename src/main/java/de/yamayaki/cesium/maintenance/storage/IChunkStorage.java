package de.yamayaki.cesium.maintenance.storage;

import net.minecraft.world.level.ChunkPos;

import java.util.List;

public interface IChunkStorage extends AutoCloseable {
    List<ChunkPos> getAllChunks();

    void flush();

    /**
     * ChunkData
     **/
    void setChunkData(ChunkPos chunkPos, byte[] bytes);

    byte[] getChunkData(ChunkPos chunkPos);

    /**
     * POIData
     **/
    void setPOIData(ChunkPos chunkPos, byte[] bytes);

    byte[] getPOIData(ChunkPos chunkPos);

    /**
     * EntityData
     **/
    void setEntityData(ChunkPos chunkPos, byte[] bytes);

    byte[] getEntityData(ChunkPos chunkPos);
}
