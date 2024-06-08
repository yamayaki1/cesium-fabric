package de.yamayaki.cesium.api.io;

public interface ICompressor {
    byte[] compress(final byte[] input);

    byte[] decompress(final byte[] input);
}
