/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 */
package com.mna.gui.widgets.guide;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemGridWidget
extends AbstractWidget {
    public static final int ITEM_SIZE = 16;
    private final int items_per_row;
    private final float scale;
    private final int padding;
    private final ItemStack[] renderStacks;
    private final Consumer<List<Component>> tooltipFunction;
    private ItemStack hoveredStack = ItemStack.EMPTY;

    public ItemGridWidget(int pX, int pY, int pWidth, int pHeight, float scale, ItemStack[] stacks, Consumer<List<Component>> tooltipFunction) {
        this(pX, pY, pWidth, pHeight, 4, 2, scale, stacks, tooltipFunction);
    }

    public ItemGridWidget(int pX, int pY, int pWidth, int pHeight, int items_per_row, int padding, float scale, ItemStack[] stacks, Consumer<List<Component>> tooltipFunction) {
        super(pX, pY, pWidth, pHeight, (Component)Component.literal((String)""));
        this.items_per_row = items_per_row;
        this.renderStacks = stacks;
        this.scale = scale;
        this.padding = padding;
        this.tooltipFunction = tooltipFunction;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.renderStacks == null || this.renderStacks.length == 0) {
            return;
        }
        this.hoveredStack = ItemStack.EMPTY;
        int row_items = Math.min(this.items_per_row, this.renderStacks.length);
        int width_from_items = row_items * 16;
        int width_from_padding = Math.max(0, row_items - 1) * this.padding;
        int row_width = (int)((float)(width_from_items + width_from_padding) * this.scale);
        int xPos = this.getX() + (108 - row_width) / 2;
        int yPos = this.getY();
        for (int i = 0; i < this.renderStacks.length; ++i) {
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().scale(this.scale, this.scale, this.scale);
            int itemX = (int)((float)xPos / this.scale);
            int itemY = (int)((float)yPos / this.scale);
            pGuiGraphics.renderItem(this.renderStacks[i], itemX, itemY);
            if (this.isHovered && pMouseX >= itemX && pMouseX <= itemX + 16 + this.padding && pMouseY >= itemY && pMouseY <= itemY + 16 + this.padding) {
                this.hoveredStack = this.renderStacks[i];
                if (this.tooltipFunction != null) {
                    Minecraft mc = Minecraft.getInstance();
                    this.tooltipFunction.accept(this.hoveredStack.getTooltipLines((Player)mc.player, (TooltipFlag)(mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.ADVANCED)));
                }
            }
            if (i % this.items_per_row == 0 && i != 0) {
                xPos = this.getX() + (108 - row_width) / 2;
                yPos += 16 + this.padding;
            } else {
                xPos += 16 + this.padding;
            }
            pGuiGraphics.pose().popPose();
        }
    }

    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}

