 
package com.mna.enchantments;

import com.mna.enchantments.base.MAEnchantmentBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import com.mna.api.enchantments.MAEnchantmentBridge;

public class Bouncing
extends MAEnchantmentBase {
    public Bouncing(MAEnchantmentBridge.Rarity rarityIn) {
    }

    public boolean canEnchant(ItemStack pStack) {
        return com.mna.api.enchantments.MAEnchantmentBridge.isBoots(pStack.getItem());
    }

    protected boolean checkCompatibility(Enchantment ench) {
        return true;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }
}

