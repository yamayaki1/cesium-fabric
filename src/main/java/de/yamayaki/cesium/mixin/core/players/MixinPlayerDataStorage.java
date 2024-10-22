package de.yamayaki.cesium.mixin.core.players;

import com.llamalad7.mixinextras.sugar.Local;
import de.yamayaki.cesium.api.accessor.DatabaseSetter;
import de.yamayaki.cesium.api.database.IDBInstance;
import de.yamayaki.cesium.common.spec.PlayerDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage implements DatabaseSetter {
    @Unique
    private IDBInstance database;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;mkdirs()Z"
            )
    )
    private boolean disableMkdirs(File file) {
        return true;
    }

    @Override
    public void cesium$setStorage(IDBInstance storage) {
        this.database = storage;
    }

    @Redirect(
            method = "load(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;exists()Z"
            )
    )
    public boolean redirectFileExists(File instance) {
        return true;
    }

    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;isFile()Z"
            )
    )
    public boolean redirectFileIsFile(File instance) {
        return true;
    }

    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtIo;readCompressed(Ljava/io/File;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    public CompoundTag redirectPlayerLoad(File file, @Local(argsOnly = true) Player player) {
        return this.database
                .getDatabase(PlayerDatabaseSpecs.PLAYER_DATA)
                .getValue(player.getUUID());
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;createTempFile(Ljava/lang/String;Ljava/lang/String;Ljava/io/File;)Ljava/io/File;"
            )
    )
    public File disableFileCreation(String se, String prefix, File suffix) {
        return null;
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/io/File;)V"
            )
    )
    public void redirectWrite(CompoundTag compoundTag, File file, @Local(argsOnly = true) Player player) {
        this.database
                .getTransaction(PlayerDatabaseSpecs.PLAYER_DATA)
                .add(player.getUUID(), compoundTag);
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/io/File;Ljava/io/File;Ljava/io/File;)V"
            )
    )
    public void disableFileMove(File file, File file2, File file3) {
        // Do nothing
    }
}
