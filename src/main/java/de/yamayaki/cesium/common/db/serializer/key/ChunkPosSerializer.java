package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import net.minecraft.world.level.ChunkPos;

import java.nio.ByteBuffer;

public class ChunkPosSerializer implements KeySerializer<ChunkPos> {
    @Override
    public void serializeKey(ByteBuffer buf, ChunkPos value) {
        buf.putInt(0, value.x);
        buf.putInt(4, value.z);
    }

    @Override
    public ChunkPos deserializeKey(ByteBuffer buffer) {
        return new ChunkPos(buffer.getInt(0), buffer.getInt(4));
    }

    @Override
    public int getKeyLength() {
        return 8;
    }
}
