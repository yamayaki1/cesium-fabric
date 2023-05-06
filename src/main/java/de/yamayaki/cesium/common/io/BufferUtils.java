package de.yamayaki.cesium.common.io;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;

public class BufferUtils {
    private final static ThreadLocal<ArrayDeque<ByteBuffer>> BUFFER = ThreadLocal.withInitial(() -> new ArrayDeque<>(128));

    public static ByteBuffer getBuffer(final int size) {
        ByteBuffer buffer = BUFFER.get().poll();

        if (buffer != null && buffer.limit() >= size) {
            buffer.limit(size);
            return buffer;
        } else {
            return ByteBuffer.allocateDirect(size);
        }
    }

    public static byte[] toArray(ByteBuffer buf) {
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);

        buf.clear();
        BUFFER.get().offer(buf);

        return arr;
    }

    public static ByteBuffer ofArray(byte[] array) {
        ByteBuffer buffer = getBuffer(array.length);
        buffer.put(array);

        return buffer;
    }
}
