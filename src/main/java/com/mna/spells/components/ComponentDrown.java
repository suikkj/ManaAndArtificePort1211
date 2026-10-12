 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.entities.DamageHelper;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
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
import com.mna.config.GeneralConfig;
import com.mna.effects.EffectInit;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

public class ComponentDrown
extends SpellEffect {
    public ComponentDrown(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 15.0f), new AttributeValuePair(Attribute.DAMAGE, 10.0f, 10.0f, 30.0f, 1.0f, 2.0f), new AttributeValuePair(Attribute.DURATION, 5.0f, 2.0f, 5.0f, 1.0f, 10.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isLivingEntity() && target.getLivingEntity().canDrownInFluidType(NeoForgeMod.WATER_TYPE) && !MobEffectUtil.hasWaterBreathing((LivingEntity)target.getLivingEntity())) {
            int magnitude = (int)modificationData.getValue(Attribute.MAGNITUDE);
            int air_per_magnitude = (int)((float)target.getLivingEntity().getMaxAirSupply() * 0.2f);
            int air_removed = air_per_magnitude * magnitude;
            target.getLivingEntity().setAirSupply(target.getLivingEntity().getAirSupply() - air_removed);
            if (target.getLivingEntity().getAirSupply() <= 0 && !target.getLivingEntity().hasEffect(MobEffects.WATER_BREATHING)) {
                target.getLivingEntity().hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.DROWN, context.getLevel().registryAccess(), (Entity)source.getCaster()), modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier());
            }
            target.getLivingEntity().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.ASPHYXIATE.get()), (int)modificationData.getValue(Attribute.DURATION) * 20, 0));
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WATER;
    }

    @Override
    public float initialComplexity() {
        return 40.0f;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.WATER;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 10) {
            return;
        }
        float particle_spread = 1.0f;
        float v = 1.0f;
        int particleCount = 10;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 velocity = new Vec3(0.0, Math.random() * (double)v, 0.0);
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ENDER, Affinity.WATER, Affinity.ICE);
    }
}


