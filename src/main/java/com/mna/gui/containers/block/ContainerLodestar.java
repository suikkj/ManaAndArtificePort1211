/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.gui.containers.block;

import com.mna.blocks.artifice.LodestarBlock;
import com.mna.blocks.tileentities.LodestarTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.widgets.lodestar.LodestarNodeErrorTypes;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.ServerMessageDispatcher;
import java.util.HashMap;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.Property;

public class ContainerLodestar
extends AbstractContainerMenu {
    private LodestarTile lodestar;

    public ContainerLodestar(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, (LodestarTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));
    }

    public ContainerLodestar(int id, Inventory inv, LodestarTile lodestar) {
        super((MenuType)ContainerInit.LODESTAR.get(), id);
        int xpos;
        this.lodestar = lodestar;
        for (xpos = 0; xpos < 3; ++xpos) {
            for (int ypos = 0; ypos < 9; ++ypos) {
                this.addSlot(new Slot((Container)inv, ypos + xpos * 9 + 9, 48 + ypos * 18, 173 + xpos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)inv, xpos, 48 + xpos * 18, 231));
        }
        if (!this.lodestar.getLevel().isClientSide()) {
            ServerMessageDispatcher.sendLodestarLogicMessage((ServerPlayer)inv.player, this.lodestar.getBlockPos(), this.getLogic());
        }
    }

    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return ItemStack.EMPTY;
    }

    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return false;
    }

    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        return false;
    }

    public boolean canDragTo(Slot slotIn) {
        return false;
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    public boolean isLogicDownloaded() {
        return this.lodestar.isLogicDownloaded();
    }

    public void setTileLogic(CompoundTag logic) {
        if (this.lodestar.getLevel().isClientSide()) {
            ClientMessageDispatcher.sendLodestarLogicMessage(this.lodestar.getBlockPos(), logic);
        }
        this.lodestar.setLogic(logic, false);
    }

    public void updateTileLogic(int index, int numMessages, int totalDataSize, byte[] buffer, boolean sync) {
        this.lodestar.setLogicPartial(index, numMessages, totalDataSize, buffer, sync);
    }

    public CompoundTag getLogic() {
        return this.lodestar.getLogic();
    }

    public boolean isLowTier() {
        return (Boolean)this.lodestar.getBlockState().getValue((Property)LodestarBlock.LOW_TIER);
    }

    public List<Component> getWarnings() {
        return this.lodestar.getWarnings();
    }

    public List<Component> getErrors() {
        return this.lodestar.getErrors();
    }

    public HashMap<String, LodestarNodeErrorTypes> getNodeErrors() {
        return this.lodestar.getNodeErrors();
    }
}

