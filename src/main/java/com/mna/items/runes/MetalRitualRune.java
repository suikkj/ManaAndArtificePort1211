/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.items.runes;

import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.items.runes.Rune;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class MetalRitualRune
extends Rune {
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).isFaceSturdy((BlockGetter)context.getLevel(), context.getClickedPos(), Direction.UP) && (context.getLevel().isEmptyBlock(context.getClickedPos().above()) || context.getLevel().isWaterAt(context.getClickedPos().above()))) {
            context.getLevel().setBlockAndUpdate(context.getClickedPos().above(), (BlockState)((BlockState)((BlockState)((BlockState)((Block)BlockInit.CHALK_RUNE.get()).defaultBlockState().setValue(ChalkRuneBlock.RUNEINDEX, Integer.valueOf((int)Math.floor(Math.random() * (double)(ChalkRuneBlock.RUNEINDEX.getPossibleValues().size() - 1))))).setValue(ChalkRuneBlock.METAL, Boolean.valueOf(true))).setValue(ChalkRuneBlock.ACTIVATED, Boolean.valueOf(false))).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(context.getLevel().isWaterAt(context.getClickedPos().above()))));
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}

