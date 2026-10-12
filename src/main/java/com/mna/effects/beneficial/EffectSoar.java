 
package com.mna.effects.beneficial;

import com.mna.effects.EffectInit;
import com.mna.effects.beneficial.SimpleBeneficialEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public class EffectSoar
extends SimpleBeneficialEffect {
    public EffectSoar() {
        super(1825251);
    }

    public static boolean canSoar(LivingEntity entity) {
        return entity.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAR.get()));
    }
}

