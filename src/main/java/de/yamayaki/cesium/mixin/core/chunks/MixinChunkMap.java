package de.yamayaki.cesium.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.common.db.DatabaseItem;
import de.yamayaki.cesium.common.ChunkDatabaseAccess;
import de.yamayaki.cesium.common.db.LMDBInstance;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ChunkMap.class)
public class MixinChunkMap {
    @Shadow
    @Final
    private PoiManager poiManager;

    private LMDBInstance database;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, BlockableEventLoop<?> blockableEventLoop, LightChunkGetter lightChunkGetter, ChunkGenerator chunkGenerator, ChunkProgressListener chunkProgressListener, ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier<?> supplier, int i, boolean bl, CallbackInfo ci) {
        this.database = ((DatabaseItem) serverLevel).getStorage();

        ((ChunkDatabaseAccess) this.poiManager)
                .setDatabase(this.database);

        ((ChunkDatabaseAccess) this)
                .setDatabase(this.database);
    }

    @Inject(method = "saveAllChunks", at = @At("RETURN"))
    private void postSaveChunks(boolean flush, CallbackInfo ci) {
        this.flushChunks();
    }

    @Inject(method = "processUnloads", at = @At("RETURN"))
    private void postUnloadChunks(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.flushChunks();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void postTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.flushChunks();
    }

    private void flushChunks() {
        this.database.flushChanges();
    }
}
