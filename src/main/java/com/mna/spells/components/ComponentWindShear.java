/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.factions.Factions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentWindShear
extends SpellEffect {
    public ComponentWindShear(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.HEIGHT, 1.0f, 1.0f, 15.0f, 1.0f, 0.5f), new AttributeValuePair(Attribute.PRECISION, 5.0f, 1.0f, 5.0f, 0.5f, 1.5f));
    }

    @Override
    public int requiredXPForRote() {
        return 250;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock() || !target.isLivingEntity() || target.getLivingEntity().onGround() || !source.hasCasterReference()) {
            return ComponentApplicationResult.FAIL;
        }
        float precision = Math.max(modificationData.getValue(Attribute.PRECISION), 1.0f);
        float precisionMax = modificationData.getMaximumValue(Attribute.PRECISION);
        float precisionMin = modificationData.getMinimumValue(Attribute.PRECISION);
        float heightTarget = modificationData.getValue(Attribute.HEIGHT);
        float heightMaximum = modificationData.getMaximumValue(Attribute.HEIGHT);
        float heightMinimum = modificationData.getMinimumValue(Attribute.HEIGHT);
        float precisionPct = (precision - precisionMin) / (precisionMax - precisionMin);
        float heightPct = (heightTarget - heightMinimum) / (heightMaximum - heightMinimum);
        float baselineDamage = 5.0f;
        float maximumVariableDamage = 5.0f + 20.0f * precisionPct;
        int distanceToGround = 0;
        BlockPos startPos = target.getLivingEntity().blockPosition();
        while (startPos.getY() > context.getServerLevel().getMinBuildHeight() && context.getServerLevel().getBlockState(startPos).isAir()) {
            ++distanceToGround;
            startPos = startPos.below();
        }
        if (distanceToGround == 0) {
            return ComponentApplicationResult.FAIL;
        }
        float damageMultiplier = 0.05f + 0.95f * heightPct;
        float decay = 0.7f;
        int rangeBands = (int)Math.floor(Math.abs((float)distanceToGround - heightTarget) / precision);
        float damage = (baselineDamage + maximumVariableDamage * (float)Math.pow(decay, rangeBands)) * damageMultiplier;
        target.getLivingEntity().hurt(target.getLivingEntity().damageSources().mobAttack(source.getCaster()), damage);
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WIND;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.FEY;
    }

    @Override
    public float initialComplexity() {
        return 15.0f;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 10) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            Vec3 offset = new Vec3(-1.0 + Math.random() * 2.0, 0.0, -1.0 + Math.random() * 2.0);
            Vec3 velocity = offset.normalize().scale(-0.5);
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.15f).setColor(30, 30, 30), impact_position.x + offset.x, impact_position.y + 0.5, impact_position.z + offset.z, velocity.x, velocity.y, velocity.z);
        }
    }
}

