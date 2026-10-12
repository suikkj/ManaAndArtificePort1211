/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FastColor$ARGB32
 */
package com.mna.gui.widgets.lodestar;

import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;

public class IntegerParameterInput
extends LodestarParameter<Integer> {
    public static final int PIP_U = 250;
    public static final int PIP_V = 244;
    public static final int PIP_HEIGHT = 6;
    public static final int BAR_U = 216;
    public static final int BAR_V = 250;
    public static final int BAR_HEIGHT = 6;
    public static final int BAR_OFFSET = 16;
    public static final int BAR_WIDTH = 40;
    public static final int V = 66;
    private boolean isDragging = false;
    private int min = 0;
    private int max = 100;

    public IntegerParameterInput(boolean lowTier, int x, int y, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 66, 0, pressHandler, tooltip);
    }

    public IntegerParameterInput(boolean lowTier, int x, int y, int defaultValue, int min, int max, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 66, defaultValue, pressHandler, tooltip);
        this.min = min;
        this.max = max;
        if (this.max < this.min + 1) {
            this.max = this.min + 1;
        }
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        boolean wasHovered = this.isHovered;
        this.isHovered = false;
        pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        float pct = (float)((Integer)this.value - this.min) / (float)(this.max - this.min);
        pGuiGraphics.blit(this.resourceLocation, this.getX() + 16, this.getY() + this.height - 6, 216.0f, 250.0f, 40, 6, 256, 256);
        pGuiGraphics.blit(this.resourceLocation, this.getX() + 16 + (int)(34.0f * pct), this.getY() + this.height - 6, 250.0f, 244.0f, 6, 6, 256, 256);
        MutableComponent tc = Component.literal((String)((Integer)this.value).toString());
        int valWidth = this.mc.font.width((FormattedText)tc);
        Font font = this.mc.font;
        int n = this.getX() + 36 - valWidth / 2;
        int n2 = this.getY() + this.height / 2;
        Objects.requireNonNull(this.mc.font);
        pGuiGraphics.drawString(font, (Component)tc, n, n2 - 9 / 2, FastColor.ARGB32.color((int)255, (int)205, (int)223, (int)225));
        if (wasHovered) {
            this.isHovered = true;
            pGuiGraphics.renderTooltip(this.mc.font, (Component)tc, pMouseX, pMouseY);
        }
    }

    public void onClick(double mouse_x, double mouse_y) {
        super.onClick(mouse_x, mouse_y);
        if (mouse_y >= (double)(this.getY() + this.height - 6) && mouse_y <= (double)(this.getY() + this.height)) {
            this.isDragging = true;
            this.setValueFromMouse(mouse_x);
        }
        this.setFocused(true);
    }

    public boolean mouseReleased(double mouse_x, double mouse_y, int button) {
        this.isDragging = false;
        this.setFocused(false);
        return super.mouseReleased(mouse_x, mouse_y, button);
    }

    protected void onDrag(double mouse_x, double mouse_y, double delta_x, double delta_y) {
        if (this.isDragging) {
            this.setValueFromMouse(mouse_x);
        }
        super.onDrag(mouse_x, mouse_y, delta_x, delta_y);
    }

    private void setValueFromMouse(double mouse_x) {
        double pct = (mouse_x - (double)(this.getX() + 16)) / 40.0;
        int value = this.min + (int)((double)(this.max - this.min) * pct);
        value = MathUtils.clamp(value, this.min, this.max);
        this.setValue(value);
    }

    @Override
    public void saveTo(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskIntegerParameter) {
            ((ConstructTaskIntegerParameter)param).setValue((Integer)this.value);
        }
    }

    @Override
    public void loadFrom(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskIntegerParameter) {
            this.value = ((ConstructTaskIntegerParameter)param).getValue();
        }
    }

    @Override
    public List<Component> getTooltipItems() {
        List<Component> baseTT = super.getTooltipItems();
        baseTT.add((Component)Component.literal((String)" "));
        baseTT.add((Component)Component.literal((String)((Integer)this.value).toString()));
        return baseTT;
    }
}

