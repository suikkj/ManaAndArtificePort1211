 
package com.mna.effects.harmful;

import com.mna.effects.harmful.SimpleHarmfulEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class EffectChill
extends SimpleHarmfulEffect {
    public EffectChill() {
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.parse("7107DE5E-7CE8-4030-940E-514C1F160890"), -0.15f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        int k = 50 >> pAmplifier;
        if (k > 0) {
            return pDuration % k == 0;
        }
        return true;
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.canFreeze()) {
            pLivingEntity.setTicksFrozen(pLivingEntity.getTicksFrozen() + 5 * pAmplifier);

        return true;
    }
    }
}

