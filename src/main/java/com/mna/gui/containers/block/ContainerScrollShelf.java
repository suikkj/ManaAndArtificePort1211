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
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.wrapper.InvWrapper
 */
package com.mna.gui.containers.block;

import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.blocks.tileentities.ScrollShelfTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.slots.AnyItemSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class ContainerScrollShelf
extends AbstractContainerMenu {
    private final TileEntityWithInventory scrollshelf;
    public static int SIZE = 81;

    public ContainerScrollShelf(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, (ScrollShelfTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));
    }

    public ContainerScrollShelf(int id, Inventory playerInventory, ScrollShelfTile scrollShelfTile) {
        super((MenuType)ContainerInit.SCROLLSHELF.get(), id);
        int xpos;
        this.scrollshelf = scrollShelfTile;
        InvWrapper wrapper = new InvWrapper((Container)this.scrollshelf);
        int slotIndex = 0;
        for (int row = 0; row < 7; ++row) {
            boolean shortRow = row % 2 != 0;
            int slotX = shortRow ? 21 : 12;
            int cols = shortRow ? 2 : 3;
            for (int col = 0; col < cols; ++col) {
                this.addSlot((Slot)new AnyItemSlot((IItemHandler)wrapper, slotIndex++, slotX + col * 18, 11 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            for (int row = 0; row < 7; ++row) {
                this.addSlot((Slot)new AnyItemSlot((IItemHandler)wrapper, slotIndex++, 84 + col * 18, 11 + row * 18));
            }
        }
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                this.addSlot(new Slot((Container)playerInventory, ypos + xpos * 9 + 9, 48 + ypos * 18, 144 + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)playerInventory, xpos, 48 + xpos * 18, 202));
        }
    }

    public boolean stillValid(Player playerIn) {
        return this.scrollshelf.stillValid(playerIn);
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
        if (!this.scrollshelf.getLevel().isClientSide()) {
            this.scrollshelf.getLevel().sendBlockUpdated(this.scrollshelf.getBlockPos(), this.scrollshelf.getBlockState(), this.scrollshelf.getBlockState(), 3);
        }
    }
}

