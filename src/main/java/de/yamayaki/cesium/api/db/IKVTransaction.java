package de.yamayaki.cesium.api.db;

public interface IKVTransaction<K, V> {
    void add(K key, V value);
}
