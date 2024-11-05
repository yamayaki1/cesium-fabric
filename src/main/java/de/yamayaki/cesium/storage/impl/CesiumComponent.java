package de.yamayaki.cesium.storage.impl;

import de.yamayaki.cesium.api.database.DatabaseSpec;
import de.yamayaki.cesium.api.database.ICloseableIterator;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.api.database.IKVDatabase;
import de.yamayaki.cesium.storage.IComponentStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CesiumComponent<K, V> implements IComponentStorage<K, V> {
    private final @NotNull IKVDatabase<K, V> database;

    public CesiumComponent(final @NotNull IDBInstance dbInstance, final @NotNull DatabaseSpec<K, V> databaseSpec) {
        this.database = dbInstance.getDatabase(databaseSpec);
    }

    @Override
    public @Nullable V getValue(final @NotNull K key) {
        return this.database.getValue(key);
    }

    @Override
    public byte @Nullable [] getRaw(final @NotNull K key) {
        return this.database.getSerialized(key);
    }

    @Override
    public void putValue(final @NotNull K key, final @Nullable V value) {
        this.database.addValue(key, value);
    }

    @Override
    public void putRaw(final @NotNull K key, final byte @Nullable [] value) {
        this.database.addSerialized(key, value);
    }

    @Override
    public <S> void scan(final @NotNull K key, final @NotNull S scanner) {
        this.database.scan(key, scanner);
    }

    @Override
    public @NotNull List<K> allKeys() {
        final List<K> list = new ArrayList<>();

        try (final ICloseableIterator<K> crs = this.database.getIterator()) {
            while (crs.hasNext()) {
                list.add(crs.next());
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Could not iterate on cursor.", t);
        }

        return list;
    }
}
