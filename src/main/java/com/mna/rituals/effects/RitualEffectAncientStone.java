/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.api.sound.SFX;
import com.mna.items.ItemInit;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

public class RitualEffectAncientStone
extends RitualEffectCreateEssence {
    public RitualEffectAncientStone(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        if (context.getCenter().getY() > 0) {
            return Component.translatable((String)"ritual.mna.ancient_stone.too_high");
        }
        return null;
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return SFX.Loops.EARTH_RUMBLE;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        NonNullList<RitualBlockPos> positions = context.getAllPositions();
        for (RitualBlockPos pos : positions) {
            if (pos == null) continue;
            BlockPos cur = pos.getBlockPos();
            BlockState state = context.getLevel().getBlockState(cur.below());
            context.getLevel().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, state), (double)cur.getX() + Math.random(), (double)cur.getY(), (double)cur.getZ() + Math.random(), 0.0, 0.05 * Math.random(), 0.0);
        }
        return true;
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack((ItemLike)ItemInit.MOTE_EARTH.get());
    }
}

