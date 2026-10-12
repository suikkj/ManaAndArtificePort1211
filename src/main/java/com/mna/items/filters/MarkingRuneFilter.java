/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.items.ItemInit;
import com.mna.items.filters.ItemFilter;
import com.mna.items.runes.ItemRuneMarking;
import net.minecraft.world.item.ItemStack;

public class MarkingRuneFilter
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        return stack.getItem() == ItemInit.RUNE_MARKING.get() && ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getLocation(stack) != null;
    }
}

