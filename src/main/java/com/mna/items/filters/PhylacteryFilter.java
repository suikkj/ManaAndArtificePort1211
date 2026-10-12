/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.api.items.IPhylacteryItem;
import com.mna.items.filters.ItemFilter;
import net.minecraft.world.item.ItemStack;

public class PhylacteryFilter
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        return stack.getItem() instanceof IPhylacteryItem && ((IPhylacteryItem)stack.getItem()).getFillPct(stack) > 0.0f;
    }
}

