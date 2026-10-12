/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickType
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
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;

public class ContainerMarkBook
extends HeldContainerBase {
    public ContainerMarkBook(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, new ItemInventoryBase(new ItemStack((ItemLike)ItemInit.BOOK_MARKS.get(), 16), 16));
    }

    public ContainerMarkBook(int i, Inventory playerInv, ItemInventoryBase basebag) {
        super((MenuType)ContainerInit.MARK_BOOK.get(), i, playerInv, basebag);
    }

    @Override
    protected void initializeSlots(Inventory playerInv) {
        int slotIndex = 0;
        for (int row = 0; row < this.numRows(); ++row) {
            for (int col = 0; col < this.slotsPerRow(); ++col) {
                this.addSlot((Slot)this.slot((IItemHandler)this.inventory, col + row * this.slotsPerRow(), 18 + row * 115, 7 + col * 18));
                ++slotIndex;
            }
        }
        int i = (this.slotsPerRow() - 4) * 18;
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
        return new SingleItemSlot(inv, index, x, y, (Item)ItemInit.RUNE_MARKING.get());
    }

    @Override
    protected int slotsPerRow() {
        return 8;
    }

    @Override
    protected int numRows() {
        return 2;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        try {
            if (this.getSlot(slotId).getItem().hashCode() == this.bagHash) {
                return;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.numRows() * this.slotsPerRow() ? !this.moveItemStackTo(itemstack1, this.numRows() * this.slotsPerRow(), this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, this.numRows() * this.slotsPerRow(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}

