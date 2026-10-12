/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.ImageButton
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.widgets.lodestar;

import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.gui.GuiTextures;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.mna.gui.widgets.MNAImageButton;

public abstract class LodestarParameter<T>
extends MNAImageButton {
    public static final int WIDTH = 56;
    public static final int HEIGHT = 22;
    public static final int U = 200;
    protected T value;
    protected Minecraft mc;

    public LodestarParameter(boolean lowTier, int x, int y, int v, T defaultValue, Button.OnPress pressHandler, Component tooltip) {
        this(lowTier, x, y, 22, v, defaultValue, pressHandler, tooltip);
    }

    public LodestarParameter(boolean lowTier, int x, int y, int height, int v, T defaultValue, Button.OnPress pressHandler, Component tooltip) {
        super(x, y, 56, 22, 200, v, 0, lowTier ? GuiTextures.Blocks.LODESTAR_LESSER_EXTENSION : GuiTextures.Blocks.LODESTAR_EXTENSION, 256, 256, pressHandler, tooltip);
        this.value = defaultValue;
        this.mc = Minecraft.getInstance();
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.isMouseOver(pMouseX, pMouseY)) {
            pGuiGraphics.renderTooltip(this.mc.font, this.getTooltipItems(), Optional.empty(), pMouseX, pMouseY);
        }
    }

    public T value() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract void saveTo(ConstructAITaskParameter var1);

    public abstract void loadFrom(ConstructAITaskParameter var1);

    protected ItemStack getCursorHeldItem() {
        return this.mc.player.containerMenu.getCarried();
    }

    public List<Component> getTooltipItems() {
        ArrayList<Component> baseline = new ArrayList<Component>();
        baseline.add(this.getMessage());
        return baseline;
    }
}

