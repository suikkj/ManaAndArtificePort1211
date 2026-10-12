 
package com.mna.api.blocks;

import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.blocks.interfaces.IWizardLabConnect;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.tools.BlockUtilities;
import java.util.HashMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class WizardLabBlock
extends WaterloggableBlockWithOffset
implements SimpleWaterloggedBlock,
ICutoutBlock,
IWizardLabConnect {
    public static final BooleanProperty RIGHT = BooleanProperty.create((String)"right");
    public static final BooleanProperty LEFT = BooleanProperty.create((String)"left");
    public static final VoxelShape BASE = Shapes.or((VoxelShape)Block.box((double)0.0, (double)0.0, (double)4.0, (double)16.0, (double)2.0, (double)16.0), (VoxelShape[])new VoxelShape[]{Block.box((double)1.0, (double)2.0, (double)5.0, (double)15.0, (double)12.0, (double)15.0), Block.box((double)0.0, (double)12.0, (double)4.0, (double)16.0, (double)13.0, (double)16.0), Block.box((double)1.0, (double)13.0, (double)5.0, (double)15.0, (double)15.0, (double)15.0)});
    public static final VoxelShape LEFT_FRONT_PILLAR = Shapes.or((VoxelShape)Block.box((double)0.0, (double)0.0, (double)1.0, (double)3.0, (double)2.0, (double)4.0), (VoxelShape[])new VoxelShape[]{Block.box((double)1.0, (double)2.0, (double)2.0, (double)2.0, (double)12.0, (double)3.0), Block.box((double)0.0, (double)12.0, (double)1.0, (double)3.0, (double)13.0, (double)4.0), Block.box((double)1.0, (double)13.0, (double)2.0, (double)2.0, (double)15.0, (double)3.0)});
    public static final VoxelShape RIGHT_FRONT_PILLAR = Shapes.or((VoxelShape)Block.box((double)13.0, (double)0.0, (double)1.0, (double)16.0, (double)2.0, (double)4.0), (VoxelShape[])new VoxelShape[]{Block.box((double)14.0, (double)2.0, (double)2.0, (double)15.0, (double)12.0, (double)3.0), Block.box((double)13.0, (double)12.0, (double)1.0, (double)16.0, (double)13.0, (double)4.0), Block.box((double)14.0, (double)13.0, (double)2.0, (double)15.0, (double)15.0, (double)3.0)});
    public static final VoxelShape TABLETOP = Shapes.or((VoxelShape)Block.box((double)0.0, (double)15.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape[])new VoxelShape[0]);
    public static final VoxelShape NORTH_BASE_STANDALONE = Shapes.or((VoxelShape)BASE, (VoxelShape[])new VoxelShape[]{LEFT_FRONT_PILLAR, RIGHT_FRONT_PILLAR, TABLETOP});
    public static final VoxelShape NORTH_BASE_CONNECT_RIGHT = Shapes.or((VoxelShape)BASE, (VoxelShape[])new VoxelShape[]{LEFT_FRONT_PILLAR, TABLETOP});
    public static final VoxelShape NORTH_BASE_CONNECT_LEFT = Shapes.or((VoxelShape)BASE, (VoxelShape[])new VoxelShape[]{RIGHT_FRONT_PILLAR, TABLETOP});
    public static final VoxelShape NORTH_BASE_CONNECT_BOTH = Shapes.or((VoxelShape)BASE, (VoxelShape)TABLETOP);
    public static final HashMap<Direction, VoxelShape[]> VOXEL_LOOKUP = new HashMap();

    public WizardLabBlock(BlockBehaviour.Properties blockProperties, BlockPos ... offsets) {
        super(blockProperties, false, offsets);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(LEFT, Boolean.valueOf(false))).setValue(RIGHT, Boolean.valueOf(false)));
    }

    @Nullable
    protected abstract MenuProvider getProvider(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, BlockHitResult var6);

    protected Consumer<FriendlyByteBuf> getContainerBufferWriter(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    public boolean canConnectToDirection(BlockState thisState, BlockState otherState, Direction dir) {
        if (dir == Direction.UP || dir == Direction.DOWN) {
            return false;
        }
        return otherState.getBlock() instanceof IWizardLabConnect && this.directionToLeftRightProperty(thisState, dir) != null;
    }

    protected boolean shouldPlayAmbient(BlockState state, Level world, BlockPos pos, RandomSource rnd) {
        return false;
    }

    protected SoundEvent getAmbientSound() {
        return null;
    }

    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rnd) {
        if (this.shouldPlayAmbient(state, world, pos, rnd) && this.getAmbientSound() != null) {
            double d0 = (double)pos.getX() + 0.5;
            double d1 = pos.getY();
            double d2 = (double)pos.getZ() + 0.5;
            if (rnd.nextDouble() < 0.1) {
                world.playLocalSound(d0, d1, d2, this.getAmbientSound(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            }
        }
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider provider = this.getProvider(state, level, pos, player, hand, hitResult);
        if (provider == null) {
            return InteractionResult.SUCCESS;
        }
        Consumer<FriendlyByteBuf> dataWriter = this.getContainerBufferWriter(state, level, pos, player, hand, hitResult);
        if (dataWriter == null) {
            player.openMenu(provider, (BlockPos)pos);
        } else {
            player.openMenu(provider, (java.util.function.Consumer)dataWriter);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{LEFT, RIGHT, HorizontalDirectionalBlock.FACING});
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return (Boolean)state.getValue((Property)BlockStateProperties.WATERLOGGED) == false;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue((Property)BlockStateProperties.WATERLOGGED) != false ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter getter, BlockPos pos, PathComputationType pathType) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction blockFacingDirection = context.getHorizontalDirection().getOpposite();
        Level blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction leftDir = blockFacingDirection.getCounterClockWise();
        Direction rightDir = blockFacingDirection.getClockWise();
        BlockState leftNeighborState = blockgetter.getBlockState(blockpos.offset(leftDir.getStepX(), leftDir.getStepY(), leftDir.getStepZ()));
        BlockState rightNeighborState = blockgetter.getBlockState(blockpos.offset(rightDir.getStepX(), rightDir.getStepY(), rightDir.getStepZ()));
        BlockState baseState = super.getStateForPlacement(context);
        BlockState disconnectedState = (BlockState)baseState.setValue(HorizontalDirectionalBlock.FACING, blockFacingDirection);
        return (BlockState)((BlockState)((BlockState)baseState.setValue(HorizontalDirectionalBlock.FACING, blockFacingDirection)).setValue(LEFT, Boolean.valueOf(this.canConnectToDirection(disconnectedState, leftNeighborState, leftDir)))).setValue(RIGHT, Boolean.valueOf(this.canConnectToDirection(disconnectedState, rightNeighborState, rightDir)));
    }

    @Override
    public BlockState updateShape(BlockState myState, Direction direction, BlockState otherState, LevelAccessor accessor, BlockPos myPos, BlockPos otherPos) {
        BooleanProperty prop;
        if (((Boolean)myState.getValue((Property)BlockStateProperties.WATERLOGGED)).booleanValue()) {
            accessor.scheduleTick(myPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)accessor));
        }
        return (prop = this.directionToLeftRightProperty(myState, direction)) != null ? (BlockState)myState.setValue(prop, Boolean.valueOf(this.canConnectToDirection(myState, otherState, direction))) : myState;
    }

    @Nullable
    private BooleanProperty directionToLeftRightProperty(BlockState myState, Direction direction) {
        Direction myDir = (Direction)myState.getValue((Property)HorizontalDirectionalBlock.FACING);
        Direction myLeft = myDir.getCounterClockWise();
        Direction myRight = myDir.getClockWise();
        if (direction == myLeft) {
            return LEFT;
        }
        if (direction == myRight) {
            return RIGHT;
        }
        return null;
    }

    public static Vec3i getOffsetLeft(BlockState myState) {
        Direction myDir = (Direction)myState.getValue((Property)HorizontalDirectionalBlock.FACING);
        Direction myLeft = myDir.getCounterClockWise();
        return myLeft.getNormal();
    }

    public static Vec3i getOffsetRight(BlockState myState) {
        Direction myDir = (Direction)myState.getValue((Property)HorizontalDirectionalBlock.FACING);
        Direction myRight = myDir.getClockWise();
        return myRight.getNormal();
    }

    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.dropInventory(worldIn, pos);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private void dropInventory(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityWithInventory) {
            Containers.dropContents((Level)world, (BlockPos)pos, (Container)((Container)tileEntity));
        }
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int connectedLeft = (Boolean)pState.getValue((Property)LEFT) != false ? 2 : 0;
        int connectedRight = (Boolean)pState.getValue((Property)RIGHT) != false ? 1 : 0;
        Direction facing = (Direction)pState.getValue((Property)HorizontalDirectionalBlock.FACING);
        VoxelShape[] voxels = VOXEL_LOOKUP.getOrDefault(facing, new VoxelShape[]{Shapes.block()});
        int connectionIndex = connectedRight | connectedLeft;
        return voxels[connectionIndex % voxels.length];
    }

    static {
        VOXEL_LOOKUP.put(Direction.NORTH, new VoxelShape[]{NORTH_BASE_STANDALONE, NORTH_BASE_CONNECT_RIGHT, NORTH_BASE_CONNECT_LEFT, NORTH_BASE_CONNECT_BOTH});
        VOXEL_LOOKUP.put(Direction.SOUTH, new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, NORTH_BASE_STANDALONE), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, NORTH_BASE_CONNECT_RIGHT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, NORTH_BASE_CONNECT_LEFT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, NORTH_BASE_CONNECT_BOTH)});
        VOXEL_LOOKUP.put(Direction.EAST, new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, NORTH_BASE_STANDALONE), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, NORTH_BASE_CONNECT_RIGHT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, NORTH_BASE_CONNECT_LEFT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, NORTH_BASE_CONNECT_BOTH)});
        VOXEL_LOOKUP.put(Direction.WEST, new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, NORTH_BASE_STANDALONE), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, NORTH_BASE_CONNECT_RIGHT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, NORTH_BASE_CONNECT_LEFT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, NORTH_BASE_CONNECT_BOTH)});
    }
}


