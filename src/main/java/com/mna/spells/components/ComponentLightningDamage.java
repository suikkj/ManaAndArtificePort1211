 
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
import com.mna.api.spells.base.IDamageComponent;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.config.GeneralConfig;
import com.mna.effects.EffectInit;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.math.MathUtils;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ComponentLightningDamage
extends SpellEffect
implements IDamageComponent {
    public ComponentLightningDamage(ResourceLocation icon) {
        super(icon, new AttributeValuePair(Attribute.DAMAGE, 5.0f, 1.0f, 20.0f, 0.5f, 3.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 3.0f, 0.0f, 10.0f, 1.0f, 2.0f), new AttributeValuePair(Attribute.RADIUS, 1.0f, 1.0f, 10.0f, 1.0f, 3.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        float damage = modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier();
        float branchChance = MathUtils.clamp01(modificationData.getValue(Attribute.LESSER_MAGNITUDE) / 10.0f);
        float branchRadius = modificationData.getValue(Attribute.RADIUS);
        Vec3 lastTargetPosition = target.getPosition();
        int hits = 0;
        if (target.isEntity()) {
            if (!(target.getEntity() instanceof ItemEntity) && !(target.getEntity() instanceof ExperienceOrb)) {
                branchChance = this.updateArcChanceBasedOnEntityCondition(target.getEntity(), branchChance);
                this.affectSingleTarget(context.getServerLevel(), target.getEntity(), source.getCaster(), damage, context);
                ++hits;
            }
            lastTargetPosition = target.getEntity().position().add(new Vec3(0.0, (double)target.getEntity().getEyeHeight(), 0.0));
        }
        while (Math.random() < (double)branchChance) {
            Vec3 checkTargetPosition = lastTargetPosition;
            List entities = context.getServerLevel().getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.containing((Position)lastTargetPosition)).inflate((double)branchRadius), e -> {
                if (source.hasCasterReference() && e == source.getCaster()) {
                    return false;
                }
                Vec3 entitySightPos = new Vec3(e.getX(), e.getEyeY(), e.getZ());
                boolean visionCheck = context.getServerLevel().clip(new ClipContext(checkTargetPosition, entitySightPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null)).getType() == HitResult.Type.MISS;
                return e.isAlive() && !context.hasEntityBeenAffected(this, (Entity)e) && visionCheck;
            });
            if (entities.size() == 0) break;
            Vec3 sortLTP = lastTargetPosition;
            entities.sort((o1, o2) -> {
                Double o1Dist = o1.distanceToSqr(sortLTP);
                Double o2Dist = o2.distanceToSqr(sortLTP);
                return o1Dist.compareTo(o2Dist);
            });
            LivingEntity nextTarget = (LivingEntity)entities.get(0);
            branchChance = this.updateArcChanceBasedOnEntityCondition(target.getEntity(), branchChance);
            context.addAffectedEntity(this, (Entity)nextTarget);
            this.affectSingleTarget(context.getServerLevel(), (Entity)nextTarget, source.getCaster(), damage, context);
            ServerMessageDispatcher.sendParticleSpawn(lastTargetPosition.x, lastTargetPosition.y, lastTargetPosition.z, nextTarget.getX(), nextTarget.getY() + (double)nextTarget.getEyeHeight(), nextTarget.getZ(), context.getSpell().getParticleColorOverride(), 64.0f, (ResourceKey<Level>)context.getServerLevel().dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
            lastTargetPosition = nextTarget.position().add(new Vec3(0.0, (double)nextTarget.getEyeHeight(), 0.0));
            branchChance *= 0.75f;
            ++hits;
        }
        return hits > 0 ? ComponentApplicationResult.SUCCESS : ComponentApplicationResult.FAIL;
    }

    private void affectSingleTarget(ServerLevel level, Entity target, @Nullable LivingEntity source, float damage, SpellContext context) {
        target.hurt(DamageHelper.createSourcedType(DamageHelper.LIGHTNING, context.getLevel().registryAccess(), (Entity)source), damage);
        if (level != null && target.isAlive() && target instanceof Creeper && damage >= 20.0f) {
            CompoundTag tag = new CompoundTag();
            ((Creeper)target).addAdditionalSaveData(tag);
            tag.putBoolean("powered", true);
            ((Creeper)target).readAdditionalSaveData(tag);
        }
        context.addAffectedEntity(this, target);
    }

    private float updateArcChanceBasedOnEntityCondition(Entity target, float magnitudeIn) {
        if (target instanceof LivingEntity && ((LivingEntity)target).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
            return 1.0f;
        }
        return magnitudeIn;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.LIGHTNING;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.LIGHTNING;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 1) {
            return;
        }
        float particle_spread = 1.0f;
        float v = 0.4f;
        int particleCount = 10;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 velocity = new Vec3((double)(-v / 2.0f) + Math.random() * (double)v, Math.random() * (double)v, (double)(-v / 2.0f) + Math.random() * (double)v);
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setScale(0.05f).setGravity(0.02f).setPhysics(true), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + Math.random() + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
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
        return Arrays.asList(Affinity.FIRE, Affinity.LIGHTNING);
    }
}

