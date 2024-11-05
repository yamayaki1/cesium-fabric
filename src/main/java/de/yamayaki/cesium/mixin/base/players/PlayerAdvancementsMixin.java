package de.yamayaki.cesium.mixin.base.players;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IComponentStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.UUID;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin implements IStorageSetter<UUID, String> {
    /* Vanilla imports */
    @Shadow private ServerPlayer player;
    @Shadow protected abstract void load(ServerAdvancementManager serverAdvancementManager);

    /* Our own storage */
    @Unique private IComponentStorage<UUID, String> storage;

    @Override
    public void cesium$setStorage(final @NotNull IComponentStorage<UUID, String> storage) {
        this.storage = storage;

        final MinecraftServer server = this.player.getServer();

        if (server != null) {
            this.load(server.getAdvancements());
        }
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerAdvancements;load(Lnet/minecraft/server/ServerAdvancementManager;)V"
            )
    )
    private void killInitialLoad(PlayerAdvancements playerAdvancementTracker, ServerAdvancementManager advancementLoader) {
        // Do not load advancements before storage is set
    }

    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Files;isRegularFile(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z"
            )
    )
    private boolean redirectFileExists(Path provider, LinkOption[] ioe) {
        return this.storage.getValue(this.player.getUUID()) != null;
    }

    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Files;newBufferedReader(Ljava/nio/file/Path;Ljava/nio/charset/Charset;)Ljava/io/BufferedReader;"
            )
    )
    private BufferedReader redirectBufferedReaderInstantiation(Path path, Charset cs) {
        final String advancements = this.storage.getValue(this.player.getUUID());

        if (advancements == null) {
            throw new RuntimeException("This should normally never be null. Existence is checked just right before this method is invoked.");
        }

        return new BufferedReader(new StringReader(advancements));
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/FileUtil;createDirectoriesSafe(Ljava/nio/file/Path;)V"))
    private void disableFileCreation(Path path) {
        // Do nothing
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Files;newBufferedWriter(Ljava/nio/file/Path;Ljava/nio/charset/Charset;[Ljava/nio/file/OpenOption;)Ljava/io/BufferedWriter;"
            )
    )
    private BufferedWriter replaceFileBufferedWriter(Path path, Charset cs, OpenOption[] options, @Share("stringWriter") LocalRef<StringWriter> localWriter) {
        final StringWriter stringWriter = new StringWriter();
        localWriter.set(stringWriter);

        return new BufferedWriter(stringWriter);
    }

    @Inject(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/gson/Gson;toJson(Lcom/google/gson/JsonElement;Lcom/google/gson/stream/JsonWriter;)V",
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    private void flushAndSaveStringWriterContent(CallbackInfo ci, @Local Writer writer, @Share("stringWriter") LocalRef<StringWriter> localWriter) {
        try {
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write advancements to buffer");
        }

        this.storage.putValue(this.player.getUUID(), localWriter.get().toString());
    }
}