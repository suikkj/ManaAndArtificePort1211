 
package com.mna.enchantments;

import com.mna.enchantments.base.MAEnchantmentBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class Soulbound
extends MAEnchantmentBase {
    public Soulbound() {
    }

    public boolean canEnchant(ItemStack p_92089_1_) {
        return true;
    }

    public int getMaxLevel() {
        return 1;
    }
}

