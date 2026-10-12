 
package com.mna.enchantments.base;

import com.mna.api.spells.attributes.Attribute;
import com.mna.enchantments.base.MAEnchantmentBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import com.mna.api.enchantments.MAEnchantmentBridge;

public abstract class ModifierEnchantment
extends MAEnchantmentBase {
    public final Attribute boost;
    public final int bonusStepsPerLevel;

    protected ModifierEnchantment(Attribute boost, int bonusStepsPerLevel, MAEnchantmentBridge.Rarity rarityIn, MAEnchantmentBridge.Category typeIn, EquipmentSlot[] slots) {
        this.boost = boost;
        this.bonusStepsPerLevel = bonusStepsPerLevel;
    }

    protected boolean checkCompatibility(Enchantment pOther) {
        return !(com.mna.enchantments.framework.EnchantmentBehaviors.get(pOther) instanceof ModifierEnchantment);
    }
}

