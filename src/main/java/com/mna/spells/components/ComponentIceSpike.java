/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
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
import com.mna.blocks.BlockInit;
import com.mna.config.GeneralConfig;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ComponentIceSpike
extends SpellEffect {
    private static final Predicate<? super Entity> TARGET_PREDICATE = e -> e instanceof LivingEntity && e.isAlive();

    public ComponentIceSpike(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DAMAGE, 10.0f, 1.0f, 20.0f, 1.0f, 3.0f), new AttributeValuePair(Attribute.DURATION, 10.0f, 5.0f, 60.0f, 5.0f, 3.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        BlockPos targetPos = target.getBlock();
        Direction targetFace = target.getBlockFace(this);
        if (target.isEntity()) {
            targetPos = target.getEntity().blockPosition().below();
            targetFace = Direction.UP;
        }
        SpellEffect.BlockPlaceResult bpr = this.tryPlaceBlock(source.getPlayer(), context.getServerLevel(), (Block)BlockInit.ICE_SPIKE.get(), targetPos, targetFace, true);
        if (bpr.success) {
            context.getServerLevel().getEntities((Entity)source.getCaster(), new AABB(bpr.position), TARGET_PREDICATE).stream().map(e -> (LivingEntity)e).forEach(e -> e.hurt(DamageHelper.createSourcedType(DamageHelper.FROST, context.getLevel().registryAccess(), (Entity)source.getCaster()), modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier()));
            int duration = (int)modificationData.getValue(Attribute.DURATION);
            context.getServerLevel().scheduleTick(bpr.position, (Block)BlockInit.ICE_SPIKE.get(), duration * 20 + (int)(Math.random() * 20.0));
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 2) {
            return;
        }
        normal = normal.normalize().scale(0.2);
        for (int i = 0; i < 10; ++i) {
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), (Entity)caster).setGravity(0.01f).setScale(0.1f), impact_position.x - 0.5 + Math.random(), impact_position.y - 0.5 + Math.random(), impact_position.z - 0.5 + Math.random(), normal.x, normal.y, normal.z);
        }
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ICE;
    }

    @Override
    public float initialComplexity() {
        return 15.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}

