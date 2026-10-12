 
package com.mna.effects.beneficial;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import com.mna.api.tools.MNACap;

public class EffectChronoAnchor
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectChronoAnchor() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public void onEffectAdded(LivingEntity entityLivingBaseIn, int amplifier) {
            IPlayerMagic magic;        if (entityLivingBaseIn instanceof Player && (magic = (IPlayerMagic)entityLivingBaseIn.getData(PlayerMagicProvider.MAGIC.get())) != null) {
                magic.getChronoAnchorData().fromPlayer((Player)entityLivingBaseIn);
            }
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            if (entityLivingBaseIn instanceof Player) {
                MNACap.get(entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                    m.getChronoAnchorData().revert((Player)entityLivingBaseIn);
                    m.setNeedsChronoExhaustion();
                });
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }
}

