package de.yamayaki.cesium.mixin.core.chunks;

import de.yamayaki.cesium.accessor.DatabaseSetter;
import de.yamayaki.cesium.accessor.SpecificationSetter;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SectionStorage.class)
public class MixinSectionStorage<R> implements DatabaseSetter {
    @Mutable
    @Shadow
    @Final
    private SimpleRegionStorage simpleRegionStorage;

    @Override
    public void cesium$setStorage(LMDBInstance lmdbInstance) {
        ((DatabaseSetter) this.simpleRegionStorage).cesium$setStorage(lmdbInstance);
        ((SpecificationSetter) this.simpleRegionStorage).cesium$setSpec(WorldDatabaseSpecs.POI);
    }
}
