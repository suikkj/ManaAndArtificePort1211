/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.SlotItemHandler
 */
package com.mna.gui.containers.slots;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public abstract class BaseSlot
extends SlotItemHandler {
    public BaseSlot(IItemHandler inv, int index, int xPosition, int yPosition) {
        super(inv, index, xPosition, yPosition);
    }

    public abstract boolean mayPlace(ItemStack var1);
}

