/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.factions.Factions;
import com.mna.tools.SummonUtils;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class ComponentSacrifice
extends SpellEffect {
    public ComponentSacrifice(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!source.isPlayerCaster() || !target.isLivingEntity()) {
            return ComponentApplicationResult.FAIL;
        }
        LivingEntity summon = null;
        if (source.getPlayer() == target.getLivingEntity()) {
            List<Mob> summons = SummonUtils.getSummons((LivingEntity)source.getPlayer(), (Level)context.getServerLevel());
            if (summons.size() > 0) {
                summon = (LivingEntity)summons.get(summons.size() - 1);
            }
        } else if (SummonUtils.isSummon((Entity)target.getLivingEntity()) && SummonUtils.getSummoner(target.getLivingEntity()) == source.getPlayer()) {
            summon = target.getLivingEntity();
        }
        if (summon == null) {
            return ComponentApplicationResult.FAIL;
        }
        summon.kill();
        if (!summon.isAlive()) {
            source.getPlayer().heal(summon.getMaxHealth() / 10.0f);
        }
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
    }

    @Override
    public float ire() {
        return 0.05f;
    }

    @Override
    public float initialComplexity() {
        return 35.0f;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }
}

