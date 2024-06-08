package de.yamayaki.cesium.mixin.core.storage;

import de.yamayaki.cesium.api.accessor.DatabaseActions;
import de.yamayaki.cesium.api.accessor.DatabaseSetter;
import de.yamayaki.cesium.api.accessor.SpecificationSetter;
import de.yamayaki.cesium.api.database.DatabaseSpec;
import de.yamayaki.cesium.api.database.IDBInstance;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SimpleRegionStorage.class)
public class MixinSimpleRegionStorage implements DatabaseSetter, SpecificationSetter, DatabaseActions {
    @Shadow
    @Final
    private IOWorker worker;

    @Override
    public void cesium$setStorage(IDBInstance dbInstance) {
        ((DatabaseSetter) this.worker).cesium$setStorage(dbInstance);
    }

    @Override
    public void cesium$setSpec(DatabaseSpec<?, ?> databaseSpec) {
        ((SpecificationSetter) this.worker).cesium$setSpec(databaseSpec);
    }

    @Override
    public void cesium$flush() {
        ((DatabaseActions) this.worker).cesium$flush();
    }

    @Override
    public void cesium$close() {
        ((DatabaseActions) this.worker).cesium$close();
    }
}
