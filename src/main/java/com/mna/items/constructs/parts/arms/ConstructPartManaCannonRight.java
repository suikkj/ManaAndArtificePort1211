/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.items.constructs.parts.arms;

import com.mna.api.affinity.Affinity;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.sound.SFX;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.items.constructs.parts.base.ChargeableConstructPart;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ConstructPartManaCannonRight
extends ChargeableConstructPart {
    public ConstructPartManaCannonRight(ConstructMaterial material) {
        super(material, ConstructSlot.RIGHT_ARM, 16);
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.RANGED_ATTACK};
    }

    @Override
    public float getAttackDamage() {
        return 0.0f;
    }

    @Override
    public int getAttackSpeedModifier() {
        return 10;
    }

    @Override
    public float getRangedAttackDamage() {
        return this.getMaterial().getRangedDamageBonus();
    }

    @Override
    public float getChargeCost() {
        return this.getMaterial().getRangedManaCost();
    }

    @Override
    public void onChargeReleased(ItemStack stack, Level world, LivingEntity living) {
        SpellProjectile esp = new SpellProjectile(living, world);
        esp.setForcedDamageAffinityAndTarget(Affinity.ARCANE, this.getRangedAttackDamage(), null);
        esp.shoot((Entity)living, living.getForward(), 1.0f, 0.0f);
        world.addFreshEntity((Entity)esp);
        world.playSound(null, living.getX(), living.getY(), living.getZ(), SFX.Spell.Cast.ForAffinity(Affinity.ARCANE), SoundSource.NEUTRAL, 0.25f, (float)(0.9 + Math.random() * 0.1));
    }
}

