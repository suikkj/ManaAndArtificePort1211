/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeMap
 */
package com.mna.effects.beneficial;

import com.mna.capabilities.entity.MAPFX;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class EffectManaShield
extends EffectWithCustomClientParticles
implements INoCreeperLingering {
    public EffectManaShield() {
        super(MobEffectCategory.BENEFICIAL, 0, MAPFX.Flag.MANA_SHIELD);
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }

    public float getReductionCost(int amplifier) {
        return 20.0f;
    }
}

