 
package com.mna.effects.harmful;

import com.mna.effects.harmful.SimpleHarmfulEffect;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;

public class EffectLacerate
extends SimpleHarmfulEffect
implements INoCreeperLingering {
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        int k = 100 >> pAmplifier;
        if (k > 0) {
            return pDuration % k == 0;
        }
        return true;
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.setHealth(pLivingEntity.getHealth() - 1.0f);
        pLivingEntity.gameEvent(GameEvent.ENTITY_DAMAGE, (Entity)null);

        return true;
    }
}

