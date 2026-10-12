/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  javax.annotation.Nullable
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractSelectionList$Entry
 *  net.minecraft.client.gui.components.ObjectSelectionList
 *  net.minecraft.client.gui.components.ObjectSelectionList$Entry
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 *  net.minecraft.world.item.trading.MerchantOffer
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.gui.entity;

import com.mna.gui.GuiTextures;
import com.mna.gui.base.SearchableGui;
import com.mna.gui.containers.entity.ContainerWanderingWizard;
import com.mna.network.ClientMessageDispatcher;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class GuiWanderingWizard
extends SearchableGui<ContainerWanderingWizard> {
    private boolean hasinitialized = false;
    private ItemStack tooltipStack = ItemStack.EMPTY;
    private WanderingWizardOffersList list;
    private int selectedRecipeIndex = -1;

    public GuiWanderingWizard(ContainerWanderingWizard screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.tooltipStack = ItemStack.EMPTY;
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int x, int y) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, i, j, 0.0f, 0.0f, this.imageWidth, 193, this.imageWidth, this.imageHeight);
        pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, i + 40, j + 193, 40.0f, 193.0f, 176, 63, this.imageWidth, this.imageHeight);
        if (this.list != null) {
            this.list.render(pGuiGraphics, x, y, partialTicks);
        }
        if (this.searchBox != null) {
            RenderSystem.disableDepthTest();
            this.searchBox.render(pGuiGraphics, x, y, partialTicks);
            RenderSystem.enableDepthTest();
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
        if (!this.hasinitialized && ((ContainerWanderingWizard)this.menu).isFinalized()) {
            this.initialize();
        }
        if (!this.tooltipStack.isEmpty()) {
            pGuiGraphics.renderTooltip(this.font, this.tooltipStack, x - this.leftPos, y - this.topPos);
        }
    }

    @Override
    protected void searchTermChanged(String newTerm) {
        this.currentSearchTerm = newTerm;
        this.list.clear();
        this.list.reInit(this.currentSearchTerm);
        this.list.setScrollAmount(0.0);
    }

    private void initialize() {
        this.hasinitialized = true;
        this.list = new WanderingWizardOffersList(((ContainerWanderingWizard)this.menu).getOffers());
        int tbWidth = 130;
        int x = this.leftPos + 6;
        int y = this.topPos + 6;
        this.initSearch(x, y, tbWidth, 16);
    }

    private void OnItemClicked(MerchantOffer offer, int index) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        this.selectedRecipeIndex = index = ((ContainerWanderingWizard)this.menu).calculateIndexFor(offer, index, this.currentSearchTerm);
        ((ContainerWanderingWizard)this.menu).setCurrentRecipeIndex(this.selectedRecipeIndex);
        ((ContainerWanderingWizard)this.menu).setCurrentTradeRecipeItems(this.selectedRecipeIndex);
        ClientMessageDispatcher.sendTradeSelected(index);
    }

    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (this.list != null && this.list._scrolling) {
            return this.list.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    @OnlyIn(value=Dist.CLIENT)
    class WanderingWizardOffersList
    extends ObjectSelectionList<WanderingWizardOffersList.ItemStackEntry> {
        private boolean _scrolling;
        public boolean _active;
        private final Collection<MerchantOffer> initialOffers;
        private int x0;
        private int y0;
        private int x1;
        private int y1;

        public WanderingWizardOffersList(MerchantOffers merchantOffers) {
            super(GuiWanderingWizard.this.minecraft, 244, 141, GuiWanderingWizard.this.topPos + 26, 20);
            this._scrolling = false;
            this._active = true;
            this.setRenderHeader(false, 20);
            this.y0 = GuiWanderingWizard.this.topPos + 26;
            this.y1 = GuiWanderingWizard.this.topPos + 167;
            this.x0 = GuiWanderingWizard.this.leftPos - 2;
            this.x1 = this.x0 + 250;
            this.initialOffers = merchantOffers;
            this.reInit("");
        }

        private void addItems(Collection<MerchantOffer> offers, BiConsumer<MerchantOffer, Integer> clickHandler) {
            ArrayList<MerchantOffer> segment = new ArrayList<MerchantOffer>();
            int index = 0;
            for (MerchantOffer part : offers) {
                segment.add(part);
                if (segment.size() != 3) continue;
                this.addEntry(new ItemStackEntry(segment, clickHandler, index));
                index += segment.size();
                segment.clear();
            }
            if (segment.size() > 0) {
                this.addEntry(new ItemStackEntry(segment, clickHandler, index));
            }
        }

        public void clear() {
            this.clearEntries();
        }

        public void reInit(String searchTerm) {
            this.addItems(this.initialOffers.stream().filter(o -> searchTerm == "" || o.getResult().getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, (Player)this.minecraft.player, (TooltipFlag)TooltipFlag.Default.NORMAL).stream().anyMatch(l -> l.toString().toLowerCase().contains(searchTerm.toLowerCase()))).collect(Collectors.toList()), (o, i) -> GuiWanderingWizard.this.OnItemClicked((MerchantOffer)o, (int)i));
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (!this._active) {
                return;
            }
            int scrollBarStartX = this.getScrollbarPosition();
            this.children().forEach(c -> {
                c.isHovered = false;
            });
            ItemStackEntry hov = this.getEntryAtPos(mouseX, mouseY);
            if (hov != null) {
                hov.isHovered = true;
            }
            this.getEntryAtPos(mouseX, mouseY);
            this.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
            if (this.getMaxScroll() > 0) {
                int scrollBarHeight = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                scrollBarHeight = Mth.clamp((int)scrollBarHeight, (int)32, (int)(this.y1 - this.y0 - 8));
                int top = (int)this.getScrollAmount() * (this.y1 - this.y0 - scrollBarHeight) / this.getMaxScroll() + this.y0;
                if (top < this.y0) {
                    top = this.y0;
                }
                pGuiGraphics.blit(ResourceLocation.parse("textures/gui/container/villager2.png"), scrollBarStartX, top, 25.0f, 20.0f, 3, scrollBarHeight, 512, 256);
            }
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            int i = GuiWanderingWizard.this.leftPos;
            int j = GuiWanderingWizard.this.topPos;
            pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, i, j, 0, 0, GuiWanderingWizard.this.imageWidth, 26);
            pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, i, j + 167, 0, 167, GuiWanderingWizard.this.imageWidth, 26);
            RenderSystem.enableDepthTest();
        }

        @Nullable
        protected final ItemStackEntry getEntryAtPos(double mouseX, double mouseY) {
            int lowerXBound = this.getRowLeft();
            int upperXBound = lowerXBound + this.getRowWidth();
            int adjustedY = Mth.floor((double)(mouseY - (double)this.y0)) + (int)this.getScrollAmount();
            int index = adjustedY / this.itemHeight;
            if (mouseX < (double)this.x0 || mouseX > (double)this.x1 || mouseY < (double)this.y0 || mouseY > (double)this.y1) {
                return null;
            }
            if (index >= 0 && adjustedY >= 0 && index < this.getItemCount() && mouseX < (double)this.getScrollbarPosition() && mouseX >= (double)lowerXBound && mouseX <= (double)upperXBound) {
                return (ItemStackEntry)(this.children().get(index));
            }
            return null;
        }

        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 2;
        }

        protected int getRowTop(int p_getRowTop_1_) {
            return this.y0 - (int)this.getScrollAmount() + p_getRowTop_1_ * this.itemHeight;
        }

        public int getRowWidth() {
            return this.width;
        }

        protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_) {
            super.updateScrollingState(p_updateScrollingState_1_, p_updateScrollingState_3_, p_updateScrollingState_5_);
            this._scrolling = p_updateScrollingState_5_ == 0 && p_updateScrollingState_1_ >= (double)this.getScrollbarPosition() && p_updateScrollingState_1_ < (double)(this.getScrollbarPosition() + 6);
        }

        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            if (!this._active) {
                return false;
            }
            this.updateScrollingState(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
            if (!this.isMouseOver(p_mouseClicked_1_, p_mouseClicked_3_)) {
                return false;
            }
            ItemStackEntry e = this.getEntryAtPos(p_mouseClicked_1_, p_mouseClicked_3_);
            if (e != null) {
                if (e.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
                    e.setFocused(true);
                    return true;
                }
            } else if (p_mouseClicked_5_ == 0) {
                this.setDragging(true);
                return true;
            }
            return this._scrolling;
        }

        public int getMaxScroll() {
            return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
        }

        public void setSelected(@Nullable ItemStackEntry selected) {
            super.setSelected(selected);
        }

        @OnlyIn(value=Dist.CLIENT)
        public class ItemStackEntry
        extends ObjectSelectionList.Entry<ItemStackEntry> {
            private Collection<MerchantOffer> items;
            private int spacing = 83;
            private MerchantOffer _hoveredComponent;
            private BiConsumer<MerchantOffer, Integer> _clickHandler;
            private int index;
            private int hoveredIndex;
            protected boolean isHovered;

            public ItemStackEntry(Collection<MerchantOffer> parts, BiConsumer<MerchantOffer, Integer> clickHandler, int index) {
                this.items = new ArrayList<MerchantOffer>(parts);
                this._hoveredComponent = null;
                this.index = index;
                this._clickHandler = clickHandler;
            }

            public void render(GuiGraphics pGuiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float p_render_9_) {
                int i = 0;
                for (MerchantOffer part : this.items) {
                    if (part == null) continue;
                    int x = 7 + left + i++ * this.spacing;
                    int y = top + 4;
                    pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, x, y, 16, 238, 18, 18);
                    pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, x + 18, y, 16, 238, 18, 18);
                    pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, x + 37, y + 3, 0, 246, 16, 10);
                    pGuiGraphics.blit(GuiTextures.Entities.WANDERING_WIZARD, x + 54, y, 16, 238, 18, 18);
                    pGuiGraphics.renderItem(part.getBaseCostA(), x + 1, y + 1);
                    pGuiGraphics.renderItem(part.getCostB(), x + 19, y + 1);
                    pGuiGraphics.renderItem(part.getResult(), x + 55, y + 1);
                    if (!this.isHovered || mouseX < x || mouseX > x + this.spacing) continue;
                    GuiWanderingWizard.this.tooltipStack = part.getResult();
                    this._hoveredComponent = part;
                    this.hoveredIndex = i - 1;
                }
            }

            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                WanderingWizardOffersList.this.setSelected(this);
                if (this._clickHandler != null && this._hoveredComponent != null) {
                    this._clickHandler.accept(this._hoveredComponent, this.index + this.hoveredIndex);
                }
                return true;
            }

            public Component getNarration() {
                return Component.translatable((String)"");
            }
        }
    }
}


