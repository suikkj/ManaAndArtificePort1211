/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.monster.ZombieVillager
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.factions.Factions;
import com.mna.tools.math.MathUtils;
import java.util.stream.Stream;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentBloodSacrifice
extends SpellEffect {
    public ComponentBloodSacrifice(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 6.0f, 2.0f, 20.0f, 2.0f, 0.5f));
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        LivingEntity caster = source.getCaster();
        float amount = modificationData.getValue(Attribute.LESSER_MAGNITUDE);
        if (!context.hasEntityBeenAffected(this, (Entity)caster)) {
            boolean checkDamage = true;
            LivingEntity livingEntity = source.getCaster();
            if (livingEntity instanceof Player) {
                Player player = (Player)livingEntity;
                boolean bl = checkDamage = !player.isCreative();
            }
            if (checkDamage && !source.getCaster().hurt(caster.damageSources().magic(), amount)) {
                return ComponentApplicationResult.FAIL;
            }
            context.addAffectedEntity(this, (Entity)caster);
        }
        if (target.isLivingEntity() && target.getLivingEntity() != source.getCaster()) {
            LivingEntity tgt = target.getLivingEntity();
            tgt.heal(amount);
            if (amount > 9.0f) {
                Stream<MobEffectInstance> effects = tgt.getActiveEffects().stream().filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);
                effects.forEach(e -> tgt.removeEffect(e.getEffect()));
            }
            if (amount > 19.0f && tgt instanceof ZombieVillager) {
                ZombieVillager zomb = (ZombieVillager)tgt;
                if (source.isPlayerCaster()) {
                    com.mna.tools.MNAReflection.startConverting(zomb, source.getPlayer().getGameProfile().getId(), 20);
                } else {
                    com.mna.tools.MNAReflection.startConverting(zomb, null, 20);
                }
            }
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        int i;
        if (age < 10) {
            for (i = 0; i < 1; ++i) {
                world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setColor(91, 14, 14).setScale(MathUtils.RandomBetween(0.2f, 0.4f)).setMaxAge((int)(10.0 + Math.random() * 10.0)), caster.position().x + (double)MathUtils.RandomBetween(-0.5f, 0.5f), caster.position().y + (double)(caster.getBbHeight() / 2.0f) + (double)MathUtils.RandomBetween(-0.5f, 0.5f), caster.position().z + (double)MathUtils.RandomBetween(-0.5f, 0.5f), (double)MathUtils.RandomBetween(-0.05f, 0.05f), (double)MathUtils.RandomBetween(-0.05f, 0.05f), (double)MathUtils.RandomBetween(-0.05f, 0.05f));
            }
        }
        if (age < 10) {
            for (i = 0; i < 2; ++i) {
                world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(91, 14, 14).setScale(MathUtils.RandomBetween(0.025f, 0.05f)).setMaxAge((int)(10.0 + Math.random() * 10.0)).setGravity(0.01f), caster.position().x + (double)MathUtils.RandomBetween(-0.5f, 0.5f), caster.position().y + (double)(caster.getBbHeight() / 2.0f) + (double)MathUtils.RandomBetween(-0.5f, 0.5f), caster.position().z + (double)MathUtils.RandomBetween(-0.5f, 0.5f), (double)MathUtils.RandomBetween(-0.25f, 0.25f), (double)MathUtils.RandomBetween(-0.25f, 0.25f), (double)MathUtils.RandomBetween(-0.25f, 0.25f));
            }
        }
        if (age < 5) {
            for (i = 0; i < 2; ++i) {
                world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLOOD.get())).setScale(MathUtils.RandomBetween(0.05f, 0.1f)).setMaxAge((int)(10.0 + Math.random() * 10.0)), caster.position().x + (double)MathUtils.RandomBetween(-0.5f, 0.5f), caster.position().y + (double)(caster.getBbHeight() / 2.0f) + (double)MathUtils.RandomBetween(-0.5f, 0.5f), caster.position().z + (double)MathUtils.RandomBetween(-0.5f, 0.5f), (double)MathUtils.RandomBetween(-0.25f, 0.25f), (double)MathUtils.RandomBetween(-0.25f, 0.25f), (double)MathUtils.RandomBetween(-0.25f, 0.25f));
            }
        }
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BLOOD;
    }

    @Override
    public float initialComplexity() {
        return 5.0f;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.DEMONS;
    }

    @Override
    public float ire() {
        return 0.02f;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.AoE.BLOOD;
    }
}

