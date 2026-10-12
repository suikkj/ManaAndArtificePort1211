/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.DamageEnchantment
 *  net.minecraft.world.item.enchantment.Enchantment$Rarity
 */
package com.mna.enchantments.base;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public abstract class MADamageEnchantmentBase extends com.mna.enchantments.framework.MAEnchantmentBase {
    /**
     * [PORT 1.21.1] DamageEnchantment foi removido (dano agora e DataComponent
     * ENCHANTMENT_ENTITY_DAMAGE). Classe mantida como marcador de familia.
     */
    public static final boolean IS_DISCOVERABLE = false;




    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }


}

