package de.yamayaki.cesium.api.database;

public class DatabaseSpec<K, V> {
    private final String name;

    private final Class<K> key;
    private final Class<V> value;

    private final int initialSize;

    public DatabaseSpec(final String name, final Class<K> key, final Class<V> value, final int initialSize) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.initialSize = initialSize;
    }

    public Class<K> getKeyType() {
        return this.key;
    }

    public Class<V> getValueType() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public int getInitialSize() {
        return this.initialSize;
    }

    @Override
    public String toString() {
        return String.format("DatabaseSpec{key=%s, value=%s}@%s", this.key.getName(), this.value.getName(), this.hashCode());
    }
}
