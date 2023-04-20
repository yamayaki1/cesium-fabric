package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import net.minecraft.core.SectionPos;

import java.nio.ByteBuffer;

public class ChunkSectionPosSerializer implements KeySerializer<SectionPos> {
    @Override
    public void serializeKey(ByteBuffer buf, SectionPos value) {
        buf.putInt(0, value.getX());
        buf.putInt(4, value.getY());
        buf.putInt(8, value.getZ());
    }

    @Override
    public SectionPos deserializeKey(ByteBuffer buffer) {
        return SectionPos.of(buffer.getInt(0), buffer.getInt(4), buffer.getInt(8));
    }

    @Override
    public int getKeyLength() {
        return 12;
    }
}
