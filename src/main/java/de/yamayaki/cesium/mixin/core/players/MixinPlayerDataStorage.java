package de.yamayaki.cesium.mixin.core.players;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.common.db.DatabaseItem;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage implements DatabaseItem {
    @Shadow
    @Final
    private static Logger LOGGER;

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

    @Overwrite
    public void save(Player playerEntity) {
        try {
            this.database
                    .getTransaction(PlayerDatabaseSpecs.PLAYER_DATA)
                    .add(playerEntity.getUUID(), playerEntity.saveWithoutId(new CompoundTag()));
        } catch (Exception e) {
            LOGGER.warn("Failed to save player data for {}", playerEntity.getName().getString());
        }
    }
}
