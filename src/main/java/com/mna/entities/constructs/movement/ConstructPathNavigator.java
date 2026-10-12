/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.navigation.GroundPathNavigation
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.pathfinder.Path
 *  net.minecraft.world.level.pathfinder.PathFinder
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.constructs.movement;

import com.google.common.collect.ImmutableSet;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.constructs.movement.ConstructNodeEvaluator;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class ConstructPathNavigator
extends GroundPathNavigation {
    private final Construct construct;

    public ConstructPathNavigator(Construct construct, Level level) {
        super((Mob)construct, level);
        this.construct = construct;
    }

    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new ConstructNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        return new PathFinder(this.nodeEvaluator, Math.max(maxVisitedNodes, 512));
    }

    protected Vec3 getTempMobPos() {
        if (this.construct.canFly() && !this.construct.onGround()) {
            return this.mob.position();
        }
        return super.getTempMobPos();
    }

    protected boolean canUpdatePath() {
        if (this.construct.canFly() && !this.construct.onGround()) {
            return this.canFloat() && this.mob.isInWater() || !this.mob.isPassenger();
        }
        return super.canUpdatePath();
    }

    public boolean isStableDestination(BlockPos target) {
        return this.level.getBlockState(target).entityCanStandOn((BlockGetter)this.level, target, (Entity)this.mob);
    }

    public Path createPath(BlockPos target, int reachRange) {
        if (this.construct.canFly() || this.construct.canSwim() && this.construct.isInWaterOrBubble()) {
            return this.createPath((Set)ImmutableSet.of(target), 8, false, reachRange);
        }
        return super.createPath(target, reachRange);
    }

    public Path createPath(Entity target, int reachRange) {
        return this.createPath(target.blockPosition(), reachRange);
    }

    protected boolean canMoveDirectly(Vec3 from, Vec3 to) {
        if (this.mob.isInWater()) {
            return ConstructPathNavigator.isClearForMovementBetween((Mob)this.mob, (Vec3)from, (Vec3)to, (boolean)true);
        }
        return false;
    }

    public void setEvaluatorFlags(boolean flying, boolean amphibious) {
        if (this.nodeEvaluator instanceof ConstructNodeEvaluator) {
            ((ConstructNodeEvaluator)this.nodeEvaluator).setFlying(flying);
            ((ConstructNodeEvaluator)this.nodeEvaluator).setAmphibious(amphibious);
        }
    }
}

