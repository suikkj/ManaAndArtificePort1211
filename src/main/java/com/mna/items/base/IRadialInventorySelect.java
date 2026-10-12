 
package com.mna.items.base;

import com.mna.KeybindInit;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.base.IRadialMenuItem;
import com.mna.network.ClientMessageDispatcher;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public interface IRadialInventorySelect
extends IRadialMenuItem {
    public static final String NBT_INDEX = "index";

    @Override
    default public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        String txt = I18n.get((String)((KeyMapping)KeybindInit.RadialMenuOpen.get()).getKey().getDisplayName().getString(), (Object[])new Object[0]);
        tooltip.add((Component)Component.translatable((String)"item.mna.item-with-gui.radial-open", (Object[])new Object[]{txt}).withStyle(ChatFormatting.AQUA));
    }

    default public boolean showFullTooltip() {
        return true;
    }

    public int capacity();

    default public int capacity(@Nullable Player player) {
        return this.capacity();
    }

    default public void setIndex(@Nullable Player player, InteractionHand hand, ItemStack stack, int index) {
        this.setIndex(stack, index);
    }

    public void setIndex(ItemStack var1, int var2);

    public int getIndex(ItemStack var1);

    default public IItemHandlerModifiable getInventory(ItemStack stackEquipped) {
        return this.getInventory(stackEquipped, null);
    }

    default public IItemHandlerModifiable getInventory(ItemStack stackEquipped, @Nullable Player player) {
        return new ItemInventoryBase(stackEquipped, this.capacity());
    }

    default public void setSlot(Player player, InteractionHand hand, int index, boolean offhand, boolean packet) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof IRadialInventorySelect) {
            this.setIndex(player, hand, stack, index);
            if (packet) {
                ClientMessageDispatcher.sendRadialInventorySlotChange(index, offhand);
            }
        }
    }
}

