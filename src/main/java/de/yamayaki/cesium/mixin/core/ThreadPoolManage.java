package de.yamayaki.cesium.mixin.core;

import de.yamayaki.cesium.CesiumMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ThreadPoolManage {
    @Inject(method = "stopServer", at = @At(value = "RETURN"))
    public void stopPool(CallbackInfo ci) {
        CesiumMod.resetPool();
    }
}
