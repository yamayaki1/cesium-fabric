package de.yamayaki.cesium.common.db;

import de.yamayaki.cesium.common.Scannable;
import de.yamayaki.cesium.common.db.serializer.DefaultSerializers;
import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import de.yamayaki.cesium.common.db.serializer.ValueSerializer;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.io.compression.StreamCompressor;
import org.lmdbjava.Cursor;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KVDatabase<K, V> {
    private final LMDBInstance storage;

    private final Env<byte[]> env;
    private final Dbi<byte[]> dbi;

    private final KeySerializer<K> keySerializer;
    private final ValueSerializer<V> valueSerializer;

    private final StreamCompressor compressor;

    public KVDatabase(LMDBInstance storage, DatabaseSpec<K, V> spec) {
        this.storage = storage;

        this.env = this.storage.env();
        this.dbi = this.env.openDbi(spec.getName(), DbiFlags.MDB_CREATE);

        this.keySerializer = DefaultSerializers.getKeySerializer(spec.getKeyType());
        this.valueSerializer = DefaultSerializers.getValueSerializer(spec.getValueType());
        this.compressor = spec.getCompressor();
    }

    public V getValue(K key) {
        ReentrantReadWriteLock lock = this.storage.getLock();
        lock.readLock()
                .lock();

        try {
            byte[] buf = this.dbi.get(this.env.txnRead(), this.keySerializer.serializeKey(key));

            if (buf == null) {
                return null;
            }

            byte[] decompressed;

            try {
                decompressed = this.compressor.decompress(buf);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decompress value", e);
            }

            try {
                return this.valueSerializer.deserialize(decompressed);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize value", e);
            }
        } finally {
            lock.readLock()
                    .unlock();
        }
    }

    //idea by https://github.com/mo0dss/radon-fabric
    @SuppressWarnings("unchecked")
    public <T> void scan(K key, T scanner) {
        if (!(this.keySerializer instanceof Scannable<?>)) {
            return;
        }

        ReentrantReadWriteLock lock = this.storage.getLock();
        lock.readLock()
                .lock();

        try {
            byte[] buf = this.dbi.get(this.env.txnRead(), this.keySerializer.serializeKey(key));

            if (buf == null) {
                return;
            }

            byte[] decompressed;
            try {
                decompressed = this.compressor.decompress(buf);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decompress value", e);
            }

            try {
                ((Scannable<T>) this.valueSerializer).scan(decompressed, scanner);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to scan value", ex);
            }
        } finally {
            lock.readLock()
                    .unlock();
        }
    }

    public KeySerializer<K> getKeySerializer() {
        return this.keySerializer;
    }

    public ValueSerializer<V> getValueSerializer() {
        return this.valueSerializer;
    }

    public StreamCompressor getCompressor() {
        return this.compressor;
    }

    public ReentrantReadWriteLock getLock() {
        return this.storage.getLock();
    }

    public void putValue(Txn<byte[]> txn, K key, byte[] value) {
        this.dbi.put(txn, this.keySerializer.serializeKey(key), value);
    }

    public void delete(Txn<byte[]> txn, K key) {
        this.dbi.delete(txn, this.keySerializer.serializeKey(key));
    }

    public Cursor<byte[]> getIterator() {
        return this.dbi.openCursor(this.env.txnRead());
    }

    public void close() {
        this.dbi.close();
    }
}
