package de.yamayaki.cesium.common.io;

import java.io.IOException;

//idea by https://github.com/mo0dss/radon-fabric
public interface Scannable<T> {
    void scan(byte[] byteBuffer, T scanner) throws IOException;
}
