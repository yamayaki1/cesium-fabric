package de.yamayaki.cesium.converter.gui;

import de.yamayaki.cesium.converter.WorldConverter;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ConvertWorldScreen extends Screen {
    private final BooleanConsumer callback;
    private final WorldConverter converter;

    public ConvertWorldScreen(WorldConverter.Format format, LevelStorageSource.LevelStorageAccess levelAccess, RegistryAccess registryAccess, BooleanConsumer callback) {
        super(Component.literal("Converting world"));

        this.callback = callback;
        this.converter = new WorldConverter(format, levelAccess, registryAccess);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.converter.cancelTask();
            this.callback.accept(false);
        }).bounds(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build());
    }

    @Override
    public void tick() {
        if (!this.converter.running()) {
            this.callback.accept(true);
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.converter.cancelTask();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);

        final int textColor = 16777215;
        final int textOffset = 14;

        final int offsetX = this.width / 2;
        final int startX = offsetX - 150;
        final int endX = offsetX + 150;

        guiGraphics.drawCenteredString(this.font, this.title, offsetX, 20, textColor);

        final Component[] drawables = new Component[]{
                Component.literal(String.format("Level: %s", this.converter.levelName())),
                Component.literal(String.format("Chunk: %s / %s", this.converter.currentElement(), this.converter.totalElements()))
        };

        for (int index = 0; index < drawables.length; index++) {
            guiGraphics.drawString(this.font, drawables[index], startX, 40 + ((index + 1) * textOffset), textColor);
        }

        final int statusOffset = 40 + ((drawables.length + 2) * textOffset);
        guiGraphics.drawCenteredString(this.font, this.converter.status(), offsetX, statusOffset, textColor);

        final int progressOffset = statusOffset + textOffset;
        final int barEnd = (int) Math.floor(this.converter.percentage() * (endX - startX));

        guiGraphics.fill(startX - 1, progressOffset - 1, endX + 1, progressOffset + textOffset + 1, -16777216);
        guiGraphics.fill(startX, progressOffset, startX + barEnd, progressOffset + textOffset, -13408734);
    }
}
