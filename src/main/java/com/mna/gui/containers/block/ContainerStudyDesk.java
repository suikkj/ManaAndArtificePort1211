/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 */
package com.mna.gui.containers.block;

import com.mna.blocks.tileentities.wizard_lab.StudyDeskTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.block.SimpleWizardLabContainer;
import com.mna.gui.containers.slots.ItemFilterSlot;
import com.mna.items.filters.ThesisItemFilter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public class ContainerStudyDesk
extends SimpleWizardLabContainer<ContainerStudyDesk, StudyDeskTile> {
    public ContainerStudyDesk(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super((MenuType<? extends SimpleWizardLabContainer<?, ?>>)((MenuType)ContainerInit.STUDY_DESK.get()), i, playerInventory, packetBuffer);
    }

    public ContainerStudyDesk(int id, Inventory playerInventory, StudyDeskTile tile) {
        super((MenuType)ContainerInit.STUDY_DESK.get(), id, playerInventory, tile);
    }

    @Override
    protected int addInventorySlots() {
        this.addSlot((Slot)new ItemFilterSlot(this.tileItemHandler, 0, 62, 49, new ThesisItemFilter()));
        return 1;
    }

    @Override
    protected int playerInventoryXStart() {
        return 8;
    }

    @Override
    protected int playerInventoryYStart() {
        return 72;
    }

    public StudyDeskTile.CantStartReason getCantStartReason(Player player) {
        return ((StudyDeskTile)this.tile).getCantStartReason(player);
    }
}

