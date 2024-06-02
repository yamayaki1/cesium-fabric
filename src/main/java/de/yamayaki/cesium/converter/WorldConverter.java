package de.yamayaki.cesium.converter;

import com.mojang.logging.LogUtils;
import de.yamayaki.cesium.converter.formats.anvil.AnvilChunkStorage;
import de.yamayaki.cesium.converter.formats.anvil.AnvilPlayerStorage;
import de.yamayaki.cesium.converter.formats.cesium.CesiumChunkStorage;
import de.yamayaki.cesium.converter.formats.cesium.CesiumPlayerStorage;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WorldConverter {
    private final Logger LOGGER = LogUtils.getLogger();

    private final WorldConverter.Format desiredFormat;
    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final List<ResourceKey<Level>> levels;

    private final Thread workerThread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final AtomicReference<String> status = new AtomicReference<>();

    private final AtomicInteger totalElements = new AtomicInteger(0);
    private final AtomicInteger currentElement = new AtomicInteger(0);
    private final AtomicReference<ResourceKey<Level>> currentLevel = new AtomicReference<>(null);

    public WorldConverter(final WorldConverter.Format desiredFormat, final LevelStorageSource.LevelStorageAccess levelStorageAccess, final RegistryAccess registryAccess) {
        this.desiredFormat = desiredFormat;
        this.levelAccess = levelStorageAccess;

        this.levels = registryAccess
                .registryOrThrow(Registries.LEVEL_STEM)
                .registryKeySet()
                .stream().map(Registries::levelStemToLevel)
                .toList();

        this.status.set("Loading required data into memory ...");

        this.workerThread = createWorkerThread();
        this.workerThread.start();
    }

    private @NotNull Thread createWorkerThread() {
        final Thread workerThread = new Thread(this::runTasks, "Cesium-World-Converter");
        workerThread.setDaemon(true);
        workerThread.setUncaughtExceptionHandler((thread, throwable) -> {
            // if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            //     Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_ACCESS_FAILURE, Component.literal("Cesium error"), Component.literal("Could not convert world, see logs for more information.")));
            // }

            LOGGER.error("Uncaught exception while converting world!", throwable);
            this.running.set(false);
        });

        return workerThread;
    }

    private void runTasks() {
        this.copyPlayerData();

        for (final ResourceKey<Level> levelResourceKey : this.levels) {
            this.copyLevelData(levelResourceKey);
        }

        this.running.set(false);
    }

    public void cancelTask() {
        this.running.set(false);

        try {
            this.workerThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    public boolean running() {
        return this.running.get();
    }

    private void copyPlayerData() {
        this.status.set("Copying player data …");

        final Path playerDataPath = this.levelAccess.getDimensionPath(Level.OVERWORLD);

        try (
                final IPlayerStorage _old = this.pStorage(playerDataPath, true);
                final IPlayerStorage _new = this.pStorage(playerDataPath, false)
        ) {
            final List<UUID> playerList = _old.getAllPlayers();
            final Iterator<UUID> iterator = playerList.iterator();

            this.totalElements.set(playerList.size());
            this.currentElement.set(0);

            while (this.running.get() && iterator.hasNext()) {
                this.currentElement.incrementAndGet();
                copyPlayer(iterator.next(), _old, _new);
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Could not copy all player data.", t);
        }
    }

    private static void copyPlayer(final UUID uuid, final IPlayerStorage _old, final IPlayerStorage _new) {
        _new.setPlayerNBT(uuid, _old.getPlayerNBT(uuid));
        _new.setPlayerAdvancements(uuid, _old.getPlayerAdvancements(uuid));
        _new.setPlayerStatistics(uuid, _old.getPlayerStatistics(uuid));
    }

    private void copyLevelData(final ResourceKey<Level> level) {
        this.status.set("Copying level data …");
        this.currentLevel.set(level);

        final Path dimensionPath = this.levelAccess.getDimensionPath(level);

        try (
                final IChunkStorage _old = this.cStorage(dimensionPath, true);
                final IChunkStorage _new = this.cStorage(dimensionPath, false)
        ) {
            final List<ChunkPos> chunkList = _old.getAllChunks();
            final Iterator<ChunkPos> iterator = chunkList.iterator();

            this.totalElements.set(chunkList.size());
            this.currentElement.set(0);

            final List<CompletableFuture<Void>> copyTasks = new ArrayList<>(1024);

            while (this.running.get() && iterator.hasNext()) {
                final int currentChunk = this.currentElement.incrementAndGet();
                copyTasks.add(this.copyChunkData(iterator.next(), _old, _new));

                if ((currentChunk % 1024) == 0) {
                    CompletableFuture.allOf(copyTasks.toArray(CompletableFuture[]::new)).join();

                    _new.flush();
                    copyTasks.clear();
                }
            }

            CompletableFuture.allOf(copyTasks.toArray(CompletableFuture[]::new)).join();
        } catch (final Throwable t) {
            throw new RuntimeException("Could not copy all level data.", t);
        }
    }

    private CompletableFuture<Void> copyChunkData(final ChunkPos chunkPos, final IChunkStorage _old, final IChunkStorage _new) {
        return CompletableFuture.runAsync(() -> {
            _new.setChunkData(chunkPos, _old.getChunkData(chunkPos));
            _new.setPOIData(chunkPos, _old.getPOIData(chunkPos));
            _new.setEntityData(chunkPos, _old.getEntityData(chunkPos));
        }, Util.backgroundExecutor()).exceptionally(throwable -> {
            LOGGER.error("Could not copy chunk into new storage.", throwable);
            return null;
        });
    }

    private @NotNull IChunkStorage cStorage(final Path path, final boolean old) {
        if (!old) {
            return this.desiredFormat == Format.TO_ANVIL ? new AnvilChunkStorage(path) : new CesiumChunkStorage(path);
        } else {
            return this.desiredFormat == Format.TO_ANVIL ? new CesiumChunkStorage(path) : new AnvilChunkStorage(path);
        }
    }

    private @NotNull IPlayerStorage pStorage(final Path path, final boolean old) {
        if (!old) {
            return this.desiredFormat == Format.TO_ANVIL ? new AnvilPlayerStorage(path) : new CesiumPlayerStorage(path);
        } else {
            return this.desiredFormat == Format.TO_ANVIL ? new CesiumPlayerStorage(path) : new AnvilPlayerStorage(path);
        }
    }

    public String levelName() {
        final ResourceKey<Level> curLevel = this.currentLevel.get();
        return curLevel == null ? "<unnamed>" : curLevel.toString();
    }

    public String status() {
        return this.status.get();
    }

    public int totalElements() {
        return this.totalElements.get();
    }

    public int currentElement() {
        return this.currentElement.get();
    }

    public double percentage() {
        return this.currentElement() / (double) Math.max(this.totalElements(), 1);
    }

    public enum Format {
        TO_ANVIL,
        TO_CESIUM
    }
}
