/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.items.ItemInit;
import com.mna.items.filters.ItemFilter;
import com.mna.items.ritual.PractitionersPouch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GroundRuneNonPlaceable
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        Item item = stack.getItem();
        return item == ItemInit.WIZARD_CHALK.get() || item == ItemInit.PURIFIED_VINTEUM_DUST.get() || item instanceof PractitionersPouch;
    }
}

