package de.yamayaki.cesium.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.common.CesiumActions;
import de.yamayaki.cesium.common.ChunkDatabaseAccess;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(ChunkStorage.class)
public class MixinChunkStorage implements ChunkDatabaseAccess, ChunkScanAccess, CesiumActions {
    private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap<>();

    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    @Shadow
    @Nullable
    private LegacyStructureDataHandler legacyStructureHandler;

    private LMDBInstance database;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(Path path, DataFixer dataFixer, boolean bl, CallbackInfo ci) {
        try {
            this.worker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worker = null;
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    public @NotNull CompletableFuture<Void> scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) {
        return CompletableFuture.runAsync(() -> {
            this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA)
                    .scan(chunkPos, streamTagVisitor);
        }, CesiumMod.getPool());
    }

    @Overwrite
    public ChunkScanAccess chunkScanner() {
        return this;
    }

    @Overwrite
    public CompletableFuture<Optional<CompoundTag>> read(ChunkPos chunkPos) {
        return CompletableFuture.supplyAsync(() -> {
            CompoundTag compoundTag = this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA)
                    .getValue(chunkPos);

            return Optional.ofNullable(compoundTag);
        }, CesiumMod.getPool());
    }

    @Overwrite
    public void write(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.database
                .getTransaction(WorldDatabaseSpecs.CHUNK_DATA)
                .add(chunkPos, compoundTag);

        if (this.legacyStructureHandler != null) {
            this.legacyStructureHandler.removeIndex(chunkPos.toLong());
        }
    }

    @Overwrite
    public void flushWorker() {

    }

    @Overwrite
    public void close() {

    }

    @Override
    public void setDatabase(LMDBInstance database) {
        this.database = database;
    }

    // copied from IOWorker
    @Overwrite
    public boolean isOldChunkAround(ChunkPos chunkPos, int i) {
        ChunkPos chunkPos2 = new ChunkPos(chunkPos.x - i, chunkPos.z - i);
        ChunkPos chunkPos3 = new ChunkPos(chunkPos.x + i, chunkPos.z + i);

        for (int j = chunkPos2.getRegionX(); j <= chunkPos3.getRegionX(); ++j) {
            for (int k = chunkPos2.getRegionZ(); k <= chunkPos3.getRegionZ(); ++k) {
                BitSet bitSet = this.getOrCreateOldDataForRegion(j, k).join();
                if (!bitSet.isEmpty()) {
                    ChunkPos chunkPos4 = ChunkPos.minFromRegion(j, k);
                    int l = Math.max(chunkPos2.x - chunkPos4.x, 0);
                    int m = Math.max(chunkPos2.z - chunkPos4.z, 0);
                    int n = Math.min(chunkPos3.x - chunkPos4.x, 31);
                    int o = Math.min(chunkPos3.z - chunkPos4.z, 31);

                    for (int p = l; p <= n; ++p) {
                        for (int q = m; q <= o; ++q) {
                            int r = q * 32 + p;
                            if (bitSet.get(r)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int i, int j) {
        long l = ChunkPos.asLong(i, j);
        synchronized (this.regionCacheForBlender) {
            CompletableFuture<BitSet> completableFuture = this.regionCacheForBlender.getAndMoveToFirst(l);
            if (completableFuture == null) {
                completableFuture = this.createOldDataForRegion(i, j);
                this.regionCacheForBlender.putAndMoveToFirst(l, completableFuture);
                if (this.regionCacheForBlender.size() > 1024) {
                    this.regionCacheForBlender.removeLast();
                }
            }

            return completableFuture;
        }
    }

    private CompletableFuture<BitSet> createOldDataForRegion(int i, int j) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkPos chunkPos = ChunkPos.minFromRegion(i, j);
            ChunkPos chunkPos2 = ChunkPos.maxFromRegion(i, j);
            BitSet bitSet = new BitSet();
            ChunkPos.rangeClosed(chunkPos, chunkPos2).forEach(chunkPosx -> {
                CollectFields collectFields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data"));

                try {
                    this.scanChunk(chunkPosx, collectFields).join();
                } catch (Exception var7) {
                    CesiumMod.logger().warn("Failed to scan chunk {}", chunkPosx, var7);
                    return;
                }

                Tag tag = collectFields.getResult();
                if (tag instanceof CompoundTag compoundTag && this.isOldChunk(compoundTag)) {
                    int ixx = chunkPosx.getRegionLocalZ() * 32 + chunkPosx.getRegionLocalX();
                    bitSet.set(ixx);
                }
            });
            return bitSet;
        }, CesiumMod.getPool());
    }

    private boolean isOldChunk(CompoundTag compoundTag) {
        return !compoundTag.contains("DataVersion", 99) || compoundTag.getInt("DataVersion") < 3441 || compoundTag.contains("blending_data", 10);
    }

    @Override
    public void cesiumFlush() {
        this.database.flushChanges();
    }

    @Override
    public void cesiumClose() {
        this.database.close();
    }
}
