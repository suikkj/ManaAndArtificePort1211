/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.CraftingTableBlock
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.pathfinder.PathComputationType
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.blocks.tileentities.SpectralTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.gui.containers.block.ContainerSpectralCraftingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpectralCraftingTableBlock
extends CraftingTableBlock
implements ITranslucentBlock,
EntityBlock,
SimpleWaterloggedBlock {
    public static final Component CONTAINER_NAME = Component.translatable((String)"container.crafting");
    public static final BooleanProperty PERMANENT = BooleanProperty.create((String)"permanent");
    public static final VoxelShape SHAPE = Shapes.or((VoxelShape)Block.box((double)5.5, (double)6.0, (double)5.5, (double)10.5, (double)10.0, (double)10.5), (VoxelShape[])new VoxelShape[]{Block.box((double)3.5, (double)10.0, (double)3.5, (double)12.5, (double)14.0, (double)12.5), Block.box((double)1.5, (double)14.0, (double)1.5, (double)14.5, (double)16.0, (double)14.5)});

    public SpectralCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion().noLootTable());
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(PERMANENT, Boolean.valueOf(false))).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpectralTile(pos, state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{PERMANENT});
        builder.add(new Property[]{BlockStateProperties.WATERLOGGED});
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate;
        BlockState base = this.defaultBlockState();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = pContext.getLevel().getBlockState(blockpos);
        base = blockstate.is((Block)this) ? (BlockState)base.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : (BlockState)base.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf((fluidstate = pContext.getLevel().getFluidState(blockpos)).getType() == Fluids.WATER));
        return base;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (((Boolean)state.getValue((Property)PERMANENT)).booleanValue()) {
            return null;
        }
        return type == TileEntityInit.SPECTRAL_TILE.get() && !level.isClientSide() ? (lvl, pos, state1, be) -> SpectralTile.ServerTick(lvl, pos, state1, (SpectralTile)be) : null;
    }

    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new ContainerSpectralCraftingTable(id, inventory, ContainerLevelAccess.create((Level)worldIn, (BlockPos)pos)), CONTAINER_NAME);
    }

    public RenderShape getRenderShape(BlockState p_60550_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}

