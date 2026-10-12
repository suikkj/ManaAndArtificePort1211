/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.sound.SFX;
import com.mna.items.ItemInit;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

public class RitualEffectSearingInferno
extends RitualEffectCreateEssence {
    public RitualEffectSearingInferno(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        Holder centerBiome = context.getLevel().getBiome(context.getCenter());
        if (!centerBiome.isBound() || !centerBiome.is(BiomeTags.IS_NETHER)) {
            return Component.translatable((String)"ritual.mna.searing_inferno.not_nether");
        }
        return null;
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return SFX.Loops.FIRE;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Vec3 center = new Vec3((double)context.getCenter().getX() + 0.5, (double)context.getCenter().getY() + 0.1, (double)context.getCenter().getZ() + 0.5);
        for (float i = 0.0f; i < 360.0f; i += 3.0f) {
            double y = Math.random() * 5.0;
            double radius = 1.0 + 2.0 * Math.pow(0.2, y);
            context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_ORBIT.get())), center.x, center.y + y, center.z, 0.1 + Math.random() * 0.1, 0.0 + Math.random() * 0.1, radius);
        }
        return true;
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack((ItemLike)ItemInit.MOTE_FIRE.get());
    }
}

