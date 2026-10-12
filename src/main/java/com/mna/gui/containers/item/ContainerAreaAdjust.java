/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Vector3f
 */
package com.mna.gui.containers.item;

import com.mna.api.items.IAreaItem;
import com.mna.gui.containers.ContainerInit;
import com.mna.network.ClientMessageDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class ContainerAreaAdjust
extends AbstractContainerMenu {
    Inventory playerInv;

    public ContainerAreaAdjust(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory);
    }

    public ContainerAreaAdjust(int i, Inventory playerInv) {
        super((MenuType)ContainerInit.AREA_ADJUSTMENTS.get(), i);
        this.playerInv = playerInv;
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    public boolean stillValid(Player pPlayer) {
        return true;
    }

    public void Adjust(Vector3f direction, boolean grow) {
        if (this.playerInv.player.level().isClientSide()) {
            ClientMessageDispatcher.sendAreaAdjust(direction, grow);
            ItemStack held = this.playerInv.getItem(this.playerInv.selected);
            Item item = held.getItem();
            if (item instanceof IAreaItem) {
                IAreaItem areaItem = (IAreaItem)item;
                areaItem.adjust(held, direction, grow);
            }
        }
    }
}

