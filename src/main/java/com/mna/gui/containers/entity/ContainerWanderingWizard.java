/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.npc.ClientSideMerchant
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.MerchantContainer
 *  net.minecraft.world.inventory.MerchantResultSlot
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.trading.Merchant
 *  net.minecraft.world.item.trading.MerchantOffer
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.minecraft.world.level.Level
 */
package com.mna.gui.containers.entity;

import com.mna.gui.containers.ContainerInit;
import com.mna.items.ItemInit;
import com.mna.items.ritual.ThaumaturgicLink;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public class ContainerWanderingWizard
extends AbstractContainerMenu {
    private final Merchant merchant;
    private final MerchantContainer merchantInventory;
    private boolean finalized = false;

    public ContainerWanderingWizard(int id, Inventory playerInventoryIn, FriendlyByteBuf buffer) {
        this(id, playerInventoryIn, (Merchant)new ClientSideMerchant(playerInventoryIn.player));
    }

    public ContainerWanderingWizard(int id, Inventory inv, Merchant merchant) {
        super((MenuType)ContainerInit.WANDERING_WIZARD.get(), id);
        this.merchant = merchant;
        this.merchantInventory = new MerchantContainer(merchant);
        this.addSlot(new Slot((Container)this.merchantInventory, 0, 154, 6));
        this.addSlot(new Slot((Container)this.merchantInventory, 1, 179, 6));
        this.addSlot((Slot)new MerchantResultSlot(inv.player, merchant, this.merchantInventory, 2, 234, 6));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot((Container)inv, j + i * 9 + 9, 48 + j * 18, 174 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot((Container)inv, k, 48 + k * 18, 232));
        }
    }

    public void slotsChanged(Container inventoryIn) {
        this.merchantInventory.updateSellItem();
        super.slotsChanged(inventoryIn);
    }

    public void setCurrentRecipeIndex(int currentRecipeIndex) {
        this.merchantInventory.setSelectionHint(currentRecipeIndex);
    }

    public boolean stillValid(Player playerIn) {
        return this.merchant.getTradingPlayer() == playerIn;
    }

    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        return false;
    }

    private void playMerchantYesSound() {
        if (this.merchant instanceof Entity) {
            Level level = ((Entity)this.merchant).level();
            if (!level.isClientSide) {
                Entity entity = (Entity)this.merchant;
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.merchant.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0f, 1.0f, false);
            }
        }
    }

    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
                this.playMerchantYesSound();
            } else if (index != 0 && index != 1 ? (index >= 3 && index < 30 ? !this.moveItemStackTo(itemstack1, 30, 39, false) : index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) : !this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }

    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.merchant.setTradingPlayer((Player)null);
        if (!playerIn.level().isClientSide()) {
            if (!playerIn.isAlive() || playerIn instanceof ServerPlayer && ((ServerPlayer)playerIn).hasDisconnected()) {
                ItemStack itemstack = this.merchantInventory.removeItemNoUpdate(0);
                if (!itemstack.isEmpty()) {
                    playerIn.drop(itemstack, false);
                }
                if (!(itemstack = this.merchantInventory.removeItemNoUpdate(1)).isEmpty()) {
                    playerIn.drop(itemstack, false);
                }
            } else {
                playerIn.getInventory().placeItemBackInInventory(this.merchantInventory.removeItemNoUpdate(0));
                playerIn.getInventory().placeItemBackInInventory(this.merchantInventory.removeItemNoUpdate(1));
            }
        }
    }

    public void setClientSideOffers(MerchantOffers offers) {
        MerchantOffers existing = this.merchant.getOffers();
        offers.forEach(o -> existing.add(o));
    }

    public void setXp(int xp) {
        this.merchant.overrideXp(xp);
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public boolean isFinalized() {
        return this.finalized;
    }

    public MerchantOffers getOffers() {
        return this.merchant.getOffers();
    }

    public void setCurrentTradeRecipeItems(int selectedIndex) {
        if (this.getOffers().size() > selectedIndex) {
            ItemStack itemstack1;
            ItemStack itemstack = this.merchantInventory.getItem(0);
            if (!itemstack.isEmpty()) {
                if (!this.moveItemStackTo(itemstack, 3, 39, true)) {
                    return;
                }
                this.merchantInventory.setItem(0, itemstack);
            }
            if (!(itemstack1 = this.merchantInventory.getItem(1)).isEmpty()) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return;
                }
                this.merchantInventory.setItem(1, itemstack1);
            }
            if (this.merchantInventory.getItem(0).isEmpty() && this.merchantInventory.getItem(1).isEmpty()) {
                ItemStack itemstack2 = ((MerchantOffer)this.getOffers().get(selectedIndex)).getCostA();
                this.pullItemFromPlayerInventory(0, itemstack2);
                ItemStack itemstack3 = ((MerchantOffer)this.getOffers().get(selectedIndex)).getCostB();
                this.pullItemFromPlayerInventory(1, itemstack3);
            }
        }
    }

    private void pullItemFromPlayerInventory(int p_217053_1_, ItemStack p_217053_2_) {
        if (!p_217053_2_.isEmpty()) {
            for (int i = 3; i < 39; ++i) {
                ItemStack itemstack = ((Slot)this.slots.get(i)).getItem();
                if (itemstack.isEmpty() || !ItemStack.matches((ItemStack)p_217053_2_, (ItemStack)itemstack)) continue;
                ItemStack itemstack1 = this.merchantInventory.getItem(p_217053_1_);
                int j = itemstack1.isEmpty() ? 0 : itemstack1.getCount();
                int k = Math.min(p_217053_2_.getMaxStackSize() - j, itemstack.getCount());
                ItemStack itemstack2 = itemstack.copy();
                int l = j + k;
                itemstack.shrink(k);
                itemstack2.setCount(l);
                this.merchantInventory.setItem(p_217053_1_, itemstack2);
                if (l >= p_217053_2_.getMaxStackSize()) break;
            }
        }
    }

    public int calculateIndexFor(MerchantOffer offer, int index, String currentSearchTerm) {
        if (currentSearchTerm == "" || currentSearchTerm == null) {
            return index;
        }
        ResourceLocation oLoc = ((ThaumaturgicLink)(ItemInit.THAUMATURGIC_LINK.get())).getLocationKey(offer.getResult());
        if (oLoc == null) {
            return index;
        }
        for (int i = 0; i < this.getOffers().size(); ++i) {
            MerchantOffer cur = (MerchantOffer)this.getOffers().get(i);
            ResourceLocation curLoc = ((ThaumaturgicLink)(ItemInit.THAUMATURGIC_LINK.get())).getLocationKey(cur.getResult());
            if (curLoc == null || !curLoc.equals(oLoc)) continue;
            return i;
        }
        return -1;
    }
}

