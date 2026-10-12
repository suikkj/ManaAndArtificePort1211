/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.containers.block;

import com.mna.gui.containers.ContainerInit;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ContainerOcculus
extends AbstractContainerMenu {
    private ContainerOcculus(@Nullable MenuType<?> type, int id, Inventory playerInv, ItemStack heldItem) {
        super(type, id);
    }

    public ContainerOcculus(int i, Inventory playerInv, FriendlyByteBuf buffer) {
        this((MenuType)ContainerInit.OCCULUS.get(), i, playerInv, playerInv.getItem(playerInv.selected));
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}

