package de.yamayaki.cesium.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.List;
import java.util.UUID;

public interface IWorldStorage {
    IComponentStorage<UUID, CompoundTag> playerStorage();
    IComponentStorage<UUID, String> advancementStorage();
    IComponentStorage<UUID, String> statStorage();

    void askForDimensions(final LevelStorageSource.LevelStorageAccess levelStorageAccess, final List<ResourceKey<Level>> dimensions);
    IDimensionStorage dimension(ResourceKey<Level> dimension);

    void addDebugInformation(final List<String> list);

    void flush();

    void close();
}
