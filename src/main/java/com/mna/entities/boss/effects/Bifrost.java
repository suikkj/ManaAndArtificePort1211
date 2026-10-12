/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityDimensions
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.boss.effects;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.Odin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class Bifrost
extends Entity {
    private static final Vec3 DOWN = new Vec3(0.0, -1.0, 0.0);
    private boolean summon = true;

    public Bifrost(EntityType<? extends Bifrost> type, Level world) {
        super((EntityType)EntityInit.BIFROST.get(), world);
        this.setNoGravity(true);
        this.setOnGround(false);
    }

    public Bifrost(Level worldIn, Vec3 position) {
        this((EntityType<? extends Bifrost>)((EntityType)EntityInit.BIFROST.get()), worldIn);
        this.setPos(position.x(), position.y(), position.z());
    }

    public void spawnParticles(int particleCount, float partialTick) {
        for (int i = 0; i < particleCount; ++i) {
            Vec3 pos = this.summon ? this.position().add(DOWN.scale((double)partialTick * Math.random())) : this.position().add(DOWN.scale((double)(-1.0f * partialTick) * Math.random()));
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setColor((float)Math.random() * 255.0f, (float)Math.random() * 255.0f, (float)Math.random() * 255.0f).setScale(0.08f).setMaxAge(50), pos.x, this.summon ? pos.y : pos.y - 10.0, pos.z, (double)0.6f * Math.random() + (double)0.1f, (double)-0.2f, 0.5);
            if (this.tickCount <= 50) continue;
            Vec3 vel = new Vec3(Math.random() - 0.5, 0.0, Math.random() - 0.5);
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setColor((float)Math.random() * 255.0f, (float)Math.random() * 255.0f, (float)Math.random() * 255.0f).setScale(0.1f), pos.x + vel.x, pos.y - 10.0 + Math.random(), pos.z + vel.z, vel.x, vel.y, vel.z);
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
    }

    public boolean isAttackable() {
        return false;
    }

    protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return 0.0f;
    }

    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
    }

    public void tick() {
        if (this.tickCount == 2 && !this.level().isClientSide()) {
            this.playSound(SFX.Entity.Odin.BIFROST, 1.0f, 1.0f);
        }
        if (this.tickCount > 100) {
            if (this.summon && !this.level().isClientSide()) {
                Odin odin = new Odin(this.level());
                odin.setPos(this.getX(), this.getY() - 10.0, this.getZ());
                odin.setupSpawn();
                this.level().addFreshEntity((Entity)odin);
            }
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void lerpMotion(double x, double y, double z) {
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
        return distance < (d0 = d0 * 64.0 * Bifrost.getViewScale()) * d0;
    }

    public void spawnParticleBurst(String identifier, Float radius) {
        MAParticleType particle = (MAParticleType)(ParticleInit.AIR_VELOCITY.get());
        int count = 100;
        double angleRads = 0.0;
        double step = Math.PI * 2 / (double)count;
        for (int i = 0; i < count; ++i) {
            Vec3 dir = new Vec3(Math.cos(angleRads += step), Math.random() * 0.1, Math.sin(angleRads)).normalize();
            Vec3 vel = dir.scale((double)(0.3f * radius.floatValue()));
            dir = dir.scale(0.2);
            this.level().addParticle((ParticleOptions)new MAParticleType(particle).setColor((float)Math.random(), (float)Math.random(), (float)Math.random()), this.getX() + dir.x, this.getY() + 1.0 + Math.random(), this.getZ() + dir.z, vel.x, vel.y, vel.z);
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("summon")) {
            this.summon = nbt.getBoolean("summon");
        }
    }

    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putBoolean("summon", this.summon);
    }

    public void setNoSummon() {
        this.summon = false;
    }

    public boolean shouldRender(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
        return true;
    }

    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(15.0);
    }
}


