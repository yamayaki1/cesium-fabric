package de.yamayaki.cesium.api.database;

import de.yamayaki.cesium.api.io.ICompressor;
import de.yamayaki.cesium.api.io.ISerializer;

public interface IKVDatabase<K, V> {
    V getValue(final K key);

    byte[] getBytes(final K key);

    <S> void scan(final K key, final S scanner);

    ISerializer<K> getKeySerializer();

    ISerializer<V> getValueSerializer();

    ICompressor getCompressor();

    ICloseableIterator<K> getIterator();
}
