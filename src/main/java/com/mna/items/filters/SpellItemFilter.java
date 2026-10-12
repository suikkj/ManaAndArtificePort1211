/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.api.spells.ICanContainSpell;
import com.mna.items.ItemInit;
import com.mna.items.filters.ItemFilter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpellItemFilter
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        ICanContainSpell inter;
        Item item = stack.getItem();
        return item instanceof ICanContainSpell && (inter = (ICanContainSpell)item).canAcceptSpell(stack) && stack.getItem() != ItemInit.ENCHANTED_VELLUM.get();
    }
}

