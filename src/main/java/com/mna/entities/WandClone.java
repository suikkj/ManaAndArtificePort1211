/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.LivingUtilityEntity;
import com.mna.items.ItemInit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WandClone
extends Entity {
    protected static final EntityDataAccessor<String> CASTER_UUID = SynchedEntityData.defineId(LivingUtilityEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(LivingUtilityEntity.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<String> CURRENT_ANIMATION = SynchedEntityData.defineId(LivingUtilityEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private int age = 0;
    public static final Byte GUIDING_REAGENT_PLACEMENT = 0;
    public static final Byte POWERING_UP = 1;
    public static final Byte COLLECTING_REAGENTS = 2;
    public static final Byte COLLECTING_PATTERNS = 3;
    public static final Byte PROCESSING_RITUAL = 4;
    public static final Byte COMPLETING = 5;
    public static final Byte DEAD = 99;
    private Vec3 centerPoint = new Vec3(0.0, 0.0, 0.0);

    public WandClone(EntityType<? extends WandClone> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    public byte getState() {
        return (Byte)this.entityData.get(STATE);
    }

    public void tick() {
        this.setAge(this.getAge() + 1);
        if (this.level().isClientSide()) {
            this.spawnParticles();
        }
        if (this.getAge() >= 81 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            Vec3 up = new Vec3(0.0, 1.0, 0.0);
            Vec3 perpendicular = up.cross(this.getLookAngle());
            Vec3 pointOne = new Vec3(this.getX() + perpendicular.x, this.getY() + perpendicular.y, this.getZ() + perpendicular.z);
            Vec3 pointTwo = new Vec3(this.getX() + perpendicular.x, this.getY() + perpendicular.y + 1.0, this.getZ() + perpendicular.z);
            Vec3 pointThree = new Vec3(this.getX(), this.getY() + perpendicular.y, this.getZ());
            Vec3 pointFour = new Vec3(this.getX(), this.getY() + perpendicular.y + 1.0, this.getZ());
            double CenterPointX = (pointOne.x + pointTwo.x + pointThree.x + pointFour.x) / 4.0;
            double CenterPointZ = (pointOne.z + pointTwo.z + pointThree.z + pointFour.z) / 4.0;
            double CenterPointY = this.getY() + perpendicular.y + 0.5;
            Vec3 endPoint = new Vec3(CenterPointX, CenterPointY, CenterPointZ);
            this.setCenterPoint(endPoint);
            ItemStack itemstack = new ItemStack((ItemLike)ItemInit.WAND_CLONE.get());
            ItemEntity ie = new ItemEntity(this.level(), this.getCenterPoint().x, this.getCenterPoint().y, this.getCenterPoint().z, itemstack);
            ie.setItem(new ItemStack((ItemLike)ItemInit.WAND_CLONE.get()));
            this.level().addFreshEntity((Entity)ie);
        }
    }

    public void baseTick() {
    }

    public boolean isPickable() {
        return false;
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public void checkDespawn() {
    }

    public boolean canUpdate() {
        return true;
    }

    public void spawnParticles() {
        double ySpeed = 0.006f;
        MAParticleType bpt = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_GRAVITY.get()));
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 perpendicular = up.cross(this.getLookAngle());
        if (this.getAge() % 5 == 0) {
            this.level().addParticle((ParticleOptions)bpt, this.getX() + perpendicular.x, this.getY() + perpendicular.y, this.getZ() + perpendicular.z, 0.0, ySpeed, 0.0);
            this.level().addParticle((ParticleOptions)bpt, this.getX() + perpendicular.x, this.getY() + perpendicular.y + 1.0, this.getZ() + perpendicular.z, 0.0, ySpeed, 0.0);
            this.level().addParticle((ParticleOptions)bpt, this.getX(), this.getY() + perpendicular.y, this.getZ(), 0.0, ySpeed, 0.0);
            this.level().addParticle((ParticleOptions)bpt, this.getX(), this.getY() + perpendicular.y + 1.0, this.getZ(), 0.0, ySpeed, 0.0);
        }
        if (this.getAge() > 60) {
            Vec3 pointOne = new Vec3(this.getX() + perpendicular.x, this.getY() + perpendicular.y, this.getZ() + perpendicular.z);
            Vec3 pointTwo = new Vec3(this.getX() + perpendicular.x, this.getY() + perpendicular.y + 1.0, this.getZ() + perpendicular.z);
            Vec3 pointThree = new Vec3(this.getX(), this.getY() + perpendicular.y, this.getZ());
            Vec3 pointFour = new Vec3(this.getX(), this.getY() + perpendicular.y + 1.0, this.getZ());
            double CenterPointX = (pointOne.x + pointTwo.x + pointThree.x + pointFour.x) / 4.0;
            double CenterPointZ = (pointOne.z + pointTwo.z + pointThree.z + pointFour.z) / 4.0;
            double CenterPointY = this.getY() + perpendicular.y + 0.5;
            Vec3 endPoint = new Vec3(CenterPointX, CenterPointY, CenterPointZ);
            this.setCenterPoint(endPoint);
            this.level().addParticle((ParticleOptions)ParticleInit.ARCANE.get(), CenterPointX, CenterPointY, CenterPointZ, 0.02, 0.0, 0.02);
            this.level().addParticle((ParticleOptions)ParticleInit.ARCANE.get(), CenterPointX, CenterPointY, CenterPointZ, 0.02, 0.0, 0.0);
            this.level().addParticle((ParticleOptions)ParticleInit.ARCANE.get(), CenterPointX, CenterPointY, CenterPointZ, 0.0, 0.0, 0.02);
            this.level().addParticle((ParticleOptions)ParticleInit.ARCANE.get(), CenterPointX, CenterPointY, CenterPointZ, -0.02, 0.0, 0.0);
            this.level().addParticle((ParticleOptions)ParticleInit.ARCANE.get(), CenterPointX, CenterPointY, CenterPointZ, 0.0, 0.0, -0.02);
            this.level().addParticle((ParticleOptions)ParticleInit.ARCANE.get(), CenterPointX, CenterPointY, CenterPointZ, -0.02, 0.0, -0.02);
        }
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Vec3 getCenterPoint() {
        return this.centerPoint;
    }

    public void setCenterPoint(Vec3 centerPoint) {
        this.centerPoint = centerPoint;
    }
}


