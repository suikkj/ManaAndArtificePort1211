 
package com.mna.effects.beneficial;

import com.mna.ManaAndArtifice;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EffectSenseLife
extends MobEffect
implements INoCreeperLingering {
    public EffectSenseLife() {
        super(MobEffectCategory.BENEFICIAL, 11900952);
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        int range = (1 + amplifier) * 16;
        if (entity.level().isClientSide() && entity == ManaAndArtifice.instance.proxy.getClientPlayer()) {
            Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
            int durationRemaining = player.getEffect(com.mna.api.tools.MAEffects.holder(this)).getDuration();
            player.level().getEntities((Entity)player, player.getBoundingBox().inflate((double)(range * 2)), e -> e instanceof LivingEntity).forEach(e -> {
                boolean glow = ((LivingEntity)e).getEffect(MobEffects.GLOWING) != null;
                e.setGlowingTag(glow);
            });
            if (durationRemaining > 1) {
                player.level().getEntities((Entity)player, player.getBoundingBox().inflate((double)range), e -> e instanceof LivingEntity).forEach(e -> {
                    e.setGlowingTag(true);
                });
            }
        }
        return true;
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

