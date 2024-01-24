package de.yamayaki.cesium.mixin.core.upgrader;

import net.minecraft.util.worldupdate.WorldUpgrader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldUpgrader.class)
public class MixinWorldUpgrader {
/*    @Final
    private LevelStorageSource.LevelStorageAccess levelStorage;

    @Shadow
    private volatile int converted;

    @Shadow
    private volatile int skipped;

    @Inject(method = "work", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectDatabase(CallbackInfo ci) {
        for (Map.Entry<ResourceKey<Level>, ChunkStorage> resourceKeyChunkStorageEntry : ((ImmutableMap<ResourceKey<Level>, ChunkStorage>) immutableMap2).entrySet()) {
            final LMDBInstance database = new LMDBInstance(this.levelStorage.getDimensionPath(resourceKeyChunkStorageEntry.getKey()), "chunks", new DatabaseSpec[]{
                    WorldDatabaseSpecs.CHUNK_DATA,
                    WorldDatabaseSpecs.POI,
                    WorldDatabaseSpecs.ENTITY
            });

            ((DatabaseSetter) resourceKeyChunkStorageEntry.getValue()).cesium$setStorage(database);
        }
    }

    @Inject(method = "work", at = @At(value = "INVOKE", target = "Ljava/util/ListIterator;hasNext()Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void flushData(CallbackInfo ci, ImmutableMap.Builder builder, float f, ImmutableMap immutableMap, ImmutableMap.Builder builder2, ImmutableMap immutableMap2, long l, boolean bl, float g, Iterator var10, ResourceKey resourceKey3, ListIterator listIterator, ChunkStorage chunkStorage) {
        if ((this.converted + this.skipped) % 10240 == 0) {
            ((DatabaseActions) chunkStorage).cesium$flush();
        }
    }

    @Redirect(method = "work", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/ChunkStorage;close()V"))
    private void closeDatabase(ChunkStorage instance) {
        ((DatabaseActions) instance).cesium$close();
    }*/

    /**
     * @author Yamayaki
     * @reason Cesium
     */
    /*@Overwrite
    private List<ChunkPos> getAllChunkPos(ResourceKey<Level> resourceKey) {
        final CesiumChunkStorage chunkStorage = new CesiumChunkStorage(this.levelStorage.getDimensionPath(resourceKey));
        final List<ChunkPos> chunkList = chunkStorage.getAllChunks();

        chunkStorage.close();
        return chunkList;
    }*/
}
