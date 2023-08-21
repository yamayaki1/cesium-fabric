package de.yamayaki.cesium.mixin.core.players;

import de.yamayaki.cesium.common.db.DatabaseItem;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage implements DatabaseItem {
    @Unique
    private LMDBInstance database;

    @Unique
    private Player player;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;mkdirs()Z"))
    private boolean disableMkdirs(File file) {
        return true;
    }

    @Override
    public LMDBInstance cesium$getStorage() {
        return this.database;
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
        return this.cesium$getStorage()
                .getDatabase(PlayerDatabaseSpecs.PLAYER_DATA)
                .getValue(this.player.getUUID());
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Ljava/io/File;createTempFile(Ljava/lang/String;Ljava/lang/String;Ljava/io/File;)Ljava/io/File;"))
    public File disableFileCreation(String se, String prefix, File suffix) {
        return null;
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/io/File;)V"))
    public void redirectWrite(CompoundTag compoundTag, File file) {
        this.database
                .getTransaction(PlayerDatabaseSpecs.PLAYER_DATA)
                .add(compoundTag.getUUID("UUID"), compoundTag);
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/io/File;Ljava/io/File;Ljava/io/File;)V"))
    public void disableFileMove(File file, File file2, File file3) {
        // Do nothing
    }
}
