/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.gui.radial.components;

import net.minecraft.core.component.DataComponents;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.gui.radial.GenericRadialMenu;
import com.mna.gui.radial.components.DrawingContext;
import com.mna.gui.radial.components.TextRadialMenuItem;
import com.mna.items.ItemInit;
import com.mna.tools.render.GuiRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ManaweaveRadialMenuItem
extends TextRadialMenuItem {
    private static final ItemStack WAND = new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND.get());
    private final int slot;
    private final float scale;
    private final IManaweavePattern pattern;

    public int getSlot() {
        return this.slot;
    }

    public IManaweavePattern getPattern() {
        return this.pattern;
    }

    public ManaweaveRadialMenuItem(GenericRadialMenu owner, int slot, float scale, IManaweavePattern pattern) {
        super(owner, (Component)(pattern != null ? Component.translatable((String)pattern.getRegistryId().toString()) : WAND.getHoverName()), Integer.MAX_VALUE);
        this.slot = slot;
        this.scale = scale;
        this.pattern = pattern;
    }

    @Override
    public void draw(DrawingContext context) {
        context.guiGraphics.pose().pushPose();
        if (this.pattern != null) {
            context.guiGraphics.pose().translate(context.x + 4.0f, context.y - 4.0f, context.z);
            GuiRenderUtils.renderManaweavePattern(context.guiGraphics, 0, 0, this.scale, this.pattern);
        } else {
            context.guiGraphics.renderItem(WAND, (int)context.x - 8, (int)context.y - 8);
        }
        context.guiGraphics.pose().popPose();
    }

    @Override
    public void drawTooltips(DrawingContext context) {
        if (this.getText() != null) {
            context.guiGraphics.renderTooltip(context.fontRenderer, this.getText(), (int)context.x, (int)context.y);
        }
    }

    static {
        WAND.set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"item.mna.manaweaver_wand.manual"));
    }
}

