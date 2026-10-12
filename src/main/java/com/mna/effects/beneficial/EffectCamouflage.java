 
package com.mna.effects.beneficial;

import com.mna.effects.EffectInit;
import com.mna.effects.beneficial.SimpleBeneficialEffect;
import com.mna.tools.EntityUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EffectCamouflage
extends SimpleBeneficialEffect implements com.mna.api.effects.IMNAEffectHook {
    public static final String NBT_CAMOFLAGE_PCT = "mna:camoflage_percent";
    public static final float CAMOUFLAGE_THRESHOLD = 0.5f;

    public EffectCamouflage() {
        super(2642283);
    }

    public static float getCamoflagePercent(LivingEntity entity) {
        if (entity != null) {
            if (entity.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CAMOUFLAGE.get()))) {
                return entity.getPersistentData().getFloat(NBT_CAMOFLAGE_PCT);
            }
            entity.getPersistentData().remove(NBT_CAMOFLAGE_PCT);
        }
        return 0.0f;
    }

    public static void setCamoflagePercent(LivingEntity entity, float percent) {
        if (entity != null) {
            entity.getPersistentData().putFloat(NBT_CAMOFLAGE_PCT, percent);
            if (percent > 0.5f) {
                entity.setInvisible(true);
            } else {
                entity.setInvisible(false);
            }
        }
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        float allowedMovement = pAmplifier == 1 && pLivingEntity.isDiscrete() ? 0.075f : 0.01f;
        Vec3 movement = new Vec3(pLivingEntity.getDeltaMovement().x, 0.0, pLivingEntity.getDeltaMovement().z);
        if (movement.length() > (double)allowedMovement || pLivingEntity.isCurrentlyGlowing()) {
            EffectCamouflage.setCamoflagePercent(pLivingEntity, 0.0f);
            return true;
        }
        float curCamouflage = EffectCamouflage.getCamoflagePercent(pLivingEntity);
        curCamouflage = Math.min(curCamouflage + 0.0125f, 1.0f);
        EffectCamouflage.setCamoflagePercent(pLivingEntity, curCamouflage);
        return true;
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            if (entityLivingBaseIn instanceof Player) {
                EntityUtil.removeInvisibility(entityLivingBaseIn);
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }
}

