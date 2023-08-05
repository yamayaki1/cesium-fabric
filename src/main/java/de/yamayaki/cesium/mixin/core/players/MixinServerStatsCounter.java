package de.yamayaki.cesium.mixin.core.players;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.common.db.DatabaseItem;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(ServerStatsCounter.class)
public abstract class MixinServerStatsCounter extends StatsCounter implements DatabaseItem {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private File file;

    @Shadow
    @Final
    private MinecraftServer server;

    @Unique
    private LMDBInstance database;

    @Shadow
    protected abstract String toJson();

    @Shadow public abstract void parseLocal(DataFixer dataFixer, String string);

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean killInitialLoad(File file) {
        return false;
    }

    @Unique
    private UUID getUuid() {
        return UUID.fromString(this.file.getName().replace(".json", ""));
    }

    @Override
    public void cesium$setStorage(LMDBInstance storage) {
        this.database = storage;

        String json = this.database
                .getDatabase(PlayerDatabaseSpecs.STATISTICS)
                .getValue(this.getUuid());

        if (json == null) {
            return;
        }

        try {
            this.parseLocal(this.server.getFixerUpper(), json);
        } catch (JsonParseException var5) {
            LOGGER.error("Couldn't parse statistics for player {}", this.getUuid(), var5);
        } catch (Exception var4) {
            LOGGER.error("Couldn't read statistics for player {}", this.getUuid(), var4);
        }
    }

    @Override
    public LMDBInstance cesium$getStorage() {
        return this.database;
    }

    @Overwrite
    public void save() {
        this.database
                .getTransaction(PlayerDatabaseSpecs.STATISTICS)
                .add(this.getUuid(), this.toJson());
    }
}
