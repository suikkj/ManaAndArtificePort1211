 
package com.mna.gui.widgets.lodestar;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.api.items.IPositionalItem;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import com.mna.items.ItemInit;
import com.mna.items.runes.ItemRuneMarking;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;

public class PointParameterInput
extends LodestarParameter<DirectionalPoint> {
    public static final int V = 0;
    private ItemStack markRune = new ItemStack((ItemLike)ItemInit.RUNE_MARKING.get());

    public PointParameterInput(boolean lowTier, int x, int y, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 0, null, pressHandler, tooltip);
    }

    public PointParameterInput(boolean lowTier, int x, int y, DirectionalPoint defaultValue, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 0, defaultValue, pressHandler, tooltip);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.value != null && this.markRune.has(DataComponents.CUSTOM_DATA)) {
            pGuiGraphics.renderItem(this.markRune, this.getX() + 19, this.getY() + 3);
        }
    }

    @Override
    public void saveTo(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskPointParameter) {
            ConstructTaskPointParameter point = (ConstructTaskPointParameter)param;
            if (this.value != null) {
                point.setPoint((DirectionalPoint)this.value);
            }
        }
    }

    @Override
    public void loadFrom(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskPointParameter) {
            ConstructTaskPointParameter point = (ConstructTaskPointParameter)param;
            this.value = new DirectionalPoint(point.getPosition(), point.getDirection(), point.getOriginalBlockName(), point.getOriginalBlockID());
            if (((DirectionalPoint)this.value).getPosition() != null) {
                ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(this.markRune, (DirectionalPoint)this.value);
            }
        }
    }

    public void onClick(double mouseX, double mouseY) {
        ItemStack carried = this.getCursorHeldItem();
        if (!carried.isEmpty() && carried.getItem() instanceof IPositionalItem) {
            IPositionalItem pos = (IPositionalItem)carried.getItem();
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).copyPositionFrom(carried, this.markRune);
            this.setValue(pos.getDirectionalPoint(carried));
        } else {
            this.markRune.set(DataComponents.CUSTOM_DATA, CustomData.of(null));
            this.setValue(null);
        }
        super.onClick(mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipItems() {
        List<Component> baseTT = super.getTooltipItems();
        baseTT.add((Component)Component.literal((String)" "));
        ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).appendHoverText(this.markRune, net.minecraft.world.item.Item.TooltipContext.of(ManaAndArtifice.instance.proxy.getClientWorld()), baseTT, (TooltipFlag)TooltipFlag.Default.NORMAL);
        return baseTT;
    }
}

