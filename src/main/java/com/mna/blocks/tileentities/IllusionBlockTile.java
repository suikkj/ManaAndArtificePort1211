/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.blocks.tileentities;

import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class IllusionBlockTile
extends BlockEntity {
    private static final int OFFSET_DIST = 8;
    private BlockState mimicState;
    private BlockPos mimicPos;
    private long lastRecheck = -1L;

    public IllusionBlockTile(BlockEntityType<?> p_i48289_1_, BlockPos pos, BlockState state) {
        super(p_i48289_1_, pos, state);
    }

    public IllusionBlockTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.ILLUSION_BLOCK.get(), pos, state);
    }

    public Pair<BlockPos, BlockState> getMimicBlock() {
        if (this.mimicState == null && this.level.getGameTime() >= this.lastRecheck + 100L || this.lastRecheck + 600L < this.level.getGameTime()) {
            this.lastRecheck = this.level.getGameTime();
            this.resolveMimicBlock();
        }
        return new Pair(this.mimicPos, this.mimicState);
    }

    private void resolveMimicBlock() {
        Direction dir = (Direction)this.getBlockState().getValue((Property)BlockStateProperties.FACING);
        ClipContext rtc = new ClipContext(Vec3.atCenterOf((Vec3i)this.getBlockPos().offset(dir.getStepX(), dir.getStepY(), dir.getStepZ())), Vec3.atCenterOf((Vec3i)this.getBlockPos().offset(dir.getStepX() * 8, dir.getStepY() * 8, dir.getStepZ() * 8)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        BlockHitResult brtr = this.getLevel().clip(rtc);
        if (brtr.getType() == HitResult.Type.MISS) {
            this.mimicPos = this.getBlockPos();
            this.mimicState = null;
        }
        this.mimicPos = brtr.getBlockPos();
        this.mimicState = this.level.getBlockState(this.mimicPos);
        if (!this.mimicState.isCollisionShapeFullBlock((BlockGetter)this.getLevel(), this.mimicPos)) {
            this.mimicState = null;
        }
    }
}

