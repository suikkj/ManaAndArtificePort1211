/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.control.MoveControl
 *  net.minecraft.world.entity.ai.control.MoveControl$Operation
 *  net.minecraft.world.entity.ai.navigation.PathNavigation
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.level.pathfinder.NodeEvaluator
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.minecraftforge.fluids.FluidType
 */
package com.mna.entities.constructs.movement;

import com.mna.entities.constructs.animated.Construct;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidType;

public class ConstructMoveControl
extends MoveControl {
    private final Construct construct;
    private final boolean applyGravity;
    private float inWaterSpeedModifier;
    private int maxTurnX;
    private float outsideWaterSpeedModifier;
    private float maxTurnY;

    public ConstructMoveControl(Construct construct, int pMaxTurnX, int pMaxTurnY, float pInWaterSpeedModifier, float pOutsideWaterSpeedModifier, boolean pApplyGravity) {
        super((Mob)construct);
        this.construct = construct;
        this.maxTurnX = pMaxTurnX;
        this.maxTurnY = pMaxTurnY;
        this.inWaterSpeedModifier = pInWaterSpeedModifier;
        this.outsideWaterSpeedModifier = pOutsideWaterSpeedModifier;
        this.applyGravity = pApplyGravity;
    }

    public void tick() {
        if (this.construct.isInFluidType()) {
            FluidType fluidType = this.construct.getMaxHeightFluidType();
            if (fluidType != null && this.construct.canSwimInFluidType(fluidType)) {
                this.swimTick();
            } else {
                this.groundTick();
            }
        } else if (this.construct.canFly() && !this.construct.isDefeated()) {
            this.flyTick();
        } else {
            this.groundTick();
        }
    }

    private void groundTick() {
        if (this.operation == MoveControl.Operation.STRAFE) {
            float f8;
            float f = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float)this.speedModifier * f;
            float f2 = this.strafeForwards;
            float f3 = this.strafeRight;
            float f4 = Mth.sqrt((float)(f2 * f2 + f3 * f3));
            if (f4 < 1.0f) {
                f4 = 1.0f;
            }
            f4 = f1 / f4;
            float f5 = Mth.sin((float)(this.mob.getYRot() * ((float)Math.PI / 180)));
            float f6 = Mth.cos((float)(this.mob.getYRot() * ((float)Math.PI / 180)));
            float f7 = (f2 *= f4) * f6 - (f3 *= f4) * f5;
            if (!this.isWalkable(f7, f8 = f3 * f6 + f2 * f5)) {
                this.strafeForwards = 1.0f;
                this.strafeRight = 0.0f;
            }
            this.mob.setSpeed(f1);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = MoveControl.Operation.WAIT;
        } else if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedZ - this.mob.getZ();
            double d2 = this.wantedY - this.mob.getY();
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < 2.500000277905201E-7) {
                this.mob.setZza(0.0f);
                return;
            }
            float f9 = (float)(Mth.atan2((double)d1, (double)d0) * 57.2957763671875) - 90.0f;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0f));
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos blockpos = this.mob.blockPosition();
            BlockState blockstate = this.mob.level().getBlockState(blockpos);
            if (!blockstate.is(BlockTags.LEAVES)) {
                VoxelShape voxelshape = blockstate.getCollisionShape((BlockGetter)this.mob.level(), blockpos);
                if (d2 > (double)this.mob.getStepHeight() && d0 * d0 + d1 * d1 < (double)Math.max(1.0f, this.mob.getBbWidth()) || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double)blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
                    this.mob.getJumpControl().jump();
                    this.operation = MoveControl.Operation.JUMPING;
                }
            }
        } else if (this.operation == MoveControl.Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround()) {
                this.operation = MoveControl.Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0f);
        }
    }

    private void swimTick() {
        if (this.applyGravity && this.mob.isInWater()) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.005, 0.0));
        }
        if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
            double d2;
            double d1;
            double d0 = this.wantedX - this.mob.getX();
            double d3 = d0 * d0 + (d1 = this.wantedY - this.mob.getY()) * d1 + (d2 = this.wantedZ - this.mob.getZ()) * d2;
            if (d3 < 2.500000277905201E-7) {
                this.mob.setZza(0.0f);
            } else {
                float f = (float)(Mth.atan2((double)d2, (double)d0) * 57.2957763671875) - 90.0f;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, this.maxTurnY));
                this.mob.yBodyRot = this.mob.getYRot();
                this.mob.yHeadRot = this.mob.getYRot();
                float f1 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                if (this.mob.isInWater()) {
                    this.mob.setSpeed(f1 * this.inWaterSpeedModifier);
                    double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                    if (Math.abs(d1) > (double)1.0E-5f || Math.abs(d4) > (double)1.0E-5f) {
                        float f3 = -((float)(Mth.atan2((double)d1, (double)d4) * 57.2957763671875));
                        f3 = Mth.clamp((float)Mth.wrapDegrees((float)f3), (float)(-this.maxTurnX), (float)this.maxTurnX);
                        this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f3, 5.0f));
                    }
                    float f6 = Mth.cos((float)(this.mob.getXRot() * ((float)Math.PI / 180)));
                    float f4 = Mth.sin((float)(this.mob.getXRot() * ((float)Math.PI / 180)));
                    this.mob.zza = f6 * f1;
                    this.mob.yya = -f4 * f1;
                } else {
                    float f5 = Math.abs(Mth.wrapDegrees((float)(this.mob.getYRot() - f)));
                    float f2 = ConstructMoveControl.getTurningSpeedFactor(f5);
                    this.mob.setSpeed(f1 * this.outsideWaterSpeedModifier * f2);
                }
            }
        } else {
            this.mob.setSpeed(0.0f);
            this.mob.setXxa(0.0f);
            this.mob.setYya(0.0f);
            this.mob.setZza(0.0f);
        }
    }

    private void flyTick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            this.mob.setNoGravity(true);
            double deltaX = this.wantedX - this.mob.getX();
            double deltaY = this.wantedY - this.mob.getY();
            double deltaZ = this.wantedZ - this.mob.getZ();
            double distSqr = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            if (distSqr < 2.500000277905201E-7) {
                this.mob.setYya(0.0f);
                this.mob.setZza(0.0f);
                return;
            }
            float faceAngle = (float)(Mth.atan2((double)deltaZ, (double)deltaX) * 57.2957763671875) - 90.0f;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), faceAngle, 90.0f));
            float mobSpeed = this.mob.onGround() ? (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)) : (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
            this.mob.setSpeed(mobSpeed);
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            if (Math.abs(deltaY) > (double)1.0E-5f || Math.abs(horizontalDistance) > (double)1.0E-5f) {
                float f2 = (float)(-(Mth.atan2((double)deltaY, (double)horizontalDistance) * 57.2957763671875));
                this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f2, this.maxTurnX));
                this.mob.setYya(deltaY > 0.0 ? mobSpeed : -mobSpeed);
            }
        } else {
            BlockPos cBPos = this.construct.blockPosition();
            if (this.construct.level().getBlockState(cBPos).entityCanStandOn((BlockGetter)this.construct.level(), cBPos, (Entity)this.construct) || this.construct.level().getBlockState(cBPos.below()).entityCanStandOn((BlockGetter)this.construct.level(), cBPos.below(), (Entity)this.construct)) {
                this.mob.setNoGravity(false);
            }
            this.mob.setYya(0.0f);
            this.mob.setZza(0.0f);
        }
    }

    private static float getTurningSpeedFactor(float p_249853_) {
        return 1.0f - Mth.clamp((float)((p_249853_ - 10.0f) / 50.0f), (float)0.0f, (float)1.0f);
    }

    private boolean isWalkable(float pRelativeX, float pRelativeZ) {
        NodeEvaluator nodeevaluator;
        PathNavigation pathnavigation = this.mob.getNavigation();
        return pathnavigation == null || (nodeevaluator = pathnavigation.getNodeEvaluator()) == null || nodeevaluator.getBlockPathType((BlockGetter)this.mob.level(), Mth.floor((double)(this.mob.getX() + (double)pRelativeX)), this.mob.getBlockY(), Mth.floor((double)(this.mob.getZ() + (double)pRelativeZ))) == PathType.WALKABLE;
    }
}

