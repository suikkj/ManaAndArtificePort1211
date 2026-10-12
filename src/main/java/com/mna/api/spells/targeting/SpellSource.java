/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.spells.targeting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpellSource {
    private LivingEntity caster;
    private Vec3 origin;
    private Vec3 forward;
    private InteractionHand hand;

    public SpellSource(LivingEntity caster, InteractionHand hand) {
        this.caster = caster;
        this.origin = caster.position().add(0.0, (double)caster.getEyeHeight(), 0.0);
        this.forward = caster.getLookAngle();
        this.hand = hand;
    }

    public SpellSource(LivingEntity caster, InteractionHand hand, Vec3 origin, Vec3 forward) {
        this.caster = caster;
        this.hand = hand;
        this.origin = origin;
        this.forward = forward;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public Vec3 getOrigin() {
        return this.origin;
    }

    public Vec3 getForward() {
        return this.forward;
    }

    @Nullable
    public LivingEntity getCaster() {
        return this.caster;
    }

    @Nullable
    public Player getPlayer() {
        if (this.isPlayerCaster()) {
            return (Player)this.caster;
        }
        return null;
    }

    public boolean hasCasterReference() {
        return this.caster != null;
    }

    public boolean isPlayerCaster() {
        return this.caster != null && this.caster instanceof Player;
    }

    public AABB getBoundingBox() {
        if (this.caster != null) {
            return this.caster.getBoundingBox();
        }
        return new AABB(BlockPos.containing((Position)this.origin));
    }
}

