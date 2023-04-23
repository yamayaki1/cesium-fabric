package de.yamayaki.cesium.common.io.compression;

public interface StreamCompressor {
    byte[] compress(byte[] in);

    byte[] decompress(byte[] in);
}
