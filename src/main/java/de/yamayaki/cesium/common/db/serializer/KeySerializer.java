package de.yamayaki.cesium.common.db.serializer;

import java.nio.ByteBuffer;

public interface KeySerializer<T> {
    void serializeKey(ByteBuffer buf, T value);

    T deserializeKey(ByteBuffer buffer);

    int getKeyLength();
}
