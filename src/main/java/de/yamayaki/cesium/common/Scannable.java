package de.yamayaki.cesium.common;

import java.io.IOException;
import java.nio.ByteBuffer;

//idea by https://github.com/mo0dss/radon-fabric
public interface Scannable<T> {
    void scan(ByteBuffer byteBuffer, T scanner) throws IOException;
}
