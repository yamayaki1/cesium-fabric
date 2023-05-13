package de.yamayaki.cesium.mixin.core.chunks;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.common.ChunkDatabaseAccess;
import de.yamayaki.cesium.common.IOWorkerExtended;
import de.yamayaki.cesium.common.KVProvider;
import de.yamayaki.cesium.common.db.KVDatabase;
import de.yamayaki.cesium.common.db.KVTransaction;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.function.Function;

@Mixin(SectionStorage.class)
public class MixinSectionStorage<R> implements ChunkDatabaseAccess, KVProvider {
    @Mutable
    @Shadow
    @Final
    private IOWorker worker;

    private LMDBInstance database;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(Path path, Function<?, ?> function, Function<?, ?> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl, RegistryAccess registryAccess, LevelHeightAccessor levelHeightAccessor, CallbackInfo ci) {
        ((IOWorkerExtended) this.worker).setKVProvider(this);
    }

    @Override
    public KVDatabase<ChunkPos, CompoundTag> getDatabase() {
        return this.database.getDatabase(WorldDatabaseSpecs.POI);
    }

    @Override
    public void setDatabase(LMDBInstance storage) {
        this.database = storage;
    }

    @Override
    public KVTransaction<ChunkPos, CompoundTag> getTransaction() {
        return this.database.getTransaction(WorldDatabaseSpecs.POI);
    }
}
