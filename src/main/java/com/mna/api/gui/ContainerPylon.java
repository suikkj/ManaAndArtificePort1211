/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.wrapper.InvWrapper
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.mna.api.gui;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.gui.SingleItemSlot;
import java.util.HashMap;
import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class ContainerPylon
extends AbstractContainerMenu {
    protected final PylonTileBase tile;
    protected final IItemHandler tileItemHandler;
    protected final Player user;

    public ContainerPylon(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, (PylonTileBase)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));
    }

    public ContainerPylon(int i, Inventory playerInv, PylonTileBase tile) {
        super(ManaAndArtificeMod.getContainerHelper().GetPylonContainerType(), i);
        this.user = playerInv.player;
        this.tile = tile;
        this.tileItemHandler = new InvWrapper((Container)tile);
        this.initializeSlots(playerInv);
    }

    private void initializeSlots(Inventory playerInventory) {
        int xpos;
        Item markingRune = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "rune_marking"));
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 0, 100, 19, markingRune).setMaxStackSize(1));
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 1, 118, 19, markingRune).setMaxStackSize(1));
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                this.addSlot(new Slot((Container)playerInventory, ypos + xpos * 9 + 9, this.playerInventoryXStart() + ypos * 18, this.playerInventoryYStart() + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)playerInventory, xpos, this.playerInventoryXStart() + xpos * 18, this.playerInventoryYStart() + 58));
        }
    }

    protected int playerInventoryXStart() {
        return 37;
    }

    protected int playerInventoryYStart() {
        return 73;
    }

    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.tile.getContainerSize() ? !this.moveItemStackTo(itemstack1, this.tile.getContainerSize(), this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, this.tile.getContainerSize(), false)) {
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

    public boolean stillValid(Player pPlayer) {
        return pPlayer != null && pPlayer.distanceToSqr(Vec3.atCenterOf((Vec3i)this.tile.getBlockPos())) < 64.0;
    }

    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        return this.tile.powerRequirementStatus();
    }

    public boolean isRedstonePowered() {
        return this.tile.isRedstonePowered();
    }

    public boolean affectFactionOnly() {
        return this.tile.isFactionOnly();
    }

    public int getRadius() {
        return this.tile.getRadius();
    }

    public float getMultiplier() {
        return this.tile.getPowerMultiplier();
    }

    @Nullable
    public String getErrorMessage() {
        return this.tile.getErrorMessage();
    }

    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0: {
                this.tile.setFactionOnly(!this.tile.isFactionOnly());
                break;
            }
            case 1: {
                int r = this.tile.getRadius();
                if (r < 16) {
                    r = 0;
                }
                this.tile.setRadius(r + 16);
                break;
            }
            case 2: {
                this.tile.setRadius(this.tile.getRadius() - 16);
            }
        }
        return true;
    }

    public float getPowerRequestRate(Affinity affinity) {
        return this.tile.getPowerRequestRate(affinity);
    }

    public String getFactionCheckboxTooltip() {
        return this.tile.getFactionCheckboxTooltip();
    }
}


