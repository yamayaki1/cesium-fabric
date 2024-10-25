package de.yamayaki.cesium.api.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface ICompressor {
    byte @NotNull [] compress(final byte @NotNull [] input) throws IOException;

    byte @NotNull [] decompress(final byte @NotNull [] input) throws IOException;
}
