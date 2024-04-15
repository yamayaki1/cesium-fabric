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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Mixin(IOWorker.class)
public class ChunkExporterMixin {
    @Unique
    private static final Path basePath = Path.of("./exportedChunks/");

    static {
        try {
            if (!Files.isDirectory(basePath)) {
                Files.createDirectories(basePath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("This should not have happened");
        }
    }

    @Inject(method = "store", at = @At("HEAD"))
    public void storeChunkOnDisk(ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        try {
            if (compoundTag == null || !compoundTag.contains("Status")) {
                return;
            }

            NbtIo.write(compoundTag, basePath.resolve(chunkPos.toLong() + ".nbt"));
        } catch (IOException ignored) {
        }
    }
}
