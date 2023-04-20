package me.jellysquid.mods.radon.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import me.jellysquid.mods.radon.common.db.DatabaseItem;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements implements DatabaseItem {
    @Shadow
    @Final
    private static Gson GSON;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private static TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN;

    @Shadow
    private ServerPlayer player;

    @Shadow
    @Final
    private DataFixer dataFixer;

    @Shadow
    @Final
    private Set<Advancement> progressChanged;

    @Shadow
    @Final
    private Map<Advancement, AdvancementProgress> progress;

    private LMDBInstance database;

    @Shadow
    protected abstract void startProgress(Advancement advancement, AdvancementProgress advancementProgress);

    @Shadow
    protected abstract void checkForAutomaticTriggers(ServerAdvancementManager serverAdvancementManager);

    @Shadow
    protected abstract void registerListeners(ServerAdvancementManager serverAdvancementManager);

    @Shadow
    protected abstract void markForVisibilityUpdate(Advancement advancement);

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;load(Lnet/minecraft/server/ServerAdvancementManager;)V"))
    private void redirectInitialLoad(PlayerAdvancements playerAdvancementTracker, ServerAdvancementManager advancementLoader) {

    }

    @Overwrite
    private void load(ServerAdvancementManager serverAdvancementManager) {
        String json = this.database
                .getDatabase(PlayerDatabaseSpecs.ADVANCEMENTS)
                .getValue(this.getUuid());

        if (json != null) {
            try {
                JsonReader jsonReader = new JsonReader(new StringReader(json));
                jsonReader.setLenient(false);

                Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(jsonReader));

                if (dynamic.get("DataVersion").asNumber().result().isEmpty()) {
                    dynamic = dynamic.set("DataVersion", dynamic.createInt(1343));
                }

                dynamic = DataFixTypes.ADVANCEMENTS.updateToCurrentVersion(this.dataFixer, dynamic, dynamic.get("DataVersion").asInt(1343));
                dynamic = dynamic.remove("DataVersion");

                Map<ResourceLocation, AdvancementProgress> map = GSON.getAdapter(TYPE_TOKEN).fromJsonTree(dynamic.getValue());

                if (map == null) {
                    throw new JsonParseException("Found null for advancements");
                }

                map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach((entry) -> {
                    Advancement advancement = serverAdvancementManager.getAdvancement(entry.getKey());
                    if (advancement == null) {
                        LOGGER.warn("Ignored advancement '{}' in progress file for player {} - it doesn't exist anymore?", entry.getKey(), this.getUuid());
                    } else {
                        this.startProgress(advancement, entry.getValue());
                        this.progressChanged.add(advancement);
                        this.markForVisibilityUpdate(advancement);
                    }
                });
            } catch (JsonParseException e) {
                LOGGER.error("Couldn't parse player advancements for player {}", this.getUuid(), e);
            } catch (Exception e) {
                LOGGER.error("Couldn't read player advancements for player {}", this.getUuid(), e);
            }
        }

        this.checkForAutomaticTriggers(serverAdvancementManager);
        this.registerListeners(serverAdvancementManager);
    }

    @Overwrite
    public void save() {
        Map<ResourceLocation, AdvancementProgress> map = new LinkedHashMap<>();

        for (Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            AdvancementProgress advancementProgress = entry.getValue();

            if (advancementProgress.hasProgress()) {
                map.put(entry.getKey().getId(), advancementProgress);
            }
        }

        JsonElement json = GSON.toJsonTree(map);
        json.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

        try (StringWriter writer = new StringWriter()) {
            GSON.toJson(json, writer);

            this.database
                    .getTransaction(PlayerDatabaseSpecs.ADVANCEMENTS)
                    .add(this.getUuid(), writer.toString());
        } catch (IOException var35) {
            LOGGER.error("Couldn't save player advancements for {}", this.getUuid());
        }
    }

    private UUID getUuid() {
        return this.player.getUUID();
    }

    @Override
    public LMDBInstance getStorage() {
        return this.database;
    }

    @Override
    public void setStorage(LMDBInstance storage) {
        this.database = storage;

        this.load(this.player.getLevel().getServer().getAdvancements());
    }
}
