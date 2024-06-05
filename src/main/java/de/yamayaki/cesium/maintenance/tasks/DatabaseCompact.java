package de.yamayaki.cesium.maintenance.tasks;

import com.mojang.logging.LogUtils;
import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseCompact implements ICesiumTask {
    private final Logger LOGGER = LogUtils.getLogger();

    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final List<ResourceKey<Level>> levels;

    private final Thread workerThread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final AtomicReference<String> status = new AtomicReference<>();

    private final AtomicInteger totalElements = new AtomicInteger(0);
    private final AtomicInteger currentElement = new AtomicInteger(0);
    private final AtomicReference<ResourceKey<Level>> currentLevel = new AtomicReference<>(null);

    public DatabaseCompact(final LevelStorageSource.LevelStorageAccess levelStorageAccess, final RegistryAccess registryAccess) {
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
        final Thread workerThread = new Thread(this::runTasks, "Cesium-Compact-Database");
        workerThread.setDaemon(true);
        workerThread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Uncaught exception while compacting world!", throwable);
            this.running.set(false);
        });

        return workerThread;
    }

    private void runTasks() {
        this.totalElements.set(this.levels.size());

        for (final ResourceKey<Level> levelResourceKey : this.levels) {
            this.currentElement.incrementAndGet();
            this.currentLevel.set(levelResourceKey);

            this.compactLevelDatabase(levelResourceKey);
        }

        this.running.set(false);
    }

    @Override
    public void cancelTask() {
        this.running.set(false);

        try {
            this.workerThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public boolean running() {
        return this.running.get();
    }

    private void compactLevelDatabase(final ResourceKey<Level> level) {
        final Path dimensionPath = this.levelAccess.getDimensionPath(level);

        final Path originalPath = dimensionPath.resolve("chunks" + CesiumMod.getFileEnding());
        final Path copyPath = dimensionPath.resolve("chunks.copy");

        final LMDBInstance lmdbInstance = openDatabase(dimensionPath);

        this.status.set("Compacting level data for " + level.location().getPath());

        try {
            lmdbInstance.createCopy(copyPath);
            lmdbInstance.close();

            if (Files.isRegularFile(copyPath) && Files.isRegularFile(originalPath)) {
                Files.move(copyPath, originalPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Failed to compact level.", t);
        } finally {
            if (!lmdbInstance.closed()) {
                lmdbInstance.close();
            }
        }
    }

    private static @NotNull LMDBInstance openDatabase(final Path dimensionPath) {
        return new LMDBInstance(dimensionPath, "chunks", new DatabaseSpec[]{
                WorldDatabaseSpecs.CHUNK_DATA,
                WorldDatabaseSpecs.POI,
                WorldDatabaseSpecs.ENTITY
        });
    }

    @Override
    public String levelName() {
        final ResourceKey<Level> curLevel = this.currentLevel.get();
        return curLevel == null ? "<unnamed>" : curLevel.toString();
    }

    @Override
    public String status() {
        return this.status.get();
    }

    @Override
    public int totalElements() {
        return this.totalElements.get();
    }

    @Override
    public int currentElement() {
        return this.currentElement.get();
    }

    @Override
    public double percentage() {
        return this.currentElement() / (double) Math.max(this.totalElements(), 1);
    }
}
