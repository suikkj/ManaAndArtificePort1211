 
package com.mna.spells.components;

import com.mna.tools.MATools;
import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ComponentHemmorhage
extends SpellEffect {
    public ComponentHemmorhage(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DAMAGE, 5.0f, 5.0f, 20.0f, 5.0f, 3.0f), new AttributeValuePair(Attribute.RADIUS, 1.0f, 1.0f, 32.0f, 1.0f, 3.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isLivingEntity()) {
            LivingEntity living = target.getLivingEntity();
            if ((MATools.isUndead(living))) {
                return ComponentApplicationResult.FAIL;
            }
            if (living.isAlive() && living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CURSE_OF_AGONY.get()))) {
                living.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CURSE_OF_AGONY.get()));
                float damage = modificationData.getValue(Attribute.DAMAGE);
                float radius = modificationData.getValue(Attribute.RADIUS);
                living.hurt(living.damageSources().magic(), damage);
                if (living.isDeadOrDying()) {
                    context.getLevel().getEntities((Entity)source.getCaster(), living.getBoundingBox().inflate((double)radius)).forEach(e -> {
                        LivingEntity secondaryTarget;
                        if (e instanceof LivingEntity && (!(MATools.isUndead(secondaryTarget = (LivingEntity)e)))) {
                            secondaryTarget.hurt(secondaryTarget.damageSources().magic(), damage / 2.0f);
                        }
                    });
                }
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BLOOD;
    }

    @Override
    public float initialComplexity() {
        return 30.0f;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.DEMONS;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.AoE.BLOOD;
    }
}

