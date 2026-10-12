/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.items.IItemHandler
 */
package com.mna.gui.containers.item;

import com.mna.tools.EnchantmentUtils;
import net.minecraft.core.component.DataComponents;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.HeldContainerBase;
import com.mna.gui.containers.slots.BaseSlot;
import com.mna.gui.containers.slots.ItemFilterSlot;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.filters.ItemFilterGroup;
import java.util.HashMap;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;

public class ContainerRunicMalus
extends HeldContainerBase {
    public ContainerRunicMalus(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, new ItemInventoryBase(new ItemStack((ItemLike)ItemInit.RUNIC_MALUS.get(), 6), 6));
    }

    public ContainerRunicMalus(int i, Inventory playerInv, ItemInventoryBase basebag) {
        super((MenuType)ContainerInit.RUNIC_MALUS.get(), i, playerInv, basebag);
    }

    @Override
    protected void initializeSlots(Inventory playerInv) {
        int xpos;
        this.addSlot((Slot)this.slot((IItemHandler)this.inventory, 0, 42, 1));
        this.addSlot((Slot)this.slot((IItemHandler)this.inventory, 1, 118, 1));
        this.addSlot((Slot)this.slot((IItemHandler)this.inventory, 2, 36, 25));
        this.addSlot((Slot)this.slot((IItemHandler)this.inventory, 3, 124, 25));
        this.addSlot((Slot)this.slot((IItemHandler)this.inventory, 4, 42, 49));
        this.addSlot((Slot)this.slot((IItemHandler)this.inventory, 5, 118, 49));
        int slotIndex = 6;
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                ++slotIndex;
                this.addSlot(new Slot((Container)playerInv, ypos + xpos * 9 + 9, 8 + ypos * 18, 127 + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            if (xpos == playerInv.selected) {
                this.mySlot = slotIndex;
            }
            this.addSlot(new Slot((Container)playerInv, xpos, 8 + xpos * 18, 185));
            ++slotIndex;
        }
    }

    @Override
    public BaseSlot slot(IItemHandler inv, int index, int x, int y) {
        return new ItemFilterSlot(inv, index, x, y, ItemFilterGroup.ANY_ENCHANTED_RUNE).setMaxStackSize(1);
    }

    @Override
    protected int slotsPerRow() {
        return 3;
    }

    @Override
    protected int numRows() {
        return 2;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        try {
            if (this.getSlot(slotId).getItem().hashCode() == this.bagHash) {
                return;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public void removed(Player player) {
        if (!player.level().isClientSide()) {
            HashMap chants = new HashMap();
            HashMap<net.minecraft.core.Holder<Enchantment>, Float> bonuses = new HashMap<net.minecraft.core.Holder<Enchantment>, Float>();
            this.inventory.getAllItems().forEach(is -> {
                if (is.isEmpty()) {
                    return;
                }
                EnchantmentUtils.enchantmentMap((ItemStack)is).forEach((ench, lvl) -> {
                    if (!chants.containsKey(ench) || lvl > (Integer)chants.get(ench)) {
                        chants.put(ench, lvl);
                    }
                });
            });
            this.inventory.getAllItems().forEach(is -> {
                if (is.isEmpty()) {
                    return;
                }
                EnchantmentUtils.enchantmentMap((ItemStack)is).forEach((ench, lvl) -> {
                    float bonus = 0.0f;
                    int maxLvl = (Integer)chants.get(ench);
                    if (lvl == maxLvl - 1) {
                        bonus += 0.5f;
                    } else if (lvl == maxLvl) {
                        bonus += 1.0f;
                    }
                    if (!bonuses.containsKey(ench)) {
                        bonuses.put((net.minecraft.core.Holder<Enchantment>)ench, Float.valueOf(bonus - 1.0f));
                    } else {
                        bonuses.put((net.minecraft.core.Holder<Enchantment>)ench, Float.valueOf(((Float)bonuses.get(ench)).floatValue() + bonus));
                    }
                });
            });
            bonuses.forEach((k, v) -> {
                if (chants.containsKey(k)) {
                    chants.put(k, (Integer)chants.get(k) + (int)Math.floor(v.floatValue()));
                }
            });
            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable chMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
            chants.forEach((k, v) -> chMut.set((net.minecraft.core.Holder<Enchantment>)k, (Integer)v));
            ((ItemStack)this.inventory.getStack()).set(DataComponents.ENCHANTMENTS, chMut.toImmutable());
        }
        super.removed(player);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.numRows() * this.slotsPerRow() ? !this.moveItemStackTo(itemstack1, this.numRows() * this.slotsPerRow(), this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, this.numRows() * this.slotsPerRow(), false)) {
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
}

