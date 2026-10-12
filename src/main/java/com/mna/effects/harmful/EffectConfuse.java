 
package com.mna.effects.harmful;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class EffectConfuse
extends MobEffect
implements INoCreeperLingering {
    public EffectConfuse() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof Mob) {
            Mob mob = (Mob)entityLivingBaseIn;
            mob.setTarget(null);
            if (Math.random() < 0.5) {
                entityLivingBaseIn.level().getEntitiesOfClass(LivingEntity.class, entityLivingBaseIn.getBoundingBox().inflate(10.0), e -> e.isAlive() && e != entityLivingBaseIn).stream().findFirst().ifPresent(t -> mob.setTarget(t));
            } else if (Math.random() < 0.75) {
                entityLivingBaseIn.level().getEntitiesOfClass(Player.class, entityLivingBaseIn.getBoundingBox().inflate(10.0), e -> e.isAlive() && e != entityLivingBaseIn).stream().findFirst().ifPresent(t -> mob.setTarget((LivingEntity)t));

        return true;
    }
        }
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}

