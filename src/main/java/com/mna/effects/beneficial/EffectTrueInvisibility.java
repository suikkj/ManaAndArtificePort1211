 
package com.mna.effects.beneficial;

import com.mna.capabilities.entity.MAPFX;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import com.mna.spells.components.ComponentTrueInvisibility;
import com.mna.tools.EntityUtil;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

public class EffectTrueInvisibility
extends EffectWithCustomClientParticles
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectTrueInvisibility() {
        super(MobEffectCategory.BENEFICIAL, 0, MAPFX.Flag.TRUE_INVISIBILITY);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            if (entityLivingBaseIn instanceof Player) {
                EntityUtil.removeInvisibility(entityLivingBaseIn);
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        if (!ComponentTrueInvisibility.IsDisabledFor(entityLivingBaseIn)) {
            entityLivingBaseIn.setInvisible(true);
        } else {
            EntityUtil.removeInvisibility(entityLivingBaseIn);

        return true;
    }
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

