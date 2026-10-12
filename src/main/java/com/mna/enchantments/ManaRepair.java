 
package com.mna.enchantments;

import com.mna.enchantments.base.MAEnchantmentBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ManaRepair
extends MAEnchantmentBase {
    public ManaRepair() {
    }

    public boolean canEnchant(ItemStack stack) {
        return stack.isDamageableItem() && stack.isRepairable();
    }
}

