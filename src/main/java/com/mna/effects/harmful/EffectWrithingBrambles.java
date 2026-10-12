 
package com.mna.effects.harmful;

import com.mna.effects.harmful.SimpleHarmfulEffect;
import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;

public class EffectWrithingBrambles
extends SimpleHarmfulEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
            pLivingEntity.getPersistentData().remove("brambles_distance");
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        double damageDist;
        Vec3 pos = pLivingEntity.position();
        Vec3 oldPos = null;
        if (!pLivingEntity.getPersistentData().contains("brambles_old_x")) {
            pLivingEntity.getPersistentData().putDouble("brambles_old_x", pos.x);
            pLivingEntity.getPersistentData().putDouble("brambles_old_y", pos.y);
            pLivingEntity.getPersistentData().putDouble("brambles_old_z", pos.z);
            return true;

        return true;
    }
        double oldX = pLivingEntity.getPersistentData().getDouble("brambles_old_x");
        double oldY = pLivingEntity.getPersistentData().getDouble("brambles_old_y");
        double oldZ = pLivingEntity.getPersistentData().getDouble("brambles_old_z");
        oldPos = new Vec3(oldX, oldY, oldZ);
        pLivingEntity.getPersistentData().putDouble("brambles_old_x", pos.x);
        pLivingEntity.getPersistentData().putDouble("brambles_old_y", pos.y);
        pLivingEntity.getPersistentData().putDouble("brambles_old_z", pos.z);
        double dist = oldPos.distanceTo(pos);
        if (pLivingEntity.getPersistentData().contains("brambles_distance")) {
            dist += pLivingEntity.getPersistentData().getDouble("brambles_distance");
        }
        if (dist >= (damageDist = (double)(10 >> pAmplifier))) {
            pLivingEntity.hurt(pLivingEntity.damageSources().cactus(), 1.0f);
            dist = 0.0;
        }
        pLivingEntity.getPersistentData().putDouble("brambles_distance", dist);
    }
}

