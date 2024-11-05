package de.yamayaki.cesium.maintenance.tasks;

import de.yamayaki.cesium.maintenance.AbstractTask;
import de.yamayaki.cesium.storage.IDimensionStorage;
import de.yamayaki.cesium.storage.IWorldStorage;
import de.yamayaki.cesium.storage.impl.CesiumWorldStorage;
import de.yamayaki.cesium.storage.impl.VanillaWorldStorage;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseConvert extends AbstractTask {
    private Task task = null;

    public DatabaseConvert(final Task task, final LevelStorageSource.LevelStorageAccess levelStorageAccess, final RegistryAccess registryAccess) {
        super("Convert", levelStorageAccess, registryAccess);
        this.task = task;
    }

    private static void copyPlayer(final IWorldStorage oldStorage, final IWorldStorage newStorage, final UUID uuid) {
        newStorage.playerStorage().copyFrom(oldStorage.playerStorage(), uuid);
        newStorage.advancementStorage().copyFrom(oldStorage.advancementStorage(), uuid);
        newStorage.statStorage().copyFrom(oldStorage.statStorage(), uuid);
    }

    @Override
    protected void runTasks() {
        final IWorldStorage oldStorage;
        final IWorldStorage newStorage;

        {
            final var cesiumWorld = new CesiumWorldStorage(this.levelAccess.getDimensionPath(Level.OVERWORLD));
            cesiumWorld.askForDimensions(this.levelAccess, this.levels);

            final var vanillaWorld = new VanillaWorldStorage(this.levelAccess.getDimensionPath(Level.OVERWORLD));
            vanillaWorld.askForDimensions(this.levelAccess, this.levels);

            while (this.task == null) {
                Thread.onSpinWait();
            }

            if (this.task == Task.TO_ANVIL) {
                oldStorage = cesiumWorld;
                newStorage = vanillaWorld;
            } else {
                oldStorage = vanillaWorld;
                newStorage = cesiumWorld;
            }
        }

        this.copyPlayerData(oldStorage, newStorage);

        for (final ResourceKey<Level> levelResourceKey : this.levels) {
            this.copyLevelData(oldStorage, newStorage, levelResourceKey);
        }

        oldStorage.close();
        newStorage.close();

        this.running.set(false);
    }

    private void copyPlayerData(final IWorldStorage oldStorage, final IWorldStorage newStorage) {
        this.status.set("Copying player data …");

        try {
            final List<UUID> playerList = oldStorage.playerStorage().allKeys();

            this.logger.info("Converting {} player profiles", playerList.size());

            final Iterator<UUID> iterator = playerList.iterator();

            this.totalElements.set(playerList.size());
            this.currentElement.set(0);

            while (this.running.get() && iterator.hasNext()) {
                this.currentElement.incrementAndGet();
                copyPlayer(oldStorage, newStorage, iterator.next());
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Could not copy all player data.", t);
        }
    }

    private void copyLevelData(final IWorldStorage oldStorage, final IWorldStorage newStorage, final ResourceKey<Level> level) {
        this.status.set("Copying level data …");
        this.currentLevel.set(level);

        try {
            final IDimensionStorage _old = oldStorage.dimension(level);
            final IDimensionStorage _new = newStorage.dimension(level);

            final List<ChunkPos> chunkList = _old.chunkStorage().allKeys();
            final Iterator<ChunkPos> iterator = chunkList.iterator();

            this.totalElements.set(chunkList.size());
            this.currentElement.set(0);

            final int taskCount = Math.min(Math.max(Runtime.getRuntime().availableProcessors() * 2, 8), 32);
            final List<CompletableFuture<Void>> copyTasks = new ArrayList<>(taskCount);

            while (this.running.get() && iterator.hasNext()) {
                final int currentChunk = this.currentElement.incrementAndGet();
                copyTasks.add(this.copyChunkData(iterator.next(), _old, _new));

                if ((currentChunk % taskCount) == 0) {
                    CompletableFuture.allOf(copyTasks.toArray(CompletableFuture[]::new)).join();

                    newStorage.flush();
                    copyTasks.clear();
                }
            }

            CompletableFuture.allOf(copyTasks.toArray(CompletableFuture[]::new)).join();
        } catch (final Throwable t) {
            throw new RuntimeException("Could not copy all level data.", t);
        }
    }

    private CompletableFuture<Void> copyChunkData(final ChunkPos chunkPos, final IDimensionStorage _old, final IDimensionStorage _new) {
        return CompletableFuture.runAsync(() -> {
            _new.chunkStorage().copyFrom(_old.chunkStorage(), chunkPos);
            _new.poiStorage().copyFrom(_old.poiStorage(), chunkPos);
            _new.entityStorage().copyFrom(_old.entityStorage(), chunkPos);
        }, Util.backgroundExecutor()).exceptionally(throwable -> {
            this.logger.error("Could not copy chunk into new storage.", throwable);
            return null;
        });
    }
}
