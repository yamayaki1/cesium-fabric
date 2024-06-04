package de.yamayaki.cesium.mixin.gui;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.maintenance.CesiumWorkScreen;
import de.yamayaki.cesium.maintenance.tasks.ICesiumTask;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Shadow
    @Final
    private LinearLayout layout;

    protected MixinEditWorldScreen(Component component) {
        super(component);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 9))
    public void reInit(CallbackInfo ci) {
        LinearLayout linearLayout = LinearLayout.horizontal();

        linearLayout.addChild(Button.builder(Component.literal("Anvil → Cesium"), buttonx -> {
            this.minecraft.setScreen(new CesiumWorkScreen(ICesiumTask.CesiumTask.TO_CESIUM, this.levelAccess, this.createRegistry(this.minecraft, this.levelAccess), this.callback));
        }).width(200 / 3).build());

        linearLayout.addChild(Button.builder(Component.literal("Cesium → Anvil"), buttonx -> {
            this.minecraft.setScreen(new CesiumWorkScreen(ICesiumTask.CesiumTask.TO_ANVIL, this.levelAccess, this.createRegistry(this.minecraft, this.levelAccess), this.callback));
        }).width(200 / 3).build());

        linearLayout.addChild(Button.builder(Component.literal("Compact DB"), buttonx -> {
            this.minecraft.setScreen(new CesiumWorkScreen(ICesiumTask.CesiumTask.COMPACT, this.levelAccess, this.createRegistry(this.minecraft, this.levelAccess), this.callback));
        }).width(200 / 3).build());

        this.layout.addChild(linearLayout);
    }

    /*
     * See vanilla code net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen.create(...);
     */
    @Unique
    private RegistryAccess.Frozen createRegistry(final Minecraft minecraft, final LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        try {
            final WorldOpenFlows worldOpenFlows = minecraft.createWorldOpenFlows();
            final PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);

            try (final WorldStem worldStem = worldOpenFlows.loadWorldStem(levelAccess.getDataTag(), false, packRepository)) {
                final WorldData worldData = worldStem.worldData();
                final RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();

                levelStorageAccess.saveDataTag(frozen, worldData);

                return frozen;
            }
        } catch (Throwable throwable) {
            CesiumMod.logger().warn("Failed to load datapacks, can't convert world", throwable);
        }

        return null;
    }
}
