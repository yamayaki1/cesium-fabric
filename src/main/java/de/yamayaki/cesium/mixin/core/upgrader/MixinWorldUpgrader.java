package de.yamayaki.cesium.mixin.core.upgrader;

import com.google.common.collect.ImmutableMap;
import de.yamayaki.cesium.common.CesiumActions;
import de.yamayaki.cesium.common.ChunkDatabaseAccess;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import de.yamayaki.cesium.converter.formats.cesium.CesiumChunkStorage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(WorldUpgrader.class)
public class MixinWorldUpgrader {
    @Shadow
    @Final
    private LevelStorageSource.LevelStorageAccess levelStorage;

    @Shadow
    private volatile int converted;

    @Shadow
    private volatile int skipped;

    @Inject(method = "work", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectDatabase(CallbackInfo ci, ImmutableMap.Builder builder, float f, ImmutableMap immutableMap, ImmutableMap.Builder builder2, ImmutableMap immutableMap2) {
        for (Map.Entry<ResourceKey<Level>, ChunkStorage> resourceKeyChunkStorageEntry : ((ImmutableMap<ResourceKey<Level>, ChunkStorage>) immutableMap2).entrySet()) {
            final LMDBInstance database = new LMDBInstance(this.levelStorage.getDimensionPath(resourceKeyChunkStorageEntry.getKey()).toFile(), "chunks", new DatabaseSpec[]{
                    WorldDatabaseSpecs.CHUNK_DATA,
                    WorldDatabaseSpecs.POI,
                    WorldDatabaseSpecs.ENTITY
            });

            ((ChunkDatabaseAccess) resourceKeyChunkStorageEntry.getValue()).setDatabase(database);
        }
    }

    @Inject(method = "work", at = @At(value = "INVOKE", target = "Ljava/util/ListIterator;hasNext()Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void flushData(CallbackInfo ci, ImmutableMap.Builder builder, float f, ImmutableMap immutableMap, ImmutableMap.Builder builder2, ImmutableMap immutableMap2, long l, boolean bl, float g, Iterator var10, ResourceKey resourceKey3, ListIterator listIterator, ChunkStorage chunkStorage) {
        if ((this.converted + this.skipped) % 10240 == 0) {
            ((CesiumActions) chunkStorage).cesiumFlush();
        }
    }

    @Redirect(method = "work", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/ChunkStorage;close()V"))
    private void closeDatabase(ChunkStorage instance) {
        ((CesiumActions) instance).cesiumClose();
    }

    /**
     * @author Yamayaki
     * @reason Cesium
     */
    @Overwrite
    private List<ChunkPos> getAllChunkPos(ResourceKey<Level> resourceKey) {
        final CesiumChunkStorage chunkStorage = new CesiumChunkStorage(this.levelStorage.getDimensionPath(resourceKey));
        final List<ChunkPos> chunkList = chunkStorage.getAllChunks();

        chunkStorage.close();
        return chunkList;
    }
}
