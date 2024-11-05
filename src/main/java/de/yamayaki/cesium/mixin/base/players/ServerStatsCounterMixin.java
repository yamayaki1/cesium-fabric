package de.yamayaki.cesium.mixin.base.players;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IComponentStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.UUID;

@Mixin(ServerStatsCounter.class)
public abstract class ServerStatsCounterMixin extends StatsCounter implements IStorageSetter<UUID, String> {
    /* Vanilla imports */
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private File file;
    @Shadow @Final private MinecraftServer server;
    @Shadow public abstract void parseLocal(DataFixer dataFixer, String string);

    /* Our own storage */
    @Unique private IComponentStorage<UUID, String> storage;

    @Unique
    private UUID getUuid() {
        return UUID.fromString(this.file.getName().replace(".json", ""));
    }

    @Override
    public void cesium$setStorage(final @NotNull IComponentStorage<UUID, String> storage) {
        this.storage = storage;

        final String json = this.storage.getValue(this.getUuid());

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

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;isFile()Z"
            )
    )
    public boolean killInitialLoad(File file) {
        return false;
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/commons/io/FileUtils;writeStringToFile(Ljava/io/File;Ljava/lang/String;)V",
                    remap = false
            )
    )
    public void redirectWrite(File file, String data) {
        this.storage.putValue(this.getUuid(), data);
    }
}
