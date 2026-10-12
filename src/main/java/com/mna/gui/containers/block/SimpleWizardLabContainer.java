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

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import java.util.HashMap;
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

public abstract class SimpleWizardLabContainer<T extends AbstractContainerMenu, V extends WizardLabTile>
extends AbstractContainerMenu {
    protected boolean closed = false;
    protected final V tile;
    protected final IItemHandler tileItemHandler;
    protected final Player user;

    public SimpleWizardLabContainer(MenuType<? extends SimpleWizardLabContainer<?, ?>> type, int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(type, i, playerInventory, (V)(WizardLabTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));
    }

    public SimpleWizardLabContainer(MenuType<? extends SimpleWizardLabContainer<?, ?>> type, int i, Inventory playerInv, V tile) {
        super(type, i);
        this.user = playerInv.player;
        this.tile = tile;
        this.tileItemHandler = new InvWrapper(tile);
        this.initializeSlots(playerInv);
    }

    private void initializeSlots(Inventory playerInventory) {
        int xpos;
        this.addInventorySlots();
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                this.addSlot(new Slot((Container)playerInventory, ypos + xpos * 9 + 9, this.playerInventoryXStart() + ypos * 18, this.playerInventoryYStart() + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)playerInventory, xpos, this.playerInventoryXStart() + xpos * 18, this.playerInventoryYStart() + 58));
        }
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    protected int playerInventoryYStart() {
        return 88;
    }

    protected int playerInventoryXStart() {
        return 48;
    }

    protected final int getInventorySize() {
        return ((TileEntityWithInventory)(this.tile)).getContainerSize();
    }

    protected abstract int addInventorySlots();

    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.getInventorySize() ? !this.moveItemStackTo(itemstack1, this.getInventorySize(), this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, this.getInventorySize(), false)) {
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

    public boolean canActivate() {
        if (this.tile == null) {
            return false;
        }
        return ((WizardLabTile)this.tile).canStart(this.user);
    }

    public boolean clickMenuButton(Player player, int buttonIndex) {
        return ((WizardLabTile)this.tile).clickMenuButton(player, buttonIndex);
    }

    public float getProgress() {
        return ((WizardLabTile)this.tile).getPctComplete();
    }

    public boolean isActive() {
        return ((WizardLabTile)this.tile).isActive();
    }

    public int getXPCost() {
        return ((WizardLabTile)this.tile).getXPCost(this.user);
    }

    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        return ((WizardLabTile)this.tile).powerRequirementStatus();
    }
}

