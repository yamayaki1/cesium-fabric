package de.yamayaki.cesium.common.lmdb;

import de.yamayaki.cesium.CesiumConfig;
import de.yamayaki.cesium.api.database.DatabaseSpec;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.api.database.IKVDatabase;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.lmdbjava.ByteArrayProxy;
import org.lmdbjava.CopyFlags;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.EnvInfo;
import org.lmdbjava.LmdbException;
import org.lmdbjava.Stat;
import org.lmdbjava.Txn;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LMDBInstance implements IDBInstance {
    private static final int MAX_COMMIT_TRIES = 3;

    private final Reference2ObjectMap<DatabaseSpec<?, ?>, KVDatabase<?, ?>> databases = new Reference2ObjectOpenHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected final Env<byte[]> env;

    private final Logger logger;
    private final boolean logsMapGrows;
    private final int resizeStep;

    protected volatile boolean dirty = false;

    public LMDBInstance(final Path databasePath, final DatabaseSpec<?, ?>[] databases, final Logger logger, final CesiumConfig config) {
        this.logger = logger;
        this.logsMapGrows = config.general.logMapGrows;

        this.env = Env.create(ByteArrayProxy.PROXY_BA)
                .setMaxDbs(databases.length)
                .open(databasePath.toFile(), EnvFlags.MDB_NOLOCK, EnvFlags.MDB_NOSUBDIR);

        this.resizeStep = Arrays.stream(databases).mapToInt(DatabaseSpec::initialSize).sum();

        EnvInfo info = this.env.info();
        if (info.mapSize < this.resizeStep) {
            this.env.setMapSize(this.resizeStep);
        }

        for (DatabaseSpec<?, ?> spec : databases) {
            this.databases.put(spec, new KVDatabase<>(this, spec, config.compression.enableCompression));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> @NotNull IKVDatabase<K, V> getDatabase(final @NotNull DatabaseSpec<K, V> spec) {
        KVDatabase<?, ?> database = this.databases.get(spec);

        if (database == null) {
            throw new NullPointerException("No database is registered for spec " + spec);
        }

        return (IKVDatabase<K, V>) database;
    }

    @Override
    public void flushChanges() {
        if (!this.dirty) return;

        this.lock.writeLock()
                .lock();

        try {
            this.databases.values()
                    .forEach(KVDatabase::createSnapshot);

            try {
                this.commitTransaction();
            } finally {
                this.dirty = false;

                this.databases.values()
                        .forEach(KVDatabase::clearSnapshot);
            }
        } finally {
            this.lock.writeLock()
                    .unlock();
        }
    }

    private void commitTransaction() {
        for (int tries = 1; tries < MAX_COMMIT_TRIES + 1; tries++) {
            try (final Txn<?> txn = this.prepareTransaction()) {
                txn.commit();

                break;
            } catch (final LmdbException l) {
                if (l instanceof Env.MapFullException) {
                    this.growMap();

                    tries--;
                    continue;
                }

                this.logger.info("Commit of transaction failed; trying again ({}/{}): {}", tries, MAX_COMMIT_TRIES, l.getMessage());
            }

            if (tries == MAX_COMMIT_TRIES) {
                throw new RuntimeException("Could not commit transactions!");
            }
        }
    }

    private Txn<?> prepareTransaction() throws LmdbException {
        final Txn<byte[]> txn = this.env.txnWrite();

        try {
            for (final KVDatabase<?, ?> dbi : this.databases.values()) {
                dbi.addChanges(txn);
            }
        } catch (final LmdbException l) {
            txn.abort();
            throw l;
        }

        return txn;
    }

    private void growMap() {
        EnvInfo info = this.env.info();

        long oldSize = info.mapSize;
        long newSize = oldSize + (long) this.resizeStep;

        this.env.setMapSize(newSize);

        if (this.logsMapGrows) {
            this.logger.info("Grew map size from {} to {} MB", (oldSize / 1024 / 1024), (newSize / 1024 / 1024));
        }
    }

    public void compact(final Path path) {
        this.lock.writeLock()
                .lock();

        try {
            this.env.copy(path.toFile(), CopyFlags.MDB_CP_COMPACT);
        } finally {
            this.lock.writeLock()
                    .unlock();
        }
    }

    @Override
    public @NotNull List<Stat> getStats() {
        this.lock.readLock()
                .lock();

        try {
            return this.databases.values().stream()
                    .map(KVDatabase::getStats)
                    .toList();
        } finally {
            this.lock.readLock()
                    .unlock();
        }

    }

    ReentrantReadWriteLock lock() {
        return this.lock;
    }

    @Override
    public void close() {
        if (this.env.isClosed()) {
            return;
        }

        this.flushChanges();

        for (KVDatabase<?, ?> database : this.databases.values()) {
            database.close();
        }

        this.env.close();
    }
}
