package de.yamayaki.cesium.common.zstd;

import com.github.luben.zstd.ZstdCompressCtx;
import com.github.luben.zstd.ZstdDecompressCtx;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.NotNull;

public class ZSTDContext {
    private final ZstdCompressCtx compressCtx;
    private final Long2ObjectMap<ZstdDecompressCtx> decompressCtx;

    public ZSTDContext(final boolean usesDictionary, final int compressionLevel, final ZSTDDictionary dictionaries) {
        this.compressCtx = this.createCompressCtx(usesDictionary, compressionLevel, dictionaries);
        this.decompressCtx = this.createDecompressCtx(dictionaries);
    }

    private ZstdCompressCtx createCompressCtx(final boolean usesDictionary, final int compressionLevel, final ZSTDDictionary dictionaries) {
        var ctx = new ZstdCompressCtx();

        ctx.setLevel(compressionLevel);

        if (usesDictionary) {
            ctx.loadDict(dictionaries.compressDictionary());
        }

        return ctx;
    }

    private Long2ObjectMap<ZstdDecompressCtx> createDecompressCtx(final ZSTDDictionary dictionaries) {
        var ctxMap = new Long2ObjectArrayMap<ZstdDecompressCtx>();

        for (long dictId : dictionaries.dictionaries()) {
            var dict = dictionaries.decompressDictionary(dictId);
            var ctx = new ZstdDecompressCtx().loadDict(dict);

            ctxMap.put(dictId, ctx);
        }

        ctxMap.put(0, new ZstdDecompressCtx());

        return ctxMap;
    }

    public @NotNull ZstdCompressCtx compress() {
        return this.compressCtx;
    }

    public @NotNull ZstdDecompressCtx decompress(final long dictId) {
        var ctx = this.decompressCtx.get(dictId);

        if (ctx == null) {
            throw new RuntimeException("Could not find context for dictionary with id " + dictId);
        }

        return ctx;
    }
}
