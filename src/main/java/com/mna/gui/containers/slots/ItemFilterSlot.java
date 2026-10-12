/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.minecraftforge.items.IItemHandler
 */
package com.mna.gui.containers.slots;

import com.mna.gui.containers.slots.BaseSlot;
import com.mna.items.filters.ItemFilter;
import com.mna.items.filters.ItemFilterGroup;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemFilterSlot
extends BaseSlot {
    private ItemFilterGroup itemType;
    private int maxStackSize = -1;
    private Consumer<Integer> changedCallback;
    private boolean active = true;

    public ItemFilterSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, ItemFilter validItems) {
        super(itemHandler, index, xPosition, yPosition);
        this.itemType = new ItemFilterGroup(validItems);
    }

    public ItemFilterSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, ItemFilterGroup validItems) {
        super(itemHandler, index, xPosition, yPosition);
        this.itemType = validItems;
    }

    public ItemFilterSlot setMaxStackSize(int stackSize) {
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
        boolean valid = this.itemType.anyMatchesFilter(stack);
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

    @OnlyIn(value=Dist.CLIENT)
    public boolean isActive() {
        return this.active;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void setActive(boolean active) {
        this.active = active;
    }
}


