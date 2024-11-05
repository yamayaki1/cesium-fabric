package de.yamayaki.cesium.api.database;

import org.jetbrains.annotations.NotNull;
import org.lmdbjava.Stat;

import java.nio.file.Path;
import java.util.List;

public interface IDBInstance {
    <K, V> @NotNull IKVDatabase<K, V> getDatabase(final @NotNull DatabaseSpec<K, V> spec);

    @NotNull List<Stat> getStats();

    void flushChanges();

    void compact(final Path copyPath);

    void close();
}
