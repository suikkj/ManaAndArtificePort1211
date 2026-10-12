 
package com.mna.effects.beneficial;

import com.mna.api.entities.DamageHelper;
import com.mna.capabilities.entity.MAPFX;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import com.mna.tools.SummonUtils;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EffectUndeadFrostWalker
extends EffectWithCustomClientParticles
implements INoCreeperLingering {
    public EffectUndeadFrostWalker() {
        super(MobEffectCategory.BENEFICIAL, 0, MAPFX.Flag.AURA_OF_FROST);
    }

    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (living.level().isClientSide()) {
            return true;
        }
        living.level().getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(6.0), e -> !SummonUtils.isTargetFriendly((Entity)e, living) && e.canFreeze() && e != living).stream().map(e -> e).forEach(le -> {
            le.setTicksFrozen(le.getTicksFrozen() + 5 * amplifier);
            le.hurt(DamageHelper.createSourcedType(DamageHelper.FROST, living.level().registryAccess(), (Entity)living), (float)amplifier);
        });
        return true;
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        int baseline = 100;
        int targetDuration = baseline / (amplifier + 1);
        return duration % targetDuration == 0;
    }
}

