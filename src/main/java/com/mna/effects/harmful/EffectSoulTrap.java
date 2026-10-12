/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectCategory
 */
package com.mna.effects.harmful;

import com.mna.capabilities.entity.MAPFX;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import net.minecraft.world.effect.MobEffectCategory;

public class EffectSoulTrap
extends EffectWithCustomClientParticles
implements INoCreeperLingering {
    public EffectSoulTrap() {
        super(MobEffectCategory.HARMFUL, 4002680, MAPFX.Flag.SOUL_TRAP);
    }
}

