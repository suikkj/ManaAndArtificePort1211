/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.mna.api.tools.MNAItemData;

public class BrazierTile
extends BlockEntity {
    private int color = -1;

    public BrazierTile(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType)TileEntityInit.BRAZIER.get(), pPos, pBlockState);
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        nbt.putInt("color", this.color);
    }

    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        if (nbt.contains("color")) {
            this.color = nbt.getInt("color");
        }
        super.loadAdditional(nbt, registries);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("color", this.color);
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag superTag = super.getUpdateTag(registries);
        superTag.putInt("color", this.color);
        return superTag;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        this.color = tag.getInt("color");
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.color = pkt.getTag().getInt("color");
    }
}

