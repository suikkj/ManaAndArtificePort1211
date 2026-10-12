/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.goal.Goal
 */
package com.mna.entities.ai;

import com.mna.tools.SummonUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class NeverTargetOwnerGoal
extends Goal {
    private final Mob goalOwner;

    public NeverTargetOwnerGoal(Mob mobIn) {
        this.goalOwner = mobIn;
    }

    public boolean canUse() {
        return this.goalOwner.getTarget() == SummonUtils.getSummoner((LivingEntity)this.goalOwner);
    }

    public void start() {
        this.goalOwner.setTarget(null);
    }
}

