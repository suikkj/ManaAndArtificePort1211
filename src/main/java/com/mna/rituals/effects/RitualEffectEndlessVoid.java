/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.sound.SFX;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.items.ItemInit;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class RitualEffectEndlessVoid
extends RitualEffectCreateEssence {
    public RitualEffectEndlessVoid(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        BlockState state = context.getLevel().getBlockState(context.getCenter());
        boolean semiActiveRitual = false;
        if (state.getBlock() == BlockInit.CHALK_RUNE.get()) {
            semiActiveRitual = (Boolean)state.getValue((Property)ChalkRuneBlock.ACTIVATED) == true;
        }
        for (int i = 0; i < context.getRecipe().getLowerBound(); ++i) {
            for (int j = 0; j < context.getRecipe().getLowerBound(); ++j) {
                int lightLevel = context.getLevel().getMaxLocalRawBrightness(context.getCenter().offset(-i, 0, j));
                if (semiActiveRitual) {
                    lightLevel -= 6;
                }
                if (lightLevel <= 0) continue;
                return Component.translatable((String)"ritual.mna.endless_void.too_bright");
            }
        }
        return null;
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return SFX.Loops.ENDER;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Vec3 center = new Vec3((double)context.getCenter().getX() + 0.5, (double)context.getCenter().getY() + 0.1, (double)context.getCenter().getZ() + 0.5);
        double radius = context.getRecipe().getLowerBound();
        for (float i = 0.0f; i < 360.0f; i += 15.0f) {
            double angleR = Math.toRadians(i);
            double offsetX = Math.cos(angleR) * radius;
            double offsetZ = Math.sin(angleR) * radius;
            Vec3 start = center.add(offsetX, 0.0, offsetZ);
            context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), start.x, start.y, start.z, center.x, center.y, center.z);
        }
        return true;
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack((ItemLike)ItemInit.MOTE_ENDER.get());
    }
}

