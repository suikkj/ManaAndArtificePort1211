/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 */
package com.mna.api.recipes;

import com.mna.api.recipes.IMARecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IRuneforgeEnchantRecipe
extends IMARecipe {
    public ItemStack getRune();

    public Enchantment getEnchant();

    public int getEnchantMagnitude();
}

