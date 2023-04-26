package de.yamayaki.cesium.common.db;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.lmdbjava.*;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LMDBInstance {
    protected final Env<byte[]> env;
    protected final Reference2ObjectMap<DatabaseSpec<?, ?>, KVDatabase<?, ?>> databases = new Reference2ObjectOpenHashMap<>();
    protected final Reference2ObjectMap<DatabaseSpec<?, ?>, KVTransaction<?, ?>> transactions = new Reference2ObjectOpenHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final int resizeStep;

    public LMDBInstance(File dir, String name, DatabaseSpec<?, ?>[] databases) {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new RuntimeException("Couldn't create directory: " + dir);
        }

        File file = new File(dir, name + ".db");

        this.env = Env.create(ByteArrayProxy.PROXY_BA)
                .setMaxDbs(databases.length)
                .open(file, EnvFlags.MDB_NOLOCK, EnvFlags.MDB_NOSUBDIR);

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
        this.lock.writeLock()
                .lock();

        try {
            this.commitTransaction();
        } finally {
            this.lock.writeLock()
                    .unlock();
        }
    }

    private void commitTransaction() {
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

        txn.commit();

        this.transactions.values()
                .forEach(KVTransaction::clear);
    }

    private void growMap() {
        EnvInfo info = this.env.info();

        long oldSize = info.mapSize;
        long newSize = oldSize + this.resizeStep;

        this.env.setMapSize(newSize);
        CesiumMod.logger().info("Grew map size from {} to {} MB", (oldSize / 1024 / 1024), (newSize / 1024 / 1024));
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
