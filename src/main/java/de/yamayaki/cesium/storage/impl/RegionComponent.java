package de.yamayaki.cesium.storage.impl;

import de.yamayaki.cesium.FileHelper;
import de.yamayaki.cesium.api.accessor.RawAccess;
import de.yamayaki.cesium.storage.IComponentStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.yamayaki.cesium.Helpers.throwing;
import static de.yamayaki.cesium.Helpers.throwingV;

public class RegionComponent implements IComponentStorage<ChunkPos, CompoundTag>, Closeable {
    private static final Pattern MCA_REG = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");

    private final Path regionsPath;
    private final @NotNull RegionFileStorage storage;

    public RegionComponent(final Path basePath, final String name) {
        var storageInfo = new RegionStorageInfo("cesium", null, name);

        this.regionsPath = basePath.resolve(name);
        this.storage = new RegionFileStorage(storageInfo, this.regionsPath, false);
    }

    @Override
    public synchronized @Nullable CompoundTag getValue(final @NotNull ChunkPos key) {
        return throwing(() -> this.storage.read(key));
    }

    @Override
    public synchronized byte @Nullable [] getRaw(final @NotNull ChunkPos key) {
        return throwing(() -> ((RawAccess) (Object) this.storage).cesium$getBytes(key));
    }

    @Override
    public synchronized void putValue(final @NotNull ChunkPos key, final @Nullable CompoundTag value) {
        throwingV(() -> this.storage.write(key, value));
    }

    @Override
    public synchronized void putRaw(final @NotNull ChunkPos key, final byte @Nullable [] value) {
        throwingV(() -> ((RawAccess) (Object) this.storage).cesium$putBytes(key, value));
    }

    @Override
    public synchronized <S> void scan(final @NotNull ChunkPos key, final @NotNull S scanner) {
        throwingV(() -> this.storage.scanChunk(key, (StreamTagVisitor) scanner));
    }

    @Override
    public @NotNull List<ChunkPos> allKeys() {
        final List<ChunkPos> list = new ArrayList<>();

        FileHelper.traverseFilesSafe(
                () -> "Could not resolve regions from directory, aborting.",
                this.regionsPath,
                new FileHelper.NoOpMapper(),
                new FileHelper.PatternRule(MCA_REG)
        ).forEach(region -> {
            Matcher matcher = MCA_REG.matcher(region.getFileName().toString());

            if (matcher.matches()) {
                final int regionX = Integer.parseInt(matcher.group(1)) << 5;
                final int regionZ = Integer.parseInt(matcher.group(2)) << 5;

                for (int chunkX = 0; chunkX < 32; ++chunkX) {
                    for (int chunkY = 0; chunkY < 32; ++chunkY) {
                        list.add(new ChunkPos(chunkX + regionX, chunkY + regionZ));
                    }
                }
            }
        });

        return list;
    }

    public void flush() throws IOException {
        this.storage.flush();
    }

    @Override
    public void close() throws IOException {
        this.storage.close();
    }
}
