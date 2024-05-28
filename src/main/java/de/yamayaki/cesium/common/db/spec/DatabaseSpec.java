package de.yamayaki.cesium.common.db.spec;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.common.io.compression.DefaultStreamCompressors;
import de.yamayaki.cesium.common.io.compression.StreamCompressor;

public class DatabaseSpec<K, V> {
    private final String name;

    private final Class<K> key;
    private final Class<V> value;

    private final int initialSize;

    private final StreamCompressor compressor;

    public DatabaseSpec(String name, Class<K> key, Class<V> value, int initialSize) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.initialSize = initialSize;

        this.compressor = CesiumMod.config().isUncompressed()
                ? DefaultStreamCompressors.NONE
                : DefaultStreamCompressors.ZSTD;
    }

    public Class<K> getKeyType() {
        return this.key;
    }

    public Class<V> getValueType() {
        return this.value;
    }

    public StreamCompressor getCompressor() {
        return this.compressor;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("DatabaseSpec{key=%s, value=%s}@%s", this.key.getName(), this.value.getName(), this.hashCode());
    }

    public int getInitialSize() {
        return this.initialSize;
    }
}
