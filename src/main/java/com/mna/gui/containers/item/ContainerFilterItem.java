/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.DataSlot
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.containers.item;

import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.slots.GhostSlot;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.artifice.Manifest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFilterItem
extends AbstractContainerMenu {
    public ItemStack heldItem;
    private ItemInventoryBase inventory;
    public int bagHash;
    protected int mySlot;
    private int myPlayerIndex;
    private final DataSlot matchDurability = DataSlot.standalone();
    private final DataSlot matchTag = DataSlot.standalone();

    public ContainerFilterItem(MenuType<?> type, int id, Inventory playerInv, ItemInventoryBase inventory) {
        super(type, id);
        this.inventory = inventory;
        this.bagHash = inventory.getStack().hashCode();
        this.myPlayerIndex = playerInv.selected;
        this.heldItem = inventory.getStack();
        this.matchDurability.set(((Manifest)ItemInit.FILTER_ITEM.get()).getMatchDurability(this.heldItem) ? 1 : 0);
        this.matchTag.set(((Manifest)ItemInit.FILTER_ITEM.get()).getMatchTag(this.heldItem) ? 1 : 0);
        this.initializeSlots(playerInv);
    }

    public ContainerFilterItem(int i, Inventory playerInv, FriendlyByteBuf buffer) {
        this((MenuType)ContainerInit.FILTER_ITEM.get(), i, playerInv, new ItemInventoryBase(playerInv.getItem(playerInv.selected), 24));
    }

    protected void initializeSlots(Inventory playerInv) {
        int col;
        int slotIndex = 0;
        int bagSlotIndex = 0;
        this.addDataSlot(this.matchDurability);
        this.addDataSlot(this.matchTag);
        for (col = 0; col < 6; ++col) {
            for (int row = 0; row < 4; ++row) {
                this.addSlot((Slot)new GhostSlot(this.inventory, bagSlotIndex++, 35 + col * 18, 7 + row * 18));
                ++slotIndex;
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int col2 = 0; col2 < 9; ++col2) {
                this.addSlot(new Slot((Container)playerInv, col2 + row * 9 + 9, 8 + col2 * 18, 106 + row * 18));
                ++slotIndex;
            }
        }
        for (col = 0; col < 9; ++col) {
            if (col == playerInv.selected) {
                this.mySlot = slotIndex;
            }
            this.addSlot(new Slot((Container)playerInv, col, 8 + col * 18, 164));
            ++slotIndex;
        }
    }

    public boolean stillValid(Player p_38874_) {
        return true;
    }

    public void broadcastChanges() {
        super.broadcastChanges();
        this.inventory.writeItemStack();
    }

    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == this.mySlot || clickTypeIn == ClickType.SWAP && dragType == this.myPlayerIndex) {
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public void removed(Player p_38940_) {
        if (!p_38940_.level().isClientSide()) {
            ((Manifest)ItemInit.FILTER_ITEM.get()).setMatchDurability(this.inventory.getStack(), this.getDurabilityMatching());
            ((Manifest)ItemInit.FILTER_ITEM.get()).setMatchTag(this.inventory.getStack(), this.getTagMatching());
        }
        super.removed(p_38940_);
    }

    public boolean clickMenuButton(Player player, int index) {
        if (index == 0) {
            this.matchDurability.set(this.getDurabilityMatching() ? 0 : 1);
        } else if (index == 1) {
            this.matchTag.set(this.getTagMatching() ? 0 : 1);
        }
        this.broadcastChanges();
        return true;
    }

    public boolean getDurabilityMatching() {
        return this.matchDurability.get() == 1;
    }

    public boolean getTagMatching() {
        return this.matchTag.get() == 1;
    }

    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = (Slot)this.slots.get(index);
        int mainSlots = 24;
        if (slot != null && slot.hasItem()) {
            ItemStack shiftClickedStack = slot.getItem();
            if (index > mainSlots) {
                this.moveItemStackTo(shiftClickedStack, 0, mainSlots, false);
            }
        }
        return ItemStack.EMPTY;
    }

    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean wrap) {
        boolean flag = false;
        int i = startIndex;
        if (wrap) {
            i = endIndex - 1;
        }
        i = wrap ? endIndex - 1 : startIndex;
        while (!(!wrap ? i >= endIndex : i < startIndex)) {
            Slot slot = (Slot)this.slots.get(i);
            ItemStack slotStack = slot.getItem();
            if (slotStack.isEmpty() && (slot instanceof GhostSlot || slot.mayPlace(stack))) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                slot.set(copy);
                slot.setChanged();
                flag = true;
                break;
            }
            if (wrap) {
                --i;
                continue;
            }
            ++i;
        }
        return flag;
    }
}

