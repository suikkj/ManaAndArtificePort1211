/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.gui.containers.item;

import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.LargeContainer;
import com.mna.gui.containers.slots.ExtendedItemStackFilterSlot;
import com.mna.gui.containers.slots.ExtendedItemStackSlot;
import com.mna.interop.CuriosInterop;
import com.mna.inventory.InventoryRitualKit;
import com.mna.items.ItemInit;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import java.util.ArrayList;
import java.util.HashMap;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ContainerPractitionersPouch
extends LargeContainer {
    private InventoryRitualKit inventory;
    public int bagHash;
    protected int mySlot;
    private int myPlayerIndex;
    private HashMap<PractitionersPouchPatches, ArrayList<ExtendedItemStackFilterSlot>> patchSlots;
    private HashMap<Integer, Boolean> slotsActive;

    public ContainerPractitionersPouch(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, buffer.readBoolean() ? new InventoryRitualKit(CuriosInterop.GetNthCurio((LivingEntity)playerInventory.player, buffer.readUtf(), buffer.readInt())) : new InventoryRitualKit(playerInventory.getItem(playerInventory.selected)));
    }

    public ContainerPractitionersPouch(int windowID, Inventory playerInv, InventoryRitualKit kitInv) {
        super((MenuType)ContainerInit.RITUAL_KIT.get(), windowID, playerInv, kitInv);
        this.inventory = kitInv;
        this.bagHash = kitInv.getStack().hashCode();
        this.myPlayerIndex = kitInv.curioSlotIdentifier != null ? -1 : playerInv.selected;
        this.patchSlots = new HashMap();
        this.slotsActive = new HashMap();
        this.initializeSlots(playerInv);
    }

    protected void initializeSlots(Inventory playerInv) {
        int col;
        int slotIndex = 0;
        int bagSlotIndex = 0;
        for (int row = 0; row < 3; ++row) {
            for (col = 0; col < 7; ++col) {
                this.addSlot((Slot)new ExtendedItemStackSlot(this.inventory, bagSlotIndex++, 18 + col * 18, 75 + row * 18).blacklistItem((Item)ItemInit.PRACTITIONERS_POUCH.get()).blacklistItem((Item)ItemInit.MITHIONS_MAGNIFICENT_MBAG.get()).blacklistItem(Items.SHULKER_BOX));
                this.slotsActive.put(slotIndex++, true);
            }
        }
        PractitionersPouchPatches[] patches = PractitionersPouchPatches.values();
        for (int i = 0; i < patches.length; ++i) {
            PractitionersPouchPatches patch = patches[i];
            if (!patch.hasInventory()) continue;
            ArrayList<ExtendedItemStackFilterSlot> patchSlotList = new ArrayList<ExtendedItemStackFilterSlot>();
            boolean patchActive = this.patchEnabled(patch, 1);
            for (int row = 0; row < patch.getInventoryRows(); ++row) {
                for (int col2 = 0; col2 < patch.getInventoryCols(); ++col2) {
                    ExtendedItemStackFilterSlot patchSlot = new ExtendedItemStackFilterSlot(this.inventory, bagSlotIndex++, 164 + col2 * 18, patch.getRowStart() + row * 18, patch.getSlotFilter());
                    if (playerInv.player.level().isClientSide()) {
                        patchSlot.setActive(false);
                    }
                    this.addSlot((Slot)patchSlot);
                    patchSlotList.add(patchSlot);
                    this.slotsActive.put(slotIndex++, patchActive);
                }
            }
            this.patchSlots.put(patch, patchSlotList);
        }
        for (int row = 0; row < 3; ++row) {
            for (int col3 = 0; col3 < 9; ++col3) {
                this.addSlot(new Slot((Container)playerInv, col3 + row * 9 + 9, 48 + col3 * 18, 144 + row * 18));
                this.slotsActive.put(slotIndex++, true);
            }
        }
        for (col = 0; col < 9; ++col) {
            if (col == playerInv.selected) {
                this.mySlot = slotIndex;
            }
            this.addSlot(new Slot((Container)playerInv, col, 48 + col * 18, 202));
            this.slotsActive.put(slotIndex++, true);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void setPatchActive(PractitionersPouchPatches patch) {
        this.patchSlots.entrySet().forEach(e -> ((ArrayList)e.getValue()).forEach(v -> v.setActive(false)));
        if (patch != null && this.patchSlots.containsKey(patch)) {
            this.patchSlots.get(patch).forEach(s -> s.setActive(true));
        }
    }

    public int getBagSlot() {
        return this.myPlayerIndex;
    }

    @Override
    public boolean stillValid(Player p_75145_1_) {
        return true;
    }

    public boolean patchEnabled(PractitionersPouchPatches patch, int level) {
        return ((PractitionersPouch)this.inventory.getStack().getItem()).getPatchLevel(this.inventory.getStack(), patch) >= level;
    }

    @Override
    public void broadcastChanges() {
        if (!this.isClientside) {
            this.inventory.writeItemStack();
        }
        super.broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == this.mySlot || clickTypeIn == ClickType.SWAP && dragType == this.myPlayerIndex) {
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Nonnull
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        int mainSlots = 21;
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < mainSlots ? !this.moveItemStackTo(itemstack1, mainSlots, this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, mainSlots, false)) {
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

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean wrap) {
        boolean flag = false;
        int i = startIndex;
        if (wrap) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && !(wrap ? i < startIndex : i >= endIndex)) {
                Slot slot = (Slot)this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (this.slotsActive.get(i).booleanValue() && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents((ItemStack)itemstack, (ItemStack)stack)) {
                    int maxSize;
                    int j = itemstack.getCount() + stack.getCount();
                    if (j <= (maxSize = slot.getMaxStackSize())) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }
                if (wrap) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            i = wrap ? endIndex - 1 : startIndex;
            while (!(wrap ? i < startIndex : i >= endIndex)) {
                Slot slot1 = (Slot)this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (this.slotsActive.get(i).booleanValue() && itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(stack.split(stack.getCount()));
                    }
                    slot1.setChanged();
                    flag = true;
                    break;
                }
                if (wrap) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return flag;
    }
}


