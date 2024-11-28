package de.yamayaki.cesium.mixin.base.storage;

import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IComponentStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(IOWorker.class)
public class IOWorkerMixin implements IStorageSetter<ChunkPos, CompoundTag> {
    /* Vanilla imports */
    @Shadow @Final private RegionFileStorage storage;

    /* Our own storage */
    @Unique private @Nullable IComponentStorage<ChunkPos, CompoundTag> componentStorage = null;

    @Override
    public void cesium$setStorage(final @NotNull IComponentStorage<ChunkPos, CompoundTag> componentStorage) {
        this.componentStorage = componentStorage;

        try {
            this.storage.close();
        } catch (final IOException ignored) {
        }
    }

    /**
     * @see IOWorker#loadAsync(ChunkPos)
     */
    @Redirect(
            method = "method_27943",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;read(Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private CompoundTag cesium$read(RegionFileStorage instance, ChunkPos chunkPos) throws IOException {
        if (this.componentStorage == null) {
            return instance.read(chunkPos);
        } else {
            return this.componentStorage.getValue(chunkPos);
        }
    }

    /**
     * @see IOWorker#scanChunk(ChunkPos, StreamTagVisitor)
     */
    @Redirect(
            method = "method_39801",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;scanChunk(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/StreamTagVisitor;)V"
            )
    )
    private void cesium$scanChunk(RegionFileStorage instance, ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) throws IOException {
        if (this.componentStorage == null) {
            instance.scanChunk(chunkPos, streamTagVisitor);
        } else {
            this.componentStorage.scan(chunkPos, streamTagVisitor);
        }
    }

    /**
     * @see IOWorker#synchronize(boolean)
     */
    @Redirect(
            method = "method_27946",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;flush()V"
            )
    )
    private void cesium$flush(RegionFileStorage instance) throws IOException {
        if (this.componentStorage == null) {
            instance.flush();
        }
    }

    @Redirect(
            method = "runStore",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"
            )
    )
    private void cesium$write(RegionFileStorage instance, ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
        if (this.componentStorage == null) {
            instance.write(chunkPos, compoundTag);
        } else {
            this.componentStorage.putValue(chunkPos, compoundTag);
        }
    }

    @Redirect(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;close()V"
            )
    )
    private void cesium$close(RegionFileStorage instance) throws IOException {
        if (this.componentStorage == null) {
            instance.close();
        }
    }
}
