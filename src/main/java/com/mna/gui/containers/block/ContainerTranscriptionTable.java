/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 */
package com.mna.gui.containers.block;

import com.mna.api.gui.SingleItemSlot;
import com.mna.blocks.tileentities.wizard_lab.TranscriptionTableTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.block.SimpleWizardLabContainer;
import com.mna.gui.containers.slots.ItemFilterSlot;
import com.mna.items.ItemInit;
import com.mna.items.filters.SpellItemFilter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ContainerTranscriptionTable
extends SimpleWizardLabContainer<ContainerTranscriptionTable, TranscriptionTableTile> {
    public ContainerTranscriptionTable(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super((MenuType<? extends SimpleWizardLabContainer<?, ?>>)((MenuType)ContainerInit.TRANSCRIPTION_TABLE.get()), i, playerInventory, packetBuffer);
    }

    public ContainerTranscriptionTable(int id, Inventory playerInventory, TranscriptionTableTile tile) {
        super((MenuType)ContainerInit.TRANSCRIPTION_TABLE.get(), id, playerInventory, tile);
    }

    @Override
    protected int addInventorySlots() {
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 0, 8, 8, (Item)ItemInit.ARCANIST_INK.get()));
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 1, 31, 8, Items.LAPIS_LAZULI));
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 2, 8, 31, (Item)ItemInit.ROTE_BOOK.get()));
        this.addSlot((Slot)new ItemFilterSlot(this.tileItemHandler, 3, 80, 31, new SpellItemFilter()).setMaxStackSize(1));
        return 4;
    }

    public int getInkRequired() {
        return ((TranscriptionTableTile)this.tile).getInkRequired();
    }

    public int getLapisRequired(Player player) {
        return ((TranscriptionTableTile)this.tile).getLapisRequired(player);
    }

    @Override
    protected int playerInventoryXStart() {
        return 8;
    }

    @Override
    protected int playerInventoryYStart() {
        return 77;
    }
}

