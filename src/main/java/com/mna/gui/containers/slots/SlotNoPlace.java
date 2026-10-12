/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 */
package com.mna.gui.containers.slots;

import com.mna.gui.containers.slots.BaseSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class SlotNoPlace
extends BaseSlot {
    public SlotNoPlace(IItemHandler craftResult, int index, int x, int y) {
        super(craftResult, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}

