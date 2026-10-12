/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.item.ArrowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.tools.MATags;
import com.mna.config.GeneralConfig;
import com.mna.entities.summon.Simulacrum;
import com.mna.entities.utility.ImpulseProjectile;
import com.mna.items.ItemInit;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentImpulse
extends SpellEffect {
    public static final String NBT_CANNONSHOT = "cannon_shot";
    public final List<SpellReagent> requiredReagents = Arrays.asList(new SpellReagent((SpellEffect)this, new ItemStack((ItemLike)Items.GUNPOWDER), false, false, true, true, new IFaction[0]));

    public ComponentImpulse(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DAMAGE, 3.0f, 1.0f, 20.0f, 0.5f, 1.5f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 0.0f, 0.0f, 25.0f, 5.0f, 10.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock() || context.countAffectedEntities(this) > 0) {
            return ComponentApplicationResult.FAIL;
        }
        if (source.getCaster() instanceof Simulacrum) {
            return ComponentApplicationResult.FAIL;
        }
        ItemStack offhandItem = source.getCaster().getItemInHand(source.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        ItemStack handItem = source.getCaster().getItemInHand(source.getHand());
        if (offhandItem.isEmpty()) {
            return ComponentApplicationResult.FAIL;
        }
        float velocityScale = 1.5f;
        float damage = modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier();
        float recoveryChance = modificationData.getValue(Attribute.LESSER_MAGNITUDE) / 100.0f;
        if (!context.isReagentMissing(Items.GUNPOWDER) || handItem.getItem() == ItemInit.HELLFIRE_STAFF.get()) {
            damage *= 2.0f;
            velocityScale = 3.0f;
            if (!context.getLevel().isClientSide()) {
                context.getLevel().playSound(null, source.getOrigin().x, source.getOrigin().y, source.getOrigin().z, SFX.Spell.Cast.KABLAM, source.isPlayerCaster() ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1.0f, 0.9f + (float)(Math.random() * (double)0.2f));
            }
        }
        Vec3 shootVector = null;
        shootVector = target.getEntity() == source.getCaster() ? source.getCaster().getForward().normalize() : target.getEntity().getEyePosition().subtract(source.getCaster().getEyePosition());
        shootVector.scale((double)velocityScale);
        if (offhandItem.getItem() instanceof ArrowItem) {
            this.shootArrow(offhandItem, damage, shootVector, context, source);
        } else {
            this.spawnCannonShot(offhandItem, damage, recoveryChance, shootVector, context, source);
        }
        offhandItem.shrink(1);
        context.addAffectedEntity(this, (Entity)source.getCaster());
        return ComponentApplicationResult.SUCCESS;
    }

    private void spawnCannonShot(ItemStack offhandItem, float damage, float recoveryChance, Vec3 shootVector, SpellContext context, SpellSource source) {
        if (MATags.isItemEqual(offhandItem, MATags.Items.CANNON_HALFBOOST)) {
            damage = (float)((double)damage * 1.5);
            recoveryChance *= 2.0f;
        } else if (MATags.isItemEqual(offhandItem, MATags.Items.CANNON_FULLBOOST)) {
            damage *= 2.0f;
            recoveryChance *= 4.0f;
        }
        ImpulseProjectile ecs = new ImpulseProjectile((Level)context.getServerLevel(), source.getCaster(), offhandItem.copy(), damage);
        ecs.setChanceForRecovery(recoveryChance);
        ecs.setDamage(damage);
        ecs.shoot(shootVector.x, shootVector.y, shootVector.z, 1.0f, 0.0f);
        context.getServerLevel().addFreshEntity((Entity)ecs);
    }

    private void shootArrow(ItemStack offhandItem, float damage, Vec3 shootVector, SpellContext context, SpellSource source) {
        AbstractArrow worroa = ProjectileUtil.getMobArrow((LivingEntity)source.getCaster(), (ItemStack)offhandItem, (float)1.0f);
        worroa.shoot(shootVector.x, shootVector.y, shootVector.z, damage / 10.0f, 0.0f);
        worroa.setCritArrow(true);
        worroa.getPersistentData().putBoolean("return_on_next_arrow_shot", true);
        ItemStack returnItem = offhandItem.copy();
        returnItem.setCount(1);
        worroa.getPersistentData().put("return_stack", (Tag)returnItem.save(new CompoundTag()));
        worroa.getPersistentData().putBoolean(NBT_CANNONSHOT, true);
        context.getLevel().addFreshEntity((Entity)worroa);
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.AoE.EARTH;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.FIRE;
    }

    @Override
    public List<SpellReagent> getRequiredReagents(Player caster, InteractionHand hand) {
        if (hand != null && caster.getItemInHand(hand).getItem() == ItemInit.HELLFIRE_STAFF.get()) {
            return Arrays.asList(new SpellReagent[0]);
        }
        return this.requiredReagents;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public float initialComplexity() {
        return 15.0f;
    }

    @Override
    public boolean targetsEntities() {
        return true;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.SELF;
    }

    @Override
    public boolean isHellfireBoosted(Attribute attr) {
        return true;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ARCANE, Affinity.ENDER, Affinity.WIND, Affinity.ICE);
    }
}

