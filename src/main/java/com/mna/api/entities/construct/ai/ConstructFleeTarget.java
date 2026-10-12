/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.goal.AvoidEntityGoal
 *  net.minecraft.world.entity.ai.navigation.PathNavigation
 *  net.minecraft.world.entity.ai.util.GoalUtils
 *  net.minecraft.world.entity.ai.util.RandomPos
 *  net.minecraft.world.entity.animal.AbstractGolem
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.entities.construct.ai;

import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.phys.Vec3;

public class ConstructFleeTarget
extends AvoidEntityGoal<LivingEntity> {
    final IConstruct<?> construct;

    public ConstructFleeTarget(IConstruct<?> construct, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier) {
        super((PathfinderMob)construct.asEntity(), LivingEntity.class, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier);
        this.construct = construct;
    }

    public boolean canUse() {
        if (this.construct.getIntelligence() < 16 || this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.BLOCK) || !this.construct.getConstructData().isAnyCapabilityEnabled(ConstructCapability.CAST_SPELL, ConstructCapability.RANGED_ATTACK, ConstructCapability.FLUID_DISPENSE)) {
            return false;
        }
        AbstractGolem constructEntity = this.construct.asEntity();
        this.toAvoid = constructEntity.getTarget();
        if (this.toAvoid == null) {
            return false;
        }
        if (constructEntity.distanceTo((Entity)this.toAvoid) > 4.0f || this.construct.isRangedAttacking()) {
            return false;
        }
        Vec3 vec3 = ConstructFleeTarget.getPosAway(this.construct, 10, 7, this.toAvoid.position());
        if (vec3 == null) {
            return false;
        }
        if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr((Entity)this.mob)) {
            return false;
        }
        if (this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.FLY)) {
            int count = 0;
            while (count < 5 && constructEntity.level().isEmptyBlock(BlockPos.containing((double)vec3.x, (double)vec3.y, (double)vec3.z))) {
                ++count;
                vec3 = vec3.add(0.0, 1.0, 0.0);
            }
            vec3 = vec3.subtract(0.0, 1.0, 0.0);
        }
        this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
        return this.path != null;
    }

    @Nullable
    private static Vec3 getPosAway(IConstruct<?> pMob, int pRadius, int pYRange, Vec3 pVectorPosition) {
        AbstractGolem constructEntity = pMob.asEntity();
        Vec3 vec3 = constructEntity.position().subtract(pVectorPosition);
        return RandomPos.generateRandomPos((PathfinderMob)constructEntity, () -> {
            BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians((RandomSource)constructEntity.getRandom(), (int)pRadius, (int)pYRange, (int)0, (double)vec3.x, (double)vec3.z, (double)1.5707963705062866);
            return blockpos == null ? null : ConstructFleeTarget.generateRandomPosTowardDirection(pMob, pRadius, false, blockpos);
        });
    }

    @Nullable
    private static BlockPos generateRandomPosTowardDirection(IConstruct<?> pMob, int pRadius, boolean pShortCircuit, BlockPos pPos) {
        AbstractGolem constructEntity = pMob.asEntity();
        BlockPos blockpos = RandomPos.generateRandomPosTowardDirection((PathfinderMob)constructEntity, (int)pRadius, (RandomSource)constructEntity.getRandom(), (BlockPos)pPos);
        if (!pMob.getConstructData().isCapabilityEnabled(ConstructCapability.FLY) && GoalUtils.isNotStable((PathNavigation)constructEntity.getNavigation(), (BlockPos)blockpos)) {
            return null;
        }
        return !GoalUtils.isOutsideLimits((BlockPos)blockpos, (PathfinderMob)constructEntity) && !GoalUtils.hasMalus((PathfinderMob)constructEntity, (BlockPos)blockpos) ? blockpos : null;
    }
}

