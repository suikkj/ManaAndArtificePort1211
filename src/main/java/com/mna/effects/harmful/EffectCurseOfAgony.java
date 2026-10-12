 
package com.mna.effects.harmful;

import com.mna.capabilities.entity.MAPFX;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectCurseOfAgony
extends EffectWithCustomClientParticles {
    public EffectCurseOfAgony() {
        super(MobEffectCategory.HARMFUL, 14773082, MAPFX.Flag.CURSE_OF_AGONY);
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().getGameTime() % 100L == 0L) {
            pLivingEntity.hurt(pLivingEntity.damageSources().fellOutOfWorld(), 1.0f);

        return true;
    }
    }
}

