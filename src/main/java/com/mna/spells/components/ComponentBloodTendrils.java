/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 */
package com.mna.spells.components;

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
import com.mna.entities.summon.BloodTendril;
import com.mna.factions.Factions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ComponentBloodTendrils
extends SpellEffect {
    public ComponentBloodTendrils(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 30.0f, 30.0f, 1200.0f, 30.0f, 1.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 80;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        int offsetCount;
        if (context.hasEntityBeenAffected(this, (Entity)source.getCaster())) {
            return ComponentApplicationResult.FAIL;
        }
        context.addAffectedEntity(this, (Entity)source.getCaster());
        for (offsetCount = 0; !context.getServerLevel().isEmptyBlock(target.getBlock().above(offsetCount)) && offsetCount < 5; ++offsetCount) {
        }
        if (offsetCount >= 5) {
            return ComponentApplicationResult.FAIL;
        }
        BloodTendril tendril = new BloodTendril((Level)context.getServerLevel()).setSummoner(source.getCaster(), (int)(modificationData.getValue(Attribute.DURATION) * 20.0f));
        tendril.setPos(target.getPosition().x, target.getPosition().y + (double)offsetCount - 0.5, target.getPosition().z);
        tendril.invulnerableTime = 60;
        context.getServerLevel().addFreshEntity((Entity)tendril);
        target.overrideSpellTarget((Entity)tendril);
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BLOOD;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
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
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.BLOOD;
    }
}

