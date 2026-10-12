/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.navigation.FlyingPathNavigation
 *  net.minecraft.world.entity.ai.navigation.GroundPathNavigation
 *  net.minecraft.world.entity.ai.navigation.PathNavigation
 *  net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.LeavesBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.level.pathfinder.WalkNodeEvaluator
 */
package com.mna.entities.ai;

import com.mna.tools.SummonUtils;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FollowOwnerGoal
extends Goal {
    private final Mob tameable;
    private LivingEntity owner;
    private final LevelReader world;
    private final double followSpeed;
    private final PathNavigation navigator;
    private int timeToRecalcPath;
    private final float pathDist;
    private final float attackDist;
    private final float snapDist;
    private float oldWaterCost;
    private final boolean teleportToLeaves;

    public FollowOwnerGoal(Mob tameable, double speed, float pathDist, float attackDist, float snapDist, boolean teleportToLeaves) {
        this.tameable = tameable;
        this.world = tameable.level();
        this.followSpeed = speed;
        this.navigator = tameable.getNavigation();
        this.pathDist = pathDist;
        this.snapDist = snapDist;
        this.attackDist = attackDist;
        this.teleportToLeaves = teleportToLeaves;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(tameable.getNavigation() instanceof GroundPathNavigation || tameable.getNavigation() instanceof FlyingPathNavigation || tameable.getNavigation() instanceof WaterBoundPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean canUse() {
        LivingEntity livingentity = SummonUtils.getSummoner((LivingEntity)this.tameable);
        if (livingentity == null) {
            this.tameable.hurt(this.tameable.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
            return false;
        }
        if (this.tameable.distanceToSqr((Entity)livingentity) < (double)(this.pathDist * this.pathDist)) {
            return false;
        }
        if (this.tameable.getTarget() != null && this.tameable.distanceToSqr((Entity)livingentity) < (double)(this.attackDist * this.attackDist)) {
            return false;
        }
        this.owner = livingentity;
        this.tameable.setTarget(null);
        return true;
    }

    public boolean canContinueToUse() {
        return !this.navigator.isDone() && this.owner != null;
    }

    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tameable.getPathfindingMalus(PathType.WATER);
        this.tameable.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public void stop() {
        this.owner = null;
        this.navigator.stop();
        this.tameable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    public void tick() {
        this.tameable.getLookControl().setLookAt((Entity)this.owner, 10.0f, (float)this.tameable.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.tameable.isLeashed() && !this.tameable.isPassenger()) {
                if (this.tameable.distanceToSqr((Entity)this.owner) >= (double)(this.snapDist * this.snapDist)) {
                    this.tryToTeleportNearEntity();
                } else {
                    this.navigator.moveTo((Entity)this.owner, this.followSpeed);
                }
            }
        }
    }

    private void tryToTeleportNearEntity() {
        BlockPos blockpos = this.owner.blockPosition();
        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomNumber(-3, 3);
            int k = this.getRandomNumber(-1, 1);
            int l = this.getRandomNumber(-3, 3);
            boolean flag = this.tryToTeleportToLocation(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
            if (!flag) continue;
            return;
        }
    }

    private boolean tryToTeleportToLocation(int x, int y, int z) {
        if (Math.abs((double)x - this.owner.getX()) < 2.0 && Math.abs((double)z - this.owner.getZ()) < 2.0) {
            return false;
        }
        if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
            return false;
        }
        this.tameable.moveTo((double)x + 0.5, (double)y, (double)z + 0.5, this.tameable.getYRot(), this.tameable.getXRot());
        this.navigator.stop();
        return true;
    }

    private boolean isTeleportFriendlyBlock(BlockPos pos) {
        PathType pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic((BlockGetter)this.world, (BlockPos.MutableBlockPos)pos.mutable());
        if (pathnodetype != PathType.WALKABLE) {
            return false;
        }
        BlockState blockstate = this.world.getBlockState(pos.below());
        if (!this.teleportToLeaves && blockstate.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos blockpos = pos.subtract((Vec3i)this.tameable.blockPosition());
        return this.world.noCollision((Entity)this.tameable, this.tameable.getBoundingBox().move(blockpos));
    }

    private int getRandomNumber(int min, int max) {
        return this.tameable.getRandom().nextInt(max - min + 1) + min;
    }
}

