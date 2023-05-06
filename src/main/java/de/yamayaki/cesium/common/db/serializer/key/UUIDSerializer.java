package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import de.yamayaki.cesium.common.io.BufferUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDSerializer implements KeySerializer<UUID> {
    @Override
    public byte[] serializeKey(UUID value) {
        ByteBuffer buf = BufferUtils.getBuffer(16);
        buf.putLong(0, value.getLeastSignificantBits());
        buf.putLong(8, value.getMostSignificantBits());
        return BufferUtils.toArray(buf);
    }

    @Override
    public UUID deserializeKey(byte[] array) {
        ByteBuffer buf = BufferUtils.ofArray(array);
        return new UUID(buf.getLong(8), buf.getLong(0));
    }
}
