package de.yamayaki.cesium.common.lmdb;

import de.yamayaki.cesium.api.database.ICloseableIterator;
import de.yamayaki.cesium.api.io.ISerializer;
import org.lmdbjava.Cursor;

public class CursorIterator<K> implements ICloseableIterator<K> {
    private final ISerializer<K> keySerializer;
    private final Cursor<byte[]> cursor;

    private boolean hadSetPosition = false;

    public CursorIterator(final ISerializer<K> keySerializer, final Cursor<byte[]> cursor) {
        this.keySerializer = keySerializer;
        this.cursor = cursor;
    }

    @Override
    public boolean hasNext() {
        if (!this.hadSetPosition) {
            this.hadSetPosition = true;

            return this.cursor.first();
        }

        return this.cursor.next();
    }

    @Override
    public K next() {
        return this.keySerializer.deserializeKey(this.cursor.key());
    }

    @Override
    public void close() {
        this.cursor.close();
    }
}
