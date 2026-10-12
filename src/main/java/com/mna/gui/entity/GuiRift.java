/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package com.mna.gui.entity;

import com.mna.gui.GuiTextures;
import com.mna.gui.containers.entity.ContainerRift;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiRift
extends AbstractContainerScreen<ContainerRift> {
    public GuiRift(ContainerRift book, Inventory inv, Component comp) {
        super(book, inv, (Component)Component.literal((String)""));
        this.imageWidth = 176;
        this.imageHeight = 212;
    }

    public ResourceLocation texture() {
        return GuiTextures.Entities.RIFT;
    }

    public int rows() {
        return 8;
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}

