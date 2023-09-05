package de.yamayaki.cesium.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.common.IOWorkerExtended;
import de.yamayaki.cesium.common.KVProvider;
import de.yamayaki.cesium.common.db.DatabaseItem;
import de.yamayaki.cesium.common.db.KVDatabase;
import de.yamayaki.cesium.common.db.KVTransaction;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.concurrent.Executor;

@Mixin(EntityStorage.class)
public class MixinEntityStorage implements KVProvider {
    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    @Unique
    protected LMDBInstance storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initCesiumEntities(ServerLevel serverLevel, Path path, DataFixer dataFixer, boolean bl, Executor executor, CallbackInfo ci) {
        this.storage = ((DatabaseItem) serverLevel).cesium$getStorage();
        ((IOWorkerExtended) this.worker).cesium$setKVProvider(this);
    }

    @Override
    public KVDatabase<ChunkPos, CompoundTag> cesium$getDatabase() {
        return this.storage.getDatabase(WorldDatabaseSpecs.ENTITY);
    }

    @Override
    public KVTransaction<ChunkPos, CompoundTag> cesium$getTransaction() {
        return this.storage.getTransaction(WorldDatabaseSpecs.ENTITY);
    }
}
