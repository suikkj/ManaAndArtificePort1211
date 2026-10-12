/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.StairBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.block.state.properties.StairsShape
 */
package com.mna.recipes.multiblock.block_matchers;

import com.mna.api.tools.RLoc;
import com.mna.recipes.multiblock.block_matchers.IBlockMatcher;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.StairsShape;

public class StairsBlockMatcher
implements IBlockMatcher {
    private final ResourceLocation id = RLoc.create("stairs");

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean match(Level world, BlockPos offset, BlockPos worldPos, BlockState desired, BlockState inWorld, boolean matchBlock) {
        if (desired.getBlock() instanceof StairBlock && inWorld.getBlock() instanceof StairBlock) {
            if (matchBlock && desired.getBlock() != inWorld.getBlock()) {
                return false;
            }
            StairsShape desired_shape = (StairsShape)desired.getValue((Property)StairBlock.SHAPE);
            if (desired_shape == StairsShape.STRAIGHT) {
                return this.MatchStateProperties(desired, inWorld, new String[0]);
            }
            if (this.MatchStateProperties(desired, inWorld, StairBlock.HALF.getName(), StairBlock.WATERLOGGED.getName())) {
                int existingPalletteId;
                int[] PALETTE_SHAPE_MAP = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
                int desiredPalletteId = ((StairsShape)desired.getValue((Property)StairBlock.SHAPE)).ordinal() * 4 + ((Direction)desired.getValue((Property)StairBlock.FACING)).get2DDataValue();
                return PALETTE_SHAPE_MAP[desiredPalletteId] == PALETTE_SHAPE_MAP[existingPalletteId = ((StairsShape)inWorld.getValue((Property)StairBlock.SHAPE)).ordinal() * 4 + ((Direction)inWorld.getValue((Property)StairBlock.FACING)).get2DDataValue()];
            }
        }
        return false;
    }

    @Override
    public ArrayList<ItemStack> getValidBlocks(Block block) {
        ArrayList<ItemStack> valid = new ArrayList<ItemStack>();
        if (block instanceof StairBlock) {
            valid.add(new ItemStack((ItemLike)block));
        }
        return valid;
    }
}

