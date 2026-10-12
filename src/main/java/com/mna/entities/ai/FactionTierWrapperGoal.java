/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 */
package com.mna.entities.ai;

import com.mna.api.entities.IFactionEnemy;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

public class FactionTierWrapperGoal
extends Goal {
    private int requiredTier;
    private IFactionEnemy<?> entity;
    private Goal delegateGoal;

    public FactionTierWrapperGoal(int tier, IFactionEnemy<?> entity, Goal delegateGoal) {
        this.requiredTier = tier;
        this.entity = entity;
        this.delegateGoal = delegateGoal;
    }

    public boolean canUse() {
        return this.entity.getTier() >= this.requiredTier && this.delegateGoal.canUse();
    }

    public void stop() {
        this.delegateGoal.stop();
    }

    public boolean canContinueToUse() {
        return this.delegateGoal.canContinueToUse();
    }

    public boolean requiresUpdateEveryTick() {
        return this.delegateGoal.requiresUpdateEveryTick();
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

