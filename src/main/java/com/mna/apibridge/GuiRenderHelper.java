/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.mna.apibridge;

import com.mna.api.faction.IFaction;
import com.mna.api.gui.IGuiRenderHelper;
import com.mna.tools.render.GuiRenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public class GuiRenderHelper
implements IGuiRenderHelper {
    @Override
    public void renderFactionIcon(GuiGraphics pGuiGraphics, IFaction faction, int x, int y) {
        GuiRenderUtils.renderFactionIcon(pGuiGraphics, faction, x, y);
    }
}

