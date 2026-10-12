/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.DataSlot
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.gui;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.EldrinCapacitorTile;
import com.mna.api.faction.IFaction;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public class EldrinCapacitorPermissionsContainer
extends AbstractContainerMenu {
    private static final int MAX_INTERACT_DISTANCE = 256;
    private Player user;
    private EldrinCapacitorTile tile;

    public EldrinCapacitorPermissionsContainer(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, ((EldrinCapacitorTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos())).readFrom(packetBuffer));
    }

    public EldrinCapacitorPermissionsContainer(int i, Inventory playerInv, EldrinCapacitorTile tile) {
        super(ManaAndArtificeMod.getContainerHelper().GetEldrinPermissionsContainerType(), i);
        this.user = playerInv.player;
        this.tile = tile;
        this.addDataSlots(tile);
        tile.addContainer(this);
    }

    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.tile.removeContainer(this);
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    public boolean stillValid(Player pPlayer) {
        if (this.user == null || this.tile == null || this.user != pPlayer) {
            return false;
        }
        return this.tile.getBlockPos().distToCenterSqr(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ()) <= 256.0;
    }

    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0: {
                this.toggleShareTeam();
                break;
            }
            case 1: {
                this.toggleShareFaction();
                break;
            }
            case 2: {
                this.toggleSharePublic();
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    public void setData(int pId, int pData) {
        super.setData(pId, pData);
        this.broadcastChanges();
    }

    public boolean userCanEdit() {
        return this.user.getGameProfile().getId().equals(this.tile.getPlacedBy());
    }

    public boolean shareTeam() {
        return ((DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(0)).get() == 1;
    }

    public boolean shareFaction() {
        return ((DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(1)).get() == 1;
    }

    public boolean sharePublic() {
        return ((DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(2)).get() == 1;
    }

    public float getAmount(Affinity affinity) {
        int dataSlotIndex = -1;
        switch (affinity.getShiftAffinity()) {
            case ARCANE: {
                dataSlotIndex = 3;
                break;
            }
            case EARTH: {
                dataSlotIndex = 7;
                break;
            }
            case ENDER: {
                dataSlotIndex = 4;
                break;
            }
            case FIRE: {
                dataSlotIndex = 5;
                break;
            }
            case WATER: {
                dataSlotIndex = 6;
                break;
            }
            case WIND: {
                dataSlotIndex = 8;
                break;
            }
            default: {
                return 0.0f;
            }
        }
        return (float)((DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(dataSlotIndex)).get() / 100.0f;
    }

    public float getCapacity(Affinity affinity) {
        return this.tile.getCapacity(affinity);
    }

    public float getChargeRate() {
        return (float)((DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(9)).get() / 100.0f * 20.0f;
    }

    public float getDispersalRate() {
        return (float)((DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(11)).get() / 100.0f * 20.0f;
    }

    public float getDispersalRadius() {
        return (float)((DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(10)).get() / 100.0f;
    }

    public void toggleShareTeam() {
        DataSlot dataSlot = (DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(0);
        if (dataSlot.get() == 1) {
            dataSlot.set(0);
        } else {
            dataSlot.set(1);
        }
    }

    public void toggleShareFaction() {
        DataSlot dataSlot = (DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(1);
        if (dataSlot.get() == 1) {
            dataSlot.set(0);
        } else {
            dataSlot.set(1);
        }
    }

    public void toggleSharePublic() {
        DataSlot dataSlot = (DataSlot)com.mna.gui.containers.MNAMenuAccess.dataSlots(this).get(2);
        if (dataSlot.get() == 1) {
            dataSlot.set(0);
        } else {
            dataSlot.set(1);
        }
    }

    @Nullable
    public IFaction getShareFaction() {
        return this.tile.getPlacedByFaction();
    }

    @Nullable
    public String getShareTeam() {
        return this.tile.getPlacedByTeam();
    }

    @Nullable
    public String getPlacedByPlayerName() {
        return this.tile.getPlacedByPlayerName();
    }

    public boolean supplies(Affinity affinity) {
        return this.tile.supplies(affinity);
    }
}

