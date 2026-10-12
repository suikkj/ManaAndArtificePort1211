 
package com.mna.gui.item;

import com.google.common.collect.Lists;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.item.ContainerSpellRecipe;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class GuiSpellRecipe
extends GuiJEIDisable<ContainerSpellRecipe> {
    private static final int ITEMSTACK_WIDTH = 16;
    private static final int CYCLE_TICKS = 40;

    public GuiSpellRecipe(ContainerSpellRecipe screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 213;
        this.imageHeight = 256;
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(GuiTextures.Items.SPELL_RECIPE, i, j, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        int x = this.leftPos + 6;
        Objects.requireNonNull(this.font);
        int y = this.topPos + 24 + 9;
        int column_width = 24;
        for (List<ItemStack> stacks : ((ContainerSpellRecipe)this.menu).getReagents()) {
            this.renderItemStack(pGuiGraphics, stacks, x, y, pMouseX, pMouseY, 1.0f);
            if ((x += 16 + column_width) < this.leftPos + this.imageWidth - 16 - column_width) continue;
            y += 19;
            x = this.leftPos + 6;
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int y = 10;
        String rawText = Component.translatable((String)"item.mna.enchanted_vellum.subtitle").getString();
        List split_string = this.font.getSplitter().splitLines(rawText, this.imageWidth - 10, Style.EMPTY);
        for (FormattedText itp : split_string) {
            pGuiGraphics.drawString(this.font, itp.getString(), 8, y, FastColor.ARGB32.color((int)255, (int)71, (int)71, (int)71), false);
            Objects.requireNonNull(this.font);
            y += 9;
        }
    }

    protected final int renderItemStack(GuiGraphics pGuiGraphics, List<ItemStack> stackToRender, int x, int y, int mouseX, int mouseY, float scale) {
        List toolTip;
        if (stackToRender == null || stackToRender.size() == 0) {
            return 0;
        }
        ItemStack stack = this.getCurrentIndex(stackToRender);
        pGuiGraphics.renderItem(stack, x, y);
        StringBuilder sb = new StringBuilder();
        sb.append(" x ");
        sb.append(stack.getCount());
        pGuiGraphics.drawString(this.font, sb.toString(), x + 16, y + 3, FastColor.ARGB32.color((int)255, (int)71, (int)71, (int)71), false);
        int adjustedX = (int)((float)x * scale + 16.0f * scale);
        int adjustedY = (int)((float)y * scale + 16.0f * scale);
        if (adjustedX >= mouseX && (float)adjustedX <= (float)mouseX + 16.0f * scale && adjustedY >= mouseY && (float)adjustedY <= (float)mouseY + 16.0f * scale && (toolTip = stack.getTooltipLines(null, (TooltipFlag)TooltipFlag.Default.NORMAL)) != null) {
            pGuiGraphics.renderTooltip(this.font, Lists.transform((List)toolTip, Component::getVisualOrderText), (int)((float)mouseX / scale), (int)((float)mouseY / scale));
        }
        return x + 16 + this.font.width(sb.toString());
    }

    private ItemStack getCurrentIndex(List<ItemStack> stacks) {
        if (stacks.size() == 1) {
            return stacks.get(0);
        }
        return stacks.get((int)(this.minecraft.level.getGameTime() / 40L % (long)stacks.size()));
    }
}

