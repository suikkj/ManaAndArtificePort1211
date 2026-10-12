 
package com.mna.blocks.decoration;

import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.tools.BlockUtilities;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.BookshelfTile;
import com.mna.gui.containers.block.ContainerBookshelf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BookshelfBlock
extends WaterloggableBlockWithOffset
implements EntityBlock {
    private static final Component CONTAINER_TITLE = Component.translatable((String)RLoc.create("container.bookshelf").toString());
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape BASE_OUTER_NORTH = Shapes.or((VoxelShape)Block.box((double)1.0, (double)6.0, (double)5.0, (double)2.0, (double)16.0, (double)16.0), (VoxelShape[])new VoxelShape[]{Block.box((double)14.0, (double)6.0, (double)5.0, (double)15.0, (double)16.0, (double)16.0), Block.box((double)1.0, (double)6.0, (double)15.0, (double)15.0, (double)16.0, (double)16.0), Block.box((double)0.0, (double)0.0, (double)4.0, (double)16.0, (double)5.0, (double)16.0)});
    public static final VoxelShape BASE_OUTER_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, BASE_OUTER_NORTH);
    public static final VoxelShape BASE_OUTER_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, BASE_OUTER_NORTH);
    public static final VoxelShape BASE_OUTER_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, BASE_OUTER_NORTH);
    public static final VoxelShape OFFSET_1_OUTER_NORTH = Shapes.or((VoxelShape)Block.box((double)1.0, (double)0.0, (double)15.0, (double)15.0, (double)14.0, (double)16.0), (VoxelShape[])new VoxelShape[]{Block.box((double)1.0, (double)0.0, (double)5.0, (double)2.0, (double)14.0, (double)16.0), Block.box((double)14.0, (double)0.0, (double)5.0, (double)15.0, (double)14.0, (double)16.0), Block.box((double)0.0, (double)2.0, (double)4.0, (double)3.0, (double)5.0, (double)16.0), Block.box((double)14.0, (double)2.0, (double)4.0, (double)16.0, (double)5.0, (double)16.0), Block.box((double)14.0, (double)10.0, (double)4.0, (double)16.0, (double)14.0, (double)16.0), Block.box((double)0.0, (double)10.0, (double)4.0, (double)2.0, (double)14.0, (double)16.0), Block.box((double)2.0, (double)12.0, (double)4.0, (double)14.0, (double)14.0, (double)16.0), Block.box((double)1.5, (double)14.0, (double)5.5, (double)14.5, (double)15.5, (double)14.5)});
    public static final VoxelShape OFFSET_1_OUTER_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_1_OUTER_NORTH);
    public static final VoxelShape OFFSET_1_OUTER_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_1_OUTER_NORTH);
    public static final VoxelShape OFFSET_1_OUTER_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_1_OUTER_NORTH);
    private static final VoxelShape BASE_NORTH = Shapes.or((VoxelShape)BASE_OUTER_NORTH, (VoxelShape)Block.box((double)1.0, (double)12.0, (double)6.0, (double)15.0, (double)13.0, (double)16.0));
    private static final VoxelShape BASE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, BASE_NORTH);
    private static final VoxelShape BASE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, BASE_NORTH);
    private static final VoxelShape BASE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, BASE_NORTH);
    private static final VoxelShape OFFSET_1_NORTH = Shapes.or((VoxelShape)OFFSET_1_OUTER_NORTH, (VoxelShape)Block.box((double)2.0, (double)4.0, (double)6.0, (double)14.0, (double)5.0, (double)15.0));
    private static final VoxelShape OFFSET_1_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_1_NORTH);
    private static final VoxelShape OFFSET_1_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_1_NORTH);
    private static final VoxelShape OFFSET_1_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_1_NORTH);

    public BookshelfBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion().isSuffocating(BlockInit.NEVER), false, new BlockPos(0, 1, 0));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BookshelfTile(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.dropInventory(worldIn, pos, state);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private void dropInventory(Level world, BlockPos pos, BlockState state) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof BookshelfTile) {
            Containers.dropContents((Level)world, (BlockPos)pos, (Container)((Container)tileEntity));
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
        player.openMenu(provider, (BlockPos)pos);
        return InteractionResult.SUCCESS;
    }

    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !(be instanceof BookshelfTile)) {
            return null;
        }
        return new SimpleMenuProvider((id, playerInv, user) -> new ContainerBookshelf(id, playerInv, (BookshelfTile)be), CONTAINER_TITLE);
    }

    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 1.0f;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction dir = (Direction)pState.getValue((Property)HorizontalDirectionalBlock.FACING);
        switch (dir) {
            case EAST: {
                return BASE_EAST;
            }
            case NORTH: {
                return BASE_NORTH;
            }
            case SOUTH: {
                return BASE_SOUTH;
            }
            case WEST: {
                return BASE_WEST;
            }
        }
        return Shapes.block();
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        Direction dir = (Direction)parentState.getValue((Property)HorizontalDirectionalBlock.FACING);
        switch (dir) {
            case EAST: {
                return OFFSET_1_EAST;
            }
            case NORTH: {
                return OFFSET_1_NORTH;
            }
            case SOUTH: {
                return OFFSET_1_SOUTH;
            }
            case WEST: {
                return OFFSET_1_WEST;
            }
        }
        return Shapes.block();
    }
}


