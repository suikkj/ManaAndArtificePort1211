/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.AbstractArrow$Pickup
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.tools;

import com.mna.tools.math.MathUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class ProjectileHelper {
    public static void ReflectProjectile(LivingEntity target, Projectile p, boolean atShooter, float outSpeed) {
        Vec3 reflected;
        if (!atShooter) {
            reflected = ProjectileHelper.reflectVector(target, p, outSpeed);
        } else if (p.getOwner() != null) {
            Vec3 targetPos = p.getOwner().getEyePosition(0.0f).add(p.getDeltaMovement());
            reflected = p.position().subtract(targetPos).normalize().scale((double)outSpeed);
        } else {
            reflected = ProjectileHelper.reflectVector(target, p, outSpeed);
        }
        p.setOwner((Entity)target);
        p.tickCount = 0;
        if (p instanceof AbstractArrow) {
            ((AbstractArrow)p).pickup = AbstractArrow.Pickup.DISALLOWED;
            ((AbstractArrow)p).setBaseDamage(((AbstractArrow)p).getBaseDamage() * 2.0);
        }
        p.setDeltaMovement(reflected);
    }

    private static Vec3 reflectVector(LivingEntity target, Projectile p, float outSpeed) {
        Vec3 direction = p.getDeltaMovement();
        Vec3 look = Vec3.directionFromRotation((Vec2)target.getRotationVector());
        Vec3 normal = direction.add(look).scale(0.5).normalize();
        return MathUtils.reflect(direction, normal).normalize().scale((double)outSpeed);
    }
}

