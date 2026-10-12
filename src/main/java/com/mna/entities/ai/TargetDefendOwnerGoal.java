/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 */
package com.mna.entities.ai;

import com.mna.api.entities.ISummonTargetPredicate;
import com.mna.entities.ai.SummonEntityPredicate;
import com.mna.tools.SummonUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class TargetDefendOwnerGoal<T extends LivingEntity>
extends NearestAttackableTargetGoal<T> {
    public TargetDefendOwnerGoal(Mob goalOwnerIn, Class<T> targetClassIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(goalOwnerIn, targetClassIn, targetChanceIn, checkSight, nearbyOnlyIn, null);
        this.targetConditions = new SummonEntityPredicate().range(this.getFollowDistance() * 2.0).selector(e -> {
            boolean result;
            boolean friendly = SummonUtils.isTargetFriendly((Entity)e, SummonUtils.getSummoner((LivingEntity)goalOwnerIn));
            boolean bl = result = !friendly;
            if (goalOwnerIn instanceof ISummonTargetPredicate) {
                result = ((ISummonTargetPredicate)goalOwnerIn).shouldSummonTarget((Entity)e, friendly);
            }
            return result;
        }).ignoreInvisibilityTesting();
    }

    protected void findTarget() {
        this.target = this.mob.level().getNearestEntity(this.targetType, this.targetConditions, (LivingEntity)this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetSearchArea(this.getFollowDistance()));
    }
}

