package de.yamayaki.cesium.common.db.serializer;

public interface KeySerializer<T> {
    byte[] serializeKey(T value);

    T deserializeKey(byte[] buffer);
}
