 
package com.mna.effects.beneficial;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class EffectBindWounds
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public static final String PERSISTENT_DATA_KEY_LASTPOS = "bind_wounds_last_pos";

    public EffectBindWounds() {
        super(MobEffectCategory.BENEFICIAL, 0xC0FFCC);
    }

    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        BlockPos lastPos = null;
        BlockPos curPos = living.blockPosition();
        if (living.getPersistentData().contains(PERSISTENT_DATA_KEY_LASTPOS)) {
            lastPos = BlockPos.of((long)living.getPersistentData().getLong(PERSISTENT_DATA_KEY_LASTPOS));
        }
        if (lastPos != null && !lastPos.equals(curPos)) {
            living.removeEffect(com.mna.api.tools.MAEffects.holder(this));
        } else {
            living.heal(1.0f);
        }
        living.getPersistentData().putLong(PERSISTENT_DATA_KEY_LASTPOS, curPos.asLong());
        return true;
    }

    public void onEffectRemoved(LivingEntity living, int p_111187_3_) {
            living.getPersistentData().remove(PERSISTENT_DATA_KEY_LASTPOS);    
    }

    public void removeAttributeModifiers(AttributeMap p_111187_2_) {
        super.removeAttributeModifiers(p_111187_2_);
    }

    public void onEffectAdded(LivingEntity living, int p_111185_3_) {
            living.getPersistentData().remove(PERSISTENT_DATA_KEY_LASTPOS);    
    }

    public void addAttributeModifiers(AttributeMap p_111185_2_, int p_111185_3_) {
        super.addAttributeModifiers(p_111185_2_, p_111185_3_);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        int baseline = 30;
        int targetDuration = baseline / (amplifier + 1);
        return duration % targetDuration == 0;
    }
}

