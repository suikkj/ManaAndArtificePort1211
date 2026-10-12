/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.RenderShape
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
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.sound.SFX;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.EldrinFumeBlock;
import com.mna.blocks.tileentities.wizard_lab.FumeBellowsTile;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
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
import net.minecraft.world.phys.shapes.VoxelShape;

public class FumeBellowsBlock
extends WizardLabBlock
implements EntityBlock {
    private static final VoxelShape OFFSET_SHAPE = Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)9.0, (double)12.0);
    public static final BooleanProperty FUME_RIGHT = BooleanProperty.create((String)"fume_right");
    public static final BooleanProperty FUME_LEFT = BooleanProperty.create((String)"fume_left");

    public FumeBellowsBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).noOcclusion().strength(3.0f).isSuffocating(BlockInit.NEVER), new BlockPos(0, 1, 0));
    }

    @Override
    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        return OFFSET_SHAPE;
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FumeBellowsTile(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FUME_RIGHT, FUME_LEFT});
    }

    @Override
    protected boolean shouldPlayAmbient(BlockState state, Level world, BlockPos pos, RandomSource rnd) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SFX.Block.FUME_BELLOWS;
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
        return (BlockState)((BlockState)baseState.setValue((Property)FUME_LEFT, (Comparable)Boolean.valueOf(leftNeighborState.getBlock() instanceof EldrinFumeBlock))).setValue((Property)FUME_RIGHT, (Comparable)Boolean.valueOf(rightNeighborState.getBlock() instanceof EldrinFumeBlock));
    }

    @Override
    public BlockState updateShape(BlockState myState, Direction direction, BlockState otherState, LevelAccessor accessor, BlockPos myPos, BlockPos otherPos) {
        BooleanProperty prop = this.directionToLeftRightProperty(myState = super.updateShape(myState, direction, otherState, accessor, myPos, otherPos), direction);
        return prop != null ? (BlockState)myState.setValue((Property)prop, (Comparable)Boolean.valueOf(otherState.getBlock() instanceof EldrinFumeBlock)) : myState;
    }

    @Nullable
    private BooleanProperty directionToLeftRightProperty(BlockState myState, Direction direction) {
        Direction myDir = (Direction)myState.getValue((Property)HorizontalDirectionalBlock.FACING);
        Direction myLeft = myDir.getCounterClockWise();
        Direction myRight = myDir.getClockWise();
        if (direction == myLeft) {
            return FUME_LEFT;
        }
        if (direction == myRight) {
            return FUME_RIGHT;
        }
        return null;
    }
}

