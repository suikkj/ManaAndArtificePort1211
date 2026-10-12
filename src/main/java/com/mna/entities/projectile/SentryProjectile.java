 
package com.mna.entities.projectile;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.EntityInit;
import com.mna.tools.TeleportHelper;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class SentryProjectile
extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Byte> AFFINITY = SynchedEntityData.defineId(SentryProjectile.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private int ticksInAir;
    private Entity owner;

    public SentryProjectile(EntityType<? extends SentryProjectile> type, Level world) {
        super((EntityType)EntityInit.SENTRY_PROJECTILE.get(), world);
        this.setNoGravity(true);
    }

    public SentryProjectile(Level worldIn, double x, double y, double z, Affinity affinity) {
        super((EntityType)EntityInit.SENTRY_PROJECTILE.get(), worldIn);
        this.setPos(x, y, z);
        this.setAffinity(affinity);
    }

    protected void onHitEntity(EntityHitResult p_213868_1_) {
        Entity entity = p_213868_1_.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            this.entityHit(livingentity);
        }
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    protected void entityHit(LivingEntity living) {
        if (!this.level().isClientSide()) {
            switch (this.getAffinity()) {
                case EARTH: {
                    living.hurt(living.damageSources().generic(), 5.0f);
                    break;
                }
                case ENDER: {
                    this.teleportBackwards(living);
                    break;
                }
                case FIRE: 
                case HELLFIRE: 
                case LIGHTNING: {
                    living.hurt(living.damageSources().inFire(), 5.0f);
                    living.igniteForSeconds((float)(3));
                    break;
                }
                case WATER: 
                case ICE: {
                    living.hurt(living.damageSources().magic(), 5.0f);
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, false, true));
                    break;
                }
                case WIND: {
                    living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0, false, false, true));
                    break;
                }
                default: {
                    living.hurt(living.damageSources().magic(), 15.0f);
                }
            }
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private void teleportBackwards(LivingEntity living) {
        Vec3 targetPos = TeleportHelper.calculateBlinkPosition(10.0, living, this.getDeltaMovement(), this.level());
        if (targetPos != null) {
            TeleportHelper.teleportEntity((Entity)living, (ResourceKey<Level>)this.level().dimension(), targetPos);
        }
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 Vector3d = new Vec3(x, y, z).normalize().scale((double)velocity);
        this.setDeltaMovement(Vector3d);
        double f = Vector3d.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)Vector3d.x, (double)Vector3d.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)Vector3d.y, (double)f) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Nullable
    protected EntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec) {
        return ProjectileUtil.getEntityHitResult((Level)this.level(), (Entity)this, (Vec3)startVec, (Vec3)endVec, (AABB)this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), p_213871_1_ -> !p_213871_1_.isSpectator() && p_213871_1_.isAlive() && p_213871_1_.isPickable() && (p_213871_1_ != this.getShooter() || this.ticksInAir >= 5));
    }

    public void tick() {
        Vec3 Vector3d = this.getDeltaMovement();
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            double f = Vector3d.horizontalDistance();
            this.setYRot((float)(Mth.atan2((double)Vector3d.x, (double)Vector3d.z) * 57.2957763671875));
            this.setXRot((float)(Mth.atan2((double)Vector3d.y, (double)f) * 57.2957763671875));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        if (this.isInWaterOrRain()) {
            this.clearFire();
        }
        ++this.ticksInAir;
        Vec3 Vector3d2 = this.position();
        Vec3 Vector3d3 = Vector3d2.add(Vector3d);
        net.minecraft.world.phys.HitResult raytraceresult = this.level().clip(new ClipContext(Vector3d2, Vector3d3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
        if (raytraceresult.getType() != HitResult.Type.MISS) {
            Vector3d3 = raytraceresult.getLocation();
        }
        while (this.isAlive()) {
            EntityHitResult entityraytraceresult = this.rayTraceEntities(Vector3d2, Vector3d3);
            if (entityraytraceresult != null) {
                raytraceresult = entityraytraceresult;
            }
            if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult)raytraceresult).getEntity();
                Entity entity1 = this.getShooter();
                if (entity != null && entity1 != null && entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                    raytraceresult = null;
                    entityraytraceresult = null;
                }
            }
            if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS && true) {
                this.onHit((HitResult)raytraceresult);
                this.hasImpulse = true;
            }
            if (entityraytraceresult == null) break;
            raytraceresult = null;
        }
        Vector3d = this.getDeltaMovement();
        double d3 = Vector3d.x;
        double d4 = Vector3d.y;
        double d0 = Vector3d.z;
        double d5 = this.getX() + d3;
        double d1 = this.getY() + d4;
        double d2 = this.getZ() + d0;
        double f1 = Vector3d.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)d3, (double)d0) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)d4, (double)f1) * 57.2957763671875));
        while (this.getXRot() - this.xRotO < -180.0f) {
            this.xRotO -= 360.0f;
        }
        while (this.getXRot() - this.xRotO >= 180.0f) {
            this.xRotO += 360.0f;
        }
        while (this.getYRot() - this.yRotO < -180.0f) {
            this.yRotO -= 360.0f;
        }
        while (this.getYRot() - this.yRotO >= 180.0f) {
            this.yRotO += 360.0f;
        }
        this.setXRot(Mth.lerp((float)0.2f, (float)this.xRotO, (float)this.getXRot()));
        this.setYRot(Mth.lerp((float)0.2f, (float)this.yRotO, (float)this.getYRot()));
        float f2 = 0.99f;
        if (this.isInWater()) {
            for (int j = 0; j < 4; ++j) {
                this.level().addParticle((ParticleOptions)ParticleTypes.BUBBLE, d5 - d3 * 0.25, d1 - d4 * 0.25, d2 - d0 * 0.25, d3, d4, d0);
            }
            f2 = this.getWaterDrag();
        }
        this.setDeltaMovement(Vector3d.scale((double)f2));
        this.setPos(d5, d1, d2);
        this.checkInsideBlocks();
        if (this.tickCount > 200) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    protected void onHit(HitResult raytraceResultIn) {
        HitResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)raytraceResultIn);
        }
    }

    private Affinity getAffinity() {
        try {
            return Affinity.values()[(Byte)this.entityData.get(AFFINITY)];
        }
        catch (Throwable t) {
            return Affinity.UNKNOWN;
        }
    }

    private void setAffinity(Affinity affinity) {
        this.entityData.set(AFFINITY, ((byte)affinity.ordinal()));
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(AFFINITY, ((byte)Affinity.ARCANE.ordinal()));
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("affinity", this.getAffinity().ordinal());
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("affinity")) {
            try {
                this.setAffinity(Affinity.values()[compound.getInt("affinity")]);
            }
            catch (Throwable t) {
                this.setAffinity(Affinity.UNKNOWN);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            float f = Mth.sqrt((float)((float)(x * x + z * z)));
            this.setXRot((float)(Mth.atan2((double)y, (double)f) * 57.2957763671875));
            this.setYRot((float)(Mth.atan2((double)x, (double)z) * 57.2957763671875));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    @OnlyIn(value=Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(d0)) {
            d0 = 1.0;
        }
        return distance < (d0 = d0 * 256.0 * SentryProjectile.getViewScale()) * d0;
    }

    public void spawnParticles(int particleCount, float partialTick) {
        Vec3 basePos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        MAParticleType particle = null;
        switch (this.getAffinity()) {
            case EARTH: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get()));
                break;
            }
            case ENDER: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get()));
                break;
            }
            case FIRE: 
            case HELLFIRE: 
            case LIGHTNING: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get()));
                break;
            }
            case WATER: 
            case ICE: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())).setScale(0.03f);
                break;
            }
            case WIND: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.04f).setColor(30, 30, 30);
                break;
            }
            default: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get()));
            }
        }
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3(0.0, 0.0, 0.0);
            Vec3 pos = basePos.add(this.getDeltaMovement().scale(Math.random()));
            this.level().addParticle((ParticleOptions)particle, pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    public boolean isAttackable() {
        return false;
    }

    protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return 0.0f;
    }

    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
    }

    @Nullable
    public Entity getShooter() {
        return this.owner;
    }

    public void setOwner(@Nullable Entity entityIn) {
        this.owner = entityIn;
    }

    protected float getWaterDrag() {
        return 0.6f;
    }
}


