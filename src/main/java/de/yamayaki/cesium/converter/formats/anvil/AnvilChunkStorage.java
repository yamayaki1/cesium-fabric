package de.yamayaki.cesium.converter.formats.anvil;

import com.google.common.collect.ImmutableList;
import de.yamayaki.cesium.converter.IChunkStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
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

    public AnvilChunkStorage(final Logger logger, final Path basePath) {
        this.logger = logger;
        this.basePath = basePath;

        this.chunkData = new IOWorker(basePath.resolve("region"), true, "Anvil-Chunks");
        this.poiData = new IOWorker(basePath.resolve("poi"), true, "Anvil-POI");
        this.entityData = new IOWorker(basePath.resolve("entities"), true, "Anvil-Entities");
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
        this.chunkData.synchronize(true).join();
        this.poiData.synchronize(true).join();
        this.chunkData.synchronize(true).join();
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
    public void setChunkData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        this.chunkData.store(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getChunkData(final ChunkPos chunkPos) {
        return this.chunkData.loadAsync(chunkPos).join().orElse(null);

    }

    @Override
    public void setPOIData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        this.poiData.store(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getPOIData(final ChunkPos chunkPos) {
        return this.poiData.loadAsync(chunkPos).join().orElse(null);

    }

    @Override
    public void setEntityData(final ChunkPos chunkPos, final CompoundTag compoundTag) {
        this.entityData.store(chunkPos, compoundTag);
    }

    @Override
    public CompoundTag getEntityData(final ChunkPos chunkPos) {
        return this.entityData.loadAsync(chunkPos).join().orElse(null);

    }
}
