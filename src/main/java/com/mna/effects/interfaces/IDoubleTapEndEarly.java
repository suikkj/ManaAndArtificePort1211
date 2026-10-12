/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 */
package com.mna.effects.interfaces;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public interface IDoubleTapEndEarly {
    default public boolean canEndEarly(LivingEntity player, MobEffectInstance effect) {
        return true;
    }

    default public void onRemoved(LivingEntity living, MobEffectInstance effect) {
    }
}

