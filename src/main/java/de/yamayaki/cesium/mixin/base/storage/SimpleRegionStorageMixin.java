package de.yamayaki.cesium.mixin.base.storage;

import de.yamayaki.cesium.api.accessor.IStorageProvider;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IComponentStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SimpleRegionStorage.class)
public class SimpleRegionStorageMixin implements IStorageSetter<ChunkPos, CompoundTag>, IStorageProvider<ChunkPos, CompoundTag> {
    /* Vanilla imports */
    @Shadow @Final private IOWorker worker;

    /* Save this for potential world-upgrader usage */
    @Unique @Nullable private IComponentStorage<ChunkPos, CompoundTag> componentStorage;

    @Override
    @SuppressWarnings("unchecked")
    public void cesium$setStorage(final @NotNull IComponentStorage<ChunkPos, CompoundTag> componentStorage) {
        this.componentStorage = componentStorage;
        ((IStorageSetter<ChunkPos, CompoundTag>) this.worker).cesium$setStorage(componentStorage);
    }

    @Override
    public @NotNull IComponentStorage<ChunkPos, CompoundTag> cesium$storage() {
        if (this.componentStorage == null) {
            throw new IllegalStateException("ComponentStorage not yet set");
        }

        return this.componentStorage;
    }
}
