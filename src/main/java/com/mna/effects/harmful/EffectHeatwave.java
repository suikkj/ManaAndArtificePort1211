 
package com.mna.effects.harmful;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.harmful.SimpleHarmfulEffect;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import com.mna.api.tools.MNACap;

public class EffectHeatwave
extends SimpleHarmfulEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public static final String ID_MANA_REGEN_SLOW = "705bf838-ed4b-4d88-8a27-c3ac20ecae82";

    public void onEffectAdded(LivingEntity living, int amp) {
            if (living instanceof Player) {
                MNACap.get(living, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().addRegenerationModifier(ID_MANA_REGEN_SLOW, 1.0f - 0.1f * (float)(amp + 1)));
            }
    
    }

    public void addAttributeModifiers(AttributeMap attrs, int amp) {
        super.addAttributeModifiers(attrs, amp);
    }

    public void onEffectRemoved(LivingEntity living, int amp) {
            if (living instanceof Player) {
                MNACap.get(living, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().removeRegenerationModifier(ID_MANA_REGEN_SLOW));
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attrs) {
        super.removeAttributeModifiers(attrs);
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity instanceof Player) {
            ((Player)pLivingEntity).causeFoodExhaustion(0.005f * (float)(pAmplifier + 1));

        return true;
    }
    }
}

