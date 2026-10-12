 
package com.mna.enchantments;

import com.mna.enchantments.base.MAEnchantmentBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class Returning
extends MAEnchantmentBase {
    public static final String NBT_RETURNFLAG = "return_on_next_arrow_shot";
    public static final String NBT_RETURNSTACK = "return_stack";

    public Returning() {
    }

    public boolean canEnchant(ItemStack p_92089_1_) {
        return true;
    }

    public int getMaxLevel() {
        return 1;
    }
}

