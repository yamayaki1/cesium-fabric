package de.yamayaki.cesium.mixin.feature.auto_save;

import de.yamayaki.cesium.CesiumMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    /* Vanilla imports */
    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow public abstract PlayerList getPlayerList();

    @Inject(
            method = "tickServer",
            at = @At("RETURN")
    )
    public void cesium$saveData(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        if (CesiumMod.config().experimental.forceSaveDataAfterTick) {
            this.cesium$autosaveData();
        }
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
}
