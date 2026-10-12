/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.inventory;

import com.mna.api.items.inventory.ISpellBookInventory;
import com.mna.inventory.ItemInventoryBase;
import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;

public class InventorySpellBook
extends ItemInventoryBase
implements ISpellBookInventory {
    public InventorySpellBook(ItemStack bag) {
        super(bag, 54);
    }

    @Override
    public ItemStack[] getActiveSpells() {
        ArrayList<ItemStack> activeSpells = new ArrayList<ItemStack>();
        for (int i = 0; i < 8; ++i) {
            activeSpells.add(this.getStackInSlot(i * 5));
        }
        return (ItemStack[])((activeSpells).toArray(new ItemStack[0]));
    }
}

