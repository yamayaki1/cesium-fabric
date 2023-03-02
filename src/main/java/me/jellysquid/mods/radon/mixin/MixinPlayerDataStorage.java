package me.jellysquid.mods.radon.mixin;

import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.db.DatabaseItem;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage implements DatabaseItem {
    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final protected DataFixer fixerUpper;

    private LMDBInstance database;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;mkdirs()Z"))
    private boolean disableMkdirs(File file) {
        return true;
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

    @Overwrite
    @Nullable
    public CompoundTag load(Player playerEntity) {
        CompoundTag compoundTag = null;

        try {
            compoundTag = this.database
                    .getDatabase(PlayerDatabaseSpecs.PLAYER_DATA)
                    .getValue(playerEntity.getUUID());
        } catch (Exception e) {
            LOGGER.warn("Failed to load player data for {}", playerEntity.getName().getString(), e);
        }

        if (compoundTag != null) {
            int i = compoundTag.contains("DataVersion", 3) ? compoundTag.getInt("DataVersion") : -1;
            playerEntity.load(NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, compoundTag, i));
        }

        return compoundTag;
    }


    @Override
    public void setStorage(LMDBInstance storage) {
        this.database = storage;
    }

    @Override
    public LMDBInstance getStorage() {
        return this.database;
    }
}
