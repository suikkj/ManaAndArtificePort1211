/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.items.inventory;

import com.mna.api.items.inventory.ISpellBookInventory;
import com.mna.inventory.InventorySpellBook;
import java.util.ArrayList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class SpellInventory
extends SimpleContainer
implements ISpellBookInventory {
    public SpellInventory() {
        this(8);
    }

    public SpellInventory(int size) {
        super(size);
    }

    public void copyTo(ItemStack spellBook) {
        InventorySpellBook book = new InventorySpellBook(spellBook);
        for (int i = 0; i < book.getSlots(); ++i) {
            this.setItem(i, book.getStackInSlot(i));
        }
        book.writeItemStack();
    }

    @Override
    public ItemStack[] getActiveSpells() {
        ArrayList<ItemStack> activeSpells = new ArrayList<ItemStack>();
        for (int i = 0; i < this.getContainerSize(); ++i) {
            activeSpells.add(this.getItem(i));
        }
        return (ItemStack[])((activeSpells).toArray(new ItemStack[0]));
    }
}

