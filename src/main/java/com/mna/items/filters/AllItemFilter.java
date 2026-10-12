/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.items.filters.ItemFilter;
import net.minecraft.world.item.ItemStack;

public class AllItemFilter
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        return true;
    }
}

