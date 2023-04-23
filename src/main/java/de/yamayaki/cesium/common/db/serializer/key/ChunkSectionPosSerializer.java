package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import de.yamayaki.cesium.common.io.BufferUtils;
import net.minecraft.core.SectionPos;

import java.nio.ByteBuffer;

public class ChunkSectionPosSerializer implements KeySerializer<SectionPos> {
    @Override
    public byte[] serializeKey(SectionPos value) {
        ByteBuffer buf = ByteBuffer.allocateDirect(12);
        buf.putInt(0, value.getX());
        buf.putInt(4, value.getY());
        buf.putInt(8, value.getZ());
        return BufferUtils.toArray(buf);
    }

    @Override
    public SectionPos deserializeKey(byte[] array) {
        ByteBuffer buf = BufferUtils.ofArray(array);
        return SectionPos.of(buf.getInt(0), buf.getInt(4), buf.getInt(8));
    }
}
