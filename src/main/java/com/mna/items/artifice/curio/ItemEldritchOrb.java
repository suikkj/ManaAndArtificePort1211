/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.monster.Endermite
 *  net.minecraft.world.entity.monster.Silverfish
 *  net.minecraft.world.entity.monster.Spider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.mna.items.artifice.curio;

import com.mna.tools.EnchantmentUtils;
import com.mna.api.items.ChargeableItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import com.mna.api.enchantments.MNAEnchantmentHelper;
import com.mna.api.enchantments.MNAEnchantmentRegistry;

public class ItemEldritchOrb
extends ChargeableItem {
    private final ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> allowedEnchantments = new ArrayList();

    public ItemEldritchOrb() {
        super(new Item.Properties(), 1000.0f);
        this.allowedEnchantments.clear();
        this.allowedEnchantments.add(Enchantments.BANE_OF_ARTHROPODS);
        this.allowedEnchantments.add(Enchantments.FLAME);
        this.allowedEnchantments.add(Enchantments.KNOCKBACK);
        this.allowedEnchantments.add(Enchantments.MULTISHOT);
        this.allowedEnchantments.add(Enchantments.SMITE);
        this.allowedEnchantments.add(Enchantments.QUICK_CHARGE);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        Map<net.minecraft.core.Holder<Enchantment>, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);
        for (net.minecraft.core.Holder<Enchantment> ench : enchantments.keySet()) {
            if (this.allowedEnchantments.contains(ench.unwrapKey().orElse(null))) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return this.allowedEnchantments.contains(com.mna.api.enchantments.MNAEnchantmentRegistry.keyOf(enchantment));
    }

    public boolean shoot(Player caster, ItemStack orb) {
        if (caster.level().isClientSide()) {
            return false;
        }
        List targets = caster.level().getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(5.0)).stream().filter(e -> e.isAlive() && e.getSensing().hasLineOfSight((Entity)caster) && !SummonUtils.isTargetFriendly((Entity)e, (LivingEntity)caster)).collect(Collectors.toList());
        if (targets.size() <= 0) {
            return false;
        }
        Map enchants = EnchantmentUtils.enchantmentMap((ItemStack)orb);
        int numTargets = 1 + com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, orb);
        for (int i = 0; i < numTargets; ++i) {
            Mob target = (Mob)targets.get((int)Math.floor(Math.random() * (double)targets.size()));
            Vector3f translation = new Vector3f(1.0f, 2.0f, 0.0f);
            translation.rotate((Quaternionfc)Axis.YN.rotationDegrees((float)((double)(MathUtils.lerpf(caster.yHeadRotO, caster.yHeadRot, 0.0f) / 180.0f) * Math.PI)));
            ServerMessageDispatcher.sendParticleSpawn(caster.getX() + (double)translation.x(), caster.getY() + (double)translation.y(), caster.getZ() + (double)translation.z(), target.blockPosition().getX(), (float)target.blockPosition().getY() + target.getBbHeight() / 2.0f, target.blockPosition().getZ(), 0, 64.0f, (ResourceKey<Level>)caster.level().dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
            caster.level().playSound(null, caster.blockPosition(), SFX.Spell.Cast.ARCANE, SoundSource.PLAYERS, 1.0f, 1.0f);
            this.applyOrbEnchantmentEffects(caster, target, enchants);
            target.hurt(target.damageSources().playerAttack(caster), 1.0f);
        }
        return true;
    }

    private void applyOrbEnchantmentEffects(Player caster, Mob target, Map<net.minecraft.core.Holder<Enchantment>, Integer> enchants) {
        for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> e : enchants.entrySet()) {
            net.minecraft.resources.ResourceKey<Enchantment> enchKey = e.getKey().unwrapKey().orElse(null);
            if ((target instanceof Spider || target instanceof Silverfish || target instanceof Endermite) && Enchantments.BANE_OF_ARTHROPODS.equals(enchKey)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, e.getValue().intValue()));
                continue;
            }
            if (target.isInvertedHealAndHarm() && Enchantments.SMITE.equals(enchKey)) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, e.getValue().intValue()));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, e.getValue().intValue()));
                continue;
            }
            if (Enchantments.FLAME.equals(enchKey)) {
                target.igniteForSeconds((float)(e.getValue() + 1));
                continue;
            }
            if (!Enchantments.KNOCKBACK.equals(enchKey)) continue;
            Vec3 dir = caster.position().subtract(target.position()).normalize();
            target.knockback((double)((float)e.getValue().intValue() / 3.0f), dir.x, dir.z);
        }
    }

    @Override
    protected float manaPerRechargeTick() {
        return 10.0f;
    }

    @Override
    protected boolean tickEffect(ItemStack stack, Player player, Level world, int slot, float mana, boolean selected) {
        long delay;
        Map enchants = EnchantmentUtils.enchantmentMap((ItemStack)stack);
        long l = delay = (long)(20 - com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack));
        return world.getGameTime() % delay == 0L && this.shoot(player, stack);
    }
}

