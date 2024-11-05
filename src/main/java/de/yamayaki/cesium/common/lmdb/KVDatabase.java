package de.yamayaki.cesium.common.lmdb;

import de.yamayaki.cesium.api.database.DatabaseSpec;
import de.yamayaki.cesium.api.database.ICloseableIterator;
import de.yamayaki.cesium.api.database.IKVDatabase;
import de.yamayaki.cesium.api.io.ICompressor;
import de.yamayaki.cesium.api.io.IScannable;
import de.yamayaki.cesium.api.io.ISerializer;
import de.yamayaki.cesium.common.DefaultCompressors;
import de.yamayaki.cesium.common.DefaultSerializers;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.Stat;
import org.lmdbjava.Txn;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KVDatabase<K, V> implements IKVDatabase<K, V> {
    private final Object2ObjectOpenHashMap<K, byte[][]> pending = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<K, byte[][]> snapshot = new Object2ObjectOpenHashMap<>();

    private final LMDBInstance storage;

    private final Env<byte[]> env;
    private final Dbi<byte[]> dbi;

    private final ISerializer<K> keySerializer;
    private final ISerializer<V> valueSerializer;

    private final ICompressor compressor;

    public KVDatabase(final LMDBInstance storage, final DatabaseSpec<K, V> spec, final boolean enableCompression) {
        this.storage = storage;

        this.env = this.storage.env;
        this.dbi = this.env.openDbi(spec.name(), DbiFlags.MDB_CREATE);

        this.keySerializer = DefaultSerializers.getSerializer(spec.key());
        this.valueSerializer = DefaultSerializers.getSerializer(spec.value());

        this.compressor = enableCompression ? DefaultCompressors.ZSTD : DefaultCompressors.NONE;
    }

    @Override
    public void addValue(final @NotNull K key, final @Nullable V value) {
        try {
            this.addSerialized(key, value != null ? this.valueSerializer.serialize(value) : null);
        } catch (final IOException i) {
            throw new RuntimeException("Failed to encode value!", i);
        }
    }

    @Override
    public void addSerialized(final @NotNull K key, final byte @Nullable [] value) {
        this.addUncompressed(key, this.keySerializer.serializeKey(key), value);
    }

    void addUncompressed(final @NotNull K key, final byte @NotNull [] keyBytes, final byte @Nullable [] valueBytes) {
        try {
            var value = new byte[][]{keyBytes, valueBytes != null ? this.compressor.compress(valueBytes) : null};

            synchronized (this.pending) {
                this.pending.put(key, value);
            }

            this.storage.dirty = true;
        } catch (final IOException i) {
            throw new RuntimeException("Failed to compress value!", i);
        }
    }

    void addChanges(final Txn<byte[]> txn) {
        for (var entry : this.snapshot.values()) {
            var key = entry[0];
            var val = entry[1];

            if (val == null) {
                this.dbi.delete(txn, key);
            } else {
                this.dbi.put(txn, key, val);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> void scan(final @NotNull K key, final @NotNull S scanner) {
        if (!(this.valueSerializer instanceof IScannable<?>)) return;

        try {
            final byte @Nullable [] bytes = this.getSerialized(key);

            if (bytes == null) {
                return;
            }

            ((IScannable<S>) this.valueSerializer).scan(bytes, scanner);
        } catch (final IOException i) {
            throw new RuntimeException("Failed to scan value!", i);
        }
    }

    @Override
    public @Nullable V getValue(final @NotNull K key) {
        try {
            final byte @Nullable [] bytes = this.getSerialized(key);

            if (bytes == null) {
                return null;
            }

            return this.valueSerializer.deserialize(bytes);
        } catch (final IOException i) {
            throw new RuntimeException("Failed to encode value!", i);
        }
    }

    @Override
    public byte @Nullable [] getSerialized(final @NotNull K key) {
        return this.getUncompressed(this.keySerializer.serializeKey(key));
    }

    byte @Nullable [] getUncompressed(final byte @NotNull [] key) {
        try {
            final byte @Nullable [] bytes = this.getFromDB(key);

            if (bytes == null) {
                return null;
            }

            return this.compressor.decompress(bytes);
        } catch (final IOException i) {
            throw new RuntimeException("Failed to decompress value!", i);
        }
    }

    byte @Nullable [] getFromDB(final byte @NotNull [] key) {
        final ReentrantReadWriteLock lock = this.storage.lock();
        lock.readLock()
                .lock();

        try (final Txn<byte[]> txn = this.env.txnRead()) {
            return this.dbi.get(txn, key);
        } finally {
            lock.readLock()
                    .unlock();
        }
    }

    @Override
    public ICloseableIterator<K> getIterator() {
        return new CursorIterator<>(this.keySerializer, this.dbi.openCursor(this.env.txnRead()));
    }

    void createSnapshot() {
        synchronized (this.pending) {
            this.snapshot.putAll(this.pending);
            this.pending.clear();
        }
    }

    void clearSnapshot() {
        this.snapshot.clear();
    }

    public Stat getStats() {
        return this.dbi.stat(this.env.txnRead());
    }

    public void close() {
        this.dbi.close();
    }
}
