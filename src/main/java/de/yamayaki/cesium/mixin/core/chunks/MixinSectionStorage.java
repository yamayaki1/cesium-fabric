package de.yamayaki.cesium.mixin.core.chunks;

import de.yamayaki.cesium.api.accessor.DatabaseSetter;
import de.yamayaki.cesium.api.accessor.SpecificationSetter;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.common.spec.WorldDatabaseSpecs;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SectionStorage.class)
public class MixinSectionStorage<R> implements DatabaseSetter {
    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    @Override
    public void cesium$setStorage(IDBInstance dbInstance) {
        ((DatabaseSetter) this.worker).cesium$setStorage(dbInstance);
        ((SpecificationSetter) this.worker).cesium$setSpec(WorldDatabaseSpecs.POI);
    }
}
