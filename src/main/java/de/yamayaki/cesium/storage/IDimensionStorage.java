package de.yamayaki.cesium.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public interface IDimensionStorage {
    IComponentStorage<ChunkPos, CompoundTag> chunkStorage();
    IComponentStorage<ChunkPos, CompoundTag> poiStorage();
    IComponentStorage<ChunkPos, CompoundTag> entityStorage();

    void flush();

    void close();
}
