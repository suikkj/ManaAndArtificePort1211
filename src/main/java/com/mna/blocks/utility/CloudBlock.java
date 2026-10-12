/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.LayeredCauldronBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.blocks.utility;

import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.tools.MATags;
import com.mna.api.tools.RLoc;
import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

public class CloudBlock
extends Block
implements IDontCreateBlockItem {
    public static final float WATER_CAULDRON_FILL_PROBABILITY_PER_RANDOM_TICK = 0.17578125f;

    public CloudBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WATER).pushReaction(PushReaction.DESTROY).noCollission().noOcclusion().randomTicks());
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        Vec3 drop = Vec3.atBottomCenterOf((Vec3i)pPos).add(-0.5 + Math.random(), 0.0, -0.5 + Math.random());
        pLevel.addParticle((ParticleOptions)ParticleTypes.DRIPPING_WATER, drop.x, drop.y, drop.z, 0.0, 0.0, 0.0);
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        Pair<BlockState, BlockPos> below = this.getNonAirBlockBelow(pPos, pLevel);
        if (below != null) {
            boolean valid;
            BlockState belowState = (BlockState)below.getFirst();
            BlockPos belowPos = (BlockPos)below.getSecond();
            if (belowState.getBlock() == Blocks.CAULDRON) {
                if (pRandom.nextFloat() <= 0.17578125f) {
                    pLevel.setBlock(belowPos, (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(1)), 3);
                }
                return;
            }
            if (belowState.getBlock() == Blocks.WATER_CAULDRON) {
                int existingFillLevel = (Integer)belowState.getValue((Property)LayeredCauldronBlock.LEVEL);
                if (existingFillLevel < 3 && pRandom.nextFloat() <= 1.0f) {
                    pLevel.setBlock(belowPos, (BlockState)belowState.setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(existingFillLevel + 1)), 3);
                }
                return;
            }
            boolean no_age = MATags.isBlockIn(belowState.getBlock(), RLoc.create("construct_harvestables_no_age"));
            boolean bl = valid = no_age || MATags.isBlockIn(belowState.getBlock(), RLoc.create("construct_harvestables"));
            if (valid) {
                if (belowState.getBlock().isRandomlyTicking(belowState)) {
                    belowState.getBlock().randomTick(belowState, pLevel, belowPos, pRandom);
                }
                pLevel.scheduleTick(belowPos, belowState.getBlock(), 1);
            }
        }
    }

    @Nullable
    private Pair<BlockState, BlockPos> getNonAirBlockBelow(BlockPos pPos, ServerLevel pLevel) {
        int count = 0;
        BlockPos pos = pPos.below();
        BlockState belowState = pLevel.getBlockState(pos);
        while (count++ < 5 && belowState.isAir()) {
            pos = pos.below();
            belowState = pLevel.getBlockState(pos);
        }
        return belowState.isAir() ? null : new Pair(belowState, pos);
    }
}

