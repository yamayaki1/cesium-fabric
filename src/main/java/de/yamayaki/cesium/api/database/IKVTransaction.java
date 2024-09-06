package de.yamayaki.cesium.api.database;

public interface IKVTransaction<K, V> {
    void add(final K key, final V value);

    void addBytes(final K key, final byte[] value);
}
