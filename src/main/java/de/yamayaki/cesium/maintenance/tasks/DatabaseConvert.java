package de.yamayaki.cesium.maintenance.tasks;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.maintenance.AbstractTask;
import de.yamayaki.cesium.maintenance.storage.IChunkStorage;
import de.yamayaki.cesium.maintenance.storage.IPlayerStorage;
import de.yamayaki.cesium.maintenance.storage.anvil.AnvilChunkStorage;
import de.yamayaki.cesium.maintenance.storage.anvil.AnvilPlayerStorage;
import de.yamayaki.cesium.maintenance.storage.cesium.CesiumChunkStorage;
import de.yamayaki.cesium.maintenance.storage.cesium.CesiumPlayerStorage;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseConvert extends AbstractTask {
    private final Task task;

    public DatabaseConvert(final Task task, final LevelStorageSource.LevelStorageAccess levelStorageAccess, final RegistryAccess registryAccess) {
        super("Convert", levelStorageAccess, registryAccess);
        this.task = task;
    }

    @Override
    protected void runTasks() {
        this.copyPlayerData();

        for (final ResourceKey<Level> levelResourceKey : this.levels) {
            this.copyLevelData(levelResourceKey);
        }

        this.running.set(false);
    }

    private void copyPlayerData() {
        this.status.set("Copying player data …");

        final Path playerDataPath = this.levelAccess.getDimensionPath(Level.OVERWORLD);

        try (
                final IPlayerStorage _old = this.pStorage(playerDataPath, true);
                final IPlayerStorage _new = this.pStorage(playerDataPath, false)
        ) {
            final List<UUID> playerList = _old.getAllPlayers();

            CesiumMod.logger().info("Converting {} player profiles", playerList.size());

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
            this.copyChunksData(_old, _new);
        } catch (final Throwable t) {
            throw new RuntimeException("Could not copy all level data.", t);
        }
    }

    private void copyChunksData(final IChunkStorage _old, final IChunkStorage _new) {
        final List<IChunkStorage.Region> regionList = _old.getAllRegions();
        final Iterator<IChunkStorage.Region> iterator = regionList.iterator();

        this.totalElements.set(regionList.size() * 1024);
        this.currentElement.set(0);

        while (this.running.get() && iterator.hasNext()) {
            final ChunkPos[] chunks = iterator.next().chunks();
            final CompletableFuture<?>[] futures = new CompletableFuture<?>[chunks.length];

            for (int i = 0; i < chunks.length; i++) {
                futures[i] = this.copyChunkData(chunks[i], _old, _new);
            }

            CompletableFuture.allOf(futures).join();
            _new.flush();
        }
    }

    private CompletableFuture<Void> copyChunkData(final ChunkPos chunkPos, final IChunkStorage _old, final IChunkStorage _new) {
        final CompletableFuture<Void> future;

        if(chunkPos == null) {
            future =  CompletableFuture.completedFuture(null);
            this.currentElement.addAndGet(1);
        } else {
            future = CompletableFuture.runAsync(() -> {
                _new.setChunkData(chunkPos, _old.getChunkData(chunkPos));
                _new.setPOIData(chunkPos, _old.getPOIData(chunkPos));
                _new.setEntityData(chunkPos, _old.getEntityData(chunkPos));

                this.currentElement.addAndGet(1);
            }, Util.backgroundExecutor()).exceptionally(throwable -> {
                LOGGER.error("Could not copy chunk into new storage.", throwable);
                return null;
            });
        }

        return future;
    }

    private @NotNull IChunkStorage cStorage(final Path path, final boolean old) {
        if (!old) {
            return this.task == Task.TO_ANVIL ? new AnvilChunkStorage(path) : new CesiumChunkStorage(path);
        } else {
            return this.task == Task.TO_ANVIL ? new CesiumChunkStorage(path) : new AnvilChunkStorage(path);
        }
    }

    private @NotNull IPlayerStorage pStorage(final Path path, final boolean old) {
        if (!old) {
            return this.task == Task.TO_ANVIL ? new AnvilPlayerStorage(path) : new CesiumPlayerStorage(path);
        } else {
            return this.task == Task.TO_ANVIL ? new CesiumPlayerStorage(path) : new AnvilPlayerStorage(path);
        }
    }
}
