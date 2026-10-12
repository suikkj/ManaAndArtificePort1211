/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mezz.jei.api.ingredients.IIngredientRenderer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package com.mna.interop.jei.ingredients;

import com.mna.interop.jei.ingredients.ManaweavePatternIngredient;
import com.mna.tools.render.GuiRenderUtils;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ManaweavePatternIngredientRenderer
implements IIngredientRenderer<ManaweavePatternIngredient> {
    public void render(GuiGraphics pGuiGraphics, ManaweavePatternIngredient ingredient) {
        if (ingredient == null) {
            return;
        }
        float scale = 0.1f;
        Minecraft mc = Minecraft.getInstance();
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(14.0f, 1.0f, 0.0f);
        GuiRenderUtils.renderManaweavePattern(pGuiGraphics, 0, 0, scale, ingredient.getPattern((Level)mc.level));
        pGuiGraphics.pose().popPose();
    }

    public List<Component> getTooltip(ManaweavePatternIngredient ingredient, TooltipFlag tooltipFlag) {
        if (ingredient == null) {
            return Arrays.asList(new Component[0]);
        }
        Minecraft mc = Minecraft.getInstance();
        return Arrays.asList(Component.translatable((String)ingredient.getPattern((Level)mc.level).getId().toString()));
    }
}

