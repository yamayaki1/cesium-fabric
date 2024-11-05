package de.yamayaki.cesium.mixin.base.dimension;

import de.yamayaki.cesium.api.accessor.IDimensionStorageGetter;
import de.yamayaki.cesium.api.accessor.IWorldStorageGetter;
import de.yamayaki.cesium.storage.IDimensionStorage;
import de.yamayaki.cesium.storage.IWorldStorage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements IDimensionStorageGetter {
    /* Our dimension storage */
    @Unique private IDimensionStorage dimensionStorage;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getFixerUpper()Lcom/mojang/datafixers/DataFixer;"
            )
    )
    public void cesium$openStorage(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List<?> list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci) {
        final IWorldStorage worldStorage = ((IWorldStorageGetter) minecraftServer).cesium$worldStorage();
        worldStorage.askForDimensions(levelStorageAccess, List.of(resourceKey));

        this.dimensionStorage = worldStorage.dimension(resourceKey);
    }

    @Override
    public @NotNull IDimensionStorage cesium$dimensionStorage() {
        if (dimensionStorage == null) {
            throw new IllegalStateException("The dimension storage has not been set.");
        }

        return this.dimensionStorage;
    }
}
