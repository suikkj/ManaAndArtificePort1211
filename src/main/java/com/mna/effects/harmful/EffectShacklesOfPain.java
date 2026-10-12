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

public class EffectShacklesOfPain
extends EffectWithCustomClientParticles
implements INoCreeperLingering {
    public EffectShacklesOfPain() {
        super(MobEffectCategory.HARMFUL, 3451696, MAPFX.Flag.SHACKLES_OF_PAIN);
    }
}

