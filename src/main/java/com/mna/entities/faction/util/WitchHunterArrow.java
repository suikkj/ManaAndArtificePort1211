/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.faction.util;

import com.mna.entities.EntityInit;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WitchHunterArrow
extends AbstractArrow {
    private int ticksInGround = 0;

    public WitchHunterArrow(EntityType<? extends WitchHunterArrow> type, Level worldIn) {
        super(type, worldIn);
    }

    public WitchHunterArrow(Level worldIn, double x, double y, double z) {
        super((EntityType)EntityInit.WITCH_HUNTER_ARROW.get(), x, y, z, worldIn, new ItemStack((ItemLike)Items.ARROW), null);
    }

    public WitchHunterArrow(Level worldIn, LivingEntity shooter) {
        super((EntityType)EntityInit.WITCH_HUNTER_ARROW.get(), shooter, worldIn, new ItemStack((ItemLike)Items.ARROW), null);
    }

    protected boolean canHitEntity(Entity target) {
        if (target.getType() == EntityInit.SPELLBREAKER.get() || target.getType() == EntityInit.WITCH_HUNTER.get()) {
            return false;
        }
        return super.canHitEntity(target);
    }

    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    protected void tickDespawn() {
        ++this.ticksInGround;
        if (this.ticksInGround >= 60) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}


