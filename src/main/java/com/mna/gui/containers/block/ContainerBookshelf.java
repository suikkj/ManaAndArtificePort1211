/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.containers.block;

import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.blocks.tileentities.BookshelfTile;
import com.mna.gui.containers.ContainerInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerBookshelf
extends AbstractContainerMenu {
    private final TileEntityWithInventory bookshelf;
    public static int SIZE = 15;

    public ContainerBookshelf(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, (BookshelfTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));
    }

    public ContainerBookshelf(int id, Inventory playerInventory, BookshelfTile bookshelfTile) {
        super((MenuType)ContainerInit.BOOKSHELF.get(), id);
        int xpos;
        this.bookshelf = bookshelfTile;
        int slotIndex = 0;
        for (int col = 0; col < 5; ++col) {
            for (int row = 0; row < 3; ++row) {
                this.addSlot(new Slot((Container)this.bookshelf, slotIndex++, 44 + col * 18, 11 + row * 22));
            }
        }
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                this.addSlot(new Slot((Container)playerInventory, ypos + xpos * 9 + 9, 8 + ypos * 18, 80 + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)playerInventory, xpos, 8 + xpos * 18, 138));
        }
    }

    public boolean stillValid(Player playerIn) {
        return this.bookshelf.stillValid(playerIn);
    }

    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < SIZE ? !this.moveItemStackTo(itemstack1, SIZE, this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, SIZE, false)) {
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

    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (!this.bookshelf.getLevel().isClientSide()) {
            this.bookshelf.getLevel().sendBlockUpdated(this.bookshelf.getBlockPos(), this.bookshelf.getBlockState(), this.bookshelf.getBlockState(), 3);
        }
    }
}

