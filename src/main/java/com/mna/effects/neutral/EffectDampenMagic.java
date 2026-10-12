 
package com.mna.effects.neutral;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import com.mna.api.tools.MNACap;

public class EffectDampenMagic
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectDampenMagic() {
        super(MobEffectCategory.NEUTRAL, 0);
    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
            MNACap.get(pLivingEntity, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().addRegenerationModifier("c8072f03-ebf3-40ed-a876-e6c916b86ae4", 0.25f + (float)pAmplifier * 0.1f));
    
    }

    public void addAttributeModifiers(AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pAttributeMap, pAmplifier);
    }

    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
            MNACap.get(pLivingEntity, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().removeRegenerationModifier("c8072f03-ebf3-40ed-a876-e6c916b86ae4"));
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }
}

