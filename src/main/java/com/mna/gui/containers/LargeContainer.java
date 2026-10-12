/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.ContainerListener
 *  net.minecraft.world.inventory.ContainerSynchronizer
 *  net.minecraft.world.inventory.DataSlot
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.containers;

import com.mna.api.ManaAndArtificeMod;
import com.mna.gui.containers.IExtendedItemHandler;
import com.mna.gui.containers.slots.ExtendedItemStackSlot;
import com.mna.gui.containers.synchronizer.ExtendedContainerSynchronizer;
import com.mna.network.ServerMessageDispatcher;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class LargeContainer
extends AbstractContainerMenu {
    protected final Inventory playerInv;
    protected final IExtendedItemHandler inventory;
    protected final boolean isClientside;

    public LargeContainer(MenuType<?> container, int id, Inventory playerInv, IExtendedItemHandler inventory) {
        super(container, id);
        this.playerInv = playerInv;
        this.inventory = inventory;
        this.isClientside = playerInv.player.level().isClientSide();
    }

    public boolean stillValid(Player p_75145_1_) {
        return true;
    }

    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        ItemStack itemstack = ItemStack.EMPTY;
        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            int oldDragEvent = MNAMenuAccess.quickcraftStatus(this);
            MNAMenuAccess.setQuickcraftStatus(this, LargeContainer.getQuickcraftHeader((int)dragType));
            if ((oldDragEvent != 1 || MNAMenuAccess.quickcraftStatus(this) != 2) && oldDragEvent != MNAMenuAccess.quickcraftStatus(this)) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (MNAMenuAccess.quickcraftStatus(this) == 0) {
                MNAMenuAccess.setQuickcraftType(this, LargeContainer.getQuickcraftType((int)dragType));
                if (LargeContainer.isValidQuickcraftType((int)MNAMenuAccess.quickcraftType(this), (Player)player)) {
                    MNAMenuAccess.setQuickcraftStatus(this, 1);
                    MNAMenuAccess.quickcraftSlots(this).clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (MNAMenuAccess.quickcraftStatus(this) == 1) {
                Slot addedSlot = (Slot)this.slots.get(slotId);
                ItemStack mouseStack = this.getCarried();
                if (addedSlot != null && LargeContainer.canItemQuickReplace((Slot)addedSlot, (ItemStack)mouseStack, (boolean)true) && addedSlot.mayPlace(mouseStack) && (MNAMenuAccess.quickcraftType(this) == 2 || mouseStack.getCount() > MNAMenuAccess.quickcraftSlots(this).size()) && this.canDragTo(addedSlot)) {
                    MNAMenuAccess.quickcraftSlots(this).add(addedSlot);
                }
            } else if (MNAMenuAccess.quickcraftStatus(this) == 2) {
                if (!MNAMenuAccess.quickcraftSlots(this).isEmpty()) {
                    ItemStack mouseStackCopy = this.getCarried().copy();
                    int mouseCount = this.getCarried().getCount();
                    for (Slot dragSlot : MNAMenuAccess.quickcraftSlots(this)) {
                        ItemStack mouseStack = this.getCarried();
                        if (dragSlot == null || !LargeContainer.canItemQuickReplace((Slot)dragSlot, (ItemStack)mouseStack, (boolean)true) || !dragSlot.mayPlace(mouseStack) || MNAMenuAccess.quickcraftType(this) != 2 && mouseStack.getCount() < MNAMenuAccess.quickcraftSlots(this).size() || !this.canDragTo(dragSlot)) continue;
                        ItemStack dragCopy = mouseStackCopy.copy();
                        int initialDragCount = dragSlot.hasItem() ? dragSlot.getItem().getCount() : 0;
                        int placeCount = LargeContainer.getQuickCraftPlaceCount((Set)MNAMenuAccess.quickcraftSlots(this), (int)MNAMenuAccess.quickcraftType(this), (ItemStack)dragCopy);
                        dragCopy.setCount(initialDragCount + placeCount);
                        int slotLimit = dragSlot.getMaxStackSize(dragCopy);
                        if (dragCopy.getCount() > slotLimit) {
                            dragCopy.setCount(slotLimit);
                        }
                        mouseCount -= dragCopy.getCount() - initialDragCount;
                        dragSlot.set(dragCopy);
                    }
                    mouseStackCopy.setCount(mouseCount);
                    this.setCarried(mouseStackCopy);
                }
                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (MNAMenuAccess.quickcraftStatus(this) != 0) {
            this.resetQuickCraft();
        } else if (!(clickTypeIn != ClickType.PICKUP && clickTypeIn != ClickType.QUICK_MOVE || dragType != 0 && dragType != 1)) {
            if (slotId == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (dragType == 0) {
                        player.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    }
                    if (dragType == 1) {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickTypeIn == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return;
                }
                Slot transferSlot = (Slot)this.slots.get(slotId);
                if (transferSlot == null || !transferSlot.mayPickup(player)) {
                    return;
                }
                ItemStack transferStack = this.quickMoveStack(player, slotId);
                while (!transferStack.isEmpty() && ItemStack.matches((ItemStack)transferSlot.getItem(), (ItemStack)transferStack)) {
                    itemstack = transferStack.copy();
                    transferStack = this.quickMoveStack(player, slotId);
                }
            } else {
                if (slotId < 0) {
                    return;
                }
                Slot clickedSlot = (Slot)this.slots.get(slotId);
                if (clickedSlot != null) {
                    ItemStack slotStack = clickedSlot.getItem();
                    ItemStack mouseStack = this.getCarried();
                    if (!slotStack.isEmpty()) {
                        itemstack = slotStack.copy();
                    }
                    if (slotStack.isEmpty()) {
                        if (!mouseStack.isEmpty() && clickedSlot.mayPlace(mouseStack)) {
                            int splitCount;
                            int n = splitCount = dragType == 0 ? mouseStack.getCount() : 1;
                            if (splitCount > clickedSlot.getMaxStackSize(mouseStack)) {
                                splitCount = clickedSlot.getMaxStackSize(mouseStack);
                            }
                            clickedSlot.set(mouseStack.split(splitCount));
                            this.setCarried(mouseStack);
                        }
                    } else if (clickedSlot.mayPickup(player)) {
                        int j2;
                        if (mouseStack.isEmpty()) {
                            if (slotStack.isEmpty()) {
                                clickedSlot.set(ItemStack.EMPTY);
                                this.setCarried(ItemStack.EMPTY);
                            } else {
                                int toMove;
                                int n = toMove = dragType == 0 ? slotStack.getCount() : (slotStack.getCount() + 1) / 2;
                                if (clickedSlot instanceof ExtendedItemStackSlot && slotStack.getMaxStackSize() < slotStack.getCount()) {
                                    toMove = dragType == 0 ? slotStack.getMaxStackSize() : (slotStack.getMaxStackSize() + 1) / 2;
                                }
                                this.setCarried(clickedSlot.remove(toMove));
                                if (slotStack.isEmpty()) {
                                    clickedSlot.set(ItemStack.EMPTY);
                                }
                                clickedSlot.onTake(player, this.getCarried());
                            }
                        } else if (clickedSlot.mayPlace(mouseStack)) {
                            if (slotStack.getItem() == mouseStack.getItem() && ManaAndArtificeMod.getItemHelper().AreTagsEqual(slotStack, mouseStack)) {
                                int k2;
                                int n = k2 = dragType == 0 ? mouseStack.getCount() : 1;
                                if (k2 > clickedSlot.getMaxStackSize(mouseStack) - slotStack.getCount()) {
                                    k2 = clickedSlot.getMaxStackSize(mouseStack) - slotStack.getCount();
                                }
                                mouseStack.shrink(k2);
                                slotStack.grow(k2);
                            } else if (mouseStack.getCount() <= clickedSlot.getMaxStackSize(mouseStack) && slotStack.getCount() <= slotStack.getMaxStackSize()) {
                                clickedSlot.set(mouseStack);
                                this.setCarried(slotStack);
                            }
                        } else if (slotStack.getItem() == mouseStack.getItem() && mouseStack.getMaxStackSize() > 1 && ManaAndArtificeMod.getItemHelper().AreTagsEqual(slotStack, mouseStack) && !slotStack.isEmpty() && (j2 = slotStack.getCount()) + mouseStack.getCount() <= mouseStack.getMaxStackSize()) {
                            mouseStack.grow(j2);
                            slotStack = clickedSlot.remove(j2);
                            if (slotStack.isEmpty()) {
                                clickedSlot.set(ItemStack.EMPTY);
                            }
                            clickedSlot.onTake(player, this.getCarried());
                        }
                    }
                    clickedSlot.setChanged();
                }
            }
        } else if (clickTypeIn == ClickType.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && slotId >= 0) {
            Slot cloneSlot = (Slot)this.slots.get(slotId);
            if (cloneSlot != null && cloneSlot.hasItem()) {
                ItemStack clonedStack = cloneSlot.getItem().copy();
                clonedStack.setCount(clonedStack.getMaxStackSize());
                this.setCarried(clonedStack);
            }
        } else if (clickTypeIn == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
            Slot throwSlot = (Slot)this.slots.get(slotId);
            if (throwSlot != null && throwSlot.hasItem() && throwSlot.mayPickup(player)) {
                ItemStack thrownSlot = throwSlot.remove(dragType == 0 ? 1 : throwSlot.getItem().getCount());
                throwSlot.onTake(player, thrownSlot);
                player.drop(thrownSlot, true);
            }
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot pickupSlot = (Slot)this.slots.get(slotId);
            ItemStack mouseStack = this.getCarried();
            if (!(mouseStack.isEmpty() || pickupSlot != null && pickupSlot.hasItem() && pickupSlot.mayPickup(player))) {
                int i = dragType == 0 ? 0 : this.slots.size() - 1;
                int j = dragType == 0 ? 1 : -1;
                for (int k = 0; k < 2; ++k) {
                    for (int l = i; l >= 0 && l < this.slots.size() && mouseStack.getCount() < mouseStack.getMaxStackSize(); l += j) {
                        Slot slot1 = (Slot)this.slots.get(l);
                        if (!slot1.hasItem() || !LargeContainer.canItemQuickReplace((Slot)slot1, (ItemStack)mouseStack, (boolean)true) || !slot1.mayPickup(player) || !this.canTakeItemForPickAll(mouseStack, slot1)) continue;
                        ItemStack itemstack2 = slot1.getItem();
                        if (k == 0 && itemstack2.getCount() >= slot1.getMaxStackSize(itemstack2)) continue;
                        int i1 = Math.min(mouseStack.getMaxStackSize() - mouseStack.getCount(), itemstack2.getCount());
                        ItemStack itemstack3 = slot1.remove(i1);
                        mouseStack.grow(i1);
                        if (itemstack3.isEmpty()) {
                            slot1.set(ItemStack.EMPTY);
                        }
                        slot1.onTake(player, itemstack3);
                    }
                }
            }
        }
        this.broadcastChanges();
        if (itemstack.getCount() > 64) {
            itemstack = itemstack.copy();
            itemstack.setCount(64);
        }
    }

    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        while (!stack.isEmpty() && !(!reverseDirection ? i >= endIndex : i < startIndex)) {
            Slot slot = (Slot)this.slots.get(i);
            ItemStack slotStack = slot.getItem();
            if (!slotStack.isEmpty() && slotStack.getItem() == stack.getItem() && ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, slotStack)) {
                int maxSize;
                int j = slotStack.getCount() + stack.getCount();
                if (j <= (maxSize = slot.getMaxStackSize(slotStack))) {
                    stack.setCount(0);
                    slotStack.setCount(j);
                    slot.setChanged();
                    flag = true;
                } else if (slotStack.getCount() < maxSize) {
                    stack.shrink(maxSize - slotStack.getCount());
                    slotStack.setCount(maxSize);
                    slot.setChanged();
                    flag = true;
                }
            }
            i += reverseDirection ? -1 : 1;
        }
        if (!stack.isEmpty()) {
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (!(!reverseDirection ? i >= endIndex : i < startIndex)) {
                Slot slot1 = (Slot)this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize(stack)) {
                        slot1.set(stack.split(slot1.getMaxStackSize(stack)));
                    } else {
                        slot1.set(stack.split(stack.getCount()));
                    }
                    slot1.setChanged();
                    flag = true;
                    break;
                }
                i += reverseDirection ? -1 : 1;
            }
        }
        return flag;
    }

    protected boolean skipSlot(int i) {
        return this.skipSlot((Slot)this.slots.get(i));
    }

    protected boolean skipSlot(Slot slot) {
        return false;
    }

    public void broadcastChanges() {
        for (int i = 0; i < this.slots.size(); ++i) {
            if (this.skipSlot(i)) continue;
            ItemStack itemstack = ((Slot)this.slots.get(i)).getItem();
            ItemStack itemstack1 = (ItemStack)MNAMenuAccess.lastSlots(this).get(i);
            if (ItemStack.matches((ItemStack)itemstack1, (ItemStack)itemstack)) continue;
            itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
            MNAMenuAccess.lastSlots(this).set(i, itemstack1);
            for (ContainerListener listener : MNAMenuAccess.containerListeners(this)) {
                Class<?> listenerEnclosingClass = listener.getClass().getEnclosingClass();
                if (listenerEnclosingClass == ServerPlayer.class) {
                    ServerPlayer player = (ServerPlayer)this.playerInv.player;
                    this.syncSlot(player, i, itemstack1);
                    continue;
                }
                listener.slotChanged((AbstractContainerMenu)this, i, itemstack1);
            }
        }
        for (int j = 0; j < MNAMenuAccess.dataSlots(this).size(); ++j) {
            DataSlot intreferenceholder = (DataSlot)MNAMenuAccess.dataSlots(this).get(j);
            if (!intreferenceholder.checkAndClearUpdateFlag()) continue;
            for (ContainerListener icontainerlistener1 : MNAMenuAccess.containerListeners(this)) {
                icontainerlistener1.dataChanged((AbstractContainerMenu)this, j, intreferenceholder.get());
            }
        }
    }

    public void setSynchronizer(ContainerSynchronizer synchronizer) {
        if (this.playerInv.player instanceof ServerPlayer) {
            super.setSynchronizer((ContainerSynchronizer)new ExtendedContainerSynchronizer((ServerPlayer)this.playerInv.player));
        } else {
            super.setSynchronizer(synchronizer);
        }
    }

    public void syncInventory(ServerPlayer player) {
        for (int i = 0; i < this.slots.size(); ++i) {
            ItemStack stack;
            if (this.skipSlot(i) || (stack = ((Slot)this.slots.get(i)).getItem()).isEmpty()) continue;
            ServerMessageDispatcher.sendExtendedItemStack(player, this.containerId, this.getStateId(), i, stack);
        }
    }

    public void syncSlot(ServerPlayer player, int slot, ItemStack stack) {
        if (!this.skipSlot(slot)) {
            ServerMessageDispatcher.sendExtendedItemStack(player, this.containerId, this.getStateId(), slot, stack);
        }
    }
}

