package de.yamayaki.cesium.mixin.core;

import de.yamayaki.cesium.accessor.DatabaseSource;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Shadow
    private PlayerList playerList;

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    private CompletableFuture<Void> saveFuture = null;

    @Inject(method = "tickServer", at = @At("RETURN"))
    public void cesium$saveData(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        if(this.saveFuture != null) {
            this.saveFuture.join();
        }

        this.saveFuture = CompletableFuture.runAsync(() -> {
            ((DatabaseSource) this.playerList).cesium$getStorage().flushChanges();
            for (final ServerLevel level : this.levels.values()) {
                ((DatabaseSource) level).cesium$getStorage().flushChanges();
            }
        }, Util.backgroundExecutor());
    }
}
