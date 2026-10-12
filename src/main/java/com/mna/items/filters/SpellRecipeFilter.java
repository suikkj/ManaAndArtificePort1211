/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.items.ItemInit;
import com.mna.items.filters.ItemFilter;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.world.item.ItemStack;

public class SpellRecipeFilter
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        return stack.getItem() == ItemInit.ENCHANTED_VELLUM.get() && SpellRecipe.stackContainsSpell(stack);
    }
}

