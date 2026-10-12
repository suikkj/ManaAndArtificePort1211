 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.tools.MATags;
import com.mna.blocks.ritual.ChalkRuneBlock;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import com.mna.api.tools.MNAItemData;

public class ChalkRuneTile
extends TileEntityWithInventory
implements IBlockEntityExtension {
    private static final int MAX_ITEMS = 1;
    public static final int INVENTORY_SLOT_INDEX = 0;
    private boolean readOnly = false;
    private boolean ghostItem = false;

    public ChalkRuneTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state, 1);
    }

    public ChalkRuneTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.CHALK_RUNE.get(), pos, state);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (this.readOnly || this.ghostItem) {
            return;
        }
        super.setItem(index, stack);
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (this.readOnly || this.ghostItem) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = super.removeItemNoUpdate(index);
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        return stack;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack out = super.removeItem(index, count);
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        return out;
    }

    public void clearStack() {
        super.removeItemNoUpdate(0);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setGhostItem(boolean ghost) {
        this.ghostItem = ghost;
    }

    public boolean isGhostItem() {
        return this.ghostItem;
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
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        if (compound.contains("readOnly")) {
            this.readOnly = compound.getBoolean("readOnly");
        }
        if (compound.contains("ghostItem")) {
            this.ghostItem = compound.getBoolean("ghostItem");
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putBoolean("readOnly", this.readOnly);
        compound.putBoolean("ghostItem", this.ghostItem);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        if (this.isGhostItem() || ((Boolean)this.getBlockState().getValue((Property)ChalkRuneBlock.ACTIVATED)).booleanValue()) {
            return false;
        }
        return this.getItem(0).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (this.isGhostItem() || ((Boolean)this.getBlockState().getValue((Property)ChalkRuneBlock.ACTIVATED)).booleanValue()) {
            return false;
        }
        return !this.getItem(0).isEmpty();
    }
}


