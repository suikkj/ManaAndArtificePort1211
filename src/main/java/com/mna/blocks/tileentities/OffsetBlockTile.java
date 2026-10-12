/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.tile.IOffsetTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.mna.api.tools.MNAItemData;

public class OffsetBlockTile
extends BlockEntity
implements IOffsetTile {
    private BlockPos offset = new BlockPos(0, 0, 0);

    public OffsetBlockTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public OffsetBlockTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.FILLER_TILE.get(), pos, state);
    }

    @Override
    public BlockPos getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(BlockPos offset) {
        this.offset = offset;
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        nbt.put("offset", (Tag)NbtUtils.writeBlockPos((BlockPos)this.offset));
    }

    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        this.offset = NbtUtils.readBlockPos(nbt, "offset").orElse(null);
        super.loadAdditional(nbt, registries);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.offset = NbtUtils.readBlockPos(pkt.getTag(), "offset").orElse(null);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put("offset", (Tag)NbtUtils.writeBlockPos((BlockPos)this.offset));
        return tag;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        this.offset = NbtUtils.readBlockPos(tag, "offset").orElse(null);
    }
}

