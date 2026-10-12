/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.AbstractSkeleton
 *  net.minecraft.world.entity.monster.CrossbowAttackMob
 *  net.minecraft.world.entity.monster.RangedAttackMob
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.entities.possession;

import com.mna.api.spells.targeting.SpellTargetHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PossessionActions {
    private static HashMap<EntityType<?>, PosessionActionCollection<?>> _registeredActions = new HashMap();

    public static <T extends Mob> void RegisterLeftClickAction(EntityType<T> entityType, Consumer<T> callback) {
        PosessionActionCollection<T> registry = _registeredActions.getOrDefault(entityType, new PosessionActionCollection());
        registry.AddAction(callback);
        _registeredActions.put(entityType, registry);
    }

    public static <T extends Mob> boolean Invoke(T reference) {
        PosessionActionCollection registry = _registeredActions.getOrDefault(reference.getType(), null);
        if (registry == null) {
            return false;
        }
        registry.Invoke(reference);
        return true;
    }

    public static <T extends Mob> boolean InvokeDefault(T reference) {
        boolean isSkeleton = reference instanceof AbstractSkeleton;
        boolean isCrossbowUser = reference instanceof CrossbowAttackMob;
        if (isSkeleton || isCrossbowUser) {
            ItemStack itemstack = reference.getProjectile(reference.getItemInHand(ProjectileUtil.getWeaponHoldingHand(reference, item -> item == Items.BOW)));
            AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow(reference, (ItemStack)itemstack, (float)1.0f);
            if (reference.getMainHandItem().getItem() instanceof BowItem) {
                abstractarrowentity = ((BowItem)reference.getMainHandItem().getItem()).customArrow(abstractarrowentity);
            }
            Vec3 fwd = Vec3.directionFromRotation((Vec2)reference.getRotationVector());
            double d0 = fwd.x;
            double d1 = fwd.y;
            double d2 = fwd.z;
            double d3 = Mth.square((double)(d0 * d0 + d2 * d2));
            abstractarrowentity.shoot(d0, d1 + d3 * (double)0.2f, d2, 1.6f, (float)(14 - reference.level().getDifficulty().getId() * 4));
            reference.playSound(isCrossbowUser ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (reference.getRandom().nextFloat() * 0.4f + 0.8f));
            reference.level().addFreshEntity((Entity)abstractarrowentity);
            return true;
        }
        if (reference instanceof RangedAttackMob) {
            ((RangedAttackMob)reference).performRangedAttack(reference, 1.0f);
            return true;
        }
        float range = 8.0f;
        double damage = 1.0;
        if (reference.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            damage = Math.max(reference.getAttribute(Attributes.ATTACK_DAMAGE).getValue(), 1.0);
        }
        HitResult targetResult = SpellTargetHelper.rayTrace(null, reference.level(), reference.position().add(0.0, (double)reference.getEyeHeight(), 0.0), Vec3.directionFromRotation((Vec2)reference.getRotationVector()), true, false, ClipContext.Block.COLLIDER, entity -> entity.isPickable() && entity.isAlive() && entity != reference, reference.getBoundingBox().inflate((double)range, (double)range, (double)range), range);
        reference.swing(InteractionHand.MAIN_HAND, true);
        if (targetResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult ertr = (EntityHitResult)targetResult;
            ertr.getEntity().hurt(reference.damageSources().mobAttack(reference), (float)damage);
            return true;
        }
        return false;
    }

    static class PosessionActionCollection<T extends Entity> {
        ArrayList<Consumer<T>> actions = new ArrayList();

        public void AddAction(Consumer<T> callback) {
            this.actions.add(callback);
        }

        public void Invoke(T e) {
            this.actions.forEach((Consumer<Consumer<T>>)((Consumer<Consumer>)a -> a.accept(e)));
        }
    }
}

