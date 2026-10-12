/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.items.IItemHandler
 */
package com.mna.gui.containers.item;

import com.mna.api.gui.SingleItemSlot;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.HeldContainerBase;
import com.mna.gui.containers.slots.BaseSlot;
import com.mna.inventory.InventorySpellBook;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.SpellBook;
import com.mna.network.ClientMessageDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;

public class ContainerSpellBook
extends HeldContainerBase {
    public ContainerSpellBook(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, new InventorySpellBook(new ItemStack((ItemLike)ItemInit.SPELL_BOOK.get())));
    }

    public ContainerSpellBook(int i, Inventory playerInv, ItemInventoryBase basebag) {
        super((MenuType)ContainerInit.SPELL_BOOK.get(), i, playerInv, basebag);
    }

    @Override
    protected void initializeSlots(Inventory playerInv) {
        int slotIndex = 0;
        for (int k = 0; k < this.slotsPerRow(); ++k) {
            for (int j = 0; j < this.numRows(); ++j) {
                if (k % this.slotsPerRow() == 0) {
                    this.addSlot((Slot)this.slot((IItemHandler)this.inventory, k + j * this.slotsPerRow(), 18 + k * 18, 7 + j * 18));
                } else {
                    this.addSlot((Slot)this.slot((IItemHandler)this.inventory, k + j * this.slotsPerRow(), 104 + k * 29, 7 + j * 18));
                }
                ++slotIndex;
            }
        }
        int i = (this.numRows() - 4) * 18;
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot((Container)playerInv, j1 + l * 9 + 9, 48 + j1 * 18, 102 + l * 18 + i));
                ++slotIndex;
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            if (i1 == playerInv.selected) {
                this.mySlot = slotIndex;
            }
            this.addSlot(new Slot((Container)playerInv, i1, 48 + i1 * 18, 160 + i));
            ++slotIndex;
        }
    }

    @Override
    public BaseSlot slot(IItemHandler inv, int index, int x, int y) {
        return new SingleItemSlot(inv, index, x, y, (Item)ItemInit.SPELL.get());
    }

    @Override
    protected int slotsPerRow() {
        return 5;
    }

    @Override
    protected int numRows() {
        return 8;
    }

    public void onClose() {
        int curSlot = SpellBook.getActiveSpellSlot(this.inventory.getStack());
        ClientMessageDispatcher.sendSpellBookSlotChange(curSlot, InteractionHand.MAIN_HAND, null);
    }
}

