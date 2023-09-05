package de.yamayaki.cesium.mixin.core.players;

import de.yamayaki.cesium.common.PlayerDatabaseAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Shadow
    private PlayerList playerList;

    @Inject(method = "tickServer", at = @At(value = "RETURN"))
    private void postTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ((PlayerDatabaseAccess) this.playerList)
                .cesium$getDatabase()
                .flushChanges();
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;removeAll()V"))
    private void postSaveAllPlayerList(CallbackInfo ci) {
        ((PlayerDatabaseAccess) this.playerList)
                .cesium$getDatabase()
                .close();
    }
}
