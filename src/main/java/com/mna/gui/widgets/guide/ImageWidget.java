/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.gui.widgets.guide;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageWidget
extends AbstractWidget {
    private final ResourceLocation imageTextureLocation;
    private final Consumer<List<Component>> tooltipFunction;
    private final Component caption;
    private final List<Component> tooltip;

    public ImageWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation imageTextureLocation, List<Component> tooltip, Consumer<List<Component>> tooltipFunction) {
        this(pX, pY, pWidth, pHeight, imageTextureLocation, null, tooltip, tooltipFunction);
    }

    public ImageWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation imageTextureLocation, Component caption, List<Component> tooltip, Consumer<List<Component>> tooltipFunction) {
        super(pX, pY, pWidth, pHeight, (Component)Component.literal((String)""));
        this.imageTextureLocation = imageTextureLocation;
        this.caption = caption;
        this.tooltip = tooltip;
        this.tooltipFunction = tooltipFunction;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.blit(this.imageTextureLocation, this.getX(), this.getY(), 0.0f, 0.0f, this.width, this.height, this.width, this.height);
        if (this.caption != null) {
            Minecraft mc = Minecraft.getInstance();
            int n = this.getX();
            int n2 = this.getY() + this.height;
            Objects.requireNonNull(mc.font);
            int n3 = n2 - 9;
            int n4 = this.getX() + this.width;
            Objects.requireNonNull(mc.font);
            pGuiGraphics.fillGradient(n, n3, n4, 9, 0xAA0000, 0xAA0000);
            Font font = mc.font;
            int n5 = this.getX();
            int n6 = this.getY() + this.height;
            Objects.requireNonNull(mc.font);
            pGuiGraphics.drawString(font, this.caption, n5, n6 - 9, 0xFFFFFF, false);
        }
        if (this.isHovered && this.tooltipFunction != null) {
            this.tooltipFunction.accept(this.tooltip);
        }
    }

    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}

