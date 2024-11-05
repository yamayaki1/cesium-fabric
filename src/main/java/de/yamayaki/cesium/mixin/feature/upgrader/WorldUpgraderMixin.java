package de.yamayaki.cesium.mixin.feature.upgrader;

import com.llamalad7.mixinextras.sugar.Local;
import de.yamayaki.cesium.api.accessor.IStorageProvider;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IComponentStorage;
import de.yamayaki.cesium.storage.IWorldStorage;
import de.yamayaki.cesium.storage.impl.CesiumWorldStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

@Mixin(targets = "net.minecraft.util.worldupdate.WorldUpgrader$AbstractUpgrader")
public abstract class WorldUpgraderMixin {
    /* Vanilla imports */
    @Shadow @Final WorldUpgrader field_48734;

    /* Our own fields */
    @Unique private @Nullable IWorldStorage worldStorage = null;
    @Unique private double chunkCount = 0;

    @Inject(
            method = "upgrade",
            at = @At(value = "HEAD")
    )
    private void cesium$openStorage(CallbackInfo ci) {
        this.worldStorage = new CesiumWorldStorage(this.field_48734.levelStorage.getDimensionPath(Level.OVERWORLD));
        this.worldStorage.askForDimensions(this.field_48734.levelStorage, this.field_48734.levels().stream().toList());
    }

    @Inject(
            method = "getDimensionsToUpgrade",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/worldupdate/WorldUpgrader$AbstractUpgrader;getFilesToProcess(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;)Ljava/util/ListIterator;"
            )
    )
    @SuppressWarnings("unchecked")
    public <T extends AutoCloseable> void cesium$setStorage(CallbackInfoReturnable<List<WorldUpgrader.DimensionToUpgrade<T>>> cir, @Local RegionStorageInfo regionStorageInfo, @Local AutoCloseable autoCloseable) {
        if (this.worldStorage == null) {
            throw new IllegalStateException("WorldStorage not yet set!");
        }

        var dimension = regionStorageInfo.dimension();

        var storage = switch (regionStorageInfo.type()) {
            case "entities" -> this.worldStorage.dimension(dimension).entityStorage();
            case "poi" -> this.worldStorage.dimension(dimension).poiStorage();
            case "chunk" -> this.worldStorage.dimension(dimension).chunkStorage();
            default -> throw new IllegalStateException("Unexpected value: " + regionStorageInfo.type());
        };

        ((IStorageSetter<ChunkPos, CompoundTag>) autoCloseable).cesium$setStorage(storage);
    }

    @Redirect(
            method = "getDimensionsToUpgrade",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/worldupdate/WorldUpgrader$AbstractUpgrader;getFilesToProcess(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;)Ljava/util/ListIterator;"
            )
    )
    @SuppressWarnings("unchecked")
    public ListIterator<WorldUpgrader.FileToUpgrade> cesium$getChunks(WorldUpgrader.AbstractUpgrader<?> instance, RegionStorageInfo regionStorageInfo, Path path, @Local AutoCloseable autoCloseable) {
        if (!(autoCloseable instanceof IStorageProvider<?, ?> storageGetter)) {
            throw new IllegalStateException("AutoCloseable must be an instance of AutoCloseable");
        }

        var storage = (IComponentStorage<ChunkPos, CompoundTag>) storageGetter.cesium$storage();
        var regions = new HashMap<String, List<ChunkPos>>();

        for (final ChunkPos chunkPos : storage.allKeys()) {
            final String regionKey = chunkPos.getRegionX() + "." + chunkPos.getRegionZ();

            if (!regions.containsKey(regionKey)) {
                regions.put(regionKey, new ArrayList<>());
            }

            regions.get(regionKey).add(chunkPos);
        }

        this.field_48734.totalFiles += regions.size();
        this.field_48734.totalChunks += regions.values().stream().mapToInt(List::size).sum();

        return regions.values().stream().map(list -> new WorldUpgrader.FileToUpgrade(null, list)).toList().listIterator();
    }

    @Inject(
            method = "upgrade",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/worldupdate/WorldUpgrader$AbstractUpgrader;processOnePosition(Lnet/minecraft/resources/ResourceKey;Ljava/lang/AutoCloseable;Lnet/minecraft/world/level/ChunkPos;)Z"
            )
    )
    public void cesium$flushChanges(CallbackInfo ci) {
        if (this.worldStorage != null) {
            if (chunkCount % 1024 == 0) {
                this.worldStorage.flush();
            }

            chunkCount++;
        }
    }

    @Inject(
            method = "upgrade",
            at = @At("RETURN")
    )
    public void cesium$closeStorage(CallbackInfo ci) {
        if (this.worldStorage != null) {
            this.worldStorage.close();
        }
    }
}
