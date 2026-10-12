/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.screens.Screen
 */
package com.mna.gui.radial.components;

import com.mna.gui.radial.components.IDrawingHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

public interface IRadialMenuHost
extends IDrawingHelper {
    public Screen getScreen();

    public Font getFontRenderer();
}

