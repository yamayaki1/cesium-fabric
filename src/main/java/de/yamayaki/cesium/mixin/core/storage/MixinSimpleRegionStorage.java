package de.yamayaki.cesium.mixin.core.storage;

import de.yamayaki.cesium.accessor.DatabaseSetter;
import de.yamayaki.cesium.accessor.SpecificationSetter;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SimpleRegionStorage.class)
public class MixinSimpleRegionStorage implements DatabaseSetter, SpecificationSetter {
    @Shadow
    @Final
    private IOWorker worker;

    @Override
    public void cesium$setStorage(LMDBInstance lmdbInstance) {
        ((DatabaseSetter) this.worker).cesium$setStorage(lmdbInstance);
    }

    @Override
    public void cesium$setSpec(DatabaseSpec<?, ?> databaseSpec) {
        ((SpecificationSetter) this.worker).cesium$setSpec(databaseSpec);
    }
}
