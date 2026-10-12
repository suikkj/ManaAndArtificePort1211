/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.containers.item;

import com.mna.gui.containers.ContainerInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ContainerGuideBook
extends AbstractContainerMenu {
    public ContainerGuideBook(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        super((MenuType)ContainerInit.GUIDE_BOOK.get(), id);
    }

    public ContainerGuideBook(int i, Inventory playerInv, SimpleContainer itemInv) {
        super((MenuType)ContainerInit.GUIDE_BOOK.get(), i);
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}

