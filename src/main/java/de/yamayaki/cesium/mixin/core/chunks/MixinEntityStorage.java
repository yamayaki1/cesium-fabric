package de.yamayaki.cesium.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.api.accessor.DatabaseSetter;
import de.yamayaki.cesium.api.accessor.DatabaseSource;
import de.yamayaki.cesium.api.accessor.SpecificationSetter;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.common.spec.WorldDatabaseSpecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.concurrent.Executor;

@Mixin(EntityStorage.class)
public class MixinEntityStorage {
    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initCesiumEntities(ServerLevel serverLevel, Path path, DataFixer dataFixer, boolean bl, Executor executor, CallbackInfo ci) {
        IDBInstance storage = ((DatabaseSource) serverLevel).cesium$getStorage();

        ((DatabaseSetter) this.worker).cesium$setStorage(storage);
        ((SpecificationSetter) this.worker).cesium$setSpec(WorldDatabaseSpecs.ENTITY);
    }
}
