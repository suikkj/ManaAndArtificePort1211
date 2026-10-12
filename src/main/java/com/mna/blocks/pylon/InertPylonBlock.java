/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.pylon;

import com.mna.api.blocks.WaterloggableBlockWithOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InertPylonBlock
extends WaterloggableBlockWithOffset {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape MAIN_WE = Block.box((double)4.0, (double)0.0, (double)0.0, (double)12.0, (double)16.0, (double)16.0);
    private static final VoxelShape MAIN_NS = Block.box((double)0.0, (double)0.0, (double)4.0, (double)16.0, (double)16.0, (double)12.0);
    private static final VoxelShape OFFSET_1_WE = Block.box((double)5.0, (double)0.0, (double)0.0, (double)11.0, (double)16.0, (double)16.0);
    private static final VoxelShape OFFSET_1_NS = Block.box((double)0.0, (double)0.0, (double)5.0, (double)16.0, (double)16.0, (double)11.0);
    private static final VoxelShape OFFSET_2_WE = Block.box((double)5.0, (double)0.0, (double)2.0, (double)11.0, (double)16.0, (double)14.0);
    private static final VoxelShape OFFSET_2_NS = Block.box((double)2.0, (double)0.0, (double)5.0, (double)14.0, (double)16.0, (double)11.0);

    public InertPylonBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties, false, new BlockPos(0, 1, 0), new BlockPos(0, 2, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)super.getStateForPlacement(context).setValue((Property)FACING, (Comparable)context.getHorizontalDirection().getOpposite());
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = (Direction)pState.getValue((Property)FACING);
        boolean sideways = facing == Direction.EAST || facing == Direction.WEST;
        return sideways ? MAIN_WE : MAIN_NS;
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState state, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        boolean sideways = facing == Direction.EAST || facing == Direction.WEST;
        switch (delta.getY()) {
            case 1: {
                return sideways ? OFFSET_1_WE : OFFSET_1_NS;
            }
            case 2: {
                return sideways ? OFFSET_2_WE : OFFSET_2_NS;
            }
        }
        return Shapes.block();
    }
}

