package de.yamayaki.cesium.converter.gui;

import de.yamayaki.cesium.converter.WorldConverter;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ConvertWorldScreen extends Screen {
    private final BooleanConsumer callback;
    private final WorldConverter converter;

    public ConvertWorldScreen(WorldConverter.Format format, Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelAccess, BooleanConsumer callback) {
        super(Component.literal("Converting world"));

        this.callback = callback;
        this.converter = new WorldConverter(format, minecraft, levelAccess);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.converter.cancel();
            this.callback.accept(false);
        }).bounds(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build());
    }

    @Override
    public void tick() {
        if (this.converter.isFinished()) {
            this.callback.accept(true);
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.converter.cancel();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);

        final int textColor = 16777215;
        final int textOffset = 14;

        final int offsetX = this.width / 2;
        final int startX = offsetX - 150;
        final int endX = offsetX + 150;

        guiGraphics.drawCenteredString(this.font, this.title, offsetX, 20, textColor);

        final Component[] drawables = new Component[]{
                Component.literal("Total dimensions: " + this.converter.getDimensions()),
                Component.literal("Current dimension: " + this.converter.getDimName(this.converter.getCurrentDim()) + " (" + this.converter.getCurrentDim() + ")"),
                Component.empty(),
                Component.literal("Total chunks: " + this.converter.getProgressTotal()),
                Component.literal("Current chunk: " + this.converter.getProgressCurrent())
        };

        for (int index = 0; index < drawables.length; index++) {
            guiGraphics.drawString(this.font, drawables[index], startX, 40 + ((index + 1) * textOffset), textColor);
        }

        final int statusOffset = 40 + ((drawables.length + 2) * textOffset);
        guiGraphics.drawCenteredString(this.font, this.converter.getStatus(), offsetX, statusOffset, textColor);

        final int progressOffset = statusOffset + textOffset;
        final int barEnd = (int) Math.floor(this.converter.getPercentage() * (endX - startX));

        guiGraphics.fill(startX - 1, progressOffset - 1, endX + 1, progressOffset + textOffset + 1, -16777216);
        guiGraphics.fill(startX, progressOffset, startX + barEnd, progressOffset + textOffset, -13408734);

        super.render(guiGraphics, i, j, f);
    }
}
