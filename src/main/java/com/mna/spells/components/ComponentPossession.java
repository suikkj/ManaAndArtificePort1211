 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.config.GeneralConfigValues;
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
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.tools.SummonUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentPossession
extends SpellEffect {
    private final boolean greater;
    private static final AttributeValuePair[] ATTRS_STANDARD = new AttributeValuePair[]{new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 10.0f), new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 10.0f)};
    private static final AttributeValuePair[] ATTRS_GREATER = new AttributeValuePair[]{new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 10.0f)};

    public ComponentPossession(ResourceLocation guiIcon, boolean greater) {
        super(guiIcon, greater ? ATTRS_GREATER : ATTRS_STANDARD);
        this.greater = greater;
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public boolean isSilverSpell() {
        return this.greater;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!context.getServerLevel().isClientSide() && source.isPlayerCaster() && target.isLivingEntity() && target.getEntity() != source.getPlayer() && source.getPlayer().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIND_VISION.get())) == null) {
            if (target.getEntity() instanceof Player && !GeneralConfigValues.PossessionAffectsPlayers) {
                return ComponentApplicationResult.FAIL;
            }
            int magnitude = (int)modificationData.getValue(Attribute.MAGNITUDE);
            if (!SummonUtils.isSummon((Entity)target.getLivingEntity()) && !this.magnitudeHealthCheck(source, target, magnitude, 20)) {
                return ComponentApplicationResult.FAIL;
            }
            int duration = (int)modificationData.getValue(Attribute.DURATION) * 20;
            if (this.greater) {
                duration = -1;
            }
            source.getPlayer().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()), duration, this.greater ? 2 : 1));
            target.getLivingEntity().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()), duration));
            ServerMessageDispatcher.sendPlayerPosessionMessage((ServerPlayer)source.getPlayer(), target.getEntity());
            source.getPlayer().getPersistentData().putInt("posessed_entity_id", target.getLivingEntity().getId());
            context.getLevel().getEntities((Entity)target.getLivingEntity(), source.getBoundingBox().inflate(8.0), e -> {
                Mob mob;
                return e instanceof Mob && (mob = (Mob)e).getTarget() == source.getPlayer() && mob != target.getLivingEntity();
            }).forEach(e -> {
                if (e instanceof Mob) {
                    Mob mob = (Mob)e;
                    mob.setTarget(target.getLivingEntity());
                }
            });
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public float initialComplexity() {
        return 50.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 10) {
            return;
        }
        for (int i = 0; i < 2; ++i) {
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.MIST.get())).setScale(0.25f).setMover(new ParticleOrbitMover(impact_position, 0.2, 0.2, 1.0)), impact_position.x, impact_position.y, impact_position.z, 0.0, 0.0, 0.0);
        }
    }
}

