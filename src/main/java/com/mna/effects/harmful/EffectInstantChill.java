/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package com.mna.effects.harmful;

import com.mna.effects.harmful.SimpleHarmfulEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EffectInstantChill
extends SimpleHarmfulEffect {
    public boolean isInstantenous() {
        return true;
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration >= 1;
    }

    public void applyInstantenousEffect(Entity pSource, Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
        if (pLivingEntity.canFreeze()) {
            pLivingEntity.setTicksFrozen(pLivingEntity.getTicksFrozen() + 300 * pAmplifier);
        }
    }
}

