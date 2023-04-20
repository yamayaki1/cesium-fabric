package me.jellysquid.mods.radon.common.db;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import me.jellysquid.mods.radon.common.db.lightning.Txn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KVTransaction<K, V> {
    private final KVDatabase<K, V> storage;
    private final Object2ReferenceMap<K, ByteBuffer> pending = new Object2ReferenceOpenHashMap<>();

    public KVTransaction(KVDatabase<K, V> storage) {
        this.storage = storage;
    }

    public void add(K key, V value) {
        ReentrantReadWriteLock lock = this.storage.getLock();
        lock.writeLock()
                .lock();

        try {
            if (value == null) {
                this.pending.put(key, null);
                return;
            }

            ByteBuffer data = this.storage.getValueSerializer()
                    .serialize(value);

            ByteBuffer compressedData = this.storage.getCompressor()
                    .compress(data);

            this.pending.put(key, compressedData);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't serialize value", e);
        } finally {
            lock.writeLock()
                    .unlock();
        }
    }

    void addChanges(Txn txn) {
        for (Object2ReferenceMap.Entry<K, ByteBuffer> entry : this.pending.object2ReferenceEntrySet()) {
            if (entry.getValue() != null) {
                this.storage.putValue(txn, entry.getKey(), entry.getValue());
            } else {
                this.storage.delete(txn, entry.getKey());
            }
        }
    }

    void clear() {
        this.pending.clear();
    }
}
