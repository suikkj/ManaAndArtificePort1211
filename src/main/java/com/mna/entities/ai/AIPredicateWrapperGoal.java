/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 */
package com.mna.entities.ai;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class AIPredicateWrapperGoal<T extends LivingEntity>
extends Goal {
    private Goal delegateGoal;
    private Predicate<T> shouldExecute = null;
    private Predicate<T> shouldContinueExecuting = null;
    private T _entity;

    public AIPredicateWrapperGoal(T entity, Goal delegateGoal) {
        this.delegateGoal = delegateGoal;
        this._entity = entity;
    }

    public AIPredicateWrapperGoal<T> executionPredicate(Predicate<T> pred) {
        this.shouldExecute = pred;
        return this;
    }

    public AIPredicateWrapperGoal<T> continuationPredicate(Predicate<T> pred) {
        this.shouldContinueExecuting = pred;
        return this;
    }

    public boolean canUse() {
        if (this.shouldExecute != null) {
            return this.shouldExecute.test(this._entity) && this.delegateGoal.canUse();
        }
        return this.delegateGoal.canUse();
    }

    public void stop() {
        this.delegateGoal.stop();
    }

    public boolean canContinueToUse() {
        if (this.shouldContinueExecuting != null) {
            return this.shouldContinueExecuting.test(this._entity) && this.delegateGoal.canContinueToUse();
        }
        return this.delegateGoal.canContinueToUse();
    }

    public void start() {
        this.delegateGoal.start();
    }

    public void tick() {
        this.delegateGoal.tick();
    }

    public EnumSet<Goal.Flag> getFlags() {
        return this.delegateGoal.getFlags();
    }
}

