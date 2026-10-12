/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.SlabBlock
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.artifice;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.tools.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PilgrimsPathSlab
extends SlabBlock {
    public PilgrimsPathSlab() {
        super(BlockBehaviour.Properties.of().jumpFactor(2.0f).speedFactor(1.25f).friction(0.45f).explosionResistance(3.0f).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE));
    }

    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        level.sendParticles((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.1f).setColor(10, 10, 10), entity.position().x, entity.position().y, entity.position().z, numberOfParticles, 0.0, 0.0, 0.0, (double)0.15f);
        level.playSound(null, pos, SFX.Spell.Impact.AoE.WIND, SoundSource.BLOCKS, 0.25f, 1.0f);
        return true;
    }

    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            for (int i = 0; i < 10; ++i) {
                level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setGravity(0.05f).setPhysics(true), entity.position().x, entity.position().y, entity.position().z, MathUtils.RandomBetween(-0.25, 0.25), MathUtils.RandomBetween(0.1, 0.2), MathUtils.RandomBetween(-0.25, 0.25));
            }
        }
        return true;
    }

    public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
    }
}

