package de.yamayaki.cesium.mixin.core.players;

import de.yamayaki.cesium.common.PlayerDatabaseAccess;
import de.yamayaki.cesium.common.db.DatabaseItem;
import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(PlayerList.class)
public class MixinPlayerList implements PlayerDatabaseAccess {
    @Shadow
    @Final
    private PlayerDataStorage playerIo;

    private LMDBInstance database;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initCesiumPlayers(MinecraftServer minecraftServer, LayeredRegistryAccess<?> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int i, CallbackInfo ci) {
        File dir = minecraftServer.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).getParent().toFile();

        this.database = new LMDBInstance(dir, "players", new DatabaseSpec[]{
                PlayerDatabaseSpecs.PLAYER_DATA,
                PlayerDatabaseSpecs.ADVANCEMENTS,
                PlayerDatabaseSpecs.STATISTICS
        });

        ((DatabaseItem) this.playerIo)
                .cesium$setStorage(this.database);
    }

    @Inject(method = "getPlayerAdvancements", at = @At("RETURN"))
    private void postGetAdvancementTracker(ServerPlayer player, CallbackInfoReturnable<PlayerAdvancements> cir) {
        DatabaseItem item = (DatabaseItem) cir.getReturnValue();

        if (item.cesium$getStorage() == null) {
            item.cesium$setStorage(this.database);
        }
    }

    @Inject(method = "getPlayerStats", at = @At("RETURN"))
    private void postCreateStatHandler(Player player, CallbackInfoReturnable<ServerStatsCounter> cir) {
        DatabaseItem item = (DatabaseItem) cir.getReturnValue();

        if (item.cesium$getStorage() == null) {
            item.cesium$setStorage(this.database);
        }
    }

    @Override
    public LMDBInstance getDatabase() {
        return this.database;
    }
}
