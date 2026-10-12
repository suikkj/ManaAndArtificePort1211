/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantment$Rarity
 *  net.minecraft.world.item.enchantment.EnchantmentCategory
 */
package com.mna.enchantments.base;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public abstract class MAEnchantmentBase {
    /**
     * [PORT 1.21.1] Nao estende mais Enchantment (agora record imutavel).
     * As propriedades antigas viraram constantes de dados usadas por
     * MAEnchantmentBridge quando o Enchantment e construido em EnchantmentInit.
     */
    public static final boolean IS_DISCOVERABLE = false;

    public boolean isDiscoverable() {
        return IS_DISCOVERABLE;
    }



    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }


}

