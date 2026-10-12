/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.WebBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.pathfinder.PathComputationType
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.blocks.sorcery.SpectralCraftingTableBlock;
import com.mna.blocks.tileentities.SimpleSpectralTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class SpectralWebBlock
extends WebBlock
implements EntityBlock,
ITranslucentBlock,
SimpleWaterloggedBlock {
    public SpectralWebBlock(BlockBehaviour.Properties blockProps) {
        super(blockProps);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(SpectralCraftingTableBlock.PERMANENT, Boolean.valueOf(false))).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{SpectralCraftingTableBlock.PERMANENT});
        builder.add(new Property[]{BlockStateProperties.WATERLOGGED});
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate;
        BlockState base = super.getStateForPlacement(pContext);
        if (base == null) {
            return base;
        }
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = pContext.getLevel().getBlockState(blockpos);
        base = blockstate.is((Block)this) ? (BlockState)base.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : (BlockState)base.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf((fluidstate = pContext.getLevel().getFluidState(blockpos)).getType() == Fluids.WATER));
        return base;
    }

    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return true;
    }

    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 10000;
    }

    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 10000;
    }

    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SimpleSpectralTile(pPos, pState);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (((Boolean)state.getValue((Property)SpectralCraftingTableBlock.PERMANENT)).booleanValue()) {
            return null;
        }
        return type == TileEntityInit.SIMPLE_SPECTRAL_TILE.get() && !level.isClientSide() ? (lvl, pos, state1, be) -> SimpleSpectralTile.ServerTick(lvl, pos, state1, (SimpleSpectralTile)be) : null;
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue((Property)BlockStateProperties.WATERLOGGED) != false ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    public boolean placeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        return super.placeLiquid(worldIn, pos, state, fluidStateIn);
    }

    public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        return super.canPlaceLiquid(worldIn, pos, state, fluidIn);
    }

    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (((Boolean)stateIn.getValue((Property)BlockStateProperties.WATERLOGGED)).booleanValue()) {
            worldIn.scheduleTick(currentPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)worldIn));
        }
        return stateIn;
    }

    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        switch (type) {
            case LAND: {
                return false;
            }
            case WATER: {
                return worldIn.getFluidState(pos).is(FluidTags.WATER);
            }
            case AIR: {
                return false;
            }
        }
        return false;
    }
}

