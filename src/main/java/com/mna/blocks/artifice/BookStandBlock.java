/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
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
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.blocks.artifice;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.tools.BlockUtilities;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.wizard_lab.BookStandTile;
import com.mna.gui.containers.providers.NamedGuideBook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BookStandBlock
extends WizardLabBlock
implements EntityBlock {
    public static final BooleanProperty BOOK = BooleanProperty.create((String)"has_book");
    private static final VoxelShape OFFSET_SHAPE_NORTH = Shapes.or((VoxelShape)Block.box((double)3.0, (double)0.0, (double)3.0, (double)13.0, (double)3.0, (double)13.0), (VoxelShape[])new VoxelShape[]{Block.box((double)2.0, (double)0.0, (double)13.0, (double)14.0, (double)3.0, (double)15.0), Block.box((double)3.5, (double)3.0, (double)13.5, (double)4.5, (double)6.0, (double)14.5), Block.box((double)11.5, (double)3.0, (double)13.5, (double)12.5, (double)6.0, (double)14.5), Block.box((double)2.5, (double)4.0, (double)13.0, (double)13.5, (double)5.0, (double)14.0)});
    private static final VoxelShape OFFSET_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_NORTH = Shapes.or((VoxelShape)Block.box((double)3.0, (double)0.0, (double)3.0, (double)13.0, (double)1.0, (double)5.0), (VoxelShape[])new VoxelShape[]{Block.box((double)3.0, (double)0.0, (double)5.0, (double)13.0, (double)3.0, (double)13.0), Block.box((double)2.0, (double)0.0, (double)13.0, (double)14.0, (double)3.0, (double)15.0), Block.box((double)3.5, (double)3.0, (double)13.5, (double)4.5, (double)6.0, (double)14.5), Block.box((double)11.5, (double)3.0, (double)13.5, (double)12.5, (double)6.0, (double)14.5), Block.box((double)2.5, (double)4.0, (double)13.0, (double)13.5, (double)5.0, (double)14.0), Block.box((double)0.0, (double)1.0, (double)4.5, (double)16.0, (double)4.5, (double)11.5)});
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_WITH_BOOK_SHAPE_NORTH);
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_WITH_BOOK_SHAPE_NORTH);
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_WITH_BOOK_SHAPE_NORTH);

    public BookStandBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).noOcclusion().strength(3.0f).isSuffocating(BlockInit.NEVER), new BlockPos(0, 1, 0));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(LEFT, Boolean.valueOf(false))).setValue(RIGHT, Boolean.valueOf(false))).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false))).setValue(BOOK, Boolean.valueOf(false)));
    }

    @Override
    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            ItemStack handItem = player.getItemInHand(hand);
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null && be instanceof BookStandTile) {
                BookStandTile book_stand = (BookStandTile)be;
                ItemStack bookStack = book_stand.getItem(0);
                if (((Boolean)state.getValue((Property)BOOK)).booleanValue() && !bookStack.isEmpty()) {
                    if (player.isCrouching()) {
                        if (!player.addItem(bookStack)) {
                            player.spawnAtLocation(bookStack);
                        }
                        level.setBlock(pos, (BlockState)state.setValue(BOOK, Boolean.valueOf(false)), 3);
                    } else if (!ManaAndArtificeMod.checkMagicUnlock((ServerLevel)level, player)) {
                        ((ServerPlayer)((ServerPlayer)player)).openMenu((MenuProvider)new NamedGuideBook());
                    }
                } else if (MATags.isItemIn(handItem.getItem(), MATags.Items.LECTERN_ALLOWED_ITEMS)) {
                    ItemStack insertStack = handItem.copy();
                    insertStack.setCount(1);
                    book_stand.setItem(0, insertStack);
                    handItem.shrink(1);
                    level.setBlock(pos, (BlockState)state.setValue(BOOK, Boolean.valueOf(true)), 3);
                }
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{BOOK});
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BookStandTile(pPos, pState);
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        switch ((Direction)parentState.getValue((Property)HorizontalDirectionalBlock.FACING)) {
            case EAST: {
                return (Boolean)parentState.getValue((Property)BOOK) != false ? OFFSET_WITH_BOOK_SHAPE_EAST : OFFSET_SHAPE_EAST;
            }
            case NORTH: {
                return (Boolean)parentState.getValue((Property)BOOK) != false ? OFFSET_WITH_BOOK_SHAPE_NORTH : OFFSET_SHAPE_NORTH;
            }
            case SOUTH: {
                return (Boolean)parentState.getValue((Property)BOOK) != false ? OFFSET_WITH_BOOK_SHAPE_SOUTH : OFFSET_SHAPE_SOUTH;
            }
            case WEST: {
                return (Boolean)parentState.getValue((Property)BOOK) != false ? OFFSET_WITH_BOOK_SHAPE_WEST : OFFSET_SHAPE_WEST;
            }
        }
        return Shapes.block();
    }
}


