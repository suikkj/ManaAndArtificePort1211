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
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.wrapper.InvWrapper
 */
package com.mna.gui.containers.block;

import com.mna.api.gui.SingleItemSlot;
import com.mna.blocks.tileentities.SeerStoneTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.slots.ItemFilterSlot;
import com.mna.items.ItemInit;
import com.mna.items.filters.SeerStoneItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class ContainerSeerStone
extends AbstractContainerMenu {
    private final SeerStoneTile seer;
    public static int SIZE = 36;

    public ContainerSeerStone(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, (SeerStoneTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));
    }

    public ContainerSeerStone(int id, Inventory playerInventory, SeerStoneTile seerStoneTile) {
        super((MenuType)ContainerInit.SEER_STONE.get(), id);
        int xpos;
        int row;
        int col;
        this.seer = seerStoneTile;
        InvWrapper wrapper = new InvWrapper((Container)this.seer);
        int slotIndex = 0;
        for (col = 0; col < 9; ++col) {
            for (row = 0; row < 2; ++row) {
                this.addSlot((Slot)new ItemFilterSlot((IItemHandler)wrapper, slotIndex++, 37 + col * 18, 19 + row * 18, new SeerStoneItems()));
            }
        }
        for (col = 0; col < 9; ++col) {
            for (row = 0; row < 2; ++row) {
                this.addSlot((Slot)new ItemFilterSlot((IItemHandler)wrapper, slotIndex++, 37 + col * 18, 57 + row * 18, new SeerStoneItems()));
            }
        }
        this.addSlot((Slot)new SingleItemSlot((IItemHandler)wrapper, slotIndex++, 100, 95, (Item)ItemInit.RUNE_MARKING.get()));
        this.addSlot((Slot)new SingleItemSlot((IItemHandler)wrapper, slotIndex++, 118, 95, (Item)ItemInit.RUNE_MARKING.get()));
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                this.addSlot(new Slot((Container)playerInventory, ypos + xpos * 9 + 9, 37 + ypos * 18, 127 + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)playerInventory, xpos, 37 + xpos * 18, 185));
        }
    }

    public boolean stillValid(Player playerIn) {
        return this.seer.stillValid(playerIn);
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
        if (!this.seer.getLevel().isClientSide()) {
            this.seer.buildFilters();
            this.seer.getLevel().sendBlockUpdated(this.seer.getBlockPos(), this.seer.getBlockState(), this.seer.getBlockState(), 3);
        }
    }
}

