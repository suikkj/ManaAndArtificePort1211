/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectCategory
 */
package com.mna.effects.neutral;

import com.mna.capabilities.entity.MAPFX;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import net.minecraft.world.effect.MobEffectCategory;

public class EffectSoaked
extends EffectWithCustomClientParticles {
    public EffectSoaked() {
        super(MobEffectCategory.NEUTRAL, 0, MAPFX.Flag.SOAKED);
    }
}

