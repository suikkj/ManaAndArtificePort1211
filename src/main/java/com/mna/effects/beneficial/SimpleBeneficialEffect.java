/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 */
package com.mna.effects.beneficial;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SimpleBeneficialEffect
extends MobEffect
implements INoCreeperLingering {
    public SimpleBeneficialEffect() {
        this(0);
    }

    public SimpleBeneficialEffect(int potionColor) {
        super(MobEffectCategory.BENEFICIAL, potionColor);
    }
}

