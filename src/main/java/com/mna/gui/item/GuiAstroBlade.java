 
package com.mna.gui.item;

import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiBagBase;
import com.mna.gui.containers.item.ContainerAstroBlade;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiAstroBlade
extends GuiBagBase<ContainerAstroBlade> {
    public GuiAstroBlade(ContainerAstroBlade inventorySlotsIn, Inventory inv, Component comp) {
        super(inv, inventorySlotsIn);
        this.imageWidth = 176;
        this.imageHeight = 209;
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.Items.RUNIC_MALUS;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = this.leftPos + this.imageWidth / 2 - 51;
        int j = this.topPos;
        pGuiGraphics.blit(this.texture(), i, j, 154, 0, 102, 114);
        i = this.leftPos;
        j = this.topPos + 119;
        pGuiGraphics.blit(GuiTextures.Widgets.STANDALONE_INVENTORY_TEXTURE, i, j, 0.0f, 0.0f, 176, 90, 176, 90);
    }

    @Override
    public String name() {
        return "Astro Blade";
    }
}

