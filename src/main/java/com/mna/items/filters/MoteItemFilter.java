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
import java.util.ArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MoteItemFilter
extends ItemFilter {
    private ArrayList<Item> validItems = new ArrayList();

    public MoteItemFilter() {
        this.validItems.add((Item)ItemInit.MOTE_AIR.get());
        this.validItems.add((Item)ItemInit.MOTE_ARCANE.get());
        this.validItems.add((Item)ItemInit.MOTE_EARTH.get());
        this.validItems.add((Item)ItemInit.MOTE_ENDER.get());
        this.validItems.add((Item)ItemInit.MOTE_FIRE.get());
        this.validItems.add((Item)ItemInit.MOTE_WATER.get());
        this.validItems.add((Item)ItemInit.GREATER_MOTE_AIR.get());
        this.validItems.add((Item)ItemInit.GREATER_MOTE_ARCANE.get());
        this.validItems.add((Item)ItemInit.GREATER_MOTE_EARTH.get());
        this.validItems.add((Item)ItemInit.GREATER_MOTE_ENDER.get());
        this.validItems.add((Item)ItemInit.GREATER_MOTE_FIRE.get());
        this.validItems.add((Item)ItemInit.GREATER_MOTE_WATER.get());
    }

    @Override
    public boolean IsValidItem(ItemStack stack) {
        return this.validItems.contains(stack.getItem());
    }
}

