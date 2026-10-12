/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.Level
 */
package com.mna.gui.containers.block;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.blocks.tileentities.wizard_lab.ISelectSpellComponents;
import com.mna.blocks.tileentities.wizard_lab.SpellSpecializationTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.block.SimpleWizardLabContainer;
import com.mna.network.ClientMessageDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public class ContainerSpellSpecialization
extends SimpleWizardLabContainer<ContainerSpellSpecialization, SpellSpecializationTile>
implements ISelectSpellComponents {
    public ContainerSpellSpecialization(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super((MenuType<? extends SimpleWizardLabContainer<?, ?>>)((MenuType)ContainerInit.SPELL_SPECIALIZATION.get()), i, playerInventory, packetBuffer);
    }

    public ContainerSpellSpecialization(int id, Inventory playerInventory, SpellSpecializationTile tile) {
        super((MenuType)ContainerInit.SPELL_SPECIALIZATION.get(), id, playerInventory, tile);
    }

    @Override
    protected int addInventorySlots() {
        return 0;
    }

    @Override
    public void setSpellComponent(ISpellComponent part) {
        if (((SpellSpecializationTile)this.tile).isActive()) {
            return;
        }
        Level world = ((SpellSpecializationTile)this.tile).getLevel();
        if (world.isClientSide) {
            ClientMessageDispatcher.sendWizardLabSelectSpellComponentMessage(part.getRegistryName());
        }
        ((SpellSpecializationTile)this.tile).setSpellComponent(part);
    }

    @Override
    public ISpellComponent getSpellComponent() {
        return ((SpellSpecializationTile)this.tile).getSpellComponent();
    }

    public Affinity getAffinity() {
        return ((SpellSpecializationTile)this.tile).getSelectedAffinity();
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

