/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeMap
 *  net.minecraft.world.entity.player.Player
 */
package com.mna.effects.harmful;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

public class EffectDeath
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectDeath() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    public void onEffectRemoved(LivingEntity entity, int amplifier) {
            if (entity instanceof Player) {
                return;
            }
            if (entity.getPersistentData().contains("mna:deathflag")) {
                entity.hurt(entity.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                if (entity.isAlive()) {
                    entity.remove(Entity.RemovalReason.KILLED);
                }
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }
}

