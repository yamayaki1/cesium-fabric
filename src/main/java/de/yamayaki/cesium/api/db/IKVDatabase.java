package de.yamayaki.cesium.api.db;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import de.yamayaki.cesium.common.db.serializer.ValueSerializer;
import de.yamayaki.cesium.common.io.compression.StreamCompressor;

public interface IKVDatabase<K, V> {
    V getValue(K key);

    <T> void scan(K key, T scanner);

    KeySerializer<K> getKeySerializer();

    ValueSerializer<V> getValueSerializer();

    StreamCompressor getCompressor();

    ICloseableIterator<K> getIterator();
}
