package de.yamayaki.cesium.mixin.base.dimension.poi;

import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IComponentStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SectionStorage.class)
public class SectionStorageMixin implements IStorageSetter<ChunkPos, CompoundTag> {
    @Shadow @Final private SimpleRegionStorage simpleRegionStorage;

    @Override
    @SuppressWarnings("unchecked")
    public void cesium$setStorage(final @NotNull IComponentStorage<ChunkPos, CompoundTag> componentStorage) {
        ((IStorageSetter<ChunkPos, CompoundTag>) this.simpleRegionStorage).cesium$setStorage(componentStorage);
    }
}
