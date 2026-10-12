/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.EldrinFumeBlock;
import com.mna.blocks.tileentities.wizard_lab.FumePrismTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FumePrismBlock
extends Block
implements EntityBlock {
    public FumePrismBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).noOcclusion().strength(3.0f).isSuffocating(BlockInit.NEVER));
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FumePrismTile(pPos, pState);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.or((VoxelShape)Block.box((double)5.0, (double)-1.0, (double)5.0, (double)11.0, (double)2.0, (double)11.0), (VoxelShape[])new VoxelShape[]{Block.box((double)6.0, (double)2.0, (double)6.0, (double)10.0, (double)6.0, (double)10.0), Block.box((double)5.0, (double)-6.0, (double)5.0, (double)6.0, (double)0.0, (double)6.0), Block.box((double)5.0, (double)-6.0, (double)10.0, (double)6.0, (double)0.0, (double)11.0), Block.box((double)10.0, (double)-6.0, (double)5.0, (double)11.0, (double)0.0, (double)6.0), Block.box((double)10.0, (double)-6.0, (double)10.0, (double)11.0, (double)0.0, (double)11.0)});
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockState below = pLevel.getBlockState(pPos.below(2));
        return below.getBlock() instanceof EldrinFumeBlock;
    }
}

