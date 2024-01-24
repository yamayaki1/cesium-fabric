package de.yamayaki.cesium.mixin.core.chunks;

import de.yamayaki.cesium.accessor.DatabaseSetter;
import de.yamayaki.cesium.accessor.DatabaseSource;
import de.yamayaki.cesium.accessor.SpecificationSetter;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(EntityStorage.class)
public class MixinEntityStorage {
    @Mutable
    @Shadow
    @Final
    private SimpleRegionStorage simpleRegionStorage;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initCesiumEntities(SimpleRegionStorage simpleRegionStorage, ServerLevel serverLevel, Executor executor, CallbackInfo ci) {
        LMDBInstance storage = ((DatabaseSource) serverLevel).cesium$getStorage();

        ((DatabaseSetter) this.simpleRegionStorage).cesium$setStorage(storage);
        ((SpecificationSetter) this.simpleRegionStorage).cesium$setSpec(WorldDatabaseSpecs.ENTITY);
    }
}
