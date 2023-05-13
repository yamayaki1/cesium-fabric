package de.yamayaki.cesium.common;

import de.yamayaki.cesium.common.db.KVDatabase;
import de.yamayaki.cesium.common.db.KVTransaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public interface KVProvider {
    KVDatabase<ChunkPos, CompoundTag> getDatabase();

    KVTransaction<ChunkPos, CompoundTag> getTransaction();
}
