package de.yamayaki.cesium.mixin.core;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.api.accessor.DatabaseSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow
    public abstract PlayerList getPlayerList();

    @Shadow
    private PlayerList playerList;

    @Shadow public abstract void stopServer();

    @Unique
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Cesium-Async-Save"));

    @Unique
    private CompletableFuture<Void> saveFuture = null;

    @Unique
    private volatile boolean didError = false;

    @Inject(method = "tickServer", at = @At("RETURN"))
    public void cesium$saveData(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        if(this.didError) {
            this.stopServer();
        }

        if (this.saveFuture != null && !this.saveFuture.isDone()) {
            return;
        }

        if (CesiumMod.config().saveAfterTick()) {
            this.cesium$autosaveData();
        }

        this.saveFuture = CompletableFuture.runAsync(() -> {
            ((DatabaseSource) this.playerList).cesium$getStorage().flushChanges();
            for (final ServerLevel level : this.levels.values()) {
                ((DatabaseSource) level).cesium$getStorage().flushChanges();
            }
        }, this.saveExecutor).exceptionally(throwable -> {
            LOGGER.error("Failed to sync all data!", throwable);
            this.didError = true;

            return null;
        });
    }

    @Unique
    private void cesium$autosaveData() {
        // Save player data
        this.getPlayerList().saveAll();

        // Save chunk data
        for (final ServerLevel serverLevel : this.levels.values()) {
            serverLevel.save(null, false, false);
        }
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    public void cesium$stopThread(CallbackInfo ci) {
        if (this.saveFuture != null && !this.didError) {
            this.saveFuture.join();
        }

        this.saveExecutor.shutdownNow();
    }
}
