package com.mna.api.effects;

import net.minecraft.world.entity.LivingEntity;

/**
 * [PORT 1.21.1] No 1.21.1 {@code MobEffect.addAttributeModifiers/removeAttributeModifiers}
 * perderam o parametro {@code LivingEntity} (so recebem o AttributeMap). Efeitos do MNA que
 * dependiam da entidade movem essa logica para estes hooks, disparados por
 * {@code com.mna.effects.MAEffectHooks} via MobEffectEvent.
 */
public interface IMNAEffectHook {

    default void onEffectAdded(LivingEntity entity, int amplifier) {
    }

    default void onEffectRemoved(LivingEntity entity, int amplifier) {
    }
}
