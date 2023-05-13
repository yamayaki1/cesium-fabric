package de.yamayaki.cesium.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.common.CesiumActions;
import de.yamayaki.cesium.common.ChunkDatabaseAccess;
import de.yamayaki.cesium.common.IOWorkerExtended;
import de.yamayaki.cesium.common.KVProvider;
import de.yamayaki.cesium.common.db.KVDatabase;
import de.yamayaki.cesium.common.db.KVTransaction;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(ChunkStorage.class)
public class MixinChunkStorage implements ChunkDatabaseAccess, CesiumActions, KVProvider {
    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    private LMDBInstance database;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(Path path, DataFixer dataFixer, boolean bl, CallbackInfo ci) {
        ((IOWorkerExtended) this.worker).setKVProvider(this);
    }

    @Override
    public void cesiumFlush() {
        this.database.flushChanges();
    }

    @Override
    public void cesiumClose() {
        this.database.close();
    }

    @Override
    public KVDatabase<ChunkPos, CompoundTag> getDatabase() {
        return this.database.getDatabase(WorldDatabaseSpecs.CHUNK_DATA);
    }

    @Override
    public void setDatabase(LMDBInstance database) {
        this.database = database;
    }

    @Override
    public KVTransaction<ChunkPos, CompoundTag> getTransaction() {
        return this.database.getTransaction(WorldDatabaseSpecs.CHUNK_DATA);
    }
}
