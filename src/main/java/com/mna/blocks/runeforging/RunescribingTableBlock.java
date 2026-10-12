/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
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

import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.tools.BlockUtilities;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.wizard_lab.RunescribingTableTile;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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

public class RunescribingTableBlock
extends WizardLabBlock
implements ICutoutBlock,
EntityBlock {
    public static final BooleanProperty HAS_CHISEL = BooleanProperty.create((String)"has_chisel");
    public static final BooleanProperty HAS_HAMMER = BooleanProperty.create((String)"has_hammer");
    private static final VoxelShape CHISEL = Block.box((double)9.0, (double)1.25, (double)13.0, (double)10.0, (double)8.5, (double)14.0);
    private static final VoxelShape HAMMER = Shapes.or((VoxelShape)Block.box((double)6.0, (double)1.25, (double)13.0, (double)7.0, (double)6.0, (double)14.0), (VoxelShape)Block.box((double)5.0, (double)6.0, (double)12.5, (double)8.0, (double)7.5, (double)14.5));
    private static final VoxelShape OFFSET_SHAPE_NORTH = Shapes.or((VoxelShape)Block.box((double)6.0, (double)0.0, (double)2.0, (double)14.0, (double)0.5, (double)10.0), (VoxelShape[])new VoxelShape[]{Block.box((double)2.0, (double)0.0, (double)8.0, (double)5.0, (double)1.0, (double)11.0), Block.box((double)4.0, (double)0.0, (double)12.0, (double)5.0, (double)1.0, (double)16.0), Block.box((double)11.0, (double)0.0, (double)12.0, (double)12.0, (double)1.0, (double)16.0), Block.box((double)3.0, (double)0.0, (double)14.0, (double)13.0, (double)1.0, (double)15.0), Block.box((double)4.0, (double)4.0, (double)14.0, (double)12.0, (double)5.0, (double)15.0), Block.box((double)4.0, (double)0.0, (double)14.0, (double)5.0, (double)5.0, (double)15.0), Block.box((double)11.0, (double)0.0, (double)14.0, (double)12.0, (double)5.0, (double)15.0)});
    private static final VoxelShape OFFSET_SHAPE_NORTH_CHISEL = Shapes.or((VoxelShape)OFFSET_SHAPE_NORTH, (VoxelShape)CHISEL);
    private static final VoxelShape OFFSET_SHAPE_NORTH_HAMMER = Shapes.or((VoxelShape)OFFSET_SHAPE_NORTH, (VoxelShape)HAMMER);
    private static final VoxelShape OFFSET_SHAPE_NORTH_HAMMER_CHISEL = Shapes.or((VoxelShape)OFFSET_SHAPE_NORTH, (VoxelShape[])new VoxelShape[]{HAMMER, CHISEL});
    private static final VoxelShape OFFSET_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_EAST_CHISEL = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH_CHISEL);
    private static final VoxelShape OFFSET_SHAPE_EAST_HAMMER = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH_HAMMER);
    private static final VoxelShape OFFSET_SHAPE_EAST_HAMMER_CHISEL = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH_HAMMER_CHISEL);
    private static final VoxelShape OFFSET_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_SOUTH_CHISEL = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH_CHISEL);
    private static final VoxelShape OFFSET_SHAPE_SOUTH_HAMMER = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH_HAMMER);
    private static final VoxelShape OFFSET_SHAPE_SOUTH_HAMMER_CHISEL = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH_HAMMER_CHISEL);
    private static final VoxelShape OFFSET_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_WEST_CHISEL = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH_CHISEL);
    private static final VoxelShape OFFSET_SHAPE_WEST_HAMMER = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH_HAMMER);
    private static final VoxelShape OFFSET_SHAPE_WEST_HAMMER_CHISEL = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH_HAMMER_CHISEL);

    public RunescribingTableBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0f).noOcclusion().isSuffocating(BlockInit.NEVER), new BlockPos(0, 1, 0));
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue((Property)HAS_CHISEL, (Comparable)Boolean.valueOf(false))).setValue((Property)HAS_HAMMER, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{HAS_CHISEL});
        builder.add(new Property[]{HAS_HAMMER});
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunescribingTableTile(pos, state);
    }

    @Override
    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity workbench = level.getBlockEntity(pos);
        if (workbench != null && workbench instanceof RunescribingTableTile) {
            return (RunescribingTableTile)workbench;
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        if (player.isCrouching()) {
            if (worldIn.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            ItemStack activeStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);
            BlockEntity be = worldIn.getBlockEntity(pos);
            if (be != null && be instanceof RunescribingTableTile) {
                RunescribingTableTile tile = (RunescribingTableTile)be;
                ItemStack hammerStack = tile.getItem(0);
                if (hammerStack.isEmpty()) {
                    if (activeStack.getItem() == ItemInit.RUNESMITH_HAMMER.get()) {
                        tile.setItem(0, activeStack.copy());
                        activeStack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                    if (offhandStack.getItem() == ItemInit.RUNESMITH_HAMMER.get()) {
                        tile.setItem(0, offhandStack.copy());
                        offhandStack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                } else if (hammerStack.getItem() == ItemInit.RUNESMITH_HAMMER.get()) {
                    tile.setItem(0, ItemStack.EMPTY);
                    if (!player.addItem(hammerStack.copy())) {
                        player.drop(hammerStack.copy(), true);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.use(state, worldIn, pos, player, handIn, p_225533_6_);
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        Direction dir = (Direction)parentState.getValue((Property)HorizontalDirectionalBlock.FACING);
        boolean hasHammer = (Boolean)parentState.getValue((Property)HAS_HAMMER);
        boolean hasChisel = (Boolean)parentState.getValue((Property)HAS_CHISEL);
        switch (dir) {
            case EAST: {
                if (hasHammer != hasChisel) {
                    return hasHammer ? OFFSET_SHAPE_EAST_HAMMER : OFFSET_SHAPE_EAST_CHISEL;
                }
                return hasHammer ? OFFSET_SHAPE_EAST_HAMMER_CHISEL : OFFSET_SHAPE_EAST;
            }
            case NORTH: {
                if (hasHammer != hasChisel) {
                    return hasHammer ? OFFSET_SHAPE_NORTH_HAMMER : OFFSET_SHAPE_NORTH_CHISEL;
                }
                return hasHammer ? OFFSET_SHAPE_NORTH_HAMMER_CHISEL : OFFSET_SHAPE_NORTH;
            }
            case SOUTH: {
                if (hasHammer != hasChisel) {
                    return hasHammer ? OFFSET_SHAPE_SOUTH_HAMMER : OFFSET_SHAPE_SOUTH_CHISEL;
                }
                return hasHammer ? OFFSET_SHAPE_SOUTH_HAMMER_CHISEL : OFFSET_SHAPE_SOUTH;
            }
            case WEST: {
                if (hasHammer != hasChisel) {
                    return hasHammer ? OFFSET_SHAPE_WEST_HAMMER : OFFSET_SHAPE_WEST_CHISEL;
                }
                return hasHammer ? OFFSET_SHAPE_WEST_HAMMER_CHISEL : OFFSET_SHAPE_WEST;
            }
        }
        return Shapes.block();
    }
}

