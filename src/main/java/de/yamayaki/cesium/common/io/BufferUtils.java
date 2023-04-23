package de.yamayaki.cesium.common.io;

import java.nio.ByteBuffer;

public class BufferUtils {
    public static byte[] toArray(ByteBuffer buf) {
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);

        return arr;
    }

    public static ByteBuffer ofArray(byte[] array) {
        return ByteBuffer.wrap(array);
    }
}
