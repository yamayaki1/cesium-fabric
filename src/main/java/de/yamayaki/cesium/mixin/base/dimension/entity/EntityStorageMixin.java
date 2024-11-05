package de.yamayaki.cesium.mixin.base.dimension.entity;

import de.yamayaki.cesium.api.accessor.IDimensionStorageGetter;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IDimensionStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(EntityStorage.class)
public class EntityStorageMixin {
    @Shadow
    @Final
    private SimpleRegionStorage simpleRegionStorage;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    @SuppressWarnings("unchecked")
    public void cesium$setComponentStorage(SimpleRegionStorage simpleRegionStorage, ServerLevel serverLevel, Executor executor, CallbackInfo ci) {
        final IDimensionStorage dimensionStorage = ((IDimensionStorageGetter) serverLevel).cesium$dimensionStorage();
        ((IStorageSetter<ChunkPos, CompoundTag>) this.simpleRegionStorage).cesium$setStorage(dimensionStorage.entityStorage());
    }
}
