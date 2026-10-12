/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ItemParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.utility;

import com.mna.entities.EntityInit;
import com.mna.items.ItemInit;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ThrownRunescribingPattern
extends ThrowableItemProjectile {
    ItemStack defaultStack;

    public ThrownRunescribingPattern(EntityType<? extends ThrownRunescribingPattern> p_i50159_1_, Level p_i50159_2_) {
        super(p_i50159_1_, p_i50159_2_);
        this.defaultStack = new ItemStack((ItemLike)ItemInit.RUNE_PATTERN.get());
    }

    public ThrownRunescribingPattern(Level worldIn, LivingEntity throwerIn) {
        super((EntityType)EntityInit.THROWN_RUNESCRIBE_PATTERN.get(), throwerIn, worldIn);
    }

    public ThrownRunescribingPattern(Level worldIn, double x, double y, double z) {
        super((EntityType)EntityInit.THROWN_RUNESCRIBE_PATTERN.get(), x, y, z, worldIn);
    }

    protected Item getDefaultItem() {
        return (Item)ItemInit.RUNE_PATTERN.get();
    }

    @OnlyIn(value=Dist.CLIENT)
    private ParticleOptions makeParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, this.defaultStack);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions iparticledata = this.makeParticle();
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        this.playSound(SoundEvents.GLASS_BREAK, 1.0f, (float)((double)0.9f + Math.random() * (double)0.2f));
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent((Entity)this, (byte)3);
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}


