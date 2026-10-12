/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.EntityCollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.common.util.FakePlayer
 */
package com.mna.blocks.artifice;

import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.tools.BlockUtilities;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.ConstructWorkbenchTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.items.ItemInit;
import com.mna.items.constructs.BellOfBidding;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;

public class ConstructWorkbenchBlock
extends WaterloggableBlockWithOffset
implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_PART = BooleanProperty.create((String)"has_construct_part");
    private final BlockPos[] offsetsWE = new BlockPos[]{new BlockPos(0, 0, -1), new BlockPos(0, 0, 1), new BlockPos(0, 1, 0), new BlockPos(0, 1, -1), new BlockPos(0, 1, 1), new BlockPos(0, 2, 0), new BlockPos(0, 2, -1), new BlockPos(0, 2, 1)};
    private final BlockPos[] offsetsNS = new BlockPos[]{new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 1, 0), new BlockPos(-1, 1, 0), new BlockPos(1, 1, 0), new BlockPos(0, 2, 0), new BlockPos(-1, 2, 0), new BlockPos(1, 2, 0)};
    private static final VoxelShape TOP_RIGHT_CORNER = Shapes.or((VoxelShape)Block.box((double)0.0, (double)0.0, (double)5.0, (double)7.0, (double)9.0, (double)11.0), (VoxelShape[])new VoxelShape[]{Block.box((double)7.0, (double)4.0, (double)5.0, (double)12.0, (double)16.0, (double)11.0), Block.box((double)7.0, (double)9.0, (double)5.0, (double)16.0, (double)16.0, (double)11.0)});
    private static final VoxelShape TOP_LEFT_CORNER = Shapes.or((VoxelShape)Block.box((double)0.0, (double)8.0, (double)5.0, (double)7.0, (double)16.0, (double)11.0), (VoxelShape[])new VoxelShape[]{Block.box((double)4.0, (double)4.0, (double)5.0, (double)9.0, (double)16.0, (double)11.0), Block.box((double)7.0, (double)0.0, (double)5.0, (double)16.0, (double)9.0, (double)11.0)});
    private static final VoxelShape MIDDLE_LEFT = Shapes.or((VoxelShape)Block.box((double)8.0, (double)0.0, (double)3.0, (double)14.0, (double)2.0, (double)13.0), (VoxelShape)Block.box((double)8.0, (double)0.0, (double)5.0, (double)14.0, (double)16.0, (double)11.0));
    private static final VoxelShape MIDDLE_RIGHT = Shapes.or((VoxelShape)Block.box((double)2.0, (double)0.0, (double)3.0, (double)8.0, (double)2.0, (double)13.0), (VoxelShape)Block.box((double)2.0, (double)0.0, (double)5.0, (double)8.0, (double)16.0, (double)11.0));
    private static final VoxelShape BOTTOM_LEFT = Block.box((double)7.0, (double)0.0, (double)0.0, (double)15.0, (double)16.0, (double)16.0);
    private static final VoxelShape BOTTOM_RIGHT = Block.box((double)1.0, (double)0.0, (double)0.0, (double)9.0, (double)16.0, (double)16.0);
    private static final VoxelShape TOP_CENTER = Block.box((double)0.0, (double)9.5, (double)5.0, (double)16.0, (double)16.0, (double)11.0);
    private static final VoxelShape CENTER_THIN_NORTH = Block.box((double)0.0, (double)0.0, (double)5.0, (double)16.0, (double)16.0, (double)11.0);
    private static final VoxelShape[] CENTERS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, CENTER_THIN_NORTH), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, CENTER_THIN_NORTH), CENTER_THIN_NORTH, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, CENTER_THIN_NORTH)};
    private static final VoxelShape[] TOP_RIGHT_CORNERS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, TOP_RIGHT_CORNER), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, TOP_RIGHT_CORNER), TOP_RIGHT_CORNER, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, TOP_RIGHT_CORNER)};
    private static final VoxelShape[] TOP_LEFT_CORNERS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, TOP_LEFT_CORNER), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, TOP_LEFT_CORNER), TOP_LEFT_CORNER, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, TOP_LEFT_CORNER)};
    private static final VoxelShape[] MIDDLE_LEFTS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, MIDDLE_LEFT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, MIDDLE_LEFT), MIDDLE_LEFT, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, MIDDLE_LEFT)};
    private static final VoxelShape[] MIDDLE_RIGHTS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, MIDDLE_RIGHT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, MIDDLE_RIGHT), MIDDLE_RIGHT, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, MIDDLE_RIGHT)};
    private static final VoxelShape[] BOTTOM_LEFTS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, BOTTOM_LEFT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, BOTTOM_LEFT), BOTTOM_LEFT, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, BOTTOM_LEFT)};
    private static final VoxelShape[] BOTTOM_RIGHTS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, BOTTOM_RIGHT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, BOTTOM_RIGHT), BOTTOM_RIGHT, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, BOTTOM_RIGHT)};
    private static final VoxelShape[] TOP_CENTERS = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, TOP_CENTER), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, TOP_CENTER), TOP_CENTER, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, TOP_CENTER)};
    private static final VoxelShape[] MIDDLE_LEFTS_FULL = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_LEFT)), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_LEFT)), Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_LEFT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_LEFT))};
    private static final VoxelShape[] MIDDLE_RIGHTS_FULL = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_RIGHT)), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_RIGHT)), Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_RIGHT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)MIDDLE_RIGHT))};
    private static final VoxelShape[] BOTTOM_LEFTS_FULL = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_LEFT)), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_LEFT)), Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_LEFT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_LEFT))};
    private static final VoxelShape[] BOTTOM_RIGHTS_FULL = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_RIGHT)), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_RIGHT)), Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_RIGHT), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)BOTTOM_RIGHT))};
    private static final VoxelShape[] TOP_CENTERS_FULL = new VoxelShape[]{BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)TOP_CENTER)), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)TOP_CENTER)), Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)TOP_CENTER), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, Shapes.or((VoxelShape)CENTER_THIN_NORTH, (VoxelShape)TOP_CENTER))};

    public ConstructWorkbenchBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().isSuffocating(BlockInit.NEVER).strength(3.0f).lightLevel(b -> 15), false, new BlockPos[0]);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(HAS_PART, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
        builder.add(new Property[]{HAS_PART});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        BlockPos pos = context.getClickedPos();
        for (BlockPos offset : this.getOffsets(state)) {
            FluidState flState;
            BlockPos offsetPos = pos.offset((Vec3i)offset);
            if (context.getLevel().isEmptyBlock(offsetPos) || (flState = context.getLevel().getFluidState(offsetPos)) != null && !flState.isEmpty() && (flState.getType() == Fluids.WATER || flState.getType() == Fluids.FLOWING_WATER)) continue;
            return null;
        }
        return state;
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConstructWorkbenchTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.CONSTRUCT_WORKBENCH.get() ? (lvl, pos, state1, be) -> ConstructWorkbenchTile.Tick(lvl, pos, state1, (ConstructWorkbenchTile)be) : null;
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.sidedSuccess((boolean)worldIn.isClientSide);
        }
        if (player instanceof FakePlayer) {
            return InteractionResult.FAIL;
        }
        ItemStack activeStack = player.getItemInHand(handIn);
        ConstructWorkbenchTile te = (ConstructWorkbenchTile)worldIn.getBlockEntity(pos);
        if (te == null) {
            return InteractionResult.FAIL;
        }
        if (activeStack.isEmpty()) {
            if (te.startCrafting(player)) {
                return InteractionResult.SUCCESS;
            }
        } else if (activeStack.getItem() == ItemInit.ANIMUS_DUST.get() && te.summonConstructImmediately(player)) {
            if (!player.isCreative()) {
                activeStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        if (!(activeStack.getItem() instanceof ItemConstructPart)) {
            ItemStack pop = te.popPart();
            if (!pop.isEmpty() && !player.addItem(pop)) {
                player.spawnAtLocation(pop);
            }
            return InteractionResult.SUCCESS;
        }
        ItemStack result = te.placePart(activeStack);
        if (result.equals(activeStack)) {
            return InteractionResult.PASS;
        }
        if (!player.isCreative()) {
            activeStack.shrink(1);
        }
        if (!result.isEmpty() && !player.addItem(result)) {
            player.spawnAtLocation(result);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.dropInventory(worldIn, pos, null);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private void dropInventory(Level world, BlockPos pos, @Nullable BlockEntity te) {
        if (world.isClientSide) {
            return;
        }
        ConstructWorkbenchTile wb = (ConstructWorkbenchTile)(te != null ? te : world.getBlockEntity(pos));
        if (wb != null) {
            for (ConstructSlot slot : ConstructSlot.values()) {
                wb.getConstruct().getPart(slot).ifPresent(p -> {
                    ItemEntity ie = new ItemEntity(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), new ItemStack((ItemLike)p));
                    ie.setDefaultPickUpDelay();
                    world.addFreshEntity((Entity)ie);
                });
            }
        }
    }

    @Override
    protected BlockPos[] getOffsets(BlockState state) {
        Direction dir = (Direction)state.getValue((Property)FACING);
        if (dir == Direction.EAST || dir == Direction.WEST) {
            return this.offsetsWE;
        }
        return this.offsetsNS;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Player player;
        EntityCollisionContext ecc;
        Entity entity;
        boolean full = (Boolean)pState.getValue((Property)HAS_PART);
        Direction dir = (Direction)pState.getValue((Property)FACING);
        if (pContext instanceof EntityCollisionContext && (entity = (ecc = (EntityCollisionContext)pContext).getEntity()) instanceof Player && ((player = (Player)entity).getMainHandItem().getItem() instanceof ItemConstructPart || player.getOffhandItem().getItem() instanceof ItemConstructPart || player.getMainHandItem().getItem() instanceof BellOfBidding || player.getOffhandItem().getItem() instanceof BellOfBidding)) {
            full = true;
        }
        if (full) {
            return CENTERS[dir.get2DDataValue() % CENTERS.length];
        }
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState state, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        Player player;
        EntityCollisionContext ecc;
        Entity entity;
        VoxelShape[] voxelShapeArray;
        boolean full = (Boolean)state.getValue((Property)HAS_PART);
        Direction dir = (Direction)state.getValue((Property)FACING);
        if (full) {
            voxelShapeArray = CENTERS;
        } else {
            VoxelShape[] voxelShapeArray2 = new VoxelShape[1];
            voxelShapeArray = voxelShapeArray2;
            voxelShapeArray2[0] = Shapes.empty();
        }
        VoxelShape[] lookup = voxelShapeArray;
        if (pContext instanceof EntityCollisionContext && (entity = (ecc = (EntityCollisionContext)pContext).getEntity()) instanceof Player && ((player = (Player)entity).getMainHandItem().getItem() instanceof ItemConstructPart || player.getOffhandItem().getItem() instanceof ItemConstructPart || player.getMainHandItem().getItem() instanceof BellOfBidding || player.getOffhandItem().getItem() instanceof BellOfBidding)) {
            full = true;
        }
        int deltaH = delta.getX() + delta.getZ();
        if (dir == Direction.SOUTH || dir == Direction.WEST) {
            deltaH *= -1;
        }
        block0 : switch (deltaH) {
            case -1: {
                switch (delta.getY()) {
                    case 0: {
                        lookup = full ? BOTTOM_RIGHTS_FULL : BOTTOM_RIGHTS;
                        break;
                    }
                    case 1: {
                        lookup = full ? MIDDLE_RIGHTS_FULL : MIDDLE_RIGHTS;
                        break;
                    }
                    case 2: {
                        lookup = TOP_RIGHT_CORNERS;
                    }
                }
                break;
            }
            case 0: {
                switch (delta.getY()) {
                    case 1: {
                        if (!full) break;
                        lookup = CENTERS;
                        break;
                    }
                    case 2: {
                        lookup = full ? TOP_CENTERS_FULL : TOP_CENTERS;
                    }
                }
                break;
            }
            case 1: {
                switch (delta.getY()) {
                    case 0: {
                        lookup = full ? BOTTOM_LEFTS_FULL : BOTTOM_LEFTS;
                        break block0;
                    }
                    case 1: {
                        lookup = full ? MIDDLE_LEFTS_FULL : MIDDLE_LEFTS;
                        break block0;
                    }
                    case 2: {
                        lookup = TOP_LEFT_CORNERS;
                    }
                }
            }
        }
        int idx = dir.get2DDataValue();
        if (idx >= lookup.length) {
            idx = 0;
        }
        return lookup[idx];
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        BlockEntity blockEntity;
        if (pLevel.isClientSide() && pEntity.getDeltaMovement().length() > 0.1 && (blockEntity = pLevel.getBlockEntity(pPos)) instanceof ConstructWorkbenchTile) {
            ConstructWorkbenchTile tile = (ConstructWorkbenchTile)blockEntity;
            tile.swing();
        }
    }
}


