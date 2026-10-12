/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.inventory;

import com.mna.inventory.ItemInventoryBase;
import com.mna.items.filters.BottledWeaveItemFilter;
import com.mna.items.filters.MarkingRuneFilter;
import com.mna.items.filters.MoteItemFilter;
import com.mna.items.filters.RuneItemFilter;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryRitualKit
extends ItemInventoryBase {
    public static final int GENERAL_INVENTORY_START = 0;
    public static final int GENERAL_INVENTORY_END = 20;
    public static final int CONVEYANCE_INVENTORY_START = 21;
    public static final int CONVEYANCE_INVENTORY_END = 21;
    public static final int GLYPH_INVENTORY_START = 22;
    public static final int GLYPH_INVENTORY_END = 37;
    public static final int MOTE_INVENTORY_START = 38;
    public static final int MOTE_INVENTORY_END = 49;
    public static final int WEAVE_INVENTORY_START = 50;
    public static final int WEAVE_INVENTORY_END = 61;
    public static final int VOID_INVENTORY_START = 62;
    public static final int VOID_INVENTORY_END = 77;
    private final int slotLimit;
    public final String curioSlotIdentifier;

    public InventoryRitualKit(ItemStack bag) {
        this(bag, null);
    }

    public InventoryRitualKit(ItemStack bag, String curioSlotIdentifier) {
        super(bag, 78);
        this.curioSlotIdentifier = curioSlotIdentifier;
        int maxSize = ((PractitionersPouch)bag.getItem()).getPatchLevel(bag, PractitionersPouchPatches.DEPTH);
        this.slotLimit = maxSize >= 2 ? 2048 : (maxSize >= 1 ? 256 : 64);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot >= 62 && slot <= 77) {
            return 1;
        }
        if (slot == 21) {
            return 1;
        }
        return this.slotLimit;
    }

    @Override
    public int getSlotLimit(int slot, ItemStack stack) {
        return stack.isStackable() ? this.getSlotLimit(slot) : 1;
    }

    private boolean isPatchEnabled(PractitionersPouchPatches patch) {
        return ((PractitionersPouch)this.getStack().getItem()).getPatchLevel(this.getStack(), patch) > 0;
    }

    public boolean canAddItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.CONVEYANCE) && new MarkingRuneFilter().IsValidItem(stack) && this.canAddItem(stack, 21, 21)) {
            return true;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.GLYPH) && new RuneItemFilter().IsValidItem(stack) && this.canAddItem(stack, 22, 37)) {
            return true;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.GLYPH) && new MoteItemFilter().IsValidItem(stack) && this.canAddItem(stack, 38, 49)) {
            return true;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.GLYPH) && new BottledWeaveItemFilter().IsValidItem(stack) && this.canAddItem(stack, 50, 61)) {
            return true;
        }
        return this.canAddItem(stack, 0, 20);
    }

    protected boolean canAddItem(ItemStack stack, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; ++i) {
            ItemStack invStack = this.getStackInSlot(i);
            if (!invStack.isEmpty() && (!this.isSameItem(invStack, stack) || invStack.getCount() >= invStack.getMaxStackSize())) continue;
            return true;
        }
        return false;
    }

    public boolean canMergeItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.CONVEYANCE) && new MarkingRuneFilter().IsValidItem(stack) && this.canAddItem(stack, 21, 21)) {
            return true;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.GLYPH) && new RuneItemFilter().IsValidItem(stack) && this.canAddItem(stack, 22, 37)) {
            return true;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.MOTE) && new MoteItemFilter().IsValidItem(stack) && this.canAddItem(stack, 38, 49)) {
            return true;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.WEAVE) && new BottledWeaveItemFilter().IsValidItem(stack) && this.canAddItem(stack, 50, 61)) {
            return true;
        }
        return this.canMergeItem(stack, 0, 20);
    }

    protected boolean canMergeItem(ItemStack stack, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; ++i) {
            ItemStack invStack = this.getStackInSlot(i);
            if (!this.isSameItem(invStack, stack) || invStack.getCount() >= this.getSlotLimit(i, invStack)) continue;
            return true;
        }
        return false;
    }

    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        ItemStack opStack = stack.copy();
        if (this.isPatchEnabled(PractitionersPouchPatches.CONVEYANCE) && new MarkingRuneFilter().IsValidItem(opStack) && (opStack = this.addItem(opStack, 21, 21)).isEmpty()) {
            return opStack;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.GLYPH) && new RuneItemFilter().IsValidItem(stack) && (opStack = this.addItem(opStack, 22, 37)).isEmpty()) {
            return opStack;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.MOTE) && new MoteItemFilter().IsValidItem(stack) && (opStack = this.addItem(opStack, 38, 49)).isEmpty()) {
            return opStack;
        }
        if (this.isPatchEnabled(PractitionersPouchPatches.WEAVE) && new BottledWeaveItemFilter().IsValidItem(stack) && (opStack = this.addItem(opStack, 50, 61)).isEmpty()) {
            return opStack;
        }
        if (this.shouldVoidItem(opStack = this.mergeItem(opStack, 0, 20))) {
            return ItemStack.EMPTY;
        }
        return opStack;
    }

    protected ItemStack mergeItem(ItemStack stack, int startSlot, int endSlot) {
        ItemStack itemstack = stack.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemstack, startSlot, endSlot);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return itemstack;
    }

    protected ItemStack addItem(ItemStack stack, int startSlot, int endSlot) {
        ItemStack itemstack = stack.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemstack, startSlot, endSlot);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.moveItemToEmptySlots(itemstack, startSlot, endSlot);
        return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
    }

    protected void moveItemToOccupiedSlotsWithSameType(ItemStack stack, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; ++i) {
            ItemStack itemstack = this.getStackInSlot(i);
            if (!this.isSameItem(itemstack, stack)) continue;
            this.moveItemsBetweenStacks(stack, itemstack, i);
            if (!stack.isEmpty()) continue;
            return;
        }
    }

    protected void moveItemToEmptySlots(ItemStack stack, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; ++i) {
            ItemStack itemstack = this.getStackInSlot(i);
            if (!itemstack.isEmpty()) continue;
            this.setStackInSlot(i, stack.copy());
            stack.setCount(0);
            return;
        }
    }

    public boolean containsItem(ItemStack stack, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; ++i) {
            ItemStack invStack = this.getStackInSlot(i);
            if (!this.isSameItem(invStack, stack)) continue;
            return true;
        }
        return false;
    }

    public boolean shouldVoidItem(ItemStack stack) {
        if (!this.isPatchEnabled(PractitionersPouchPatches.VOID)) {
            return false;
        }
        return this.containsItem(stack, 62, 77);
    }

    @Override
    protected void moveItemsBetweenStacks(ItemStack source, ItemStack dest, int slot) {
        int i = this.getSlotLimit(slot, dest);
        int j = Math.min(source.getCount(), i - dest.getCount());
        if (j > 0) {
            dest.grow(j);
            source.shrink(j);
            this.markDirty();
        }
    }

    public int countItem(Item item) {
        int count = 0;
        for (int i = 0; i < this.getSlots(); ++i) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack.getItem() != item) continue;
            count += stack.getCount();
        }
        return count;
    }
}

