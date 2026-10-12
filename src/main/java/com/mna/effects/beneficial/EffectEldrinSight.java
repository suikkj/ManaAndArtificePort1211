 
package com.mna.effects.beneficial;

import com.mna.ManaAndArtifice;
import com.mna.effects.beneficial.EffectWellspringSight;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EffectEldrinSight
extends EffectWellspringSight
implements INoCreeperLingering {
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.level().isClientSide() && entity == ManaAndArtifice.instance.proxy.getClientPlayer()) {
            Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
            int durationRemaining = player.getEffect(com.mna.api.tools.MAEffects.holder(this)).getDuration();
            player.level().getEntities((Entity)player, player.getBoundingBox().inflate(64.0), e -> e instanceof LivingEntity).forEach(e -> {
                boolean glow = ((LivingEntity)e).getEffect(MobEffects.GLOWING) != null;
                e.setGlowingTag(glow);
            });
            if (durationRemaining > 1) {
                player.level().getEntities((Entity)player, player.getBoundingBox().inflate(32.0), e -> e instanceof LivingEntity).forEach(e -> {
                    e.setGlowingTag(true);
                });
            }
        }
        return true;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

