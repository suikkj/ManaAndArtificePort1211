 
package com.mna.gui.item;

import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.item.ContainerAreaAdjust;
import net.minecraft.client.gui.GuiGraphics;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import org.joml.Vector3f;

public class GuiAreaAdjust
extends GuiJEIDisable<ContainerAreaAdjust> {
    public GuiAreaAdjust(ContainerAreaAdjust container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    protected void init() {
        super.init();
        int buttonStartX = this.width / 2 - 65;
        int buttonStartY = this.height - 75;
        int buttonPadding = 5;
        int buttonSpacing = 16;
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding, buttonStartY + buttonPadding, 8, 8, 18, 54, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(-1.0f, 0.0f, 0.0f), true)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing, buttonStartY + buttonPadding, 8, 8, 26, 54, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, -1.0f, 0.0f), true)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing + buttonSpacing, buttonStartY + buttonPadding, 8, 8, 34, 54, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, 0.0f, -1.0f), true)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding, buttonStartY + buttonPadding + buttonSpacing, 8, 8, 18, 70, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(-1.0f, 0.0f, 0.0f), false)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing, buttonStartY + buttonPadding + buttonSpacing, 8, 8, 26, 70, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, -1.0f, 0.0f), false)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing + buttonSpacing, buttonStartY + buttonPadding + buttonSpacing, 8, 8, 34, 70, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, 0.0f, -1.0f), false)));
        this.addRenderableWidget(new MNAImageButton((buttonStartX += 80) + buttonPadding, buttonStartY + buttonPadding, 8, 8, 18, 54, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(1.0f, 0.0f, 0.0f), true)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing, buttonStartY + buttonPadding, 8, 8, 26, 54, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, 1.0f, 0.0f), true)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing + buttonSpacing, buttonStartY + buttonPadding, 8, 8, 34, 54, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, 0.0f, 1.0f), true)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding, buttonStartY + buttonPadding + buttonSpacing, 8, 8, 18, 70, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(1.0f, 0.0f, 0.0f), false)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing, buttonStartY + buttonPadding + buttonSpacing, 8, 8, 26, 70, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, 1.0f, 0.0f), false)));
        this.addRenderableWidget(new MNAImageButton(buttonStartX + buttonPadding + buttonSpacing + buttonSpacing, buttonStartY + buttonPadding + buttonSpacing, 8, 8, 34, 70, 8, GuiTextures.Widgets.WIDGETS, 128, 128, b -> ((ContainerAreaAdjust)this.menu).Adjust(new Vector3f(0.0f, 0.0f, 1.0f), false)));
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        NeoForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered((Screen)this, pGuiGraphics));
        pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, this.width / 2 - 65, this.height - 83, 45.0f, 54.0f, 50, 41, 128, 128);
        pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, this.width / 2 - 62, this.height - 80, 53.0f, 96.0f, 7, 7, 128, 128);
        pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, this.width / 2 + 15, this.height - 83, 45.0f, 54.0f, 50, 41, 128, 128);
        pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, this.width / 2 + 18, this.height - 80, 45.0f, 96.0f, 7, 7, 128, 128);
        if (mouseY > this.height - 81 && mouseY < this.height - 73) {
            if (mouseX > this.width / 2 - 65 && mouseX < this.width / 2 - 53) {
                pGuiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"item.mna_rune_marking_pair.adjust_lower"), mouseX, mouseY);
            } else if (mouseX > this.width / 2 + 15 && mouseX < this.width / 2 + 26) {
                pGuiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"item.mna_rune_marking_pair.adjust_upper"), mouseX, mouseY);
            }
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
    }
}


