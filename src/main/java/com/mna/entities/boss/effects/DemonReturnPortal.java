/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 */
package com.mna.entities.boss.effects;

import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mna.entities.UtilityEntityBase;
import com.mna.entities.boss.DemonLord;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DemonReturnPortal
extends UtilityEntityBase
{
    private DemonLord focus;

    public DemonReturnPortal(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age >= 180 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
        if (this.level().isClientSide()) {
            float bbWidth = this.getBbWidth() / 2.0f;
            float bbWidthHalf = -bbWidth / 2.0f;
            Vec3 pos = this.position().add((double)bbWidthHalf, (double)bbWidthHalf, (double)bbWidthHalf).add((double)bbWidth * Math.random(), (double)bbWidth * Math.random(), (double)bbWidth * Math.random());
            this.level().addParticle((ParticleOptions)ParticleTypes.LAVA, pos.x, pos.y, pos.z, -0.5 + Math.random(), Math.random(), -0.5 + Math.random());
        }
    }

    public void setFocus(DemonLord focus) {
        this.focus = focus;
    }

    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (this.level().isClientSide()) {
            ManaAndArtifice.instance.proxy.playEntityAliveLoopingSound(SFX.Loops.DEMON_SUMMON, this);
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level().isClientSide()) {
            ManaAndArtifice.instance.proxy.playEntityAliveLoopingSound(SFX.Loops.DEMON_SUMMON, this);
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (this.focus != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(this.focus.getId());
        } else {
            buffer.writeBoolean(false);
        }
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        if (additionalData.readBoolean()) {
            int entityID = additionalData.readInt();
            Entity e = this.level().getEntity(entityID);
            if (e != null && e instanceof DemonLord) {
                this.focus = (DemonLord)e;
            }
        }
    }
}

