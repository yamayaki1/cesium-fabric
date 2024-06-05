package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import net.minecraft.world.level.ChunkPos;

public class ChunkPosSerializer implements KeySerializer<ChunkPos> {
    @Override
    public byte[] serializeKey(final ChunkPos value) {
        final int x = value.x;
        final int z = value.z;

        return new byte[]{
                (byte) (x >> 24), (byte) (x >> 16), (byte) (x >> 8), (byte) x,
                (byte) (z >> 24), (byte) (z >> 16), (byte) (z >> 8), (byte) z
        };
    }

    @Override
    public ChunkPos deserializeKey(final byte[] array) {
        final int x = array[0] << 24 | (array[1] & 0xFF) << 16 | (array[2] & 0xFF) << 8 | (array[3] & 0xFF);
        final int z = array[4] << 24 | (array[5] & 0xFF) << 16 | (array[6] & 0xFF) << 8 | (array[7] & 0xFF);

        return new ChunkPos(x, z);
    }
}
