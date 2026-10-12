 
package com.mna.enchantments;

import com.mna.enchantments.base.MAEnchantmentBase;
import com.mna.enchantments.framework.EnchantmentEnumExtender;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class Beheading
extends MAEnchantmentBase {
    public Beheading() {
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 5;
    }

    public boolean canEnchant(ItemStack stack) {
        return EnchantmentEnumExtender.staffOrWeapon(stack.getItem());
    }
}

