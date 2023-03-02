package me.jellysquid.mods.radon.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.db.DatabaseItem;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.datafix.DataFixTypes;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
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
    private LMDBInstance database;

    @Shadow
    protected abstract String toJson();

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract <T> Optional<Stat<T>> getStat(StatType<T> statType, String string);

    @Shadow
    private static CompoundTag fromJson(JsonObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    @Final
    private File file;

    @Shadow
    @Final
    private MinecraftServer server;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean redirectDisableFileLoad(File file) {
        return false;
    }

    @Overwrite
    public void save() {
        this.database
                .getTransaction(PlayerDatabaseSpecs.STATISTICS)
                .add(this.getUuid(), this.toJson());
    }


    @Override
    public LMDBInstance getStorage() {
        return this.database;
    }

    @Override
    public void setStorage(LMDBInstance storage) {
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
            LOGGER.error("Couldn't parse statistics file for player {}", this.getUuid(), var5);
        } catch (Exception var4) {
            LOGGER.error("Couldn't read statistics file for player {}", this.getUuid(), var4);
        }
    }

    @Overwrite
    public void parseLocal(DataFixer dataFixer, String json) {
        try (JsonReader jsonReader = new JsonReader(new StringReader(json))) {
            jsonReader.setLenient(false);

            JsonElement jsonElement = Streams.parse(jsonReader);

            if (jsonElement.isJsonNull()) {
                LOGGER.error("Unable to parse Stat data from player {}", this.getUuid());
                return;
            }

            CompoundTag tag = fromJson(jsonElement.getAsJsonObject());

            if (!tag.contains("DataVersion", 99)) {
                tag.putInt("DataVersion", 1343);
            }

            tag = NbtUtils.update(dataFixer, DataFixTypes.STATS, tag, tag.getInt("DataVersion"));

            if (!tag.contains("stats", 10)) {
                return;
            }

            CompoundTag stats = tag.getCompound("stats");

            for (String string : stats.getAllKeys()) {
                if (!stats.contains(string, 10)) {
                    continue;
                }

                Util.ifElse(Registry.STAT_TYPE.getOptional(new ResourceLocation(string)), (statType) -> {
                    CompoundTag compoundTag2x = stats.getCompound(string);

                    for (String string2 : compoundTag2x.getAllKeys()) {
                        if (!compoundTag2x.contains(string2, 99)) {
                            LOGGER.warn("Invalid statistic value on player {}: Don't know what {} is for key {}", this.getUuid(), compoundTag2x.get(string2), string2);
                            continue;
                        }

                        Util.ifElse(this.getStat(statType, string2), (stat) -> {
                            this.stats.put(stat, compoundTag2x.getInt(string2));
                        }, () -> LOGGER.warn("Invalid statistic on player {}: Don't know what {} is", this.getUuid(), string2));
                    }
                }, () -> LOGGER.warn("Invalid statistic type on player {}: Don't know what {} is", this.getUuid(), string));
            }
        } catch (IOException | JsonParseException var21) {
            LOGGER.error("Unable to parse Stat data for player {}", this.getUuid(), var21);
        }
    }

    private UUID getUuid() {
        return UUID.fromString(this.file.getName().replace(".json", ""));
    }
}
