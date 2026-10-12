 
package com.mna.effects.beneficial;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import com.mna.api.tools.MNACap;

public class EffectManaBoost
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public static final int MANA_BOOST_PER_LEVEL = 50;
    public static final String modifier_key = "9ed65596-16d7-11eb-adc1-0242ac120002";

    public EffectManaBoost() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public void onEffectAdded(LivingEntity entityLivingBaseIn, int amplifier) {
            MNACap.get(entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().addModifier(modifier_key, 50 * (amplifier + 1)));
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            MNACap.get(entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().removeModifier(modifier_key));
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }
}

