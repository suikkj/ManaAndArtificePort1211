/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 */
package com.mna.gui.containers;

import com.mna.gui.containers.slots.BaseSlot;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.base.IBagItem;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class HeldContainerBase
extends AbstractContainerMenu {
    public ItemInventoryBase inventory;
    public int bagHash;
    protected int mySlot;
    private int myPlayerIndex;

    public abstract BaseSlot slot(IItemHandler var1, int var2, int var3, int var4);

    public HeldContainerBase(@Nullable MenuType<?> type, int id, Inventory playerInv, ItemInventoryBase basebag) {
        super(type, id);
        this.inventory = basebag;
        this.bagHash = basebag.getStack().hashCode();
        this.myPlayerIndex = playerInv.selected;
        this.initializeSlots(playerInv);
    }

    protected abstract int slotsPerRow();

    protected abstract int numRows();

    protected void initializeSlots(Inventory playerInv) {
        int slotIndex = 0;
        for (int j = 0; j < this.numRows(); ++j) {
            for (int k = 0; k < this.slotsPerRow(); ++k) {
                this.addSlot((Slot)this.slot((IItemHandler)this.inventory, k + j * this.slotsPerRow(), 8 + k * 18, 8 + j * 18));
                ++slotIndex;
            }
        }
        int i = (this.numRows() - 4) * 18;
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot((Container)playerInv, j1 + l * 9 + 9, 8 + j1 * 18, 94 + l * 18 + i));
                ++slotIndex;
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            if (i1 == playerInv.selected) {
                this.mySlot = slotIndex;
            }
            this.addSlot(new Slot((Container)playerInv, i1, 8 + i1 * 18, 152 + i));
            ++slotIndex;
        }
    }

    public int size() {
        return this.numRows() * this.slotsPerRow();
    }

    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == this.mySlot || clickTypeIn == ClickType.SWAP && dragType == this.myPlayerIndex) {
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public void broadcastChanges() {
        super.broadcastChanges();
        this.inventory.writeItemStack();
    }

    public boolean stillValid(@Nonnull Player player) {
        ItemStack held = player.getMainHandItem();
        return held == this.inventory.getStack() && !this.inventory.getStack().isEmpty() && held.hashCode() == this.bagHash && held.getItem() instanceof IBagItem;
    }

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

