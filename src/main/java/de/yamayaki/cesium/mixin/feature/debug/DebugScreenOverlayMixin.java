package de.yamayaki.cesium.mixin.feature.debug;

import com.llamalad7.mixinextras.sugar.Local;
import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.api.accessor.IWorldStorageGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(
            method = "getSystemInformation",
            at = @At("RETURN")
    )
    private void cesium$addDebugInfo(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        if (!CesiumMod.config().general.showDebugInformation) {
            return;
        }

        final MinecraftServer minecraftServer = Minecraft.getInstance().getSingleplayerServer();
        if (minecraftServer == null || Minecraft.getInstance().level == null) {
            return;
        }

        ((IWorldStorageGetter) minecraftServer).cesium$worldStorage().addDebugInformation(list);
    }
}
