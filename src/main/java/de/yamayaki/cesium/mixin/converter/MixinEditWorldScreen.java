package de.yamayaki.cesium.mixin.converter;

import de.yamayaki.cesium.converter.WorldConverter;
import de.yamayaki.cesium.converter.gui.ConvertWorldScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public abstract class MixinEditWorldScreen extends Screen {
    @Shadow
    @Final
    private LevelStorageSource.LevelStorageAccess levelAccess;

    @Shadow
    @Final
    private BooleanConsumer callback;

    protected MixinEditWorldScreen(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void reInit(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("Anvil → Cesium"), buttonx -> {
            this.minecraft.setScreen(new ConvertWorldScreen(WorldConverter.Format.TO_CESIUM, this.minecraft, this.levelAccess, this.callback));
        }).bounds(this.width / 2 - 100, this.height / 4 + 168 + 5, 98, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cesium → Anvil"), buttonx -> {
            this.minecraft.setScreen(new ConvertWorldScreen(WorldConverter.Format.TO_ANVIL, this.minecraft, this.levelAccess, this.callback));
        }).bounds(this.width / 2 + 2, this.height / 4 + 168 + 5, 98, 20).build());
    }
}
