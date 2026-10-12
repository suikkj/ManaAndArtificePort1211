/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.recipes.multiblock.block_matchers;

import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.RefractionLensBlock;
import com.mna.recipes.multiblock.block_matchers.IBlockMatcher;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RefractionLensBlockMatcher
implements IBlockMatcher {
    private final ResourceLocation id = RLoc.create("refraction_lenses");

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean match(Level world, BlockPos offset, BlockPos worldPos, BlockState desired, BlockState inWorld, boolean matchBlock) {
        return desired.getBlock() instanceof RefractionLensBlock && inWorld.getBlock() instanceof RefractionLensBlock;
    }

    @Override
    public ArrayList<ItemStack> getValidBlocks(Block block) {
        ArrayList<ItemStack> valid = new ArrayList<ItemStack>();
        if (block instanceof RefractionLensBlock) {
            valid.add(new ItemStack((ItemLike)BlockInit.REFRACTION_LENS_ARCANE.get()));
            valid.add(new ItemStack((ItemLike)BlockInit.REFRACTION_LENS_ENDER.get()));
            valid.add(new ItemStack((ItemLike)BlockInit.REFRACTION_LENS_WIND.get()));
            valid.add(new ItemStack((ItemLike)BlockInit.REFRACTION_LENS_EARTH.get()));
            valid.add(new ItemStack((ItemLike)BlockInit.REFRACTION_LENS_FIRE.get()));
            valid.add(new ItemStack((ItemLike)BlockInit.REFRACTION_LENS_WATER.get()));
        }
        return valid;
    }
}

