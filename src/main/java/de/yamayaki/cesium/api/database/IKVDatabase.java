package de.yamayaki.cesium.api.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IKVDatabase<K, V> {
    void addValue(final @NotNull K key, final @Nullable V value);
    void addSerialized(final @NotNull K key, final byte @Nullable [] value);

    <S> void scan(final @NotNull K key, final @NotNull S scanner);

    @Nullable V getValue(final @NotNull K key);
    byte @Nullable [] getSerialized(final @NotNull K key);

    ICloseableIterator<K> getIterator();
}
