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
import com.mna.blocks.tileentities.pylon.RechargePylonTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RechargePylonBlock
extends PylonBlock {
    public RechargePylonBlock(BlockBehaviour.Properties properties, String runeword) {
        super(properties, runeword, new BlockPos(0, 1, 0), new BlockPos(0, 2, 0));
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        RechargePylonTile tile = new RechargePylonTile(pPos, pState);
        tile.setRuneword(this.runeword);
        return tile;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pBlockEntityType == TileEntityInit.RECHARGE_PYLON.get() ? (lvl, pos, state1, be) -> RechargePylonTile.Tick(lvl, pos, state1, (RechargePylonTile)be) : null;
    }
}

