/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.affinity.Affinity;
import com.mna.blocks.tileentities.EldrinMatrixTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EldrinMatrixBlock
extends Block
implements EntityBlock {
    private Affinity generatingAffinity = Affinity.UNKNOWN;
    private static final VoxelShape SHAPE = Shapes.or((VoxelShape)Block.box((double)7.5, (double)5.0, (double)7.5, (double)8.5, (double)12.0, (double)8.5), (VoxelShape)Block.box((double)4.5, (double)7.5, (double)4.5, (double)11.5, (double)8.5, (double)11.5));

    public EldrinMatrixBlock(Affinity affinity) {
        super(BlockBehaviour.Properties.of().noCollission().noOcclusion().strength(2.0f));
        this.generatingAffinity = affinity;
    }

    public Affinity getGeneratingAffinity() {
        return this.generatingAffinity;
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EldrinMatrixTile(pPos, pState);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.ELDRIN_MATRIX.get() ? (lvl, pos, state1, be) -> EldrinMatrixTile.Tick(lvl, pos, state1, (EldrinMatrixTile)be) : null;
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer instanceof Player) {
            Player player = (Player)pPlacer;
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof EldrinMatrixTile) {
                EldrinMatrixTile capTile = (EldrinMatrixTile)be;
                capTile.setPlacedBy(player);
            }
        }
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }
}

