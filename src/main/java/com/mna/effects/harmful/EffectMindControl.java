 
package com.mna.effects.harmful;

import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.tools.SummonUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.Goal;

public class EffectMindControl
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectMindControl() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof Mob) {
            Mob mob = (Mob)entityLivingBaseIn;
            mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
            if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
                mob.setTarget(null);
                this.controlTarget(mob);

        return true;
    }
        }
    }

    private void controlTarget(Mob host) {
        int controllerID = host.getPersistentData().getInt("mind_controller");
        if (controllerID < 1) {
            return;
        }
        Entity controller = host.level().getEntity(controllerID);
        if (controller == null || !(controller instanceof LivingEntity)) {
            return;
        }
        host.level().getEntitiesOfClass(LivingEntity.class, host.getBoundingBox().inflate(10.0), e -> e != host && !SummonUtils.isTargetFriendly((Entity)e, (LivingEntity)controller) && Math.abs(e.getY() - host.getY()) <= 10.0 && host.getSensing().hasLineOfSight((Entity)e)).stream().sorted((a, b) -> (int)(a.distanceToSqr((Entity)host) - b.distanceToSqr((Entity)host))).findFirst().ifPresent(e -> host.setTarget(e));
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            this.resetAIFlags(entityLivingBaseIn);
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }

    private void resetAIFlags(LivingEntity entityLivingBaseIn) {
        if (entityLivingBaseIn instanceof Mob) {
            Mob mob = (Mob)entityLivingBaseIn;
            mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
        }
    }
}

