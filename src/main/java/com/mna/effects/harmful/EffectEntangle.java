 
package com.mna.effects.harmful;

import com.mna.ManaAndArtifice;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;

public class EffectEntangle
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectEntangle() {
        super(MobEffectCategory.HARMFUL, 0);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.parse("845ba178-29e0-4845-84ec-69a11b705380"), -0.95, AttributeModifier.Operation.ADD_VALUE);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity instanceof Player) {
            Player player = (Player)entity;
            ManaAndArtifice.instance.proxy.setFlightEnabled(player, false);
        }
        Vec3 scaledVel = entity.getDeltaMovement().scale((double)0.05f);
        entity.setDeltaMovement(scaledVel.x, entity.getDeltaMovement().y > 0.0 ? scaledVel.y : entity.getDeltaMovement().y - (double)0.01f, scaledVel.z);
        return true;
    }

    public void onEffectAdded(LivingEntity entity, int amplifier) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.getAbilities().mayfly && !player.isCreative()) {
                    player.getPersistentData().putBoolean("ma_entangle_did_allow_flight", true);
                    ManaAndArtifice.instance.proxy.setFlightEnabled(player, false);
                } else {
                    player.getPersistentData().putBoolean("ma_entangle_did_allow_flight", false);
                }
            }
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entity, int amplifier) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.getPersistentData().getBoolean("ma_entangle_did_allow_flight")) {
                    ManaAndArtifice.instance.proxy.setFlightEnabled(player, true);
                }
                player.getPersistentData().remove("ma_entangle_did_allow_flight");
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }
}

