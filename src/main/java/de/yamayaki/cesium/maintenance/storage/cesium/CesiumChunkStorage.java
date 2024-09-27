package de.yamayaki.cesium.maintenance.storage.cesium;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.api.database.DatabaseSpec;
import de.yamayaki.cesium.api.database.ICloseableIterator;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.common.lmdb.LMDBInstance;
import de.yamayaki.cesium.common.spec.WorldDatabaseSpecs;
import de.yamayaki.cesium.maintenance.storage.IChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.level.ChunkPos;
import org.lmdbjava.LmdbException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CesiumChunkStorage implements IChunkStorage {
    private final IDBInstance database;

    public CesiumChunkStorage(final Path basePath) {
        this.database = new LMDBInstance(basePath, "chunks", new DatabaseSpec[]{
                WorldDatabaseSpecs.CHUNK_DATA,
                WorldDatabaseSpecs.POI,
                WorldDatabaseSpecs.ENTITY
        });
    }


    @Override
    public List<Region> getAllRegions() {
        final Long2ObjectMap<Region> regionMap = new Long2ObjectArrayMap<>();

        try (final ICloseableIterator<ChunkPos> crs = this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA).getIterator()) {
            ChunkPos chunkPos;
            Region region;
            long regionKey;

            while (crs.hasNext()) {
                chunkPos = crs.next();

                regionKey = regionKey(chunkPos.getRegionX(), chunkPos.getRegionZ());
                region = regionMap.get(regionKey);

                if(region == null) {
                    region = Region.create(chunkPos.getRegionX(), chunkPos.getRegionZ());
                    regionMap.put(regionKey, region);
                }

                region.addChunk(chunkPos);
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Could not iterate on cursor.", t);
        }

        return regionMap.values().stream().toList();
    }

    private static long regionKey(final int x, final int z) {
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }

    @Override
    public void flush() {
        try {
            this.database.flushChanges();
        } catch (LmdbException lmdbException) {
            CesiumMod.logger().info(lmdbException);
        }
    }

    @Override
    public void close() {
        this.flush();
        this.database.close();
    }

    @Override
    public void setChunkData(final ChunkPos chunkPos, final byte[] bytes) {
        this.database.getTransaction(WorldDatabaseSpecs.CHUNK_DATA).addBytes(chunkPos, bytes);
    }

    @Override
    public byte[] getChunkData(final ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA).getBytes(chunkPos);
    }

    @Override
    public void setPOIData(final ChunkPos chunkPos, final byte[] bytes) {
        this.database.getTransaction(WorldDatabaseSpecs.POI).addBytes(chunkPos, bytes);
    }

    @Override
    public byte[] getPOIData(final ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.POI).getBytes(chunkPos);
    }

    @Override
    public void setEntityData(final ChunkPos chunkPos, final byte[] bytes) {
        this.database.getTransaction(WorldDatabaseSpecs.ENTITY).addBytes(chunkPos, bytes);
    }

    @Override
    public byte[] getEntityData(final ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.ENTITY).getBytes(chunkPos);
    }
}
