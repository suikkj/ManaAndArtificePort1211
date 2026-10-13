 
package com.mna.gui.item;

import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiBagBase;
import com.mna.gui.containers.item.ContainerRunicMalus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiRunicMalus
extends GuiBagBase<ContainerRunicMalus> {
    public GuiRunicMalus(ContainerRunicMalus inventorySlotsIn, Inventory inv, Component comp) {
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
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        int i = this.leftPos + this.imageWidth / 2 - 53;
        int j = this.topPos;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, 106, 114);
        i = this.leftPos;
        j = this.topPos + 119;
        pGuiGraphics.blit(GuiTextures.Widgets.STANDALONE_INVENTORY_TEXTURE, i, j, 0.0f, 0.0f, 176, 90, 176, 90);
    }

    @Override
    public String name() {
        return "Runic Malus";
    }
}

