 
package com.mna.effects.harmful;

import com.mna.capabilities.entity.MAPFX;
import com.mna.config.GeneralConfig;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import com.mna.entities.utility.MAExplosion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;

public class EffectLivingBomb
extends EffectWithCustomClientParticles
implements INoCreeperLingering {
    public EffectLivingBomb() {
        super(MobEffectCategory.HARMFUL, 0, MAPFX.Flag.LIVING_BOMB);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            entity.hurt(entity.damageSources().onFire(), (float)(amplifier + 1));
            if (!entity.isAlive() && !entity.getPersistentData().contains("living_bomb_exploded")) {
                entity.getPersistentData().putBoolean("living_bomb_exploded", true);
                int radius = amplifier + 3;
                MAExplosion.make(null, (ServerLevel)entity.level(), entity.getX(), entity.getY(), entity.getZ(), radius, (float)((amplifier + 1) * 5) * GeneralConfig.getDamageMultiplier(), true, Explosion.BlockInteraction.KEEP, entity.damageSources().explosion(null, null));
                if (amplifier > 0) {
                    entity.level().getEntities((Entity)entity, entity.getBoundingBox().inflate((double)radius)).forEach(e -> {
                        if (e != entity && e.isAlive() && e instanceof LivingEntity) {
                            ((LivingEntity)e).addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(this), 60, amplifier - 1));

        return true;
    }
                    });
                }
            }
        }
    }
}

