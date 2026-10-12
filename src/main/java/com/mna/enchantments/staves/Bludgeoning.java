 
package com.mna.enchantments.staves;

import com.mna.enchantments.base.MADamageEnchantmentBase;
import com.mna.enchantments.framework.EnchantmentEnumExtender;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class Bludgeoning
extends MADamageEnchantmentBase {
    public Bludgeoning() {
    }

    public boolean canEnchant(ItemStack stack) {
        return EnchantmentEnumExtender.staves(stack.getItem());
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 5;
    }


    public boolean checkCompatibility(Enchantment pEnch) {
        return !(com.mna.enchantments.framework.EnchantmentBehaviors.get(pEnch) instanceof com.mna.enchantments.base.MADamageEnchantmentBase);
    }
}

