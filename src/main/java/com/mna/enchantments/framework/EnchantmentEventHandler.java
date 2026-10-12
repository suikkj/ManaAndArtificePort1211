 
package com.mna.enchantments.framework;

import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.items.base.IManaRepairable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class EnchantmentEventHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        Inventory inv = event.getEntity().getInventory();
        int numSlots = event.getEntity().getInventory().getContainerSize();
        for (int i = 0; i < numSlots; ++i) {
            ItemStack invStack = inv.getItem(i);
            if (!invStack.isRepairable() || EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MANA_REPAIR, event.getEntity()) <= 0) continue;
            IManaRepairable.tickRepair(invStack, event.getEntity(), 0.25f, 0.01f, i);
        }
    }
}


