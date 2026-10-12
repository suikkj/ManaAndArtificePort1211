/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 */
package com.mna.gui.widgets.lodestar;

import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.gui.GuiTextures;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class BooleanParameterInput
extends LodestarParameter<Boolean> {
    public static final int NUB_U = 250;
    public static final int NUB_V = 244;
    public static final int V = 88;

    public BooleanParameterInput(boolean lowTier, int x, int y, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 88, false, pressHandler, tooltip);
    }

    public BooleanParameterInput(boolean lowTier, int x, int y, boolean defaultValue, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 88, defaultValue, pressHandler, tooltip);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (((Boolean)this.value).booleanValue()) {
            pGuiGraphics.blit(GuiTextures.Blocks.LODESTAR_EXTENSION, this.getX() + 24, this.getY() + 8, 250, 244, 6, 6);
        }
    }

    public void toggle() {
        this.value = (Boolean)this.value == false;
    }

    @Override
    public void saveTo(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskBooleanParameter) {
            ((ConstructTaskBooleanParameter)param).setValue((Boolean)this.value);
        }
    }

    @Override
    public void loadFrom(ConstructAITaskParameter param) {
        this.value = ((ConstructTaskBooleanParameter)param).getValue();
    }

    public void onClick(double mouseX, double mouseY) {
        this.toggle();
        super.onClick(mouseX, mouseY);
    }
}

