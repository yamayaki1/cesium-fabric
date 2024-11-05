package de.yamayaki.cesium.mixin.base.storage;

import de.yamayaki.cesium.api.accessor.RawAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Mixin(RegionFileStorage.class)
public abstract class RegionFileStorageMixin implements RawAccess {
    /* Vanilla imports */
    @Shadow protected abstract RegionFile getRegionFile(ChunkPos chunkPos) throws IOException;

    @Override
    public byte[] cesium$getBytes(final ChunkPos chunkPos) throws IOException {
        final RegionFile regionFile = this.getRegionFile(chunkPos);

        try (final DataInputStream inputStream = regionFile.getChunkDataInputStream(chunkPos)) {
            if (inputStream == null) return null;
            return inputStream.readAllBytes();
        }
    }

    @Override
    public void cesium$putBytes(final ChunkPos chunkPos, byte[] bytes) throws IOException {
        final RegionFile regionFile = this.getRegionFile(chunkPos);

        if (bytes == null) {
            regionFile.clear(chunkPos);
        } else {
            try (final DataOutputStream outputStream = regionFile.getChunkDataOutputStream(chunkPos)) {
                outputStream.write(bytes);
            }
        }
    }
}
