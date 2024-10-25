package de.yamayaki.cesium.api.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IScannable<T> {
    void scan(byte @NotNull [] input, T scanner) throws IOException;
}
