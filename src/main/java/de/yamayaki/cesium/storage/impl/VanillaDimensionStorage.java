package de.yamayaki.cesium.storage.impl;

import de.yamayaki.cesium.storage.IComponentStorage;
import de.yamayaki.cesium.storage.IDimensionStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.io.IOException;
import java.nio.file.Path;

public class VanillaDimensionStorage implements IDimensionStorage {
    private final RegionComponent chunkStorage;
    private final RegionComponent poiStorage;
    private final RegionComponent entityStorage;

    public VanillaDimensionStorage(final Path basePath) {
        this.chunkStorage = new RegionComponent(basePath, "region");
        this.poiStorage = new RegionComponent(basePath, "poi");
        this.entityStorage = new RegionComponent(basePath, "entities");
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
        try {
            this.chunkStorage.flush();
            this.entityStorage.flush();
            this.poiStorage.flush();
        } catch (final IOException i) {
            throw new IllegalStateException("Could not flush storage(s)", i);
        }
    }

    @Override
    public void close() {
        try {
            this.chunkStorage.close();
            this.entityStorage.close();
            this.poiStorage.close();
        } catch (final IOException i) {
            throw new IllegalStateException("Could not flush storage(s)", i);
        }
    }
}
