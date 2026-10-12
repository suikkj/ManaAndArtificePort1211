/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.SlotItemHandler
 */
package com.mna.gui.containers.slots;

import com.mna.gui.containers.IExtendedItemHandler;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ExtendedItemStackSlot
extends SlotItemHandler {
    private final IExtendedItemHandler inventory;
    private final int index;
    private List<Item> blacklistedItems;

    public ExtendedItemStackSlot(IExtendedItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super((IItemHandler)itemHandler, index, xPosition, yPosition);
        this.index = index;
        this.inventory = itemHandler;
        this.blacklistedItems = new ArrayList<Item>();
    }

    public ExtendedItemStackSlot blacklistItem(Item item) {
        this.blacklistedItems.add(item);
        return this;
    }

    public boolean mayPlace(ItemStack stack) {
        if (this.blacklistedItems.contains(stack.getItem()) || !stack.getItem().canFitInsideContainerItems()) {
            return false;
        }
        return super.mayPlace(stack);
    }

    public boolean mayPickup(Player playerIn) {
        return true;
    }

    public int getMaxStackSize(@Nonnull ItemStack stack) {
        if (!stack.isStackable()) {
            return 1;
        }
        return this.inventory.getSlotLimit(this.index);
    }

    public boolean isSameInventory(Slot other) {
        return other instanceof ExtendedItemStackSlot && ((ExtendedItemStackSlot)other).getItemHandler() == this.getItemHandler();
    }

    public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {
        super.onQuickCraft(oldStackIn, newStackIn);
        this.setChanged();
    }

    public void setChanged() {
        this.inventory.markDirty();
    }
}

