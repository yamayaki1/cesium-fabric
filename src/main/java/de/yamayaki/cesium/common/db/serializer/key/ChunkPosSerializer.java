package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import de.yamayaki.cesium.common.io.BufferUtils;
import net.minecraft.world.level.ChunkPos;

import java.nio.ByteBuffer;

public class ChunkPosSerializer implements KeySerializer<ChunkPos> {
    @Override
    public byte[] serializeKey(ChunkPos value) {
        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.putInt(0, value.x);
        buf.putInt(4, value.z);
        return BufferUtils.toArray(buf);
    }

    @Override
    public ChunkPos deserializeKey(byte[] array) {
        ByteBuffer buf = BufferUtils.ofArray(array);
        return new ChunkPos(buf.getInt(0), buf.getInt(4));
    }
}
