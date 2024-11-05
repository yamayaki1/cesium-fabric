package de.yamayaki.cesium.storage.impl;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.common.spec.PlayerDatabaseSpecs;
import de.yamayaki.cesium.storage.IComponentStorage;
import de.yamayaki.cesium.storage.IWorldStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.lmdbjava.Stat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CesiumWorldStorage implements IWorldStorage {
    private final IDBInstance playerDatabase;

    private final IComponentStorage<UUID, CompoundTag> playerStorage;
    private final IComponentStorage<UUID, String> advancementStorage;
    private final IComponentStorage<UUID, String> statStorage;

    private final Object2ObjectArrayMap<ResourceKey<Level>, CesiumDimensionStorage> dimensions = new Object2ObjectArrayMap<>();

    public CesiumWorldStorage(final Path worldPath) {
        this.playerDatabase = CesiumMod.openPlayerDB(worldPath);

        this.playerStorage = new CesiumComponent<>(this.playerDatabase, PlayerDatabaseSpecs.PLAYER_DATA);
        this.advancementStorage = new CesiumComponent<>(this.playerDatabase, PlayerDatabaseSpecs.ADVANCEMENTS);
        this.statStorage = new CesiumComponent<>(this.playerDatabase, PlayerDatabaseSpecs.STATISTICS);
    }

    @Override
    public IComponentStorage<UUID, CompoundTag> playerStorage() {
        return this.playerStorage;
    }

    @Override
    public IComponentStorage<UUID, String> advancementStorage() {
        return this.advancementStorage;
    }

    @Override
    public IComponentStorage<UUID, String> statStorage() {
        return this.statStorage;
    }

    @Override
    public void askForDimensions(LevelStorageSource.LevelStorageAccess levelStorageAccess, List<ResourceKey<Level>> dimensions) {
        for (final ResourceKey<Level> dimension : dimensions) {
            if (this.dimensions.containsKey(dimension)) continue;

            this.dimensions.put(dimension, new CesiumDimensionStorage(levelStorageAccess.getDimensionPath(dimension)));
        }
    }

    @Override
    public CesiumDimensionStorage dimension(final ResourceKey<Level> dimension) {
        final CesiumDimensionStorage storage = this.dimensions.get(dimension);

        if (storage == null) {
            throw new IllegalArgumentException("Dimension " + dimension + " is not registered!");
        }

        return storage;
    }

    @Override
    public void addDebugInformation(final List<String> list) {
        list.add("");
        list.add("[Cesium Storage Format]");

        list.add("{Players}");
        this.addDebugInformation(list, this.playerDatabase);

        for (final Map.Entry<ResourceKey<Level>, CesiumDimensionStorage> dimension : this.dimensions.entrySet()) {
            list.add("");
            list.add("{" + dimension.getKey().location() + "}");
            this.addDebugInformation(list, dimension.getValue().dimensionDatabase());
        }
    }

    private void addDebugInformation(final List<String> list, final IDBInstance dbInstance) {
        final List<Stat> stats = dbInstance.getStats();

        final int ms_depth = stats.stream().mapToInt(es -> es.depth).max().orElse(0);
        final long ms_branch_pages = stats.stream().mapToLong(es -> es.branchPages).sum();
        final long ms_leaf_pages = stats.stream().mapToLong(es -> es.leafPages).sum();
        final long ms_entries = stats.stream().mapToLong(es -> es.entries).sum();

        list.add(String.format("dp: %s; bp: %s; lp: %s; en: %s", ms_depth, ms_branch_pages, ms_leaf_pages, ms_entries));
    }

    @Override
    public void flush() {
        this.playerDatabase.flushChanges();

        for (final CesiumDimensionStorage dimension : this.dimensions.values()) {
            dimension.flush();
        }
    }

    @Override
    public void close() {
        this.playerDatabase.close();

        for (final CesiumDimensionStorage dimension : this.dimensions.values()) {
            dimension.close();
        }
    }
}
