/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.FlyingMob
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.control.MoveControl
 *  net.minecraft.world.entity.ai.control.MoveControl$Operation
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.summon;

import com.mna.ManaAndArtifice;
import com.mna.api.entities.ISummonTargetPredicate;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.ai.RetaliateOnAttackGoal;
import com.mna.tools.SummonUtils;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InsectSwarm
extends FlyingMob
implements ISummonTargetPredicate {
    public InsectSwarm(EntityType<? extends FlyingMob> type, Level world) {
        super(type, world);
        this.moveControl = new MoveHelperController(this);
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            LivingEntity owner;
            if (this.getTarget() != null && (this.getTarget().hasEffect(MobEffects.POISON) || !this.getTarget().isAlive())) {
                this.setTarget(null);
            }
            if ((owner = SummonUtils.getSummoner((LivingEntity)this)) == null || !owner.isAlive()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(5, (Goal)new SwarmFlyGoal(this));
        this.targetSelector.addGoal(1, (Goal)new RetaliateOnAttackGoal((Mob)this, new Class[0]));
    }

    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        if (pPotioneffect.getEffect() == MobEffects.POISON || pPotioneffect.getEffect() == MobEffects.WITHER) {
            return false;
        }
        return super.canBeAffected(pPotioneffect);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public boolean doHurtTarget(Entity pEntity) {
        if (pEntity instanceof LivingEntity) {
            ((LivingEntity)pEntity).addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        }
        return true;
    }

    public void spawnParticle() {
        if (ManaAndArtifice.instance.proxy.isGamePaused()) {
            return;
        }
        float height = this.getType().getHeight();
        float width = this.getType().getWidth();
        float offsetAvg = (height + width) / 2.0f;
        float halfOffsetAvg = offsetAvg / 2.0f;
        for (int i = 0; i < 2; ++i) {
            Vec3 pos = new Vec3(this.getX() - (double)halfOffsetAvg + Math.random() * (double)offsetAvg, this.getY() + Math.random() * (double)offsetAvg, this.getZ() - (double)halfOffsetAvg + Math.random() * (double)offsetAvg);
            Vec3 vel = pos.subtract(this.position()).subtract(0.0, (double)height * Math.random(), 0.0).normalize().scale((double)-0.05f);
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setColor(69, 66, 30), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        }
    }

    @Override
    public boolean shouldSummonTarget(Entity candidate, boolean isFriendly) {
        if (isFriendly || !(candidate instanceof LivingEntity)) {
            return false;
        }
        return ((LivingEntity)candidate).canBeAffected(new MobEffectInstance(MobEffects.POISON)) && !((LivingEntity)candidate).hasEffect(MobEffects.POISON);
    }

    static class MoveHelperController
    extends MoveControl {
        private final InsectSwarm parentEntity;
        private int courseChangeCooldown;

        public MoveHelperController(InsectSwarm parent) {
            super((Mob)parent);
            this.parentEntity = parent;
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO && this.courseChangeCooldown-- <= 0) {
                this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
                Vec3 vector3d = new Vec3(this.wantedX - this.parentEntity.getX(), this.wantedY - this.parentEntity.getY(), this.wantedZ - this.parentEntity.getZ());
                double d0 = vector3d.length();
                if (this.isCollided(vector3d = vector3d.normalize(), Mth.ceil((double)d0))) {
                    this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add(vector3d.scale(0.1)));
                } else {
                    this.operation = MoveControl.Operation.WAIT;
                }
            }
        }

        private boolean isCollided(Vec3 p_220673_1_, int p_220673_2_) {
            AABB axisalignedbb = this.parentEntity.getBoundingBox();
            for (int i = 1; i < p_220673_2_; ++i) {
                axisalignedbb = axisalignedbb.move(p_220673_1_);
                if (this.parentEntity.level().noCollision((Entity)this.parentEntity, axisalignedbb)) continue;
                return false;
            }
            return true;
        }
    }

    static class SwarmFlyGoal
    extends Goal {
        private final InsectSwarm parentEntity;

        public SwarmFlyGoal(InsectSwarm pixie) {
            this.parentEntity = pixie;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            MoveControl movementcontroller = this.parentEntity.getMoveControl();
            if (!movementcontroller.hasWanted()) {
                return true;
            }
            if (this.parentEntity.getTarget() == null) {
                double d2;
                double d1;
                double d0 = movementcontroller.getWantedX() - this.parentEntity.getX();
                double d3 = d0 * d0 + (d1 = movementcontroller.getWantedY() - this.parentEntity.getY()) * d1 + (d2 = movementcontroller.getWantedZ() - this.parentEntity.getZ()) * d2;
                return d3 < 4.0 || d3 > 3600.0;
            }
            double dist = this.parentEntity.getTarget().distanceToSqr((Entity)this.parentEntity);
            return !this.parentEntity.getSensing().hasLineOfSight((Entity)this.parentEntity.getTarget()) || dist > 25.0 || dist < 4.0;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            if (this.parentEntity.getTarget() == null) {
                LivingEntity summoner;
                boolean setRandom = true;
                if (SummonUtils.isSummon((Entity)this.parentEntity) && (summoner = SummonUtils.getSummoner((LivingEntity)this.parentEntity)) != null && summoner.distanceTo((Entity)this.parentEntity) > 16.0f) {
                    setRandom = false;
                    this.parentEntity.getMoveControl().setWantedPosition(summoner.getX(), summoner.getEyeY(), summoner.getZ(), this.parentEntity.getAttributeValue(Attributes.MOVEMENT_SPEED));
                }
                if (setRandom) {
                    RandomSource random = this.parentEntity.getRandom();
                    double d0 = this.parentEntity.getX() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
                    double d1 = this.parentEntity.getY() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
                    double d2 = this.parentEntity.getZ() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
                    int count = 0;
                    int y = (int)d1;
                    boolean ground = false;
                    while (count < 16) {
                        BlockPos bpTestPos = BlockPos.containing((double)d0, (double)y, (double)d2);
                        if (this.parentEntity.level().isEmptyBlock(bpTestPos)) {
                            ++count;
                            --y;
                            continue;
                        }
                        if (this.parentEntity.level().isWaterAt(bpTestPos)) {
                            ++count;
                            ++y;
                            ground = true;
                            continue;
                        }
                        ground = true;
                        break;
                    }
                    if (!ground) {
                        d1 -= 16.0;
                    }
                    this.parentEntity.getMoveControl().setWantedPosition(d0, d1, d2, 1.0);
                }
            } else {
                Vec3 target = this.parentEntity.getTarget().getEyePosition();
                this.parentEntity.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.0);
            }
        }
    }
}

