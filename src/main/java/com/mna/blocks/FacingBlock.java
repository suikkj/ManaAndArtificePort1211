/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.blocks;

import com.mna.api.blocks.WaterloggableBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class FacingBlock
extends WaterloggableBlock {
    public static final IntegerProperty SURFACE_TYPE = IntegerProperty.create((String)"surface", (int)1, (int)3);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected FacingBlock(BlockBehaviour.Properties builder) {
        super(builder, false);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{SURFACE_TYPE, FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face;
        int surfaceType;
        if (context.getClickedFace() == Direction.UP) {
            surfaceType = 1;
            face = context.getHorizontalDirection().getOpposite();
        } else if (context.getClickedFace() == Direction.DOWN) {
            surfaceType = 2;
            face = context.getHorizontalDirection().getOpposite();
        } else {
            surfaceType = 3;
            face = context.getClickedFace();
        }
        return (BlockState)((BlockState)super.getStateForPlacement(context).setValue(FACING, face)).setValue(SURFACE_TYPE, Integer.valueOf(surfaceType));
    }
}

