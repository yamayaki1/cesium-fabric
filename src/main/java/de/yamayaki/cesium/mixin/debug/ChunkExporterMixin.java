package de.yamayaki.cesium.mixin.debug;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Mixin(IOWorker.class)
public class ChunkExporterMixin {
    @Unique
    private static final File basePath = new File(".", "exportedChunks/");

    static {
        if (!basePath.exists() && !basePath.isDirectory() && !basePath.mkdirs()) {
            throw new IllegalStateException("This should not have happened");
        }
    }

    @Inject(method = "store", at = @At("HEAD"))
    public void storeChunkOnDisk(ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        try {
            if (compoundTag == null || !compoundTag.contains("Status")) {
                return;
            }

            NbtIo.write(compoundTag, new File(basePath, chunkPos.toLong() + ".nbt"));
        } catch (IOException ignored) {
        }
    }
}
