package de.yamayaki.cesium.mixin.base.players;

import com.llamalad7.mixinextras.sugar.Local;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.api.accessor.IWorldStorageGetter;
import de.yamayaki.cesium.storage.IWorldStorage;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    /* Vanilla imports */
    @Shadow @Final private PlayerDataStorage playerIo;

    /* Our world storage */
    @Unique private IWorldStorage worldStorage;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    @SuppressWarnings("unchecked")
    private void initCesiumPlayers(MinecraftServer minecraftServer, LayeredRegistryAccess<?> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
        this.worldStorage = ((IWorldStorageGetter) minecraftServer).cesium$worldStorage();

        ((IStorageSetter<UUID, CompoundTag>) this.playerIo).cesium$setStorage(this.worldStorage.playerStorage());
    }

    @Inject(
            method = "getPlayerAdvancements",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    @SuppressWarnings("unchecked")
    private void setAdvancementsStorage(ServerPlayer serverPlayer, CallbackInfoReturnable<PlayerAdvancements> cir, @Local PlayerAdvancements playerAdvancements) {
        ((IStorageSetter<UUID, String>) playerAdvancements).cesium$setStorage(this.worldStorage.advancementStorage());
    }

    @Inject(
            method = "getPlayerStats",
            at = @At(
                    value = "RETURN",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    @SuppressWarnings("unchecked")
    private void setStatsStorage(Player player, CallbackInfoReturnable<ServerStatsCounter> cir, @Local ServerStatsCounter serverStatsCounter) {
        ((IStorageSetter<UUID, String>) serverStatsCounter).cesium$setStorage(this.worldStorage.statStorage());
    }
}
