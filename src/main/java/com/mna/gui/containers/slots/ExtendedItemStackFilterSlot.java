/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.SlotItemHandler
 */
package com.mna.gui.containers.slots;

import com.mna.gui.containers.IExtendedItemHandler;
import com.mna.items.filters.ItemFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ExtendedItemStackFilterSlot
extends SlotItemHandler {
    private final IExtendedItemHandler inventory;
    private final int index;
    private ItemFilter itemType;
    private boolean active = true;

    public ExtendedItemStackFilterSlot(IExtendedItemHandler itemHandler, int index, int xPosition, int yPosition, ItemFilter validItems) {
        super((IItemHandler)itemHandler, index, xPosition, yPosition);
        this.index = index;
        this.inventory = itemHandler;
        this.itemType = validItems;
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
        return other instanceof ExtendedItemStackFilterSlot && ((ExtendedItemStackFilterSlot)other).getItemHandler() == this.getItemHandler();
    }

    public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {
        super.onQuickCraft(oldStackIn, newStackIn);
        this.setChanged();
    }

    public boolean mayPlace(ItemStack stack) {
        if (this.itemType.IsValidItem(stack)) {
            return super.mayPlace(stack);
        }
        return false;
    }

    public void setChanged() {
        this.inventory.markDirty();
    }

    @OnlyIn(value=Dist.CLIENT)
    public boolean isActive() {
        return this.active;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void setActive(boolean active) {
        this.active = active;
    }
}


