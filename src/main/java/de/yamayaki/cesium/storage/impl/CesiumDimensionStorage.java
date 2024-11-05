package de.yamayaki.cesium.storage.impl;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.common.spec.WorldDatabaseSpecs;
import de.yamayaki.cesium.storage.IComponentStorage;
import de.yamayaki.cesium.storage.IDimensionStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.nio.file.Path;

public class CesiumDimensionStorage implements IDimensionStorage {
    private final IDBInstance dimensionDatabase;

    private final IComponentStorage<ChunkPos, CompoundTag> chunkStorage;
    private final IComponentStorage<ChunkPos, CompoundTag> poiStorage;
    private final IComponentStorage<ChunkPos, CompoundTag> entityStorage;

    public CesiumDimensionStorage(final Path basePath) {
        this.dimensionDatabase = CesiumMod.openWorldDB(basePath);

        this.chunkStorage = new CesiumComponent<>(this.dimensionDatabase, WorldDatabaseSpecs.CHUNK_DATA);
        this.poiStorage = new CesiumComponent<>(this.dimensionDatabase, WorldDatabaseSpecs.POI);
        this.entityStorage = new CesiumComponent<>(this.dimensionDatabase, WorldDatabaseSpecs.ENTITY);
    }

    protected IDBInstance dimensionDatabase() {
        return this.dimensionDatabase;
    }

    @Override
    public IComponentStorage<ChunkPos, CompoundTag> chunkStorage() {
        return this.chunkStorage;
    }

    @Override
    public IComponentStorage<ChunkPos, CompoundTag> poiStorage() {
        return this.poiStorage;
    }

    @Override
    public IComponentStorage<ChunkPos, CompoundTag> entityStorage() {
        return this.entityStorage;
    }

    @Override
    public void flush() {
        this.dimensionDatabase.flushChanges();
    }

    @Override
    public void close() {
        this.dimensionDatabase.close();
    }
}
