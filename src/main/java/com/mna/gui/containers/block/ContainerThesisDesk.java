/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.Level
 */
package com.mna.gui.containers.block;

import com.mna.api.affinity.Affinity;
import com.mna.api.gui.SingleItemSlot;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.blocks.tileentities.wizard_lab.ISelectSpellComponents;
import com.mna.blocks.tileentities.wizard_lab.ThesisDeskTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.block.SimpleWizardLabContainer;
import com.mna.gui.containers.slots.ItemFilterSlot;
import com.mna.gui.containers.slots.SlotNoPlace;
import com.mna.items.ItemInit;
import com.mna.items.filters.VellumFilter;
import com.mna.network.ClientMessageDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ContainerThesisDesk
extends SimpleWizardLabContainer<ContainerThesisDesk, ThesisDeskTile>
implements ISelectSpellComponents {
    public ContainerThesisDesk(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super((MenuType<? extends SimpleWizardLabContainer<?, ?>>)((MenuType)ContainerInit.THESIS_DESK.get()), i, playerInventory, packetBuffer);
    }

    public ContainerThesisDesk(int id, Inventory playerInventory, ThesisDeskTile tile) {
        super((MenuType)ContainerInit.THESIS_DESK.get(), id, playerInventory, tile);
    }

    @Override
    protected int addInventorySlots() {
        this.addSlot((Slot)new ItemFilterSlot(this.tileItemHandler, 0, 159, 7, new VellumFilter()));
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 1, 133, 7, (Item)ItemInit.ARCANIST_INK.get()));
        this.addSlot((Slot)new SingleItemSlot(this.tileItemHandler, 2, 107, 7, (Item)ItemInit.ROTE_BOOK.get()));
        this.addSlot((Slot)new SlotNoPlace(this.tileItemHandler, 3, 229, 129));
        return 4;
    }

    @Override
    public void setSpellComponent(ISpellComponent part) {
        if (((ThesisDeskTile)this.tile).isActive()) {
            return;
        }
        Level world = ((ThesisDeskTile)this.tile).getLevel();
        if (world.isClientSide) {
            ClientMessageDispatcher.sendWizardLabSelectSpellComponentMessage(part.getRegistryName());
        }
        ((ThesisDeskTile)this.tile).setSpellComponent(part);
    }

    @Override
    public ISpellComponent getSpellComponent() {
        return ((ThesisDeskTile)this.tile).getSpellComponent();
    }

    public Affinity getAffinity() {
        return ((ThesisDeskTile)this.tile).getSelectedAffinity();
    }

    @Override
    protected int playerInventoryXStart() {
        return 48;
    }

    @Override
    protected int playerInventoryYStart() {
        return 151;
    }
}

