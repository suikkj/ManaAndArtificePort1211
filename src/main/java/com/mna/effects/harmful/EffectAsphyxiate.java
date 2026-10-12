 
package com.mna.effects.harmful;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectAsphyxiate
extends MobEffect {
    public EffectAsphyxiate() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        entityLivingBaseIn.setAirSupply(Math.max(entityLivingBaseIn.getAirSupply() - (amplifier + 4), 0));

        return true;
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

