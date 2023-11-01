package de.yamayaki.cesium.mixin.core.players;

import de.yamayaki.cesium.accessor.DatabaseSetter;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

@Debug(export = true)
@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage implements DatabaseSetter {
    @Unique
    private LMDBInstance database;

    @Unique
    private Player player;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;mkdirs()Z"))
    private boolean disableMkdirs(File file) {
        return true;
    }

    @Override
    public void cesium$setStorage(LMDBInstance storage) {
        this.database = storage;
    }

    @Inject(method = "load", at = @At("HEAD"))
    public void setPlayer(Player player, CallbackInfoReturnable<CompoundTag> cir) {
        this.player = player;
    }

    @ModifyVariable(method = "load", at = @At(value = "STORE"))
    private CompoundTag loadNbt(CompoundTag compoundTag) {
        return this.database
                .getDatabase(PlayerDatabaseSpecs.PLAYER_DATA)
                .getValue(this.player.getUUID());
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;createTempFile(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/String;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;"))
    public Path disableFileCreation(Path path, String a, String b, FileAttribute[] fileAttributes) {
        return null;
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"))
    public void redirectWrite(CompoundTag compoundTag, Path path) {
        this.database
                .getTransaction(PlayerDatabaseSpecs.PLAYER_DATA)
                .add(compoundTag.getUUID("UUID"), compoundTag);
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V"))
    public void disableFileMove(Path path, Path path2, Path path3) {
        // Do nothing
    }
}
