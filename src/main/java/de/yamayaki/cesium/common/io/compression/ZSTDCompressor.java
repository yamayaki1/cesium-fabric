package de.yamayaki.cesium.common.io.compression;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdCompressCtx;
import com.github.luben.zstd.ZstdDecompressCtx;
import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import de.yamayaki.cesium.CesiumMod;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ZSTDCompressor implements StreamCompressor {
    private static final int compressionLevel = CesiumMod.config().getCompression().getLevel();
    private static final boolean usesDict = CesiumMod.config().getCompression().usesDictionary();

    private final ThreadLocal<ZstdCompressCtx> cCtx = ThreadLocal.withInitial(() -> {
        ZstdCompressCtx ctx = new ZstdCompressCtx();
        ctx.setLevel(compressionLevel);
        return ctx;
    });
    private final ThreadLocal<ZstdDecompressCtx> dCtx = ThreadLocal.withInitial(ZstdDecompressCtx::new);
    private final Long2ObjectMap<ZstdDictDecompress> zstdDictMap = new Long2ObjectArrayMap<>();

    private final ZstdDictCompress zstdDictCompress;

    public ZSTDCompressor() {
        loadDictionary("1");
        this.zstdDictCompress = loadDictionary("2");
    }

    private static long checkError(long rc) {
        if (Zstd.isError(rc)) {
            throw new IllegalStateException(Zstd.getErrorName(rc));
        }

        return rc;
    }

    private ZstdDictCompress loadDictionary(final String name) {
        try (final InputStream inputStream = CesiumMod.class.getResourceAsStream("/dictionaries/" + name + ".zstd.dict")) {
            if (inputStream == null) {
                throw new IOException("Dictionary file not available");
            }

            final byte[] dictionary = inputStream.readAllBytes();

            final ZstdDictCompress dictCompress = new ZstdDictCompress(dictionary, compressionLevel);
            final ZstdDictDecompress dictDecompress = new ZstdDictDecompress(dictionary);

            this.zstdDictMap.put(Zstd.getDictIdFromDict(dictionary), dictDecompress);

            return dictCompress;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public byte[] compress(byte[] src) {
        final byte[] dst = new byte[(int) Zstd.compressBound(src.length)];

        final ZstdCompressCtx ctx = this.cCtx.get();
        if (usesDict) {
            ctx.loadDict(this.zstdDictCompress);
        } else {
            ctx.loadDict((byte[]) null);
        }

        final int size = (int) checkError(ctx.compress(dst, src));
        return Arrays.copyOfRange(dst, 0, size);
    }

    @Override
    public byte[] decompress(byte[] src) {
        byte[] dst = new byte[(int) checkError(Zstd.getFrameContentSize(src))];
        final long dictKey = Zstd.getDictIdFromFrame(src);

        final ZstdDecompressCtx ctx = this.dCtx.get();
        if (dictKey != 0) {
            ctx.loadDict(this.zstdDictMap.get(dictKey));
        } else { // Let's just assume that no dictionary is needed to decompress the data
            ctx.loadDict((byte[]) null);
        }

        checkError(ctx.decompress(dst, src));

        return dst;
    }
}
