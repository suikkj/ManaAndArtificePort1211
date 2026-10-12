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
 *  net.minecraft.world.item.Items
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.wrapper.InvWrapper
 */
package com.mna.gui.containers.block;

import com.mna.api.gui.SingleItemSlot;
import com.mna.blocks.tileentities.wizard_lab.RunescribingTableTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.slots.ItemFilterSlot;
import com.mna.items.ItemInit;
import com.mna.items.filters.RuneHammerFilter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class ContainerRunescribingTable
extends AbstractContainerMenu {
    private final Container inventory;
    private final RunescribingTableTile table;
    static int rowLength = 9;
    static int rowCount = 3;

    public ContainerRunescribingTable(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, ((RunescribingTableTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos())).readFrom(packetBuffer));
    }

    public ContainerRunescribingTable(int windowId, Inventory playerInventory, RunescribingTableTile inventory) {
        super((MenuType)ContainerInit.RUNESCRIBING_TABLE.get(), windowId);
        int xpos;
        this.inventory = inventory;
        this.table = inventory;
        inventory.startOpen(playerInventory.player);
        InvWrapper iWrapper = new InvWrapper((Container)inventory);
        this.addSlot((Slot)new ItemFilterSlot((IItemHandler)iWrapper, 0, 8, 8, new RuneHammerFilter()).setMaxStackSize(1));
        this.addSlot((Slot)new SingleItemSlot((IItemHandler)iWrapper, 1, 8, 32, (Item)ItemInit.RUNESMITH_CHISEL.get()).setMaxStackSize(1));
        this.addSlot((Slot)new SingleItemSlot((IItemHandler)iWrapper, 2, 174, 130, (Item)ItemInit.RUNE_PATTERN.get()).setMaxStackSize(1));
        this.addSlot((Slot)new SingleItemSlot((IItemHandler)iWrapper, 3, 251, 8, (Item)ItemInit.RECIPE_SCRAP_RUNESCRIBING.get()).setMaxStackSize(1));
        this.addSlot((Slot)new SingleItemSlot((IItemHandler)iWrapper, 4, 8, 56, Items.CLAY_BALL));
        int slotRowStartX = 215;
        int slotColStep = 18;
        int slotRowY = 138;
        for (int pSlotRow = 0; pSlotRow < 5; ++pSlotRow) {
            int slotRowX = 215;
            for (int pSlotCol = 0; pSlotCol < 5; ++pSlotCol) {
                int idx = pSlotRow * 5 + pSlotCol;
                this.addSlot((Slot)new SingleItemSlot((IItemHandler)iWrapper, 5 + idx, slotRowX, slotRowY, (Item)ItemInit.RECIPE_SCRAP_RUNESCRIBING.get()));
                slotRowX += 18;
            }
            slotRowX = 215;
            slotRowY += 18;
        }
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                this.addSlot(new Slot((Container)playerInventory, ypos + xpos * 9 + 9, 19 + ypos * 18, 156 + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)playerInventory, xpos, 19 + xpos * 18, 214));
        }
    }

    public boolean stillValid(Player playerIn) {
        return this.inventory.stillValid(playerIn);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot == null || !slot.hasItem()) return itemstack;
        ItemStack itemstack1 = slot.getItem();
        itemstack = itemstack1.copy();
        if (index < 30) {
            if (index == 3) {
                if (!this.moveItemStackTo(itemstack1, 5, 30, false) && !this.moveItemStackTo(itemstack1, 30, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 5 && index <= 29) {
                if (this.moveItemStackTo(itemstack1, 3, 4, true)) return ItemStack.EMPTY;
                if (!this.moveItemStackTo(itemstack1, 30, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 30, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(itemstack1, 0, 30, false)) {
            return ItemStack.EMPTY;
        }
        if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
            return itemstack;
        } else {
            slot.setChanged();
        }
        return itemstack;
    }

    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.inventory.stopOpen(playerIn);
    }

    public void writeMutex(long mutex_h, long mutex_v, Player player, int playerTier, boolean isRemove) {
        this.table.writeMutexChanges(mutex_h, mutex_v, player, playerTier, isRemove, player.level().getRandom());
    }

    public boolean hasRequiredItems() {
        return this.table.hasRequiredItems(true);
    }

    public boolean hasPattern() {
        return this.table.hasRequiredItems(false);
    }

    public long getHMutex() {
        return this.table.getHMutex();
    }

    public long getVMutex() {
        return this.table.getVMutex();
    }

    public boolean canUndo(Player player) {
        return this.table.canUndo(player);
    }
}

