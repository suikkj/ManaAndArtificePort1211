/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.inventory;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.ManaAndArtificeMod;
import com.mna.inventory.stack_extension.AbstractItemHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import com.mna.api.tools.MNAItemData;

public class ItemInventoryBase
extends AbstractItemHandler {
    protected final ItemStack stack;
    private static final String TAG_ITEMS = "Items";

    public ItemInventoryBase(ItemStack stack) {
        super(54);
        this.stack = stack;
        this.setup();
    }

    public ItemInventoryBase(ItemStack stack, int numSlots) {
        super(numSlots);
        this.stack = stack;
        this.setup();
    }

    public void setup() {
        this.readItemStack();
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void readItemStack() {
        if (this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() != null && this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(TAG_ITEMS)) {
            if (this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(TAG_ITEMS).contains("slots")) {
                this.deserialize(this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(TAG_ITEMS));
            } else {
                this.readNBT(this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            }
        }
    }

    public void writeItemStack() {
        if (this.isEmpty()) {
            CompoundTag __t = this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            __t.remove(TAG_ITEMS);
            this.stack.set(DataComponents.CUSTOM_DATA, CustomData.of(__t));
        } else {
            CompoundTag tag = this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.put(TAG_ITEMS, (Tag)this.serialize());
        }
    }

    protected boolean isSameItem(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && ManaAndArtificeMod.getItemHelper().AreTagsEqual(a, b);
    }

    protected void moveItemToEmptySlots(ItemStack stack) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemstack = this.getStackInSlot(i);
            if (!itemstack.isEmpty()) continue;
            this.setStackInSlot(i, stack.copy());
            stack.setCount(0);
            return;
        }
    }

    protected void moveItemToOccupiedSlotsWithSameType(ItemStack stack) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemstack = this.getStackInSlot(i);
            if (!this.isSameItem(itemstack, stack)) continue;
            this.moveItemsBetweenStacks(stack, itemstack, i);
            if (!stack.isEmpty()) continue;
            return;
        }
    }

    protected void moveItemsBetweenStacks(ItemStack source, ItemStack dest, int slot) {
        int i = Math.min(this.getSlotLimit(slot, dest), dest.getMaxStackSize());
        int j = Math.min(source.getCount(), i - dest.getCount());
        if (j > 0) {
            dest.grow(j);
            source.shrink(j);
            this.markDirty();
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    private void readNBT(CompoundTag compound) {
        ManaAndArtifice.LOGGER.info("Upconverting old ritual kit to Practitioner's Pouch");
        NonNullList list = NonNullList.withSize((int)this.getSlots(), ItemStack.EMPTY);
        MNAItemData.loadItems((CompoundTag)compound, (NonNullList)list);
        int mySlotIndex = 0;
        for (int i = 0; i < list.size(); ++i) {
            if (((ItemStack)list.get(i)).isEmpty()) continue;
            this.setStackInSlot(mySlotIndex++, (ItemStack)list.get(i));
        }
        this.writeItemStack();
    }

    public NonNullList<ItemStack> getNonAirItems() {
        NonNullList items = NonNullList.create();
        this.stacks.forEach(is -> {
            if (!is.isEmpty()) {
                items.add(is.getStackOriginal());
            }
        });
        return items;
    }

    public NonNullList<ItemStack> getAllItems() {
        NonNullList items = NonNullList.create();
        this.stacks.forEach(is -> items.add(is.getStackOriginal()));
        return items;
    }
}

