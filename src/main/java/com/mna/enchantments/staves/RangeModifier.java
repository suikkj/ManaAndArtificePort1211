 
package com.mna.enchantments.staves;

import com.mna.api.spells.attributes.Attribute;
import com.mna.enchantments.base.ModifierEnchantment;
import com.mna.enchantments.framework.EnchantmentEnumExtender;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class RangeModifier
extends ModifierEnchantment {
    public RangeModifier() {
        super(Attribute.RANGE, 1, com.mna.api.enchantments.MAEnchantmentBridge.Rarity.UNCOMMON, com.mna.api.enchantments.MAEnchantmentBridge.Category.STAVES, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
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
}

