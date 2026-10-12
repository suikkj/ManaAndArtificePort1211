/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.sound.SFX;
import com.mna.items.ItemInit;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

public class RitualEffectDeepOcean
extends RitualEffectCreateEssence {
    public RitualEffectDeepOcean(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        Holder centerBiome = context.getLevel().getBiome(context.getCenter());
        if (!centerBiome.isBound() || !centerBiome.is(BiomeTags.IS_OCEAN)) {
            return Component.translatable((String)"ritual.mna.deep_ocean.not_ocean");
        }
        return null;
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return SFX.Loops.WATER;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Vec3 center = new Vec3((double)context.getCenter().getX() + 0.5, (double)(context.getCenter().getY() + 3), (double)context.getCenter().getZ() + 0.5);
        double radius = context.getRecipe().getLowerBound();
        for (int i = 0; i < 40; ++i) {
            context.getLevel().addParticle((ParticleOptions)ParticleTypes.FALLING_WATER, center.x - radius + Math.random() * radius * 2.0, center.y, center.z - radius + Math.random() * radius * 2.0, 0.0, 0.0, 0.0);
        }
        return true;
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack((ItemLike)ItemInit.MOTE_WATER.get());
    }
}

