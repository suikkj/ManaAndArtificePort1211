/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.tools.MATags;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ScrollShelfTile
extends TileEntityWithInventory {
    private int scrollFlags = 0;
    private int bottleFlags = 0;

    public ScrollShelfTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.SCROLLSHELF.get(), pos, state, 81);
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        this.updateScrollFlags();
        CompoundTag tag = new CompoundTag();
        tag.putInt("scrollFlags", this.scrollFlags);
        tag.putInt("bottleFlags", this.bottleFlags);
        return tag;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        if (tag.contains("scrollFlags")) {
            this.scrollFlags = tag.getInt("scrollFlags");
        }
        if (tag.contains("bottleFlags")) {
            this.bottleFlags = tag.getInt("bottleFlags");
        }
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        CompoundTag compoundtag = pkt.getTag();
        if (compoundtag != null) {
            this.scrollFlags = 0;
            this.bottleFlags = 0;
            if (compoundtag.contains("scrollFlags")) {
                this.scrollFlags = compoundtag.getInt("scrollFlags");
            }
            if (compoundtag.contains("bottleFlags")) {
                this.bottleFlags = compoundtag.getInt("bottleFlags");
            }
        }
    }

    public boolean isScrollDisplayable(int index) {
        return (this.scrollFlags & 1 << index) != 0;
    }

    public boolean displayAsBottle(int index) {
        return (this.bottleFlags & 1 << index) != 0;
    }

    private void updateScrollFlags() {
        this.scrollFlags = 0;
        this.bottleFlags = 0;
        for (int i = 0; i < 18; ++i) {
            if (this.getItem(i).isEmpty()) continue;
            this.scrollFlags |= 1 << i;
            if (!MATags.isItemIn(this.getItem(i).getItem(), MATags.Items.SCROLL_SHELF_POTIONS)) continue;
            this.bottleFlags |= 1 << i;
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] items = new int[this.getContainerSize()];
        for (int i = 0; i < this.getContainerSize(); ++i) {
            items[i] = i;
        }
        return items;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).expandTowards(0.0, 1.0, 0.0);
    }
}

