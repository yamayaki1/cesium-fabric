package de.yamayaki.cesium.common.io.compression;

import de.yamayaki.cesium.CesiumMod;

public class DefaultStreamCompressors {
    public static final StreamCompressor NONE = new StreamCompressor() {
        @Override
        public byte[] compress(byte[] in) {
            return in;
        }

        @Override
        public byte[] decompress(byte[] in) {
            return in;
        }
    };

    public static final StreamCompressor ZSTD = CesiumMod.config().getCompression().usesDictionary() ? new ZSTDDictionaryCompressor() :  new ZSTDCompressor();
}
