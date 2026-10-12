/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
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
import com.mna.blocks.BlockInit;
import com.mna.config.GeneralConfig;
import com.mna.spells.components.ComponentFling;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ComponentImpale
extends SpellEffect
implements IDamageComponent {
    private static final Predicate<? super Entity> TARGET_PREDICATE = e -> e instanceof LivingEntity && e.isAlive();

    public ComponentImpale(ResourceLocation icon) {
        super(icon, new AttributeValuePair(Attribute.DAMAGE, 5.0f, 1.0f, 15.0f, 0.5f, 3.0f), new AttributeValuePair(Attribute.SPEED, 1.0f, 1.0f, 3.0f, 0.5f, 3.0f), new AttributeValuePair(Attribute.DURATION, 3.0f, 1.0f, 10.0f, 1.0f, 1.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        BlockPos targetPos = target.getBlock();
        Direction targetFace = target.getBlockFace(this);
        if (target.isEntity()) {
            targetPos = target.getEntity().blockPosition().below();
            targetFace = Direction.UP;
        }
        SpellEffect.BlockPlaceResult bpr = this.tryPlaceBlock(source.getPlayer(), context.getServerLevel(), (Block)BlockInit.IMPALE_SPIKE.get(), targetPos, targetFace, true);
        if (bpr.success) {
            float strength = modificationData.getValue(Attribute.SPEED);
            Vec3 vel = targetFace.getAxis() == Direction.Axis.Y ? Vec3.atLowerCornerOf((Vec3i)targetFace.getOpposite().getNormal()).normalize().scale((double)strength).add(0.0, 1.0, 0.0) : Vec3.atLowerCornerOf((Vec3i)targetFace.getOpposite().getNormal()).normalize().scale((double)strength);
            context.getServerLevel().getEntities((Entity)source.getCaster(), new AABB(bpr.position), TARGET_PREDICATE).stream().map(e -> (LivingEntity)e).forEach(e -> {
                e.hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.FALLING_STALACTITE, context.getLevel().registryAccess(), (Entity)source.getCaster()), modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier());
                ComponentFling.flingTarget(e, vel, strength, 2.0f, 1.0f);
            });
            int duration = (int)modificationData.getValue(Attribute.DURATION);
            context.getServerLevel().scheduleTick(bpr.position, (Block)BlockInit.IMPALE_SPIKE.get(), duration * 20 + (int)(Math.random() * 20.0));
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 1) {
            return;
        }
        float particle_spread = 1.0f;
        float v = 0.1f;
        int particleCount = 10;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 velocity = new Vec3((double)(-v / 2.0f) + Math.random() * (double)v, Math.random() * (double)v, (double)(-v / 2.0f) + Math.random() * (double)v);
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.AoE.EARTH;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.EARTH;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
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
        return Arrays.asList(Affinity.EARTH, Affinity.ICE);
    }
}

