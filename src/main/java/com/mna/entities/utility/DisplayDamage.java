/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.utility;

import com.mna.api.config.GeneralConfigValues;
import com.mna.entities.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DisplayDamage
extends Entity
{
    public static final int AGE_TIME = 30;
    public static final int FADE_OUT_START = 10;
    public static final int FADE_OUT_DURATION = 20;
    public static final int DEFAULT_COLOR = 0xFFFFFF;
    float amount = 0.0f;
    int textColor = 0xFFFFFF;

    public DisplayDamage(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public DisplayDamage(Level pLevel, DamageSource source, float amount) {
        this((EntityType)EntityInit.DAMAGE_NUMBERS.get(), pLevel);
        this.amount = amount;
        GeneralConfigValues.InsightDamageColors.stream().filter(s -> s.contains(source.getMsgId() + ":")).findFirst().ifPresent(s -> {
            String color = s.split(":")[1];
            try {
                this.textColor = Integer.decode(color.replace("0x", "#"));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    public void tick() {
        if (++this.tickCount >= 30) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
        this.move(MoverType.SELF, new Vec3(0.0, (double)0.01f, 0.0));
    }

    public float getAmount() {
        return this.amount;
    }

    public int getColor() {
        return this.textColor | this.getTextAlpha() << 24;
    }

    public int getTextAlpha() {
        if (this.tickCount < 10) {
            return 255;
        }
        return 255 - 255 * (this.tickCount - 10) / 20;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.amount);
        buffer.writeInt(this.textColor);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.amount = additionalData.readFloat();
        this.textColor = additionalData.readInt();
    }
}


