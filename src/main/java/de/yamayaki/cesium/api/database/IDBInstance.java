package de.yamayaki.cesium.api.database;

import org.lmdbjava.Stat;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface IDBInstance {
    <K, V> IKVDatabase<K, V> getDatabase(final DatabaseSpec<K, V> spec);

    void flushChanges();

    List<Stat> getStats();

    void createCopy(final Path copyPath);

    ReentrantReadWriteLock getLock();

    boolean closed();

    void close();
}
