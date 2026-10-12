 
package com.mna.gui.widgets.lodestar;

import com.mna.ManaAndArtifice;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemStackParameterInput
extends LodestarParameter<ItemStack> {
    public static final int V = 110;

    public ItemStackParameterInput(boolean lowTier, int x, int y, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 110, ItemStack.EMPTY, pressHandler, tooltip);
    }

    public ItemStackParameterInput(boolean lowTier, int x, int y, ItemStack defaultValue, Button.OnPress pressHandler, Component tooltip) {
        super(lowTier, x, y, 110, defaultValue, pressHandler, tooltip);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.renderItem((ItemStack)this.value, this.getX() + 19, this.getY() + 3);
    }

    @Override
    public void saveTo(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskItemStackParameter && this.value != null) {
            ((ConstructTaskItemStackParameter)param).setStack((ItemStack)this.value);
        }
    }

    @Override
    public void loadFrom(ConstructAITaskParameter param) {
        if (param instanceof ConstructTaskItemStackParameter) {
            this.value = ((ConstructTaskItemStackParameter)param).getStack();
        }
    }

    public void onClick(double mouseX, double mouseY) {
        this.setValue(this.getCursorHeldItem().copy());
        super.onClick(mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipItems() {
        List<Component> baseTT = super.getTooltipItems();
        if (!((ItemStack)this.value).isEmpty()) {
            baseTT.add((Component)Component.literal((String)" "));
            baseTT.add(((ItemStack)this.value).getHoverName());
            ((ItemStack)this.value).getItem().appendHoverText((ItemStack)this.value, net.minecraft.world.item.Item.TooltipContext.of(ManaAndArtifice.instance.proxy.getClientWorld()), baseTT, (TooltipFlag)TooltipFlag.Default.NORMAL);
        }
        return baseTT;
    }
}

