/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.mna.gui.radial.components;

import com.mna.gui.radial.GenericRadialMenu;
import com.mna.gui.radial.components.DrawingContext;
import com.mna.gui.radial.components.RadialMenuItem;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public class TextRadialMenuItem
extends RadialMenuItem {
    private final Component text;
    private final int color;

    public Component getText() {
        return this.text;
    }

    public int getColor() {
        return this.color;
    }

    public TextRadialMenuItem(GenericRadialMenu owner, Component text) {
        this(owner, text, -1);
    }

    public TextRadialMenuItem(GenericRadialMenu owner, Component text, int color) {
        super(owner);
        this.text = text;
        this.color = color;
    }

    @Override
    public void draw(DrawingContext context) {
        String textString = this.text.getString();
        float x = context.x - (float)context.fontRenderer.width(textString) / 2.0f;
        float f = context.y;
        Objects.requireNonNull(context.fontRenderer);
        float y = f - 9.0f / 2.0f;
        context.guiGraphics.drawString(context.fontRenderer, textString, x, y, this.color, true);
    }

    @Override
    public void drawTooltips(DrawingContext context) {
    }
}

