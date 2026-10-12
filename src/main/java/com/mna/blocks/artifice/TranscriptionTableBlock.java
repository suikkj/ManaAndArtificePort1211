/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.tools.BlockUtilities;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.TranscriptionTableTile;
import com.mna.gui.containers.block.ContainerTranscriptionTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TranscriptionTableBlock
extends WizardLabBlock
implements EntityBlock {
    private static final Component CONTAINER_TITLE = Component.translatable((String)RLoc.create("container.transcription_table").toString());
    private static final VoxelShape BOOK = Block.box((double)1.5, (double)4.0, (double)10.0, (double)14.5, (double)11.0, (double)14.0);
    private static final VoxelShape OFFSET_SHAPE_NORTH = Shapes.or((VoxelShape)Block.box((double)1.0, (double)0.0, (double)1.0, (double)3.0, (double)1.0, (double)3.0), (VoxelShape[])new VoxelShape[]{Block.box((double)13.0, (double)0.0, (double)1.0, (double)15.0, (double)1.0, (double)3.0), Block.box((double)3.0, (double)0.0, (double)11.0, (double)5.0, (double)1.0, (double)13.0), Block.box((double)11.0, (double)0.0, (double)11.0, (double)13.0, (double)1.0, (double)13.0), Block.box((double)3.5, (double)1.0, (double)11.5, (double)4.5, (double)3.0, (double)12.5), Block.box((double)11.5, (double)1.0, (double)11.5, (double)12.5, (double)3.0, (double)12.5), Block.box((double)7.0, (double)0.0, (double)14.0, (double)9.0, (double)1.0, (double)16.0), Block.box((double)7.5, (double)1.0, (double)14.5, (double)8.5, (double)6.0, (double)15.5), Block.box((double)6.0, (double)5.0, (double)14.25, (double)10.0, (double)7.1, (double)15.75), Block.box((double)2.0, (double)3.25, (double)10.0, (double)14.0, (double)4.25, (double)12.5), Block.box((double)2.0, (double)2.4, (double)11.5, (double)14.0, (double)3.25, (double)14.0)});
    private static final VoxelShape OFFSET_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_NORTH_BOOK = Shapes.or((VoxelShape)OFFSET_SHAPE_NORTH, (VoxelShape)BOOK);
    private static final VoxelShape OFFSET_SHAPE_EAST_BOOK = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH_BOOK);
    private static final VoxelShape OFFSET_SHAPE_SOUTH_BOOK = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH_BOOK);
    private static final VoxelShape OFFSET_SHAPE_WEST_BOOK = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH_BOOK);

    public TranscriptionTableBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).noOcclusion().strength(3.0f).isSuffocating(BlockInit.NEVER), new BlockPos(0, 1, 0));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TranscriptionTableTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.TRANSCRIPTION_TABLE.get() ? (lvl, pos, state1, be) -> TranscriptionTableTile.Tick(lvl, pos, state1, (TranscriptionTableTile)be) : null;
    }

    @Override
    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !(be instanceof TranscriptionTableTile)) {
            return null;
        }
        return new SimpleMenuProvider((id, playerInv, user) -> new ContainerTranscriptionTable(id, playerInv, (TranscriptionTableTile)be), CONTAINER_TITLE);
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        switch ((Direction)parentState.getValue((Property)HorizontalDirectionalBlock.FACING)) {
            case EAST: {
                return OFFSET_SHAPE_EAST;
            }
            case NORTH: {
                return OFFSET_SHAPE_NORTH;
            }
            case SOUTH: {
                return OFFSET_SHAPE_SOUTH;
            }
            case WEST: {
                return OFFSET_SHAPE_WEST;
            }
        }
        return Shapes.block();
    }
}

