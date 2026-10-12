 
package com.mna.enchantments;

import com.mna.enchantments.base.MAEnchantmentBase;
import com.mna.items.artifice.SpectralElytra;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ExperienceToSouls
extends MAEnchantmentBase {
    public ExperienceToSouls() {
    }

    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem || stack.getItem() instanceof SpectralElytra;
    }

    public int getMaxLevel() {
        return 3;
    }

    public int getMinLevel() {
        return 1;
    }
}

