/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.AbstractArrow$Pickup
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.projectile;

import com.mna.api.entities.DamageHelper;
import com.mna.api.sound.SFX;
import com.mna.entities.EntityInit;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SkeletonAssassinShuriken
extends AbstractArrow {
    private int ticksInGround = 0;

    public SkeletonAssassinShuriken(EntityType<? extends SkeletonAssassinShuriken> p_i231584_1_, Level p_i231584_2_) {
        super(p_i231584_1_, p_i231584_2_);
        this.setSoundEvent(SFX.Entity.SkeletonAssassin.SHURIKEN_IMPACT);
    }

    public SkeletonAssassinShuriken(Level world, LivingEntity entity) {
        super((EntityType)EntityInit.SKELETON_ASSASSIN_SHURIKEN.get(), world);
        Vec3 position = entity.position().add(0.0, 1.0, 0.0);
        this.teleportTo(position.x, position.y, position.z);
        this.setOwner((Entity)entity);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.setSoundEvent(SFX.Entity.SkeletonAssassin.SHURIKEN_IMPACT);
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        DamageSource damagesource;
        Entity entity = hitResult.getEntity();
        Entity entity1 = this.getOwner();
        if (!(entity1 instanceof Player || entity.getType() != EntityInit.SKELETON_ASSASSIN.get() && entity.getType() != EntityInit.HULKING_ZOMBIE.get())) {
            return;
        }
        int i = 4;
        if (entity1 == null || !(entity1 instanceof LivingEntity)) {
            damagesource = DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.ARROW, this.level().registryAccess(), (Entity)this);
        } else {
            damagesource = DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.ARROW, this.level().registryAccess(), entity1, (Entity)this);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity)entity1).setLastHurtMob(entity);
            }
        }
        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !flag) {
            entity.igniteForSeconds((float)(5));
        }
        if (entity.hurt(damagesource, (float)i)) {
            if (flag) {
                return;
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                if (!this.level().isClientSide() && entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects((LivingEntity)livingentity, (Entity)entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)((LivingEntity)entity1), (Entity)livingentity);
                }
            }
            this.playSound(SFX.Entity.SkeletonAssassin.SHURIKEN_IMPACT, 1.0f, (float)(0.9 + Math.random() * 0.2));
            this.remove(Entity.RemovalReason.DISCARDED);
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0f);
            this.yRotO += 180.0f;
            if (!this.level().isClientSide() && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    protected void tickDespawn() {
        ++this.ticksInGround;
        if (this.ticksInGround >= 60) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public boolean isInGround() {
        return this.inGround;
    }
}


