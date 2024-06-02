package de.yamayaki.cesium.common.db;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.lmdbjava.ByteArrayProxy;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.EnvInfo;
import org.lmdbjava.LmdbException;
import org.lmdbjava.Stat;
import org.lmdbjava.Txn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LMDBInstance {
    protected final Env<byte[]> env;

    protected final Reference2ObjectMap<DatabaseSpec<?, ?>, KVDatabase<?, ?>> databases = new Reference2ObjectOpenHashMap<>();
    protected final Reference2ObjectMap<DatabaseSpec<?, ?>, KVTransaction<?, ?>> transactions = new Reference2ObjectOpenHashMap<>();

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final int maxCommitTries = 3;
    protected final int resizeStep;

    protected volatile boolean isDirty = false;

    public LMDBInstance(Path dir, String name, DatabaseSpec<?, ?>[] databases) {
        if (!Files.isDirectory(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ioException) {
                throw new RuntimeException("Failed to create directory.", ioException);
            }
        }

        this.env = Env.create(ByteArrayProxy.PROXY_BA)
                .setMaxDbs(databases.length)
                .open(dir.resolve(name + CesiumMod.getFileEnding()).toFile(), EnvFlags.MDB_NOLOCK, EnvFlags.MDB_NOSUBDIR, EnvFlags.MDB_WRITEMAP);

        this.resizeStep = Arrays.stream(databases).mapToInt(DatabaseSpec::getInitialSize).sum();

        EnvInfo info = this.env.info();
        if (info.mapSize < this.resizeStep) {
            this.env.setMapSize(this.resizeStep);
        }

        for (DatabaseSpec<?, ?> spec : databases) {
            KVDatabase<?, ?> database = new KVDatabase<>(this, spec);

            this.databases.put(spec, database);
            this.transactions.put(spec, new KVTransaction<>(database));
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> KVDatabase<K, V> getDatabase(DatabaseSpec<K, V> spec) {
        KVDatabase<?, ?> database = this.databases.get(spec);

        if (database == null) {
            throw new NullPointerException("No database is registered for spec " + spec);
        }

        return (KVDatabase<K, V>) database;
    }

    @SuppressWarnings("unchecked")
    public <K, V> KVTransaction<K, V> getTransaction(DatabaseSpec<K, V> spec) {
        KVTransaction<?, ?> transaction = this.transactions.get(spec);

        if (transaction == null) {
            throw new NullPointerException("No transaction is registered for spec " + spec);
        }

        return (KVTransaction<K, V>) transaction;
    }

    public void flushChanges() {
        if (!this.isDirty) {
            return;
        }

        this.lock.writeLock()
                .lock();

        try {
            this.commitTransaction();
        } finally {
            this.isDirty = false;

            this.lock.writeLock()
                    .unlock();
        }
    }

    private void commitTransaction() {
        boolean success = false;
        int tries = 0;

        this.transactions.values()
                .forEach(KVTransaction::createSnapshot);

        while (!success && tries < maxCommitTries) {
            tries++;

            Iterator<KVTransaction<?, ?>> it = this.transactions.values()
                    .iterator();

            Txn<byte[]> txn = this.env.txnWrite();

            try {
                while (it.hasNext()) {
                    try {
                        KVTransaction<?, ?> transaction = it.next();
                        transaction.addChanges(txn);
                    } catch (LmdbException e) {
                        if (e instanceof Env.MapFullException) {
                            txn.abort();

                            this.growMap();

                            txn = this.env.txnWrite();
                            it = this.transactions.values()
                                    .iterator();
                        } else {
                            throw e;
                        }
                    }
                }
            } catch (Throwable t) {
                txn.abort();

                throw t;
            }

            try {
                txn.commit();
                success = true;
            } catch (LmdbException e) {
                if (e instanceof Env.MapFullException) {
                    CesiumMod.logger().info("Commit of transaction failed; trying again ({}/{})", tries, this.maxCommitTries);
                    this.growMap();
                } else {
                    throw e;
                }
            }
        }

        if (!success) {
            throw new RuntimeException("Commit failed " + this.maxCommitTries + " times.");
        }

        this.transactions.values()
                .forEach(KVTransaction::clearSnapshot);
    }

    private void growMap() {
        EnvInfo info = this.env.info();

        long oldSize = info.mapSize;
        long newSize = oldSize + (long) this.resizeStep;

        this.env.setMapSize(newSize);

        if (CesiumMod.config().logMapGrows()) {
            CesiumMod.logger().info("Grew map size from {} to {} MB", (oldSize / 1024 / 1024), (newSize / 1024 / 1024));
        }
    }

    public List<Stat> getStats() {
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

    ReentrantReadWriteLock getLock() {
        return this.lock;
    }

    Env<byte[]> env() {
        return this.env;
    }

    public void close() {
        this.flushChanges();

        for (KVDatabase<?, ?> database : this.databases.values()) {
            database.close();
        }

        this.env.close();
    }
}
