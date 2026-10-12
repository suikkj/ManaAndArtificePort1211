/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.effects.EffectInit;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentDivination
extends PotionEffectComponent {
    public ComponentDivination(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.DIVINATION, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 5.0f), new AttributeValuePair(Attribute.RANGE, 1.0f, 1.0f, 7.0f, 1.0f, 15.0f));
    }

    @Override
    protected boolean modifiesMagnitude() {
        return true;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Buff.WIND;
    }

    @Override
    protected int getPotionAmplitude(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        return (int)modificationData.getValue(Attribute.RANGE) - 1;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WIND;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 10) {
            return;
        }
        float particle_spread = 1.0f;
        float v = 0.4f;
        int particleCount = 10;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 velocity = new Vec3(0.0, Math.random() * (double)v, 0.0);
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public float initialComplexity() {
        return 16.0f;
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
}

