package de.yamayaki.cesium.common.db;

import de.yamayaki.cesium.api.db.ICloseableIterator;
import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import org.lmdbjava.Cursor;

public class CursorIterator<K> implements ICloseableIterator<K> {
    private final Cursor<byte[]> cursor;
    private final KeySerializer<K> serializer;

    private boolean hasNext;

    public CursorIterator(final Cursor<byte[]> cursor, final KeySerializer<K> serializer) {
        this.cursor = cursor;
        this.serializer = serializer;

        this.hasNext = this.cursor.first();
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public K next() {
        final K key = this.serializer.deserializeKey(this.cursor.key());

        this.hasNext = this.cursor.next();

        return key;
    }

    @Override
    public void close() throws Exception {
        this.cursor.close();
    }
}
