/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.api.blocks.tile;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface ITileHelper {
    public BlockEntity createOffsetTile(BlockPos var1, BlockState var2);

    public void registerPylonRendererForType(BlockEntityType<? extends PylonTileBase> var1);

    public BlockEntityType<? extends PylonTileBase> getPotionPylonTileType();
}

