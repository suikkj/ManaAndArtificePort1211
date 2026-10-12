/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.rituals.effects;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.sound.SFX;
import com.mna.items.ItemInit;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class RitualEffectUntamedWind
extends RitualEffectCreateEssence {
    public RitualEffectUntamedWind(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        if (context.getCenter().getY() < 133) {
            return Component.translatable((String)"ritual.mna.untamed_wind.too_low");
        }
        return null;
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return SFX.Loops.AIR;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        int radius = context.getRecipe().getLowerBound();
        BlockPos start = context.getCenter().west(radius);
        for (int i = 0; i < 13; ++i) {
            context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), (double)start.getX(), (double)start.getY() + Math.random() * 3.0, (double)(start.getZ() - radius) + Math.random() * (double)radius * 2.0, 0.5 + Math.random() * 0.5, 0.0, 0.0);
        }
        return true;
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack((ItemLike)ItemInit.MOTE_AIR.get());
    }
}

