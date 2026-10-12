/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.goal.target.TargetGoal
 *  net.minecraft.world.entity.ai.targeting.TargetingConditions
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.ai;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RetaliateOnAttackGoal
extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private boolean entityCallsForHelp;
    private int revengeTimerOld;
    private final Class<?>[] excludedReinforcementTypes;
    private Class<?>[] reinforcementTypes;

    public RetaliateOnAttackGoal(Mob creatureIn, Class<?> ... excludeReinforcementTypes) {
        super(creatureIn, true);
        this.excludedReinforcementTypes = excludeReinforcementTypes;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingentity = this.mob.getLastHurtByMob();
        if (livingentity == this.mob) {
            this.mob.setLastHurtByMob(null);
            return false;
        }
        if (i != this.revengeTimerOld && livingentity != null) {
            if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            }
            for (Class<?> oclass : this.excludedReinforcementTypes) {
                if (!oclass.isAssignableFrom(livingentity.getClass())) continue;
                return false;
            }
            return this.canAttack(livingentity, HURT_BY_TARGETING);
        }
        return false;
    }

    public RetaliateOnAttackGoal setCallsForHelp(Class<?> ... reinforcementTypes) {
        this.entityCallsForHelp = true;
        this.reinforcementTypes = reinforcementTypes;
        return this;
    }

    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.revengeTimerOld = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.entityCallsForHelp) {
            this.alertOthers();
        }
        super.start();
    }

    protected void alertOthers() {
        double d0 = this.getFollowDistance();
        AABB axisalignedbb = AABB.unitCubeFromLowerCorner((Vec3)this.mob.position()).inflate(d0, 10.0, d0);
        List list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), axisalignedbb);
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Mob mobentity = (Mob)iterator.next();
            if (this.mob == mobentity || mobentity.getTarget() != null || this.mob instanceof TamableAnimal && ((TamableAnimal)this.mob).getOwner() != ((TamableAnimal)mobentity).getOwner() || mobentity.isAlliedTo((Entity)this.mob.getLastHurtByMob())) continue;
            if (this.reinforcementTypes != null) {
                boolean flag = false;
                for (Class<?> oclass : this.reinforcementTypes) {
                    if (mobentity.getClass() != oclass) continue;
                    flag = true;
                    break;
                }
                if (flag) continue;
            }
            this.setAttackTarget(mobentity, this.mob.getLastHurtByMob());
        }
        return;
    }

    protected void setAttackTarget(Mob mobIn, LivingEntity targetIn) {
        mobIn.setTarget(targetIn);
    }
}

