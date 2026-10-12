/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 */
package com.mna.gui.containers.block;

import com.mna.api.gui.SingleItemSlot;
import com.mna.blocks.tileentities.wizard_lab.DisenchanterTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.block.SimpleWizardLabContainer;
import com.mna.items.ItemInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

public class ContainerDisenchanter
extends SimpleWizardLabContainer<ContainerDisenchanter, DisenchanterTile> {
    public ContainerDisenchanter(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super((MenuType<? extends SimpleWizardLabContainer<?, ?>>)((MenuType)ContainerInit.DISENCHANTER.get()), i, playerInventory, packetBuffer);
    }

    public ContainerDisenchanter(int id, Inventory playerInventory, DisenchanterTile tile) {
        super((MenuType)ContainerInit.DISENCHANTER.get(), id, playerInventory, tile);
    }

    @Override
    protected int addInventorySlots() {
        this.addSlot(new Slot((Container)this.tile, 0, 79, 7));
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 1, 79, 49, (Item)ItemInit.RUNE_PROJECTION.get()).setMaxStackSize(1));
        return 2;
    }

    @Override
    protected int playerInventoryXStart() {
        return 8;
    }

    @Override
    protected int playerInventoryYStart() {
        return 71;
    }
}

