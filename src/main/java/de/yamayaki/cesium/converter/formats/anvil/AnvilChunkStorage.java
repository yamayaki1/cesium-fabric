package de.yamayaki.cesium.converter.formats.anvil;

import com.google.common.collect.ImmutableList;
import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.converter.IChunkStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnvilChunkStorage implements IChunkStorage {
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");

    private final Path basePath;

    private final RegionFileStorage chunkData;
    private final RegionFileStorage poiData;
    private final RegionFileStorage entityData;

    public AnvilChunkStorage(final Path basePath) {
        this.basePath = basePath;

        this.chunkData = new RegionFileStorage(basePath.resolve("region"), false);
        this.poiData = new RegionFileStorage(basePath.resolve("poi"), false);
        this.entityData = new RegionFileStorage(basePath.resolve("entities"), false);
    }

    @Override
    public List<ChunkPos> getAllChunks() {
        final File regionsFolder = new File(this.basePath.toFile(), "region");
        final File[] files = regionsFolder.listFiles((filex, string) -> string.endsWith(".mca"));

        if (files == null) {
            return ImmutableList.of();
        }

        final List<ChunkPos> list = new ArrayList<>();

        for (File region : files) {
            Matcher matcher = REGEX.matcher(region.getName());
            if (matcher.matches()) {
                final int regionX = Integer.parseInt(matcher.group(1)) << 5;
                final int regionZ = Integer.parseInt(matcher.group(2)) << 5;

                for (int chunkX = 0; chunkX < 32; ++chunkX) {
                    for (int chunkY = 0; chunkY < 32; ++chunkY) {
                        list.add(new ChunkPos(chunkX + regionX, chunkY + regionZ));
                    }
                }
            }
        }

        return list;
    }

    @Override
    public void flush() {
        try {
            this.chunkData.flush();
            this.poiData.flush();
            this.chunkData.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.flush();

        try {
            this.chunkData.close();
            this.poiData.close();
            this.entityData.close();
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to close chunk storage", exception);
        }
    }

    @Override
    public void setChunkData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        try {
            this.chunkData.write(chunkPos, compoundTag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompoundTag getChunkData(final ChunkPos chunkPos) {
        try {
            return this.chunkData.read(chunkPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPOIData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        try {
            this.poiData.write(chunkPos, compoundTag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompoundTag getPOIData(final ChunkPos chunkPos) {
        try {
            return this.poiData.read(chunkPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setEntityData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        try {
            this.entityData.write(chunkPos, compoundTag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompoundTag getEntityData(final ChunkPos chunkPos) {
        try {
            return this.entityData.read(chunkPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
