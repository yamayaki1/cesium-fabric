package de.yamayaki.cesium.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IComponentStorage<K, V> {
    @Nullable V getValue(final @NotNull K key);
    byte @Nullable [] getRaw(final @NotNull K key);

    void putValue(final @NotNull K key, final @Nullable V value);
    void putRaw(final @NotNull K key, final byte @Nullable [] value);

    <S> void scan(final @NotNull K key, final @NotNull S scanner);

    @NotNull List<K> allKeys();

    default void copyFrom(final @NotNull IComponentStorage<K, V> source, final @NotNull K key) {
        this.putRaw(key, source.getRaw(key));
    }
}
