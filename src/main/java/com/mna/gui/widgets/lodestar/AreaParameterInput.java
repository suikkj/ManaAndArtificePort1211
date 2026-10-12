 
package com.mna.gui.widgets.lodestar;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.items.IAreaItem;
import com.mna.api.items.IPositionalItem;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import com.mna.items.ItemInit;
import com.mna.items.runes.ItemRuneMarking;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.AABB;

public class AreaParameterInput
extends LodestarParameter<Pair<DirectionalPoint, DirectionalPoint>> {
    public static final int V = 22;
    private ItemStack markRuneA = new ItemStack((ItemLike)ItemInit.RUNE_MARKING.get());
    private ItemStack markRuneB = new ItemStack((ItemLike)ItemInit.RUNE_MARKING.get());

    public AreaParameterInput(boolean lowTier, int x, int y, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 22, null, pressHandler, tooltip);
    }

    public AreaParameterInput(boolean lowTier, int x, int y, Pair<DirectionalPoint, DirectionalPoint> defaultValue, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 22, defaultValue, pressHandler, tooltip);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.markRuneA.has(DataComponents.CUSTOM_DATA)) {
            pGuiGraphics.renderItem(this.markRuneA, this.getX() + 19, this.getY() + 3);
        }
        if (this.markRuneB.has(DataComponents.CUSTOM_DATA)) {
            pGuiGraphics.renderItem(this.markRuneB, this.getX() + 37, this.getY() + 3);
        }
    }

    @Override
    public void saveTo(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskAreaParameter) {
            ConstructTaskAreaParameter point = (ConstructTaskAreaParameter)param;
            if (this.value != null) {
                point.setPoints((DirectionalPoint)((Pair)this.value).getFirst(), (DirectionalPoint)((Pair)this.value).getSecond());
            }
        }
    }

    @Override
    public void loadFrom(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskAreaParameter) {
            this.value = ((ConstructTaskAreaParameter)param).getPoints();
            if (this.value != null) {
                if (((Pair)this.value).getFirst() != null) {
                    ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(this.markRuneA, (DirectionalPoint)((Pair)this.value).getFirst());
                } else {
                    this.markRuneA.set(DataComponents.CUSTOM_DATA, CustomData.of(null));
                }
                if (((Pair)this.value).getSecond() != null) {
                    ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(this.markRuneB, (DirectionalPoint)((Pair)this.value).getSecond());
                } else {
                    this.markRuneB.set(DataComponents.CUSTOM_DATA, CustomData.of(null));
                }
            }
        }
    }

    public void onClick(double mouseX, double mouseY) {
        Item item;
        ItemStack carried = this.getCursorHeldItem();
        if (!carried.isEmpty() && (item = carried.getItem()) instanceof IAreaItem) {
            IAreaItem areaItem = (IAreaItem)item;
            AABB bounds = areaItem.getBounds(carried);
            DirectionalPoint a = new DirectionalPoint(new BlockPos((int)bounds.minX, (int)bounds.minY, (int)bounds.minZ), Direction.UP, null, null);
            DirectionalPoint b = new DirectionalPoint(new BlockPos((int)bounds.maxX, (int)bounds.maxY, (int)bounds.maxZ), Direction.UP, null, null);
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(this.markRuneA, a);
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(this.markRuneB, b);
            this.setValue(new Pair(a, b));
            return;
        }
        ItemStack slotItem = this.getSlotItemFromClick(mouseX, mouseY);
        if (slotItem.isEmpty()) {
            return;
        }
        if (!carried.isEmpty() && carried.getItem() instanceof IPositionalItem) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).copyPositionFrom(carried, slotItem);
        } else {
            slotItem.set(DataComponents.CUSTOM_DATA, CustomData.of(null));
        }
        if (this.markRuneA.has(DataComponents.CUSTOM_DATA) && this.markRuneB.has(DataComponents.CUSTOM_DATA)) {
            this.setValue(new Pair(((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getDirectionalPoint(this.markRuneA), ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getDirectionalPoint(this.markRuneB)));
        } else {
            DirectionalPoint a = null;
            DirectionalPoint b = null;
            if (this.markRuneA.has(DataComponents.CUSTOM_DATA)) {
                a = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getDirectionalPoint(this.markRuneA);
            }
            if (this.markRuneB.has(DataComponents.CUSTOM_DATA)) {
                b = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getDirectionalPoint(this.markRuneB);
            }
            this.setValue(new Pair(a, b));
        }
        super.onClick(mouseX, mouseY);
    }

    private ItemStack getSlotItemFromClick(double mouseX, double mouseY) {
        int padding = 16;
        if (mouseX < (double)(this.getX() + padding)) {
            return ItemStack.EMPTY;
        }
        if (mouseX < (double)(this.getX() + padding + (this.width - padding) / 2)) {
            return this.markRuneA;
        }
        return this.markRuneB;
    }

    @Override
    public List<Component> getTooltipItems() {
        List<Component> baseTT = super.getTooltipItems();
        baseTT.add((Component)Component.literal((String)" "));
        ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).appendHoverText(this.markRuneA, net.minecraft.world.item.Item.TooltipContext.of(ManaAndArtifice.instance.proxy.getClientWorld()), baseTT, (TooltipFlag)TooltipFlag.Default.NORMAL);
        baseTT.add((Component)Component.literal((String)"-------"));
        ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).appendHoverText(this.markRuneB, net.minecraft.world.item.Item.TooltipContext.of(ManaAndArtifice.instance.proxy.getClientWorld()), baseTT, (TooltipFlag)TooltipFlag.Default.NORMAL);
        return baseTT;
    }
}

