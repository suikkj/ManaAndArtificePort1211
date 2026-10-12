/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package com.mna.blocks.tileentities;

import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class WellspringPillarTile
extends BlockEntity {
    public WellspringPillarTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public WellspringPillarTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.WELLSPRING_PILLAR.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(this.worldPosition), net.minecraft.world.phys.Vec3.atLowerCornerOf(this.worldPosition.offset(1, 6, 1)));
    }
}

