/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.Container
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.PipeBlock
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.runeforging;

import com.mna.api.blocks.IManaweaveNotifiable;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.blocks.runeforging.RuneforgeBlock;
import com.mna.blocks.tileentities.PedestalTile;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ItemManaweaveBottle;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PedestalBlock
extends WaterloggableBlock
implements ICutoutBlock,
EntityBlock,
IManaweaveNotifiable {
    public static final BooleanProperty SHOW_SIGN = BooleanProperty.create((String)"show_sign");
    public static final BooleanProperty CONNECTED = BooleanProperty.create((String)"connected");
    public static final BooleanProperty BRIMSTONE = BooleanProperty.create((String)"brimstone");
    private static final Direction[] CONNECTION_DIRS = new Direction[]{Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH};
    private static final VoxelShape SHAPE = Shapes.or((VoxelShape)Block.box((double)1.5, (double)0.0, (double)1.5, (double)14.5, (double)2.0, (double)14.5), (VoxelShape[])new VoxelShape[]{Block.box((double)4.0, (double)2.0, (double)4.0, (double)12.0, (double)14.0, (double)12.0), Block.box((double)2.25, (double)14.0, (double)2.25, (double)13.75, (double)16.0, (double)13.75)});
    private final boolean showSign;

    public PedestalBlock(boolean withSign) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).noOcclusion().sound(SoundType.STONE).isSuffocating(BlockInit.NEVER), false);
        this.showSign = withSign;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)).setValue(SHOW_SIGN, Boolean.valueOf(this.showSign))).setValue(CONNECTED, Boolean.valueOf(false))).setValue(PipeBlock.NORTH, Boolean.valueOf(false))).setValue(PipeBlock.SOUTH, Boolean.valueOf(false))).setValue(PipeBlock.EAST, Boolean.valueOf(false))).setValue(PipeBlock.WEST, Boolean.valueOf(false))).setValue(BRIMSTONE, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{HorizontalDirectionalBlock.FACING, SHOW_SIGN, CONNECTED, PipeBlock.NORTH, PipeBlock.SOUTH, PipeBlock.EAST, PipeBlock.WEST, BRIMSTONE});
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalTile(pos, state);
    }

    public VoxelShape getShape(BlockState p_48816_, BlockGetter p_48817_, BlockPos p_48818_, CollisionContext p_48819_) {
        return SHAPE;
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        ItemStack activeStack = player.getItemInHand(handIn);
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof PedestalTile) {
            PedestalTile te = (PedestalTile)tileEntity;
            if (!te.isEmpty()) {
                ItemStack stack = te.removeItem(0, 1);
                if (!player.addItem(stack)) {
                    player.drop(stack, true);
                }
            } else if (!activeStack.isEmpty() && te.isEmpty()) {
                ItemStack single = activeStack.copy();
                single.setCount(1);
                te.setItem(0, single);
                if (!player.isCreative()) {
                    activeStack.shrink(1);
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        PedestalTile pt;
        ItemStack displayed;
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null && te instanceof PedestalTile && !(displayed = (pt = (PedestalTile)te).getDisplayedItem()).isEmpty()) {
            if (MATags.isItemIn(displayed.getItem(), MATags.Items.BRIGHT_PEDESTAL_ITEMS)) {
                return 15;
            }
            if (MATags.isItemIn(displayed.getItem(), MATags.Items.DIM_PEDESTAL_ITEMS)) {
                return 10;
            }
        }
        return 0;
    }

    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.dropInventory(worldIn, pos);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private BlockState updateStateBasedOnNeighbors(LevelReader world, BlockPos pos, BlockState state) {
        state = (BlockState)state.setValue(PipeBlock.EAST, Boolean.valueOf(false));
        state = (BlockState)state.setValue(PipeBlock.WEST, Boolean.valueOf(false));
        state = (BlockState)state.setValue(PipeBlock.NORTH, Boolean.valueOf(false));
        state = (BlockState)state.setValue(PipeBlock.SOUTH, Boolean.valueOf(false));
        state = (BlockState)state.setValue(CONNECTED, Boolean.valueOf(false));
        state = (BlockState)state.setValue(BRIMSTONE, Boolean.valueOf(false));
        for (Direction offset : CONNECTION_DIRS) {
            BlockState neighborState = world.getBlockState(pos.offset(offset.getNormal()));
            state = this.calculateStateForSingleNeighbor(state, neighborState, offset);
        }
        return state;
    }

    private BlockState calculateStateForSingleNeighbor(BlockState state, BlockState neighborState, Direction offset) {
        if (neighborState.getBlock() == BlockInit.RUNEFORGE.get()) {
            Direction neighborDir = (Direction)neighborState.getValue((Property)RuneforgeBlock.FACING);
            boolean didConnect = false;
            if (neighborDir == Direction.EAST || neighborDir == Direction.WEST) {
                if (offset == Direction.NORTH) {
                    state = (BlockState)state.setValue(PipeBlock.NORTH, Boolean.valueOf(true));
                    didConnect = true;
                } else if (offset == Direction.SOUTH) {
                    state = (BlockState)state.setValue(PipeBlock.SOUTH, Boolean.valueOf(true));
                    didConnect = true;
                }
            } else if (offset == Direction.EAST) {
                state = (BlockState)state.setValue(PipeBlock.EAST, Boolean.valueOf(true));
                didConnect = true;
            } else if (offset == Direction.WEST) {
                state = (BlockState)state.setValue(PipeBlock.WEST, Boolean.valueOf(true));
                didConnect = true;
            }
            if (didConnect) {
                state = (BlockState)state.setValue(CONNECTED, Boolean.valueOf(true));
                if ((Integer)neighborState.getValue((Property)RuneforgeBlock.MATERIAL) == 1) {
                    state = (BlockState)state.setValue(BRIMSTONE, Boolean.valueOf(true));
                }
            }
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        stateIn = (BlockState)stateIn.setValue(PipeBlock.EAST, Boolean.valueOf(false));
        stateIn = (BlockState)stateIn.setValue(PipeBlock.WEST, Boolean.valueOf(false));
        stateIn = (BlockState)stateIn.setValue(PipeBlock.NORTH, Boolean.valueOf(false));
        stateIn = (BlockState)stateIn.setValue(PipeBlock.SOUTH, Boolean.valueOf(false));
        stateIn = (BlockState)stateIn.setValue(CONNECTED, Boolean.valueOf(false));
        stateIn = (BlockState)stateIn.setValue(BRIMSTONE, Boolean.valueOf(false));
        stateIn = this.calculateStateForSingleNeighbor(stateIn, facingState, facing);
        stateIn = this.updateStateBasedOnNeighbors((LevelReader)worldIn, currentPos, stateIn);
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        state = this.updateStateBasedOnNeighbors((LevelReader)context.getLevel(), context.getClickedPos(), state);
        return (BlockState)state.setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighborPos, boolean isBreak) {
        BlockState updateState = this.updateStateBasedOnNeighbors((LevelReader)world, pos, state);
        if (!world.isClientSide()) {
            world.setBlock(pos, updateState, 3);
        }
        super.neighborChanged(state, world, pos, block, neighborPos, isBreak);
    }

    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        if (world.getBlockState(neighbor).getBlock() == BlockInit.RUNEFORGE.get()) {
            state.handleNeighborChanged((Level)world, pos, world.getBlockState(neighbor).getBlock(), neighbor, false);
        }
        super.onNeighborChange(state, world, pos, neighbor);
    }

    private void dropInventory(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof PedestalTile) {
            Containers.dropContents((Level)world, (BlockPos)pos, (Container)((Container)tileEntity));
        }
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof PedestalTile) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)((PedestalTile)te));
        }
        return 0;
    }

    @Override
    public boolean notify(Level world, BlockPos pos, BlockState state, List<IManaweavePattern> patterns, LivingEntity activator) {
        BlockEntity te;
        if (!world.isClientSide && (te = world.getBlockEntity(pos)) != null && te instanceof PedestalTile && ((PedestalTile)te).getItem(0).getItem() == Items.GLASS_BOTTLE) {
            ItemStack bottle = new ItemStack((ItemLike)ItemInit.MANAWEAVE_BOTTLE.get());
            ItemManaweaveBottle.setPattern(patterns.get(0), bottle);
            ((PedestalTile)te).setItem(0, bottle);
            world.sendBlockUpdated(pos, state, state, 3);
            return true;
        }
        return false;
    }
}

