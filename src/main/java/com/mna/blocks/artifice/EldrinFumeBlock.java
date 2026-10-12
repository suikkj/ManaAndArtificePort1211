/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import com.mna.gui.containers.block.ContainerEldrinFume;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EldrinFumeBlock
extends WizardLabBlock
implements EntityBlock {
    private static final Component CONTAINER_TITLE = Component.translatable((String)RLoc.create("container.eldrin_fume").toString());
    private static final VoxelShape OFFSET_SHAPE = Shapes.or((VoxelShape)Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)2.0, (double)12.0), (VoxelShape[])new VoxelShape[]{Block.box((double)5.0, (double)2.0, (double)5.0, (double)11.0, (double)4.0, (double)11.0), Block.box((double)5.0, (double)4.0, (double)5.0, (double)6.0, (double)11.0, (double)6.0), Block.box((double)10.0, (double)4.0, (double)5.0, (double)11.0, (double)11.0, (double)6.0), Block.box((double)10.0, (double)4.0, (double)10.0, (double)11.0, (double)11.0, (double)11.0), Block.box((double)5.0, (double)4.0, (double)10.0, (double)6.0, (double)11.0, (double)11.0), Block.box((double)4.5, (double)8.0, (double)4.5, (double)6.5, (double)9.0, (double)6.5), Block.box((double)9.5, (double)8.0, (double)4.5, (double)11.5, (double)9.0, (double)6.5), Block.box((double)9.5, (double)8.0, (double)9.5, (double)11.5, (double)9.0, (double)11.5), Block.box((double)4.5, (double)8.0, (double)9.5, (double)6.5, (double)9.0, (double)11.5), Block.box((double)5.5, (double)8.25, (double)5.5, (double)10.5, (double)8.75, (double)10.5)});

    public EldrinFumeBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).noOcclusion().strength(3.0f).isSuffocating(BlockInit.NEVER), new BlockPos(0, 1, 0));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EldrinFumeTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.ELDRIN_FUME.get() ? (lvl, pos, state1, be) -> EldrinFumeTile.Tick(lvl, pos, state1, (EldrinFumeTile)be) : null;
    }

    @Override
    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !(be instanceof EldrinFumeTile)) {
            return null;
        }
        return new SimpleMenuProvider((id, playerInv, user) -> new ContainerEldrinFume(id, playerInv, (EldrinFumeTile)be), CONTAINER_TITLE);
    }

    @Override
    protected boolean shouldPlayAmbient(BlockState state, Level world, BlockPos pos, RandomSource rnd) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null || !(be instanceof EldrinFumeTile)) {
            return false;
        }
        return ((EldrinFumeTile)be).isActive();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMPFIRE_CRACKLE;
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        return OFFSET_SHAPE;
    }

    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be instanceof EldrinFumeTile) {
            EldrinFumeTile fume = (EldrinFumeTile)be;
            fume.onNeighborChanged();
        }
        super.onNeighborChange(state, level, pos, neighbor);
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be != null && be instanceof EldrinFumeTile) {
            EldrinFumeTile fume = (EldrinFumeTile)be;
            fume.onNeighborChanged();
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }
}

