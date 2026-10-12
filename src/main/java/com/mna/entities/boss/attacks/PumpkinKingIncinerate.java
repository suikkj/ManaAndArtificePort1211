/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.entities.boss.attacks;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.entities.EntityInit;
import com.mna.entities.UtilityEntityBase;
import com.mna.sound.EntityAliveLoopingSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PumpkinKingIncinerate
extends UtilityEntityBase {
    public PumpkinKingIncinerate(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    public PumpkinKingIncinerate(Level p_i48580_2_) {
        super((EntityType)EntityInit.PUMPKIN_KING_INCINERATE.get(), p_i48580_2_);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.spawnParticles();
        }
        if (this.age >= 25) {
            this.playSound(SoundEvents.DRAGON_FIREBALL_EXPLODE, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            this.level().getEntities((Entity)this, this.getBoundingBox().inflate(2.0), e -> e instanceof Player && e.isAlive() && e.isAttackable()).stream().forEach(player -> {
                player.igniteForSeconds((float)(4));
                player.hurt(this.damageSources().inFire(), 8.0f);
            });
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private void playSounds() {
        if (this.age == 1) {
            Minecraft.getInstance().getSoundManager().play((SoundInstance)new EntityAliveLoopingSound(SFX.Loops.DEMON_SUMMON, this));
        } else if (this.age == 20) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Event.Ritual.DEMON_SUMMON_END, SoundSource.PLAYERS, 1.0f, 1.0f, false);
        }
    }

    private void spawnParticles() {
        int i;
        for (i = 0; i < 5; ++i) {
            this.level().addParticle((ParticleOptions)ParticleTypes.LANDING_LAVA, this.getX() - 1.5 + Math.random() * 3.0, this.getY(), this.getZ() - 1.5 + Math.random() * 3.0, 0.0, (double)0.05f, 0.0);
        }
        this.level().addParticle((ParticleOptions)ParticleTypes.LAVA, this.getX() - 0.5 + Math.random() * 1.0, this.getY(), this.getZ() - 0.5 + Math.random() * 1.0, 0.0, (double)0.05f, 0.0);
        for (i = 0; i < 25; ++i) {
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), this.getX() - 0.25 + Math.random() * 0.5, this.getY(), this.getZ() - 0.25 + Math.random() * 0.5, 0.0, (double)0.15f, 0.0);
        }
        if (this.age == 20) {
            this.level().addParticle((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag p_70037_1_) {
    }

    protected void addAdditionalSaveData(CompoundTag p_213281_1_) {
    }
}


