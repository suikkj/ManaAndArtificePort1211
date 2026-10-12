 
package com.mna.entities.boss.attacks;

import com.mna.entities.EntityInit;
import com.mna.tools.math.MathUtils;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class OrangeChickenProjectile
extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(OrangeChickenProjectile.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private LivingEntity target = null;
    private int ticksInAir;

    public OrangeChickenProjectile(EntityType<OrangeChickenProjectile> type, Level world) {
        super((EntityType)EntityInit.ORANGE_CHICKEN.get(), world);
        this.setNoGravity(true);
    }

    public OrangeChickenProjectile(Level world, LivingEntity target) {
        this((EntityType<OrangeChickenProjectile>)((EntityType)EntityInit.ORANGE_CHICKEN.get()), world);
        if (target != null) {
            this.entityData.set(TARGET_ID, target.getId());
            this.target = target;
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(TARGET_ID, -1);
    }

    protected void onHit(HitResult raytraceResultIn) {
        if (this.level().isClientSide()) {
            return;
        }
        this.explode();
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!pSource.is(DamageTypeTags.IS_EXPLOSION) || this.isRemoved()) {
            this.explode();
        }
        return true;
    }

    private void explode() {
        if (!this.level().isClientSide() && !this.isRemoved()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), 5.0f, Level.ExplosionInteraction.TNT);
            int cookies = 5 + (int)(Math.random() * 10.0);
            for (int i = 0; i < cookies; ++i) {
                ItemEntity cookie = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack((ItemLike)Items.COOKIE));
                cookie.setDeltaMovement(new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5));
                this.level().addFreshEntity((Entity)cookie);
            }
        }
    }

    @Nullable
    private LivingEntity getTarget() {
        Entity e;
        if (this.target == null && (e = this.level().getEntity(((Integer)this.entityData.get(TARGET_ID)).intValue())) instanceof LivingEntity) {
            this.target = (LivingEntity)e;
        }
        return this.target;
    }

    public void tick() {
        EntityHitResult entityraytraceresult;
        if (this.isRemoved()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null) {
            if (!this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        float homingStrength = 5.234f;
        Vec3 myPos = this.position();
        Vec3 theirPos = target.position().add(0.0, (double)(target.getBbHeight() / 2.0f), 0.0);
        float tickTheta = 15.0f * MathUtils.clamp01(homingStrength);
        if (tickTheta > 0.0f) {
            Vec3 desiredHeading = theirPos.subtract(myPos).normalize();
            Vec3 calculatedHeading = MathUtils.rotateTowards(this.getDeltaMovement(), desiredHeading, tickTheta).normalize().scale(1.25);
            this.setDeltaMovement(calculatedHeading);
        }
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            double f = motion.horizontalDistance();
            this.setYRot((float)(Mth.atan2((double)motion.x, (double)motion.z) * 57.2957763671875));
            this.setXRot((float)(Mth.atan2((double)motion.y, (double)f) * 57.2957763671875));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        ++this.ticksInAir;
        Vec3 Vector3d2 = this.position();
        Vec3 Vector3d3 = Vector3d2.add(motion);
        net.minecraft.world.phys.HitResult raytraceresult = this.level().clip(new ClipContext(Vector3d2, Vector3d3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
        if (raytraceresult.getType() != HitResult.Type.MISS) {
            Vector3d3 = raytraceresult.getLocation();
        }
        if ((entityraytraceresult = this.rayTraceEntities(Vector3d2, Vector3d3)) != null) {
            raytraceresult = entityraytraceresult;
        }
        if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS && true) {
            this.onHit((HitResult)raytraceresult);
            this.hasImpulse = true;
        }
        motion = this.getDeltaMovement();
        double d3 = motion.x;
        double d4 = motion.y;
        double d0 = motion.z;
        double d5 = this.getX() + d3;
        double d1 = this.getY() + d4;
        double d2 = this.getZ() + d0;
        double f1 = motion.horizontalDistance();
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
        this.setPos(d5, d1, d2);
        this.checkInsideBlocks();
        if (this.tickCount > 400) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Nullable
    protected EntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec) {
        return ProjectileUtil.getEntityHitResult((Level)this.level(), (Entity)this, (Vec3)startVec, (Vec3)endVec, (AABB)this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), candidate -> !candidate.isSpectator() && candidate.isAlive() && candidate.isPickable() || this.ticksInAir >= 5);
    }
}


