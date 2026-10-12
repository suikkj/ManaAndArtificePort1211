/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.DifficultyInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.ServerLevelAccessor
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.entities.EntityInit;
import com.mna.entities.summon.InsectSwarm;
import com.mna.factions.Factions;
import com.mna.tools.SummonUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class ComponentSummonInsectSwarm
extends SpellEffect {
    public ComponentSummonInsectSwarm(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 1200.0f, 30.0f, 1.75f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
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
        InsectSwarm swarm = (InsectSwarm)((EntityType)EntityInit.INSECT_SWARM.get()).create((Level)context.getServerLevel());
        DifficultyInstance difficultyinstance = context.getServerLevel().getCurrentDifficultyAt(target.getBlock());
        swarm.finalizeSpawn((ServerLevelAccessor)context.getServerLevel(), difficultyinstance, MobSpawnType.TRIGGERED, null);
        swarm.setPos(target.getPosition().x, target.getPosition().y + (double)offsetCount, target.getPosition().z);
        swarm.invulnerableTime = 60;
        swarm.setPersistenceRequired();
        SummonUtils.clampTrackedEntities(source.getCaster());
        SummonUtils.setSummon((Mob)swarm, source.getCaster(), true, (int)(modificationData.getValue(Attribute.DURATION) * 20.0f));
        context.getServerLevel().addFreshEntity((Entity)swarm);
        SummonUtils.addTrackedEntity(source.getCaster(), (Entity)swarm);
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.FRIENDLY;
    }
}

