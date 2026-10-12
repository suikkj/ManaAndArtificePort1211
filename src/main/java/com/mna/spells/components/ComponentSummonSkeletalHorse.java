/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.DifficultyInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.ServerLevelAccessor
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
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
import com.mna.entities.summon.SummonedSpectralHorse;
import com.mna.factions.Factions;
import com.mna.tools.SummonUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class ComponentSummonSkeletalHorse
extends SpellEffect {
    public ComponentSummonSkeletalHorse(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 1200.0f, 30.0f, 1.75f));
        this.addOptionalReagent(new ItemStack((ItemLike)Items.BONE), false, true, true, new IFaction[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (source.isPlayerCaster()) {
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
            SummonedSpectralHorse horse = (SummonedSpectralHorse)((EntityType)EntityInit.SPECTRAL_HORSE.get()).create((Level)context.getServerLevel());
            DifficultyInstance difficultyinstance = context.getServerLevel().getCurrentDifficultyAt(target.getBlock());
            horse.finalizeSpawn((ServerLevelAccessor)context.getServerLevel(), difficultyinstance, MobSpawnType.TRIGGERED, null);
            horse.setPos(target.getPosition().x, target.getPosition().y + (double)offsetCount, target.getPosition().z);
            horse.invulnerableTime = 60;
            horse.setPersistenceRequired();
            horse.tameWithName(source.getPlayer());
            horse.setAge(0);
            // [PORT 1.21.1] equipSaddle(ItemStack,SoundEvent) - sem args validos aqui
            SummonUtils.clampTrackedEntities((LivingEntity)source.getPlayer());
            int duration = 20;
            duration = context.isReagentMissing(Items.BONE) ? (int)(modificationData.getValue(Attribute.DURATION) * 20.0f) : -1;
            SummonUtils.setSummon((Mob)horse, (LivingEntity)source.getPlayer(), false, duration);
            context.getServerLevel().addFreshEntity((Entity)horse);
            SummonUtils.addTrackedEntity((LivingEntity)source.getPlayer(), (Entity)horse);
            if (context.getSpawnedTargetEntity() != null && context.getSpawnedTargetEntity() instanceof LivingEntity) {
                ((LivingEntity)context.getSpawnedTargetEntity()).startRiding((Entity)horse, true);
            } else if (target.isLivingEntity() && target.getLivingEntity() == source.getCaster()) {
                source.getCaster().startRiding((Entity)horse, true);
            }
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
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
    public SoundEvent SoundEffect() {
        return SFX.Entity.SpectralHorse.SUMMON;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.FRIENDLY;
    }
}

