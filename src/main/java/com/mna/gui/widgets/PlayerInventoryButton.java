/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.ImageButton
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Tuple
 */
package com.mna.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

public class PlayerInventoryButton
extends MNAImageButton {
    private final AbstractContainerScreen<?> parentGui;
    private boolean isRecipeBookVisible = false;

    public PlayerInventoryButton(AbstractContainerScreen<?> parentGui, int xIn, int yIn, int widthIn, int heightIn, int textureOffsetX, int textureOffsetY, int yDiffText, ResourceLocation resource, Button.OnPress onPressed, Component text) {
        super(xIn, yIn, widthIn, heightIn, textureOffsetX, textureOffsetY, yDiffText, resource, 128, 128, onPressed, text);
        this.parentGui = parentGui;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.parentGui instanceof InventoryScreen) {
            boolean lastVisible = this.isRecipeBookVisible;
            this.isRecipeBookVisible = ((InventoryScreen)this.parentGui).getRecipeBookComponent().isVisible();
            if (lastVisible != this.isRecipeBookVisible) {
                Tuple<Integer, Integer> offsets = PlayerInventoryButton.getOffsets(false);
                this.setPosition(this.parentGui.getGuiLeft() + (Integer)offsets.getA(), this.parentGui.height / 2 + (Integer)offsets.getB());
            }
        } else if (this.parentGui instanceof CreativeModeInventoryScreen) {
            boolean isInventoryTab;
            CreativeModeInventoryScreen gui = (CreativeModeInventoryScreen)this.parentGui;
            this.active = isInventoryTab = gui.isInventoryOpen();
            if (!isInventoryTab) {
                return;
            }
        }
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public static Tuple<Integer, Integer> getOffsets(boolean isCreative) {
        return isCreative ? new Tuple(75, -50) : new Tuple(30, -60);
    }
}

