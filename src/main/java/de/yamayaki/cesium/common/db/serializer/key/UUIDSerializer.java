package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDSerializer implements KeySerializer<UUID> {
    @Override
    public void serializeKey(ByteBuffer buf, UUID value) {
        buf.putLong(0, value.getLeastSignificantBits());
        buf.putLong(8, value.getMostSignificantBits());
    }

    @Override
    public UUID deserializeKey(ByteBuffer buffer) {
        return new UUID(buffer.getLong(8), buffer.getLong(0));
    }

    @Override
    public int getKeyLength() {
        return Long.BYTES * 2;
    }
}
