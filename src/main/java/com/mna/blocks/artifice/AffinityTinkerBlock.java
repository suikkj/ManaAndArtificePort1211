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
import com.mna.blocks.tileentities.wizard_lab.AffinityTinkerTile;
import com.mna.gui.containers.block.ContainerAffinityTinker;
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

public class AffinityTinkerBlock
extends WizardLabBlock
implements EntityBlock {
    private static final Component CONTAINER_TITLE = Component.translatable((String)RLoc.create("container.affinity_tinker").toString());
    private static final VoxelShape OFFSET_SHAPE_NORTH = Shapes.or((VoxelShape)Block.box((double)1.1, (double)0.0, (double)3.0, (double)3.1, (double)1.0, (double)5.0), (VoxelShape[])new VoxelShape[]{Block.box((double)12.9, (double)0.0, (double)3.0, (double)14.9, (double)1.0, (double)5.0), Block.box((double)12.4, (double)0.0, (double)7.0, (double)14.4, (double)1.0, (double)9.0), Block.box((double)1.6, (double)0.0, (double)7.0, (double)3.6, (double)1.0, (double)9.0), Block.box((double)2.1, (double)0.0, (double)11.0, (double)4.1, (double)1.0, (double)13.0), Block.box((double)11.9, (double)0.0, (double)11.0, (double)13.9, (double)1.0, (double)13.0)});
    private static final VoxelShape OFFSET_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH);

    public AffinityTinkerBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion().isSuffocating(BlockInit.NEVER), new BlockPos(0, 1, 0));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AffinityTinkerTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.AFFINITY_TINKER.get() ? (lvl, pos, state1, be) -> AffinityTinkerTile.Tick(lvl, pos, state1, (AffinityTinkerTile)be) : null;
    }

    @Override
    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !(be instanceof AffinityTinkerTile)) {
            return null;
        }
        return new SimpleMenuProvider((id, playerInv, user) -> new ContainerAffinityTinker(id, playerInv, (AffinityTinkerTile)be), CONTAINER_TITLE);
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

