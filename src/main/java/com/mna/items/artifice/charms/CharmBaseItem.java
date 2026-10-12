 
package com.mna.items.artifice.charms;

import com.mna.api.items.TieredItem;
import com.mna.tools.InventoryUtilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public abstract class CharmBaseItem
extends TieredItem {
    public CharmBaseItem(Item.Properties itemProperties) {
        super(itemProperties);
    }

    public CharmBaseItem() {
        super(new Item.Properties().setNoRepair().stacksTo(1).durability(1));
    }

    public boolean consume(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() != this) {
            return false;
        }
        stack.hurtAndBreak(1, player, hand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        return stack.isEmpty() || player.isCreative();
    }

    public boolean consume(ServerPlayer player) {
        return InventoryUtilities.removeItemFromInventory(new ItemStack((ItemLike)this), true, true, (IItemHandler)new InvWrapper((Container)player.getInventory()));
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    public boolean isEnchantable(ItemStack p_77616_1_) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}

