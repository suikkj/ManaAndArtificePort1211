/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntitySelector
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.pathfinder.Node
 *  net.minecraft.world.level.pathfinder.Path
 */
package com.mna.entities.ai;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public class SummonMeleeFallbackGoal
extends Goal {
    protected final Mob attacker;
    private final double speedTowardsTarget;
    private final boolean longMemory;
    private Path path;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int delayCounter;
    private int ticksUntilNextAttack;
    private long lastCanUseCheck;
    private int failedPathFindingPenalty = 0;
    private boolean canPenalize = false;

    public SummonMeleeFallbackGoal(Mob creature, double speedIn, boolean useLongMemory) {
        this.attacker = creature;
        this.speedTowardsTarget = speedIn;
        this.longMemory = useLongMemory;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        long i = this.attacker.level().getGameTime();
        if (i - this.lastCanUseCheck < 20L) {
            return false;
        }
        this.lastCanUseCheck = i;
        LivingEntity livingentity = this.attacker.getTarget();
        if (livingentity == null) {
            return false;
        }
        if (!livingentity.isAlive()) {
            return false;
        }
        if (this.canPenalize) {
            if (--this.delayCounter <= 0) {
                this.path = this.attacker.getNavigation().createPath((Entity)livingentity, 0);
                this.delayCounter = 4 + this.attacker.getRandom().nextInt(7);
                return this.path != null;
            }
            return true;
        }
        this.path = this.attacker.getNavigation().createPath((Entity)livingentity, 0);
        if (this.path != null) {
            return true;
        }
        return this.getAttackReachSqr(livingentity) >= this.attacker.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = this.attacker.getTarget();
        if (livingentity == null) {
            return false;
        }
        if (!livingentity.isAlive()) {
            return false;
        }
        if (!this.longMemory) {
            return !this.attacker.getNavigation().isDone();
        }
        if (!this.attacker.isWithinRestriction(livingentity.blockPosition())) {
            return false;
        }
        return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
    }

    public void start() {
        this.attacker.getNavigation().moveTo(this.path, this.speedTowardsTarget);
        this.attacker.setAggressive(true);
        this.delayCounter = 0;
        this.ticksUntilNextAttack = 0;
    }

    public void stop() {
        LivingEntity livingentity = this.attacker.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.attacker.setTarget((LivingEntity)null);
        }
        this.attacker.setAggressive(false);
        this.attacker.getNavigation().stop();
    }

    public void tick() {
        LivingEntity livingentity = this.attacker.getTarget();
        if (livingentity == null) {
            return;
        }
        this.attacker.getLookControl().setLookAt((Entity)livingentity, 30.0f, 30.0f);
        double d0 = this.attacker.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
        this.delayCounter = Math.max(this.delayCounter - 1, 0);
        if ((this.longMemory || this.attacker.getSensing().hasLineOfSight((Entity)livingentity)) && this.delayCounter <= 0 && (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0 || livingentity.distanceToSqr(this.targetX, this.targetY, this.targetZ) >= 1.0 || this.attacker.getRandom().nextFloat() < 0.05f)) {
            this.targetX = livingentity.getX();
            this.targetY = livingentity.getY();
            this.targetZ = livingentity.getZ();
            this.delayCounter = 4 + this.attacker.getRandom().nextInt(7);
            if (this.canPenalize) {
                Node finalPathPoint;
                this.delayCounter += this.failedPathFindingPenalty;
                this.failedPathFindingPenalty = this.attacker.getNavigation().getPath() != null ? ((finalPathPoint = this.attacker.getNavigation().getPath().getEndNode()) != null && livingentity.distanceToSqr((double)finalPathPoint.x, (double)finalPathPoint.y, (double)finalPathPoint.z) < 1.0 ? 0 : (this.failedPathFindingPenalty += 10)) : (this.failedPathFindingPenalty += 10);
            }
            if (d0 > 1024.0) {
                this.delayCounter += 10;
            } else if (d0 > 256.0) {
                this.delayCounter += 5;
            }
            if (!this.attacker.getNavigation().moveTo((Entity)livingentity, this.speedTowardsTarget)) {
                this.delayCounter += 15;
            }
        }
        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        this.checkAndPerformAttack(livingentity, d0);
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = 20;
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected int getAttackInterval() {
        return 20;
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        return this.attacker.getBbWidth() * 2.0f * this.attacker.getBbWidth() * 2.0f + attackTarget.getBbWidth();
    }

    protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);
        if (distToEnemySqr <= d0 && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.attacker.swing(InteractionHand.MAIN_HAND);
            if (this.attacker.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE) && this.attacker.getAttributes().hasAttribute(Attributes.ATTACK_KNOCKBACK) && this.attacker.getAttributes().hasAttribute(Attributes.ATTACK_SPEED)) {
                if (!this.attacker.doHurtTarget((Entity)enemy)) {
                    enemy.hurt(this.attacker.damageSources().mobAttack((LivingEntity)this.attacker), 2.0f);
                }
            } else {
                enemy.hurt(this.attacker.damageSources().mobAttack((LivingEntity)this.attacker), 2.0f);
            }
        }
    }
}

