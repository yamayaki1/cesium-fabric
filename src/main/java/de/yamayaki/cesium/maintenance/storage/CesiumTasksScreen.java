package de.yamayaki.cesium.maintenance.storage;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.maintenance.CesiumWorkScreen;
import de.yamayaki.cesium.maintenance.tasks.ICesiumTask;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Unique;

public class CesiumTasksScreen extends Screen {
    private final LinearLayout layout = LinearLayout.vertical().spacing(5);

    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final BooleanConsumer callback;

    public CesiumTasksScreen(final LevelStorageSource.LevelStorageAccess levelAccess, final BooleanConsumer callback) {
        super(Component.literal("Cesium Maintenance"));

        this.levelAccess = levelAccess;
        this.callback = callback;

        this.layout.addChild(Button.builder(Component.literal("Anvil → Cesium"), buttonx -> {
            this.minecraft.setScreen(new CesiumWorkScreen(ICesiumTask.CesiumTask.TO_CESIUM, this.levelAccess, this.createRegistry(), this.callback));
        }).width(200).build());

        this.layout.addChild(Button.builder(Component.literal("Cesium → Anvil"), buttonx -> {
            this.minecraft.setScreen(new CesiumWorkScreen(ICesiumTask.CesiumTask.TO_ANVIL, this.levelAccess, this.createRegistry(), this.callback));
        }).width(200).build());

        this.layout.addChild(Button.builder(Component.literal("Compact Database"), buttonx -> {
            this.minecraft.setScreen(new CesiumWorkScreen(ICesiumTask.CesiumTask.COMPACT, this.levelAccess, this.createRegistry(), this.callback));
        }).width(200).build());

        this.layout.addChild(new SpacerElement(200, 20));

        this.layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, (buttonx) -> {
            this.onClose();
        }).width(200).build());

        this.layout.visitWidgets(this::addRenderableWidget);
    }

    /*
     * See vanilla code net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen.create(...);
     */
    @Unique
    private RegistryAccess.Frozen createRegistry() {
        try {
            assert this.minecraft != null;
            final WorldOpenFlows worldOpenFlows = this.minecraft.createWorldOpenFlows();
            final PackRepository packRepository = ServerPacksSource.createPackRepository(this.levelAccess);

            try (final WorldStem worldStem = worldOpenFlows.loadWorldStem(levelAccess.getDataTag(), false, packRepository)) {
                final WorldData worldData = worldStem.worldData();
                final RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();

                this.levelAccess.saveDataTag(frozen, worldData);

                return frozen;
            }
        } catch (Throwable throwable) {
            CesiumMod.logger().warn("Failed to load datapacks, can't convert world", throwable);
        }

        return null;
    }

    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
    }

    @Override
    protected void init() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    public void onClose() {
        this.callback.accept(false);
    }
}
