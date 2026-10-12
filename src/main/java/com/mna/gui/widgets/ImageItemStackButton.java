/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.ImageButton
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.SoundManager
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 */
package com.mna.gui.widgets;

import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ImageItemStackButton
extends MNAImageButton {
    final ItemStack iconStack;
    final boolean includeTooltip;
    final Consumer<List<Component>> toolTipSetter;
    int stackXOffset = 3;
    int stackYOffset = 1;
    boolean renderBackground = true;

    public ImageItemStackButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int hoverOffset, ResourceLocation textureFile, int texWidth, int texHeight, Button.OnPress clickHandler, Consumer<List<Component>> toolTipSetter, ItemStack displayStack) {
        this(x, y, width, height, xTexStart, yTexStart, hoverOffset, textureFile, texWidth, texHeight, clickHandler, toolTipSetter, displayStack, true);
    }

    public ImageItemStackButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int hoverOffset, ResourceLocation textureFile, int texWidth, int texHeight, Button.OnPress clickHandler, Consumer<List<Component>> toolTipSetter, ItemStack displayStack, boolean displayFullTooltip) {
        super(x, y, width, height, xTexStart, yTexStart, hoverOffset, textureFile, texWidth, texHeight, clickHandler, displayStack.getHoverName());
        this.iconStack = displayStack;
        this.includeTooltip = displayFullTooltip;
        this.toolTipSetter = toolTipSetter;
        this.stackXOffset = width == 18 ? 1 : 3;
        this.stackYOffset = width == 18 ? 3 : 1;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.alpha);
        if (this.renderBackground) {
            super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
        pGuiGraphics.renderItem(this.iconStack, this.getX() + this.stackXOffset, this.getY() + this.stackYOffset);
        if (this.active && this.isHovered) {
            List lines;
            if (this.includeTooltip) {
                lines = this.iconStack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, ManaAndArtifice.instance.proxy.getClientPlayer(), (TooltipFlag)TooltipFlag.Default.NORMAL);
            } else {
                lines = new ArrayList<Component>();
                lines.add(this.iconStack.getHoverName());
            }
            ArrayList<Object> tt = new ArrayList<Object>();
            for (Component comp : lines) {
                if (!this.includeTooltip) {
                    String unformatted = ChatFormatting.stripFormatting((String)comp.getString());
                    MutableComponent stc = Component.literal((String)unformatted);
                    stc.withStyle(ChatFormatting.ITALIC);
                    tt.add(stc);
                    continue;
                }
                tt.add(comp);
            }
            if (this.toolTipSetter != null) {
                this.toolTipSetter.accept(tt);
            }
        }
    }

    public void playDownSound(SoundManager soundHandler) {
        soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
    }

    public ImageItemStackButton setRenderBackground(boolean render) {
        this.renderBackground = render;
        return this;
    }

    public ImageItemStackButton setAffinityOffset(int x, int y) {
        this.stackXOffset = x;
        this.stackYOffset = y;
        return this;
    }
}

