/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 */
package com.mna.items.filters;

import com.mna.tools.EnchantmentUtils;
import com.mna.api.tools.MATags;
import com.mna.items.filters.ItemFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantedRuneFilter
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        return MATags.isItemIn(stack.getItem(), MATags.Items.RUNES) && EnchantmentUtils.enchantmentMap((ItemStack)stack).size() > 0;
    }
}

