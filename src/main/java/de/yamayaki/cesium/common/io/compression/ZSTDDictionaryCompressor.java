package de.yamayaki.cesium.common.io.compression;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import de.yamayaki.cesium.CesiumMod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ZSTDDictionaryCompressor implements StreamCompressor {
    private final ZstdDictCompress dictCompress;
    private final ZstdDictDecompress dictDecompress;

    public ZSTDDictionaryCompressor() {
        final int compressionLevel = CesiumMod.config().getCompression().getLevel();

        try (InputStream inputStream = CesiumMod.class.getResourceAsStream("/cesium.zstd.dict")) {
            if (inputStream == null) {
                throw new IOException("Dictionary file not available");
            }

            byte[] dictionary = inputStream.readAllBytes();
            this.dictCompress = new ZstdDictCompress(dictionary, compressionLevel);
            this.dictDecompress = new ZstdDictDecompress(dictionary);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static long checkError(long rc) {
        if (Zstd.isError(rc)) {
            throw new IllegalStateException(Zstd.getErrorName(rc));
        }

        return rc;
    }

    @Override
    public byte[] compress(byte[] src) {
        byte[] dst = new byte[(int) Zstd.compressBound(src.length)];
        int size = (int) checkError(Zstd.compressFastDict(dst, 0, src, 0, this.dictCompress));

        return Arrays.copyOfRange(dst, 0, size);
    }

    @Override
    public byte[] decompress(byte[] src) {
        byte[] dst = new byte[(int) checkError(Zstd.decompressedSize(src))];
        checkError(Zstd.decompressFastDict(dst, 0, src, 0, src.length, this.dictDecompress));

        return dst;
    }
}