/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.blocks.BlockWithOffset;
import com.mna.blocks.tileentities.WellspringPillarTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WellspringPillarBlock
extends BlockWithOffset
implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape BASE = Shapes.or((VoxelShape)Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)4.0, (double)16.0), (VoxelShape)Block.box((double)4.0, (double)4.0, (double)4.0, (double)12.0, (double)16.0, (double)12.0));
    private static final VoxelShape CENTER = Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)16.0, (double)12.0);
    private static final VoxelShape CAP = Shapes.or((VoxelShape)Block.box((double)0.0, (double)12.0, (double)0.0, (double)16.0, (double)20.0, (double)16.0), (VoxelShape)Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)12.0, (double)12.0));

    public WellspringPillarBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().strength(3.0f), new BlockPos(0, 1, 0), new BlockPos(0, 2, 0), new BlockPos(0, 3, 0), new BlockPos(0, 4, 0), new BlockPos(0, 5, 0));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getHorizontalDirection().getOpposite();
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return (BlockState)state.setValue(FACING, face);
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return 15;
    }

    public int getLightBlock(BlockState p_200011_1_, BlockGetter p_200011_2_, BlockPos p_200011_3_) {
        return 0;
    }

    public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
        return false;
    }

    public boolean useShapeForLightOcclusion(BlockState p_220074_1_) {
        return false;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WellspringPillarTile(pos, state);
    }

    public RenderShape getRenderShape(BlockState p_149645_1_) {
        return RenderShape.MODEL;
    }

    public VoxelShape getOcclusionShape(BlockState p_196247_1_, BlockGetter p_196247_2_, BlockPos p_196247_3_) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        switch (delta.getY()) {
            case 1: {
                return BASE;
            }
            case 2: 
            case 3: 
            case 4: {
                return CENTER;
            }
            case 5: {
                return CAP;
            }
        }
        return Shapes.block();
    }
}

