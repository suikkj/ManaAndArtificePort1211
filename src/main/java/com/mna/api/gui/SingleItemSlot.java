/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 */
package com.mna.api.gui;

import com.mna.gui.containers.slots.BaseSlot;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class SingleItemSlot
extends BaseSlot {
    private Item itemType;
    private int maxStackSize = -1;
    private boolean active = true;
    private Consumer<Integer> changedCallback;

    public SingleItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Item validItem) {
        super(itemHandler, index, xPosition, yPosition);
        this.itemType = validItem;
    }

    public SingleItemSlot setMaxStackSize(int stackSize) {
        this.maxStackSize = stackSize;
        return this;
    }

    public int getMaxStackSize() {
        return this.maxStackSize > 0 ? this.maxStackSize : super.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack stack) {
        return this.maxStackSize > 0 ? this.maxStackSize : super.getMaxStackSize(stack);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.itemType == null) {
            return true;
        }
        boolean valid = stack.getItem().equals(this.itemType);
        return valid;
    }

    public void addChangeListener(Consumer<Integer> callback) {
        this.changedCallback = callback;
    }

    public void setChanged() {
        super.setChanged();
        if (this.changedCallback != null) {
            this.changedCallback.accept(this.getSlotIndex());
        }
    }

    public ItemStack remove(int amount) {
        if (this.changedCallback != null) {
            this.changedCallback.accept(this.getSlotIndex());
        }
        return super.remove(amount);
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

