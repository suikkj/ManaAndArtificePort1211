/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.Position
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.Container
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.WorldlyContainer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.common.capabilities.Capability
 *  net.neoforged.neoforge.common.capabilities.ForgeCapabilities
 *  net.neoforged.neoforge.common.util.LazyOptional
 *  net.minecraftforge.items.wrapper.SidedInvWrapper
 */
package com.mna.api.blocks.tile;

import java.util.HashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public class TileEntityWithInventory
extends BlockEntity
implements Container,
WorldlyContainer {
    protected NonNullList<ItemStack> inventoryItems;
    protected boolean ignoreQtyLimit = false;
    protected HashMap<Direction, IItemHandler> sidedHandlers;

    public TileEntityWithInventory(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int inventorySize) {
        super(tileEntityTypeIn, pos, state);
        this.inventoryItems = NonNullList.withSize((int)inventorySize, ItemStack.EMPTY);
        this.sidedHandlers = new HashMap();
    }

    protected IItemHandler getOrCreateSidedHandler(Direction side) {
        if (!this.sidedHandlers.containsKey(side)) {
            this.sidedHandlers.put(side, new SidedInvWrapper((WorldlyContainer)this, side));
        }
        return this.sidedHandlers.get(side);
    }

    public void clearContent() {
        this.setChanged();
        this.inventoryItems.clear();
    }

    public int getContainerSize() {
        return this.inventoryItems.size();
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventoryItems) {
            if (itemstack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public boolean hasStack(int index) {
        return !this.getItem(index).isEmpty();
    }

    public ItemStack getItem(int index) {
        return (ItemStack)this.inventoryItems.get(index);
    }

    public ItemStack removeItem(int index, int count) {
        ItemStack output = ContainerHelper.removeItem(this.inventoryItems, (int)index, (int)count);
        this.setChanged();
        return output;
    }

    public ItemStack removeItemNoUpdate(int index) {
        ItemStack itemstack = (ItemStack)this.inventoryItems.get(index);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.inventoryItems.set(index, ItemStack.EMPTY);
        this.setChanged();
        return itemstack;
    }

    public void setItem(int index, ItemStack stack) {
        this.inventoryItems.set(index, stack);
        if (!this.ignoreQtyLimit && !stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        return false;
    }

    public int[] getSlotsForFace(Direction side) {
        return new int[0];
    }

    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return this.getBlockPos().closerToCenterThan((Position)player.position(), 8.0);
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        MNAItemData.saveItems((CompoundTag)compound, this.inventoryItems);
    }

    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        MNAItemData.loadItems((CompoundTag)compound, this.inventoryItems);
    }
}


