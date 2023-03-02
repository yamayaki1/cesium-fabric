package me.jellysquid.mods.radon.mixin;

import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.ChunkDatabaseAccess;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.DatabaseSpec;
import me.jellysquid.mods.radon.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
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
    private void reinit(ServerLevel serverWorld, LevelStorageSource.LevelStorageAccess session, DataFixer dataFixer, StructureManager structureManager, Executor workerExecutor, BlockableEventLoop<Runnable> mainThreadExecutor, LightChunkGetter chunkProvider, ChunkGenerator chunkGenerator, ChunkProgressListener worldGenerationProgressListener, Supplier<DimensionDataStorage> supplier, int i, boolean bl, CallbackInfo ci) {
        this.database = new LMDBInstance(session.getDimensionPath(serverWorld.dimension()), "chunks", new DatabaseSpec[] {
                WorldDatabaseSpecs.CHUNK_DATA,
                WorldDatabaseSpecs.POI
        });

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

    @Inject(method = "close", at = @At("RETURN"))
    private void postClose(CallbackInfo ci) {
        this.database.close();
    }
}
