package de.yamayaki.cesium.common.db.serializer;

import java.io.IOException;

public interface ValueSerializer<T> {
    byte[] serialize(T value) throws IOException;

    T deserialize(byte[] array) throws IOException;
}
