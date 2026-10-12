/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.neoforge.common.CommonHooks
 *  net.neoforged.neoforge.common.capabilities.ICapabilityProvider
 *  net.minecraftforge.fluids.FluidStack
 *  org.jetbrains.annotations.Nullable
 */
package com.mna.items.artifice;

import com.mna.api.items.IRelic;
import com.mna.blocks.artifice.FluidJugBlock;
import com.mna.items.artifice.FluidJug;
import com.mna.items.base.InfiniteFluidCapabilityHandler;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class InfiniteFluidJug
extends FluidJug
implements IRelic {
    final Fluid contained;
    final FluidStack containedStack;
    final boolean relic;

    public InfiniteFluidJug(FluidJugBlock forBlock, Fluid contained, boolean relic) {
        super(forBlock);
        this.contained = contained;
        this.containedStack = new FluidStack(contained, Integer.MAX_VALUE);
        this.relic = relic;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @javax.annotation.Nullable CompoundTag nbt) {
        return new InfiniteFluidCapabilityHandler(this.contained, stack);
    }

    @Override
    public FluidStack getFluidTagData(ItemStack stack) {
        return this.containedStack;
    }

    @Override
    public Component getHoverAddition(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (this.relic) {
            return IRelic.super.getHoverAddition(stack, worldIn, tooltip, flagIn);
        }
        return null;
    }

    public boolean hasCraftingRemainingItem() {
        return true;
    }

    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return new ItemStack((ItemLike)this);
    }

    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        FluidStack flStack = this.getFluidTagData(itemStack);
        ItemStack bucket = flStack.getFluid().getFluidType().getBucket(flStack);
        int burnTime = CommonHooks.getBurnTime((ItemStack)bucket, recipeType);
        return burnTime;
    }
}


