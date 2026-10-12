 
package com.mna.effects.neutral;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import com.mna.api.tools.MNACap;

public class EffectAmplifyMagic
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectAmplifyMagic() {
        super(MobEffectCategory.NEUTRAL, 0);
    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
            MNACap.get(pLivingEntity, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().addRegenerationModifier("a7ba491b-39c3-480d-adc8-0af061d7ef04", -0.25f + (float)pAmplifier * -0.1f));
    
    }

    public void addAttributeModifiers(AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pAttributeMap, pAmplifier);
    }

    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
            MNACap.get(pLivingEntity, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().removeRegenerationModifier("a7ba491b-39c3-480d-adc8-0af061d7ef04"));
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }
}

