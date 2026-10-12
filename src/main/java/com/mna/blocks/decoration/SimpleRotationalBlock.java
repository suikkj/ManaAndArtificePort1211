/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.Mirror
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.blocks.decoration;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class SimpleRotationalBlock
extends Block {
    public static final IntegerProperty SURFACE_TYPE = IntegerProperty.create((String)"surface", (int)1, (int)3);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public SimpleRotationalBlock(BlockBehaviour.Properties p_i48377_1_) {
        super(p_i48377_1_);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{SURFACE_TYPE, FACING});
    }

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

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation((Direction)pState.getValue((Property)FACING)));
    }

    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return (BlockState)pState.setValue(FACING, pRotation.rotate((Direction)pState.getValue(FACING)));
    }
}

