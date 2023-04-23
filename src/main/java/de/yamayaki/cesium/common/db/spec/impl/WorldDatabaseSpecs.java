package de.yamayaki.cesium.common.db.spec.impl;

import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.io.compression.DefaultStreamCompressors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public class WorldDatabaseSpecs {
    public static final DatabaseSpec<ChunkPos, CompoundTag> CHUNK_DATA =
            new DatabaseSpec<>("chunks", ChunkPos.class, CompoundTag.class, DefaultStreamCompressors.ZSTD, 8 * 1024 * 1024);

    public static final DatabaseSpec<ChunkPos, CompoundTag> POI =
            new DatabaseSpec<>("poi", ChunkPos.class, CompoundTag.class, DefaultStreamCompressors.ZSTD, 512 * 1024);

    public static final DatabaseSpec<ChunkPos, CompoundTag> ENTITY =
            new DatabaseSpec<>("entity", ChunkPos.class, CompoundTag.class, DefaultStreamCompressors.ZSTD, 512 * 1024);
}
