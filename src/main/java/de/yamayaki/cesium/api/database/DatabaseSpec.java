package de.yamayaki.cesium.api.database;

public record DatabaseSpec<K, V>(String name, Class<K> key, Class<V> value, int initialSize) {
    @Override
    public String toString() {
        return String.format("DatabaseSpec{key=%s, value=%s}@%s", this.key.getName(), this.value.getName(), this.hashCode());
    }
}
