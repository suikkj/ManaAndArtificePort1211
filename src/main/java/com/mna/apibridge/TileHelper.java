/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.apibridge;

import com.mna.ManaAndArtifice;
import com.mna.api.blocks.tile.ITileHelper;
import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.blocks.tileentities.OffsetBlockTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileHelper
implements ITileHelper {
    @Override
    public BlockEntity createOffsetTile(BlockPos pos, BlockState state) {
        return new OffsetBlockTile(pos, state);
    }

    @Override
    public void registerPylonRendererForType(BlockEntityType<? extends PylonTileBase> pylonTile) {
        ManaAndArtifice.instance.proxy.registerPylonRendererForType(pylonTile);
    }

    @Override
    public BlockEntityType<? extends PylonTileBase> getPotionPylonTileType() {
        return (BlockEntityType)TileEntityInit.POTION_PYLON.get();
    }
}

