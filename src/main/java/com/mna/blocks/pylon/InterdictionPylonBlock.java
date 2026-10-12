/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.pylon;

import com.mna.api.blocks.PylonBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.pylon.InterdictionPylonTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class InterdictionPylonBlock
extends PylonBlock {
    private final boolean in;

    public InterdictionPylonBlock(BlockBehaviour.Properties properties, String runeword, boolean in) {
        super(properties, runeword, new BlockPos(0, 1, 0), new BlockPos(0, 2, 0));
        this.in = in;
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        InterdictionPylonTile tile = new InterdictionPylonTile(pPos, pState).setDirection(this.in);
        tile.setRuneword(this.runeword);
        return tile;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pBlockEntityType == TileEntityInit.INTERDICTION_PYLON.get() ? (lvl, pos, state1, be) -> InterdictionPylonTile.Tick(lvl, pos, state1, (InterdictionPylonTile)be) : null;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof InterdictionPylonTile) {
            InterdictionPylonTile interdictor = (InterdictionPylonTile)tile;
            interdictor.RemoveInterdiction(pos);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}

