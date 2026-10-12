/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 */
package com.mna.effects.neutral;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SimpleNeutralEffect
extends MobEffect {
    public SimpleNeutralEffect() {
        this(0);
    }

    public SimpleNeutralEffect(int potionColor) {
        super(MobEffectCategory.NEUTRAL, potionColor);
    }
}

