package de.yamayaki.cesium.common.io.compression;

import com.github.luben.zstd.ZstdCompressCtx;
import com.github.luben.zstd.ZstdDecompressCtx;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.NotNull;

public class ZSTDContext {
    private final ThreadLocal<ZstdCompressCtx> compressCtx = ThreadLocal.withInitial(this::compressCtx);
    private final ThreadLocal<Long2ObjectMap<ZstdDecompressCtx>> decompressCtx = ThreadLocal.withInitial(this::decompressCtx);

    private final boolean usesDictionary;
    private final int compressionLevel;
    private final ZSTDDictionary dictionary;

    public ZSTDContext(final boolean usesDictionary, final int compressionLevel, final ZSTDDictionary dictionary) {
        this.usesDictionary = usesDictionary;
        this.compressionLevel = compressionLevel;
        this.dictionary = dictionary;
    }

    private ZstdCompressCtx compressCtx() {
        final ZstdCompressCtx ctx = new ZstdCompressCtx();
        ctx.setLevel(compressionLevel);

        if(usesDictionary) {
            ctx.loadDict(dictionary.compressDictionary());
        }

        return ctx;
    }

    private Long2ObjectMap<ZstdDecompressCtx> decompressCtx() {
        final Long2ObjectMap<ZstdDecompressCtx> map = new Long2ObjectArrayMap<>();

        for (final long dictId : this.dictionary.dictionaries()) {
            final ZstdDecompressCtx ctx = new ZstdDecompressCtx();
            ctx.loadDict(this.dictionary.decompressDictionary(dictId));

            map.put(dictId, ctx);
        }

        map.put(0, new ZstdDecompressCtx());

        return map;
    }

    public ZstdCompressCtx compress() {
        return this.compressCtx.get();
    }

    public @NotNull ZstdDecompressCtx decompress(final long dictId) {
        final Long2ObjectMap<ZstdDecompressCtx> map = this.decompressCtx.get();
        final ZstdDecompressCtx ctx = map.get(dictId);

        if(ctx == null) {
            throw new RuntimeException("Could not find context for dictionary with id " + dictId);
        }

        return ctx;
    }
}
