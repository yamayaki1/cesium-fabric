package de.yamayaki.cesium.maintenance.storage;

import net.minecraft.world.level.ChunkPos;

import java.util.List;

public interface IChunkStorage extends AutoCloseable {
    List<Region> getAllRegions();

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

    record Region(int x, int z, ChunkPos[] chunks) {
        public static Region create(final int x, final int z) {
            return new Region(x, z, new ChunkPos[1024]);
        }

        public void addChunk(final ChunkPos chunkPos) {
            if(chunkPos.getRegionX() != this.x || chunkPos.getRegionZ() != this.z) {
                throw new IllegalStateException("Chunk does not belong to this region!");
            }

            this.chunks[chunkPos.getRegionLocalX() + (32 * chunkPos.getRegionLocalZ())] = chunkPos;
        }
    }
}
