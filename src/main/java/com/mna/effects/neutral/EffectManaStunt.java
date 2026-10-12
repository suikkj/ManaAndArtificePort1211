 
package com.mna.effects.neutral;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import com.mna.api.tools.MNACap;

public class EffectManaStunt
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public static final float increase_per_level = 0.2f;
    public static final String modifier_key = "f85477b0-216d-11eb-adc1-0242ac120002";

    public EffectManaStunt() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public void onEffectAdded(LivingEntity entityLivingBaseIn, int amplifier) {
            MNACap.get(entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().addRegenerationModifier(modifier_key, 0.2f * (float)(amplifier + 1)));
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            MNACap.get(entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().removeRegenerationModifier(modifier_key));
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }
}

