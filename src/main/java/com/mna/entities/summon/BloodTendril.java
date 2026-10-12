 
package com.mna.entities.summon;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.entities.EntityInit;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BloodTendril
extends Monster
implements GeoEntity,
OwnableEntity {
    private static final EntityDataAccessor<Direction> SWING_DIRECTION = SynchedEntityData.defineId(BloodTendril.class, (EntityDataSerializer)EntityDataSerializers.DIRECTION);
    protected AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private int swing_count = 0;
    private int nonRotatedDeathTime = 0;
    private int duration = 0;
    private boolean dealingDamage = false;
    private LivingEntity summoner;
    private List<LivingEntity> knownTargets = new ArrayList<LivingEntity>();

    public BloodTendril(Level pLevel) {
        this((EntityType<? extends Monster>)((EntityType)EntityInit.BLOOD_TENDRIL.get()), pLevel);
    }

    public BloodTendril(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public BloodTendril setSummoner(LivingEntity summoner, int duration) {
        this.summoner = summoner;
        this.duration = duration;
        return this;
    }

    public void tick() {
        this.lookAt(EntityAnchorArgument.Anchor.FEET, this.position().add(new Vec3(1.0, 0.0, 0.0)));
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.summoner == null) {
                this.discard();
                return;
            }
            if (this.tickCount > this.duration) {
                if (this.isAlive() && !this.level().isClientSide()) {
                    this.hurt(this.damageSources().fellOutOfWorld(), 99999.0f);
                }
                return;
            }
            if (this.swing_count > 0) {
                --this.swing_count;
                if (this.swing_count == 6) {
                    this.tryHurtTarget();
                }
                if (this.swing_count == 0) {
                    this.entityData.set(SWING_DIRECTION, Direction.UP);
                    this.setTarget(null);
                }
            } else {
                if (this.getTarget() != null && (this.getTarget().isDeadOrDying() || this.getTarget().isRemoved())) {
                    this.setTarget(null);
                }
                if (this.tickCount % 20 == 0) {
                    this.reassessTargets();
                }
                this.pickTarget();
            }
        } else if (!this.isDeadOrDying()) {
            Vec3 fwd = new Vec3(0.25, 0.0, 0.0);
            Vec3 velRange = Vec3.ZERO;
            boolean isSwinging = true;
            Direction swingDir = (Direction)this.entityData.get(SWING_DIRECTION);
            switch (swingDir) {
                case EAST: {
                    velRange = new Vec3(0.1, 0.0, 0.25);
                    break;
                }
                case NORTH: {
                    fwd = new Vec3(0.0, 0.0, -0.25);
                    velRange = new Vec3(0.25, 0.0, 0.1);
                    break;
                }
                case SOUTH: {
                    fwd = new Vec3(0.0, 0.0, 0.25);
                    velRange = new Vec3(0.25, 0.0, 0.1);
                    break;
                }
                case WEST: {
                    fwd = new Vec3(-0.25, 0.0, 0.0);
                    velRange = new Vec3(0.1, 0.0, 0.25);
                    break;
                }
                default: {
                    isSwinging = false;
                }
            }
            Vec3 side = fwd.normalize().cross(new Vec3(0.0, 1.0, 0.0)).normalize().scale(0.25);
            int particleCount = isSwinging ? 5 : 1;
            for (int i = 0; i < particleCount; ++i) {
                Vec3 particleVelocity = isSwinging ? fwd.add(MathUtils.RandomBetween(-velRange.x, velRange.x), 0.25, MathUtils.RandomBetween(-velRange.z, velRange.z)) : Vec3.ZERO;
                Vec3 particlePos = this.position().add((MathUtils.RandomBetween(-fwd.x, fwd.x) + MathUtils.RandomBetween(-side.x, side.x)) * this.getBoundingBox().getXsize(), MathUtils.RandomBetween(0.25, isSwinging ? 0.75 : 0.5) * this.getBoundingBox().getYsize(), (MathUtils.RandomBetween(-fwd.z, fwd.z) + MathUtils.RandomBetween(-side.z, side.z)) * this.getBoundingBox().getZsize());
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setGravity(isSwinging ? 0.05f : 0.01f).setPhysics(true), particlePos.x, particlePos.y, particlePos.z, particleVelocity.x, particleVelocity.y, particleVelocity.z);
            }
        }
    }

    protected void tickDeath() {
        ++this.nonRotatedDeathTime;
        if (this.nonRotatedDeathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent((Entity)this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    private void tryHurtTarget() {
        if (this.getTarget() == null) {
            return;
        }
        double targetDist = 2.0 * this.getBoundingBox().getYsize();
        if (this.getTarget().distanceToSqr((Entity)this) > targetDist) {
            return;
        }
        this.dealingDamage = true;
        this.doHurtTarget((Entity)this.getTarget());
        this.dealingDamage = false;
    }

    public float getYRot() {
        if (this.dealingDamage) {
            Direction dir = (Direction)this.entityData.get(SWING_DIRECTION);
            switch (dir) {
                case EAST: {
                    return 270.0f;
                }
                case NORTH: {
                    return 180.0f;
                }
                case SOUTH: {
                    return 0.0f;
                }
                case WEST: {
                    return 90.0f;
                }
            }
        }
        return super.getYRot();
    }

    private void reassessTargets() {
        this.knownTargets.clear();
        this.knownTargets.addAll(this.level().getEntities((Entity)this, this.getBoundingBox().inflate(32.0), e -> e != this && e != this.summoner && e instanceof LivingEntity && !SummonUtils.isTargetFriendly(e, this.summoner)).stream().map(e -> (LivingEntity)e).toList());
    }

    private void pickTarget() {
        this.knownTargets.removeIf(e -> e == null || e.isDeadOrDying() || !e.isAlive());
        if (this.knownTargets.size() == 0) {
            return;
        }
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity target : this.knownTargets) {
            double dist = target.distanceToSqr((Entity)this);
            if (!(dist < closestDist)) continue;
            closestDist = dist;
            closest = target;
        }
        double targetDist = 2.0 * this.getBoundingBox().getYsize();
        if (closestDist < targetDist) {
            this.setTarget(closest);
            this.swingAtTarget();
        }
    }

    private void swingAtTarget() {
        if (this.getTarget() == null) {
            return;
        }
        Vec3 delta = this.position().subtract(this.getTarget().position());
        if (Math.abs(delta.x) > Math.abs(delta.z)) {
            if (delta.x < 0.0) {
                this.entityData.set(SWING_DIRECTION, Direction.EAST);
            } else {
                this.entityData.set(SWING_DIRECTION, Direction.WEST);
            }
        } else if (delta.z < 0.0) {
            this.entityData.set(SWING_DIRECTION, Direction.SOUTH);
        } else {
            this.entityData.set(SWING_DIRECTION, Direction.NORTH);
        }
    }

    protected SoundEvent getAmbientSound() {
        return SFX.Entity.BloodTendril.IDLE;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.BloodTendril.DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SFX.Spell.Impact.Single.BLOOD;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> this.handleAnimState((AnimationState<BloodTendril>)state));
        registrar.add(new AnimationController[]{controller});
    }

    protected PlayState handleAnimState(AnimationState<BloodTendril> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.isDeadOrDying()) {
            builder.thenPlayAndHold("animation.blood_tendril.die");
        } else {
            if (this.tickCount < 20) {
                builder.thenPlay("animation.blood_tendril.summon");
            } else {
                Direction swing = (Direction)this.entityData.get(SWING_DIRECTION);
                switch (swing) {
                    case NORTH: {
                        builder.thenPlay("animation.blood_tendril.swing_north");
                        break;
                    }
                    case SOUTH: {
                        builder.thenPlay("animation.blood_tendril.swing_south");
                        break;
                    }
                    case EAST: {
                        builder.thenPlay("animation.blood_tendril.swing_east");
                        break;
                    }
                    case WEST: {
                        builder.thenPlay("animation.blood_tendril.swing_west");
                        break;
                    }
                }
            }
            builder.thenLoop("animation.blood_tendril.idle");
        }
        return state.setAndContinue(builder);
    }

    protected void registerGoals() {
    }

    public void travel(Vec3 pTravelVector) {
    }

    public void setDeltaMovement(double pX, double pY, double pZ) {
    }

    public void setDeltaMovement(Vec3 pDeltaMovement) {
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SWING_DIRECTION, Direction.UP);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (pKey == SWING_DIRECTION) {
            this.swing_count = 10;
        }
    }

    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide()) {
            List<BloodTendril> existing = this.level().getEntitiesOfClass(BloodTendril.class, this.getBoundingBox().inflate(6.0));
            existing.sort((a, b) -> b.tickCount - a.tickCount);
            int removeCount = existing.size() - 4;
            for (int i = 0; i < removeCount; ++i) {
                ((BloodTendril)(existing.get(i))).discard();
            }
        }
    }

    public UUID getOwnerUUID() {
        return null;
    }

    public LivingEntity getOwner() {
        return this.summoner;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }
}

