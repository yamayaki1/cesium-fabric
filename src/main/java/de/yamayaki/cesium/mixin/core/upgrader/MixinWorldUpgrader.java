package de.yamayaki.cesium.mixin.core.upgrader;

import de.yamayaki.cesium.accessor.DatabaseActions;
import de.yamayaki.cesium.accessor.DatabaseSetter;
import de.yamayaki.cesium.accessor.SpecificationSetter;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.lmdbjava.Cursor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(targets = "net.minecraft.util.worldupdate.WorldUpgrader$AbstractUpgrader")
public abstract class MixinWorldUpgrader {
    @Shadow
    protected abstract boolean processOnePosition(ResourceKey<Level> resourceKey, AutoCloseable autoCloseable, ChunkPos chunkPos);

    @Unique
    private LMDBInstance tmpLMDBInstance;

    @Unique
    private DatabaseSpec<ChunkPos, CompoundTag> tmpSpec;

    @Unique
    private double chunkCount = 0;

    @Redirect(method = "upgrade", at = @At(value = "INVOKE", target = "Ljava/lang/AutoCloseable;close()V"))
    public void cesiumClose(AutoCloseable instance) throws Exception {
        if (instance instanceof DatabaseActions databaseActions) {
            databaseActions.cesium$close();
        }

        instance.close();
    }

    @Redirect(method = "upgrade", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/worldupdate/WorldUpgrader$AbstractUpgrader;processOnePosition(Lnet/minecraft/resources/ResourceKey;Ljava/lang/AutoCloseable;Lnet/minecraft/world/level/ChunkPos;)Z"))
    public boolean cesiumFlush(WorldUpgrader.AbstractUpgrader<?> instance, ResourceKey<Level> resourceKey, AutoCloseable autoCloseable, ChunkPos chunkPos) {
        if (chunkCount % 1024 == 0 && autoCloseable instanceof DatabaseActions databaseActions) {
            databaseActions.cesium$flush();
        }

        chunkCount++;

        return this.processOnePosition(resourceKey, autoCloseable, chunkPos);
    }


    @Inject(method = "getDimensionsToUpgrade", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/worldupdate/WorldUpgrader$AbstractUpgrader;getFilesToProcess(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;)Ljava/util/ListIterator;", shift = At.Shift.BY), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public <T extends AutoCloseable> void cesiumCreate(CallbackInfoReturnable<List<WorldUpgrader.DimensionToUpgrade<T>>> cir, List list, Iterator var2, ResourceKey resourceKey, RegionStorageInfo regionStorageInfo, Path path, AutoCloseable autoCloseable) {
        LMDBInstance lmdbInstance = new LMDBInstance(path.getParent(), "chunks", new DatabaseSpec[]{
                WorldDatabaseSpecs.CHUNK_DATA,
                WorldDatabaseSpecs.POI,
                WorldDatabaseSpecs.ENTITY
        });
        tmpLMDBInstance = lmdbInstance;

        DatabaseSpec<ChunkPos, CompoundTag> databaseSpec = switch (regionStorageInfo.type()) {
            case "entities" -> WorldDatabaseSpecs.ENTITY;
            case "poi" -> WorldDatabaseSpecs.POI;
            case "chunk" -> WorldDatabaseSpecs.CHUNK_DATA;
            default -> throw new IllegalStateException("Unexpected value: " + regionStorageInfo.type());
        };
        tmpSpec = databaseSpec;

        ((DatabaseSetter) autoCloseable).cesium$setStorage(lmdbInstance);
        if (autoCloseable instanceof SpecificationSetter) {
            ((SpecificationSetter) autoCloseable).cesium$setSpec(databaseSpec);
        }
    }

    @Redirect(method = "getFilesToProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/worldupdate/WorldUpgrader$AbstractUpgrader;getAllChunkPositions(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;)Ljava/util/List;"))
    public List<WorldUpgrader.FileToUpgrade> cesiumGetChunks(RegionStorageInfo regionStorageInfo, Path path) {
        final Cursor<byte[]> cursor = tmpLMDBInstance.getDatabase(tmpSpec)
                .getIterator();

        final Map<String, List<ChunkPos>> regionList = new HashMap<>();

        boolean exists = cursor.first();
        while (exists) {
            final ChunkPos chunkPos = tmpLMDBInstance.getDatabase(tmpSpec)
                    .getKeySerializer()
                    .deserializeKey(cursor.key());

            final String regionKey = chunkPos.getRegionX() + "." + chunkPos.getRegionZ();

            if (!regionList.containsKey(regionKey)) {
                regionList.put(regionKey, new ArrayList<>());
            }

            regionList.get(regionKey).add(chunkPos);

            exists = cursor.next();
        }

        cursor.close();

        tmpLMDBInstance = null;
        tmpSpec = null;

        return regionList.values().stream().map(list -> new WorldUpgrader.FileToUpgrade(null, list)).toList();
    }
}
