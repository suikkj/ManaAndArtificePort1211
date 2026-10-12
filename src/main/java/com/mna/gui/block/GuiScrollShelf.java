 
package com.mna.gui.block;

import com.mna.gui.GuiTextures;
import com.mna.gui.containers.block.ContainerScrollShelf;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiScrollShelf
extends AbstractContainerScreen<ContainerScrollShelf> {
    public GuiScrollShelf(ContainerScrollShelf pMenu, Inventory pPlayerInventory, Component pTitle) {
        super((AbstractContainerMenu)pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 256;
        this.imageHeight = 226;
    }

    public ResourceLocation texture() {
        return GuiTextures.Blocks.SCROLLSHELF;
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }
}

