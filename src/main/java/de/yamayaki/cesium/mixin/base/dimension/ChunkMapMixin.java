package de.yamayaki.cesium.mixin.base.dimension;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.api.accessor.IDimensionStorageGetter;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IDimensionStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    /* Vanilla imports */
    @Shadow @Final private PoiManager poiManager;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    @SuppressWarnings("unchecked")
    public void cesium$setComponentStorage(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, BlockableEventLoop<?> blockableEventLoop, LightChunkGetter lightChunkGetter, ChunkGenerator chunkGenerator, ChunkProgressListener chunkProgressListener, ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier<?> supplier, int i, boolean bl, CallbackInfo ci) {
        final IDimensionStorage dimensionStorage = ((IDimensionStorageGetter) serverLevel).cesium$dimensionStorage();
        ((IStorageSetter<ChunkPos, CompoundTag>) this.poiManager).cesium$setStorage(dimensionStorage.poiStorage());
        ((IStorageSetter<ChunkPos, CompoundTag>) this).cesium$setStorage(dimensionStorage.chunkStorage());
    }
}
