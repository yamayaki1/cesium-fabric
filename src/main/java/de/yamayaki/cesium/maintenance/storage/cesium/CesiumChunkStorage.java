package de.yamayaki.cesium.maintenance.storage.cesium;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.api.db.ICloseableIterator;
import de.yamayaki.cesium.api.db.IDBInstance;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import de.yamayaki.cesium.maintenance.storage.IChunkStorage;
import net.minecraft.nbt.CompoundTag;
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
    public List<ChunkPos> getAllChunks() {
        final List<ChunkPos> list = new ArrayList<>();

        try (final ICloseableIterator<ChunkPos> crs = this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA).getIterator()) {
            while (crs.hasNext()) {
                list.add(crs.next());
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Could not iterate on cursor.", t);
        }

        return list;
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
    public void setChunkData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        this.database.getTransaction(WorldDatabaseSpecs.CHUNK_DATA).add(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getChunkData(final ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA).getValue(chunkPos);
    }

    @Override
    public void setPOIData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        this.database.getTransaction(WorldDatabaseSpecs.POI).add(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getPOIData(final ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.POI).getValue(chunkPos);
    }

    @Override
    public void setEntityData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        this.database.getTransaction(WorldDatabaseSpecs.ENTITY).add(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getEntityData(final ChunkPos chunkPos) {
        return this.database.getDatabase(WorldDatabaseSpecs.ENTITY).getValue(chunkPos);
    }
}
