package me.jellysquid.mods.radon.converter.formats.anvil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.jellysquid.mods.radon.converter.IChunkStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnvilChunkStorage implements IChunkStorage {
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");

    private final Logger logger;
    private final Path basePath;

    private final IOWorker chunkData;
    private final IOWorker poiData;
    private final IOWorker entityData;

    public AnvilChunkStorage(Logger logger, Path basePath) {
        this.logger = logger;
        this.basePath = basePath;

        this.chunkData = new IOWorker(basePath.resolve("region"), true, "Anvil-Chunks");
        this.poiData = new IOWorker(basePath.resolve("poi"), true, "Anvil-POI");
        this.entityData = new IOWorker(basePath.resolve("entities"), true, "Anvil-Entities");
    }

    @Override
    public List<ChunkPos> getAllChunks() {
        File regionsFolder = new File(this.basePath.toFile(), "region");
        File[] files = regionsFolder.listFiles((filex, string) -> string.endsWith(".mca"));

        if (files == null) {
            return ImmutableList.of();
        }

        List<ChunkPos> list = new ArrayList<>();

        for (File region : files) {
            Matcher matcher = REGEX.matcher(region.getName());
            if (matcher.matches()) {
                int regionX = Integer.parseInt(matcher.group(1)) << 5;
                int regionZ = Integer.parseInt(matcher.group(2)) << 5;

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
        this.chunkData.synchronize(true);
        this.poiData.synchronize(true);
        this.chunkData.synchronize(true);
    }

    @Override
    public void close() {
        this.flush();

        try {
            this.chunkData.close();
            this.poiData.close();
            this.entityData.close();
        } catch (IOException exception) {
            this.logger.warn("[ANVIL] Failed to close chunk storage", exception);
        }
    }

    @Override
    public void setChunkData(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.chunkData.store(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getChunkData(ChunkPos chunkPos) {
        return this.chunkData.loadAsync(chunkPos).join().orElse(null);

    }

    @Override
    public void setPOIData(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.poiData.store(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getPOIData(ChunkPos chunkPos) {
        return this.poiData.loadAsync(chunkPos).join().orElse(null);

    }

    @Override
    public void setEntityData(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.entityData.store(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getEntityData(ChunkPos chunkPos) {
        return this.entityData.loadAsync(chunkPos).join().orElse(null);

    }
}
