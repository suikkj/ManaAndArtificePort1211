 
package com.mna.effects.beneficial;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import com.mna.api.tools.MNACap;

public class EffectManaRegen
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public static final float PCT_REDUCTION_PER_LEVEL = 0.05f;
    public static final String IDENTIFIER = "d56fa65a-b26b-43ba-9bb2-ab12f3e7ab9e";

    public EffectManaRegen() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public void onEffectAdded(LivingEntity entityLivingBaseIn, int amplifier) {
            MNACap.get(entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().addRegenerationModifier(IDENTIFIER, -0.05f * (float)(amplifier + 1)));
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            MNACap.get(entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().removeRegenerationModifier(IDENTIFIER));
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }
}

