package de.yamayaki.cesium.common.io.compression;

import com.github.luben.zstd.Zstd;
import de.yamayaki.cesium.CesiumMod;

import java.util.Arrays;

public class ZSTDCompressor implements StreamCompressor {
    private static long checkError(long rc) {
        if (Zstd.isError(rc)) {
            throw new IllegalStateException(Zstd.getErrorName(rc));
        }

        return rc;
    }

    @Override
    public byte[] compress(byte[] src) {
        byte[] dst = new byte[(int) Zstd.compressBound(src.length)];
        int size = (int) checkError(Zstd.compress(dst, src, CesiumMod.config().getCompression().getLevel()));

        return Arrays.copyOfRange(dst, 0, size);
    }

    @Override
    public byte[] decompress(byte[] src) {
        byte[] dst = new byte[(int) checkError(Zstd.decompressedSize(src))];
        checkError(Zstd.decompress(dst, src));

        return dst;
    }
}
