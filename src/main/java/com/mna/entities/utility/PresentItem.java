/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.utility;

import com.mna.entities.EntityInit;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PresentItem
extends ItemEntity {
    public PresentItem(EntityType<? extends ItemEntity> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setExtendedLifetime();
    }

    public PresentItem(Level worldIn, double x, double y, double z) {
        this((EntityType<? extends ItemEntity>)((EntityType)EntityInit.PRESENTATION_ENTITY.get()), worldIn);
        this.setPos(x, y, z);
        this.setYRot(this.random.nextFloat() * 360.0f);
        this.setDeltaMovement(this.random.nextDouble() * 0.2 - 0.1, 0.2, this.random.nextDouble() * 0.2 - 0.1);
    }

    public PresentItem(Level worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn, x, y, z);
        this.setItem(stack);
        this.setNoGravity(true);
        this.setExtendedLifetime();
        this.setDeltaMovement(0.0, 0.0, 0.0);
    }

    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}


