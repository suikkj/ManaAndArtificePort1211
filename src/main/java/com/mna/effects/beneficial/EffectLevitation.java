 
package com.mna.effects.beneficial;

import com.mna.ManaAndArtifice;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

public class EffectLevitation
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectLevitation() {
        super(MobEffectCategory.BENEFICIAL, 10564021);
        this.addAttributeModifier(Attributes.FLYING_SPEED, ResourceLocation.parse("1954c4d7-bfe9-48d2-b3da-0331a0c5c347"), 0.05f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof Player) {
            Player player = (Player)entityLivingBaseIn;
            MobEffectInstance activeEffect = player.getEffect(com.mna.api.tools.MAEffects.holder(this));
            if (!activeEffect.isInfiniteDuration() && activeEffect.getDuration() <= 2) {
                ManaAndArtifice.instance.proxy.setFlightEnabled(player, false);
            } else {
                ManaAndArtifice.instance.proxy.setFlightEnabled(player, true);
                ManaAndArtifice.instance.proxy.setFlySpeed(player, amplifier == 1 ? 0.05f : 0.01f);
            }
        }
        return true;
    }

    public void onEffectRemoved(LivingEntity living, int p_111187_3_) {
            if (!living.hasEffect(com.mna.api.tools.MAEffects.holder(this))) {
                if (living instanceof ServerPlayer) {
                    ManaAndArtifice.instance.proxy.setFlightEnabled((Player)((ServerPlayer)living), false);
                } else if (living instanceof Player && living.level().isClientSide()) {
                    ManaAndArtifice.instance.proxy.setFlySpeed((Player)living, 0.05f);
                }
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributemods) {
        super.removeAttributeModifiers(attributemods);
    }
}

