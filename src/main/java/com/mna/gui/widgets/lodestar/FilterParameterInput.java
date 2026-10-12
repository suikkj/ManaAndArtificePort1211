/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.gui.widgets.lodestar;

import com.mna.ManaAndArtifice;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskFilterParameter;
import com.mna.api.items.DynamicItemFilter;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.artifice.Manifest;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class FilterParameterInput
extends LodestarParameter<DynamicItemFilter> {
    public static final int V = 44;
    private ItemStack whitelistFilter = new ItemStack((ItemLike)ItemInit.FILTER_ITEM.get());
    private ItemStack blacklistFilter = new ItemStack((ItemLike)ItemInit.FILTER_ITEM.get());

    public FilterParameterInput(boolean lowTier, int x, int y, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 44, new DynamicItemFilter(), pressHandler, tooltip);
    }

    public FilterParameterInput(boolean lowTier, int x, int y, DynamicItemFilter defaultValue, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 44, defaultValue, pressHandler, tooltip);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (!((DynamicItemFilter)this.value).isWhitelistEmpty()) {
            pGuiGraphics.renderItem(this.whitelistFilter, this.getX() + 19, this.getY() + 3);
        }
        if (!((DynamicItemFilter)this.value).isBlacklistEmpty()) {
            pGuiGraphics.renderItem(this.blacklistFilter, this.getX() + 37, this.getY() + 3);
        }
    }

    @Override
    public void saveTo(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskFilterParameter && this.value != null) {
            ((ConstructTaskFilterParameter)param).getValue().copyFrom((DynamicItemFilter)this.value);
        }
    }

    @Override
    public void loadFrom(ConstructAITaskParameter param) {
        ((DynamicItemFilter)this.value).copyFrom(((ConstructTaskFilterParameter)param).getValue());
        ((Manifest)ItemInit.FILTER_ITEM.get()).setItems(this.whitelistFilter, ((DynamicItemFilter)this.value).getWhiteList(), ((DynamicItemFilter)this.value).getWhitelistMatchDurability(), ((DynamicItemFilter)this.value).getWhitelistMatchTag());
        ((Manifest)ItemInit.FILTER_ITEM.get()).setItems(this.blacklistFilter, ((DynamicItemFilter)this.value).getBlackList(), ((DynamicItemFilter)this.value).getBlacklistMatchDurability(), ((DynamicItemFilter)this.value).getBlacklistMatchTag());
    }

    public void onClick(double mouseX, double mouseY) {
        NonNullList<ItemStack> items;
        ItemStack carried = this.getCursorHeldItem();
        int slot = this.getClickSlot(mouseX, mouseY);
        if (slot == -1) {
            return;
        }
        boolean isWhitelist = slot == 0;
        boolean matchDurability = false;
        boolean matchTag = false;
        if (!carried.isEmpty() && carried.getItem() instanceof Manifest) {
            ItemInventoryBase inv = new ItemInventoryBase(carried);
            items = inv.getAllItems();
            matchDurability = ((Manifest)carried.getItem()).getMatchDurability(carried);
            matchTag = ((Manifest)carried.getItem()).getMatchTag(carried);
        } else {
            items = NonNullList.create();
        }
        if (isWhitelist) {
            ((DynamicItemFilter)this.value).setWhitelist(items, matchDurability, matchTag);
            ((Manifest)ItemInit.FILTER_ITEM.get()).setItems(this.whitelistFilter, ((DynamicItemFilter)this.value).getWhiteList(), ((DynamicItemFilter)this.value).getWhitelistMatchDurability(), ((DynamicItemFilter)this.value).getWhitelistMatchTag());
        } else {
            ((DynamicItemFilter)this.value).setBlacklist(items, matchDurability, matchTag);
            ((Manifest)ItemInit.FILTER_ITEM.get()).setItems(this.blacklistFilter, ((DynamicItemFilter)this.value).getBlackList(), ((DynamicItemFilter)this.value).getBlacklistMatchDurability(), ((DynamicItemFilter)this.value).getBlacklistMatchTag());
        }
        super.onClick(mouseX, mouseY);
    }

    private int getClickSlot(double mouseX, double mouseY) {
        int padding = 16;
        if (mouseX < (double)(this.getX() + padding)) {
            return -1;
        }
        if (mouseX < (double)(this.getX() + padding + (this.width - padding) / 2)) {
            return 0;
        }
        return 1;
    }

    @Override
    public List<Component> getTooltipItems() {
        List<Component> baseTT = super.getTooltipItems();
        baseTT.add((Component)Component.literal((String)" "));
        baseTT.add((Component)Component.translatable((String)"gui.mna.filter.whitelist"));
        ((Manifest)ItemInit.FILTER_ITEM.get()).addContentsDescription(this.whitelistFilter, ManaAndArtifice.instance.proxy.getClientWorld(), baseTT);
        baseTT.add((Component)Component.literal((String)"-------"));
        baseTT.add((Component)Component.translatable((String)"gui.mna.filter.blacklist"));
        ((Manifest)ItemInit.FILTER_ITEM.get()).addContentsDescription(this.blacklistFilter, ManaAndArtifice.instance.proxy.getClientWorld(), baseTT);
        return baseTT;
    }
}

