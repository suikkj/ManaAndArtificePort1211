/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractSelectionList$Entry
 *  net.minecraft.client.gui.components.ObjectSelectionList
 *  net.minecraft.client.gui.components.ObjectSelectionList$Entry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.gui.widgets;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.MATags;
import com.mna.gui.GuiTextures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class ReagentList
extends ObjectSelectionList<ReagentListEntry> {
    private boolean _scrolling = false;
    private boolean fullTooltip = false;
    private int x0;
    private int y0;
    private int x1;
    private int y1;
    private final Font font;
    private final Consumer<Component> tooltipFunction;

    public ReagentList(int screenLeft, int screenTop, int x, int y, int width, int height, @Nullable Consumer<Component> tooltipFunction) {
        super(Minecraft.getInstance(), width, height, screenTop + y, screenTop + y + height, 16);
        this.setRenderHeader(false, 24);
        this.x0 = screenLeft + x;
        this.x1 = screenLeft + x + width;
        this.y0 = screenTop + y;
        this.y1 = screenTop + y + height;
        Minecraft mc = Minecraft.getInstance();
        this.font = mc.font;
        this.tooltipFunction = tooltipFunction;
        this.clear();
    }

    public void setFullTooltip(boolean fullTooltip) {
        this.fullTooltip = fullTooltip;
    }

    public void clear() {
        this.clearEntries();
    }

    public void reInit(Collection<ItemStack> stacks) {
        this.clear();
        if (stacks != null) {
            this.addIconsForAll(stacks.stream().map(is -> {
                ArrayList<ItemStack> mappedCollection = new ArrayList<ItemStack>();
                mappedCollection.add((ItemStack)is);
                return mappedCollection;
            }).collect(Collectors.toList()));
        }
    }

    public void reInit(HashMap<ResourceLocation, Integer> stacks) {
        this.clear();
        if (stacks != null) {
            ArrayList<List<ItemStack>> collections = new ArrayList<List<ItemStack>>();
            stacks.entrySet().forEach(e -> {
                ArrayList collectedStacks = new ArrayList();
                List<Item> items = MATags.smartLookupItem((ResourceLocation)e.getKey());
                if (items == null || items.size() == 0) {
                    return;
                }
                items.forEach(i -> collectedStacks.add(new ItemStack((ItemLike)i, ((Integer)e.getValue()).intValue())));
                collections.add(collectedStacks);
            });
            this.addIconsForAll(collections);
        }
    }

    private void addIconsForAll(Collection<List<ItemStack>> stacks) {
        ArrayList<List<ItemStack>> segment = new ArrayList<List<ItemStack>>();
        for (List<ItemStack> part : stacks) {
            segment.add(part);
            int segmentWidth = (segment.size() + 1) * 8 + 15;
            if (segmentWidth < this.width) continue;
            this.addEntry((AbstractSelectionList.Entry)new ReagentListEntry(segment));
            segment.clear();
        }
        if (segment.size() > 0) {
            this.addEntry((AbstractSelectionList.Entry)new ReagentListEntry(segment));
        }
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        int scrollBarStartX = this.getScrollbarPosition();
        pGuiGraphics.enableScissor(this.x0, this.y0, this.x1, this.y1);
        this.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.getMaxScroll() > 0) {
            int scrollBarHeight = 20;
            int top = (int)this.getScrollAmount() * (this.y1 - this.y0 - scrollBarHeight) / this.getMaxScroll() + this.y0;
            if (top < this.y0) {
                top = this.y0;
            }
            pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, scrollBarStartX, top, 25.0f, 20.0f, 4, scrollBarHeight, 128, 128);
        }
        pGuiGraphics.disableScissor();
    }

    @Nullable
    protected final ReagentListEntry getEntryAtPos(double mouseX, double mouseY) {
        int lowerXBound = this.getRowLeft();
        int upperXBound = lowerXBound + this.getRowWidth();
        int adjustedY = Mth.floor((double)(mouseY - (double)this.y0)) + (int)this.getScrollAmount();
        int index = adjustedY / this.itemHeight;
        if (index >= 0 && adjustedY >= 0 && index < this.getItemCount() && mouseX < (double)this.getScrollbarPosition() && mouseX >= (double)lowerXBound && mouseX <= (double)upperXBound) {
            return (ReagentListEntry)(this.children().get(index));
        }
        return null;
    }

    protected int getScrollbarPosition() {
        return this.getRowLeft() + this.getRowWidth() - 4;
    }

    protected int getRowTop(int p_getRowTop_1_) {
        return this.y0 - (int)this.getScrollAmount() + p_getRowTop_1_ * this.itemHeight - 4;
    }

    public int getRowLeft() {
        return this.x0;
    }

    public int getRowWidth() {
        return this.width;
    }

    protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_) {
        super.updateScrollingState(p_updateScrollingState_1_, p_updateScrollingState_3_, p_updateScrollingState_5_);
        this._scrolling = p_updateScrollingState_5_ == 0 && p_updateScrollingState_1_ >= (double)this.getScrollbarPosition() && p_updateScrollingState_1_ < (double)(this.getScrollbarPosition() + 6);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            this.setDragging(true);
            return true;
        }
        return this._scrolling;
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public void setSelected(@Nullable ReagentListEntry selected) {
        super.setSelected((AbstractSelectionList.Entry)selected);
    }

    public boolean isScrolling() {
        return this._scrolling;
    }

    public class ReagentListEntry
    extends ObjectSelectionList.Entry<ReagentListEntry> {
        private Collection<List<ItemStack>> stacks;
        private int spacing = 10;

        public ReagentListEntry(Collection<List<ItemStack>> parts) {
            this.stacks = new ArrayList<List<ItemStack>>(parts);
        }

        public void render(GuiGraphics pGuiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float p_render_9_) {
            int i = 0;
            float scale = 0.5f;
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().scale(scale, scale, scale);
            for (List<ItemStack> stackList : this.stacks) {
                boolean isOverMe;
                int stackIndex;
                ItemStack stack;
                if (stackList == null || stackList.size() == 0 || (stack = stackList.get(stackIndex = (int)(ManaAndArtifice.instance.proxy.getClientWorld().getGameTime() / 20L) % Math.max(stackList.size(), 1))).isEmpty()) continue;
                int x = (int)((float)(5 + left + i++ * this.spacing) / scale);
                int y = (int)((float)(top + 8) / scale);
                pGuiGraphics.renderItem(stack, x, y);
                String str = String.format("%d", stack.getCount());
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate(0.0f, 0.0f, 500.0f);
                pGuiGraphics.drawString(ReagentList.this.font, str, x + 14 - ReagentList.this.font.width(str) / 2, y + 8, 0xFFFFFF, false);
                pGuiGraphics.pose().popPose();
                boolean bl = isOverMe = ReagentList.this.getEntryAtPos(mouseX, mouseY) == this;
                if (!this.isMouseOver(mouseX, mouseY) || !isOverMe || !((float)mouseX >= (float)x * scale) || !((float)mouseX <= (float)(x + this.spacing + 5) * scale) || ReagentList.this.tooltipFunction == null) continue;
                if (ReagentList.this.fullTooltip) {
                    Minecraft mc = Minecraft.getInstance();
                    stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, (Player)mc.player, (TooltipFlag)TooltipFlag.Default.NORMAL).forEach(ReagentList.this.tooltipFunction);
                    continue;
                }
                ReagentList.this.tooltipFunction.accept((Component)Component.translatable((String)stack.getDescriptionId()));
            }
            pGuiGraphics.pose().popPose();
        }

        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return true;
        }

        public Component getNarration() {
            return Component.translatable((String)"narrator.select", (Object[])new Object[]{""});
        }
    }
}


