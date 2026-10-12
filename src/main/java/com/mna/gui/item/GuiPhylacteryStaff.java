/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 */
package com.mna.gui.item;

import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiBagBase;
import com.mna.gui.containers.item.ContainerPhylacteryStaff;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiPhylacteryStaff
extends GuiBagBase<ContainerPhylacteryStaff> {
    public GuiPhylacteryStaff(ContainerPhylacteryStaff book, Inventory inv, Component comp) {
        super(inv, book);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.Items.STAFF_OF_CALLING;
    }

    @Override
    public String name() {
        return "Staff of Calling";
    }
}

