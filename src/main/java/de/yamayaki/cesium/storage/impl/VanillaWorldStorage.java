package de.yamayaki.cesium.storage.impl;

import de.yamayaki.cesium.storage.IComponentStorage;
import de.yamayaki.cesium.storage.IWorldStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class VanillaWorldStorage implements IWorldStorage {
    private final FileComponent<CompoundTag> playerStorage;
    private final FileComponent<String> advancementStorage;
    private final FileComponent<String> statStorage;

    private final Object2ObjectArrayMap<ResourceKey<Level>, VanillaDimensionStorage> dimensions = new Object2ObjectArrayMap<>();

    public VanillaWorldStorage(final Path worldPath) {
        this.playerStorage = new FileComponent<>(worldPath.resolve("playerdata"), ".dat", true);
        this.advancementStorage = new FileComponent<>(worldPath.resolve("stats"), ".json", false);
        this.statStorage = new FileComponent<>(worldPath.resolve("advancements"), ".json", false);
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

            this.dimensions.put(dimension, new VanillaDimensionStorage(levelStorageAccess.getDimensionPath(dimension)));
        }
    }

    @Override
    public VanillaDimensionStorage dimension(final ResourceKey<Level> dimension) {
        final VanillaDimensionStorage storage = this.dimensions.get(dimension);

        if (storage == null) {
            throw new IllegalArgumentException("Dimension " + dimension + " is not registered!");
        }

        return storage;
    }

    @Override
    public void addDebugInformation(final List<String> list) {
        // No Op
    }

    @Override
    public void flush() {
        for (final VanillaDimensionStorage dimension : this.dimensions.values()) {
            dimension.flush();
        }
    }

    @Override
    public void close() {
        for (final VanillaDimensionStorage dimension : this.dimensions.values()) {
            dimension.close();
        }
    }
}
