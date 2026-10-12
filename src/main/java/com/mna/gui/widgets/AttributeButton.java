/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.ImageButton
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.gui.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class AttributeButton
extends MNAImageButton {
    private List<MutableComponent> tooltips;
    private final Consumer<Component> addTooltipLineCallback;

    public AttributeButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int p_i51135_9_, int p_i51135_10_, Button.OnPress onPressIn, String[] tooltips, Consumer<Component> addTooltipLineCallback) {
        super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, p_i51135_9_, p_i51135_10_, onPressIn);
        this.tooltips = Arrays.asList(tooltips).stream().map(s -> Component.translatable((String)s)).collect(Collectors.toList());
        this.addTooltipLineCallback = addTooltipLineCallback;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.isHoveredOrFocused() && this.addTooltipLineCallback != null) {
            if (this.tooltips.size() > 0) {
                this.addTooltipLineCallback.accept((Component)this.tooltips.get(0).withStyle(ChatFormatting.AQUA));
            }
            if (this.tooltips.size() > 1) {
                this.tooltips.stream().skip(1L).forEach(tooltip -> this.addTooltipLineCallback.accept((Component)tooltip.withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}

