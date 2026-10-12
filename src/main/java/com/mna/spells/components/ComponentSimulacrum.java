/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.config.GeneralConfigValues;
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
import com.mna.entities.summon.Simulacrum;
import com.mna.factions.Factions;
import com.mna.tools.SummonUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ComponentSimulacrum
extends SpellEffect {
    public ComponentSimulacrum(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 2.0f, 1.0f, 40.0f), new AttributeValuePair(Attribute.RANGE, 0.5f, 0.5f, 1.5f, 0.5f, 0.0f), new AttributeValuePair(Attribute.PRECISION, 1.0f, 5.0f, 20.0f, 1.0f, 10.0f));
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.FEY;
    }

    @Override
    public float ire() {
        return 0.15f;
    }

    @Override
    public int requiredXPForRote() {
        return 150;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!source.isPlayerCaster()) {
            return ComponentApplicationResult.FAIL;
        }
        if (context.countAffectedEntities(this) > 0) {
            return ComponentApplicationResult.FAIL;
        }
        context.addAffectedEntity(this, (Entity)source.getCaster());
        float healthGiven = modificationData.getValue(Attribute.PRECISION);
        int numSimulacrums = (int)modificationData.getValue(Attribute.MAGNITUDE);
        float dist = modificationData.getValue(Attribute.RANGE);
        Player player = source.getPlayer();
        if (player.hurt(player.damageSources().fellOutOfWorld(), (float)numSimulacrums * healthGiven)) {
            SummonUtils.limitSummonsOfType((LivingEntity)player, "mna:simulacrum_ids", Simulacrum.class, 0);
            for (int i = 0; i < numSimulacrums; ++i) {
                float offset = (float)((double)(i % 2 == 0 ? dist : -dist) * (Math.floor(i / 2) + 2.0));
                Simulacrum simulacrum = new Simulacrum(context.getLevel(), (int)(healthGiven * (float)GeneralConfigValues.BrimstonePerHeart), player, offset);
                SummonUtils.tagAsSummon((Entity)simulacrum, (LivingEntity)player);
                SummonUtils.addTrackedEntity((LivingEntity)player, (Entity)simulacrum, "mna:simulacrum_ids");
                context.getLevel().addFreshEntity((Entity)simulacrum);
            }
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BLOOD;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
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

