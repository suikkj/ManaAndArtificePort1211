/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 */
package com.mna.gui.radial.components;

import com.mna.gui.radial.GenericRadialMenu;
import com.mna.gui.radial.components.DrawingContext;
import com.mna.gui.radial.components.TextRadialMenuItem;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemStackRadialMenuItem
extends TextRadialMenuItem {
    private final ItemStack stack;
    private final boolean forceSimpleTooltip;

    public ItemStack getStack() {
        return this.stack;
    }

    public ItemStackRadialMenuItem(GenericRadialMenu owner, ItemStack stack, Component altText) {
        this(owner, stack, altText, true);
    }

    public ItemStackRadialMenuItem(GenericRadialMenu owner, ItemStack stack, Component altText, boolean simpleTooltip) {
        super(owner, altText, Integer.MAX_VALUE);
        this.stack = stack;
        this.forceSimpleTooltip = simpleTooltip;
    }

    @Override
    public void draw(DrawingContext context) {
        if (this.stack.getCount() > 0) {
            context.guiGraphics.renderItem(this.stack, (int)context.x - 8, (int)context.y - 8);
        } else {
            super.draw(context);
        }
    }

    @Override
    public void drawTooltips(DrawingContext context) {
        if (this.stack.getCount() > 0) {
            Minecraft mc = Minecraft.getInstance();
            List<Component> tooltipLines = this.stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, (Player)mc.player, (TooltipFlag)(mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL));
            if (this.forceSimpleTooltip) {
                tooltipLines = this.stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME) ? Arrays.asList(this.stack.getHoverName()) : Arrays.asList(Component.translatable((String)this.stack.getDescriptionId()));
            }
            context.guiGraphics.renderTooltip(mc.font, tooltipLines, this.stack.getTooltipImage(), (int)context.x, (int)context.y);
        } else {
            super.drawTooltips(context);
        }
    }
}

