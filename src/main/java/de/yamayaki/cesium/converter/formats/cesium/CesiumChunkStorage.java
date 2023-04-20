package de.yamayaki.cesium.converter.formats.cesium;

import de.yamayaki.cesium.common.db.lightning.Csr;
import de.yamayaki.cesium.common.db.lightning.LmdbException;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import de.yamayaki.cesium.converter.IChunkStorage;
import de.yamayaki.cesium.common.db.LMDBInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CesiumChunkStorage implements IChunkStorage {
    private final Logger logger;

    private final LMDBInstance database;

    public CesiumChunkStorage(final Logger logger, final Path basePath) {
        this.logger = logger;

        this.database = new LMDBInstance(basePath.toFile(), "chunks", new DatabaseSpec[]{
                WorldDatabaseSpecs.CHUNK_DATA,
                WorldDatabaseSpecs.POI,
                WorldDatabaseSpecs.ENTITY
        });
    }


    @Override
    public List<ChunkPos> getAllChunks() {
        final List<ChunkPos> list = new ArrayList<>();

        final Csr cursor = this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA)
                .getIterator();

        while (cursor.hasNext()) {
            final ChunkPos chunkPos = this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA)
                    .getKeySerializer().deserializeKey(cursor.next());
            list.add(chunkPos);
        }

        cursor.close();
        return list;
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
