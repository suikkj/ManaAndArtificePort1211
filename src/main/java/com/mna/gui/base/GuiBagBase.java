 
package com.mna.gui.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class GuiBagBase<T extends AbstractContainerMenu>
extends AbstractContainerScreen<T> {
    public static final int bagXSize = 176;
    public static final int bagYSize = 207;

    public GuiBagBase(Inventory inv, T inventorySlotsIn) {
        super(inventorySlotsIn, inv, (Component)Component.literal((String)""));
        this.imageWidth = 176;
        this.imageHeight = 207;
    }

    public GuiBagBase(Inventory inv, T inventorySlotsIn, int sizex, int sizey) {
        super(inventorySlotsIn, inv, (Component)Component.literal((String)""));
        this.imageWidth = sizex;
        this.imageHeight = sizey;
    }

    public abstract ResourceLocation texture();

    public abstract String name();

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
    }
}

