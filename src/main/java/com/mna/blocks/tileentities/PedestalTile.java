 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.tools.MATags;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import com.mna.api.tools.MNAItemData;

public class PedestalTile
extends TileEntityWithInventory
implements IBlockEntityExtension {
    private static final int MAX_ITEMS = 1;
    public static final int INVENTORY_SLOT_INDEX = 0;

    public PedestalTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state, 1);
    }

    public PedestalTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.PEDESTAL.get(), pos, state);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        this.updateBlockState();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack out = super.removeItem(index, count);
        this.updateBlockState();
        return out;
    }

    private void updateBlockState() {
        this.updateBlockState("invChange", 3);
    }

    private void updateBlockState(String id, Integer data) {
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), data.intValue());
        if (this.getLevel().isLoaded(this.getBlockPos())) {
            this.getLevel().getLightEngine().checkBlock(this.getBlockPos());
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        CompoundTag sub = new CompoundTag();
        MNAItemData.save(this.getItem(0), sub);
        base.put("invSync", (Tag)sub);
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        CompoundTag sub = tag.getCompound("invSync");
        this.setItem(0, ItemStack.parseOptional(this.getLevel().registryAccess(), sub));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag sub = pkt.getTag().getCompound("invSync");
        this.setItem(0, ItemStack.parseOptional(this.getLevel().registryAccess(), sub));
    }

    public int getMaxStackSize() {
        return 1;
    }

    public ItemStack getDisplayedItem() {
        return this.getItem(0);
    }

    public boolean MatchesReagent(ResourceLocation rLoc) {
        if (this.getItem(0).isEmpty()) {
            return false;
        }
        return MATags.isItemEqual(this.getItem(0), rLoc);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        return this.getItem(0).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return !this.getItem(0).isEmpty();
    }
}


