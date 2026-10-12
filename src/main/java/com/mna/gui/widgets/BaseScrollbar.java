/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 */
package com.mna.gui.widgets;

import com.mna.gui.GuiTextures;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class BaseScrollbar
extends AbstractWidget {
    static ResourceLocation SCROLLBAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("mna", "textures/gui/scrollbar.png");
    static int sizeY = 53;
    static int sizeX = 12;
    protected double value = 0.0;
    int totalHeight;

    protected BaseScrollbar(int xpos, int ypos, int scrollbarTotalHeight) {
        super(xpos, ypos, sizeX, scrollbarTotalHeight, (Component)Component.literal((String)""));
        this.totalHeight = scrollbarTotalHeight;
    }

    public int getScrollBarY() {
        int val = (int)((double)this.getY() + this.value * (double)this.totalHeight);
        return Mth.clamp((int)(val - sizeY / 2), (int)this.getY(), (int)(this.getY() + this.totalHeight - sizeY));
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int xadd = sizeX + (this.isHoveredOrFocused() ? 0 : 1) * sizeX;
        int yPos = this.getScrollBarY();
        pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, this.getX(), this.getY(), 0, 0, sizeX, this.totalHeight);
        pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, this.getX(), yPos, xadd, 0, sizeX, 53);
    }

    public void onClick(double x, double y) {
        this.setValueFromMouse(y);
    }

    private void setValueFromMouse(double y) {
        this.setValue((y - (double)this.getY()) / (double)this.totalHeight);
    }

    private void setValue(double y) {
        double val = this.value;
        this.value = Mth.clamp((double)y, (double)0.0, (double)1.0);
        if (val != this.value) {
            this.applyValue();
        }
    }

    public void setValueFromElement(int element, int max) {
        this.value = (float)element / (float)max;
    }

    protected void onDrag(double x, double y, double f3, double f4) {
        this.setValueFromMouse(y);
        super.onDrag(x, y, f3, f4);
    }

    public void onRelease(double x, double y) {
    }

    protected abstract void applyValue();
}

