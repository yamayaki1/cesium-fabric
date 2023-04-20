package de.yamayaki.cesium.common.db;

import de.yamayaki.cesium.common.Scannable;
import de.yamayaki.cesium.common.db.lightning.Csr;
import de.yamayaki.cesium.common.db.lightning.Dbi;
import de.yamayaki.cesium.common.db.lightning.Env;
import de.yamayaki.cesium.common.db.lightning.Txn;
import de.yamayaki.cesium.common.db.serializer.DefaultSerializers;
import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import de.yamayaki.cesium.common.db.serializer.ValueSerializer;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.io.compression.StreamCompressor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.lmdb.LMDB;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KVDatabase<K, V> {
    private final LMDBInstance storage;

    private final Env env;
    private final Dbi dbi;

    private final KeySerializer<K> keySerializer;
    private final ValueSerializer<V> valueSerializer;

    private final StreamCompressor compressor;

    public KVDatabase(LMDBInstance storage, DatabaseSpec<K, V> spec) {
        this.storage = storage;

        this.env = this.storage.env();
        this.dbi = this.env.openDbi(spec.getName(), LMDB.MDB_CREATE);

        this.keySerializer = DefaultSerializers.getKeySerializer(spec.getKeyType());
        this.valueSerializer = DefaultSerializers.getValueSerializer(spec.getValueType());
        this.compressor = spec.getCompressor();
    }

    public V getValue(K key) {
        ReentrantReadWriteLock lock = this.storage.getLock();
        lock.readLock()
                .lock();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buf = this.dbi.get(this.env.txnRead(), this.getKeyBuffer(stack, key));

            if (buf == null) {
                return null;
            }

            ByteBuffer decompressed;

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

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buf = this.dbi.get(this.env.txnRead(), this.getKeyBuffer(stack, key));

            if (buf == null) {
                return;
            }

            ByteBuffer decompressed;
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

    private ByteBuffer getKeyBuffer(MemoryStack stack, K key) {
        ByteBuffer buf = stack.malloc(this.keySerializer.getKeyLength());

        this.keySerializer.serializeKey(buf, key);

        return buf;
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

    public void putValue(Txn txn, K key, ByteBuffer value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.dbi.put(txn, this.getKeyBuffer(stack, key), value, 0);
        }
    }

    public void delete(Txn txn, K key) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.dbi.delete(txn, this.getKeyBuffer(stack, key));
        }
    }

    public Csr getIterator() {
        return this.dbi.cursor(this.env.txnRead());
    }

    public void close() {
        this.dbi.close();
    }
}
