/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.sorcery.targeting;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.base.ChanneledSpellEntity;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.math.MathUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SpellEmanation
extends ChanneledSpellEntity {
    public SpellEmanation(EntityType<? extends SpellEmanation> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public SpellEmanation(LivingEntity caster, ISpellDefinition spell, Level world) {
        super((EntityType<? extends ChanneledSpellEntity>)((EntityType)EntityInit.SPELL_EMANATION.get()), caster, spell, world);
        this.setPos(caster.getX(), caster.getY(), caster.getZ());
    }

    @Override
    public void tick() {
        if (this.getCaster() != null) {
            this.setPos(this.getCaster().getX(), this.getCaster().getY() + (double)0.1f, this.getCaster().getZ());
        }
        super.tick();
    }

    @Override
    protected void applyEffect(ItemStack stack, SpellRecipe recipe, LivingEntity caster, ServerLevel world) {
        boolean below;
        if (caster == null) {
            return;
        }
        float radius_h = this.getShapeAttributeByAge(Attribute.WIDTH);
        float radius_v = this.getShapeAttributeByAge(Attribute.HEIGHT);
        SpellSource source = new SpellSource(caster, caster.getUsedItemHand());
        SpellContext context = new SpellContext((Level)world, recipe, this);
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
            AABB bb = new AABB(this.getX() - (double)radius_h, this.getY(), this.getZ() - (double)radius_h, this.getX() + (double)radius_h, this.getY() + (double)radius_v, this.getZ() + (double)radius_h);
            List entities = world.getEntities((Entity)caster, bb);
            for (Entity e : entities) {
                if (!this.losCheck(e)) continue;
                SpellCaster.ApplyComponents(recipe, source, new SpellTarget(e), context);
            }
        }
        boolean bl = below = this.getCaster() != null && this.getCaster().isCrouching();
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks())) {
            BlockPos origin = this.blockPosition();
            int i = -((int)radius_h);
            while ((float)i <= radius_h) {
                int j = 0;
                while ((float)j < radius_v) {
                    int k = -((int)radius_h);
                    while ((float)k <= radius_h) {
                        BlockPos adjusted = origin.offset(i, j - (below ? 1 : 0), k);
                        if (below && (double)adjusted.getY() < this.getY() || this.losCheck(adjusted)) {
                            SpellCaster.ApplyComponents(recipe, source, new SpellTarget(adjusted, Direction.UP).doNotOffsetFace(), context);
                        }
                        ++k;
                    }
                    ++j;
                }
                ++i;
            }
        }
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        float particle_spread = 0.0f;
        for (int i = 0; i < 20; ++i) {
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.2f).setColor(10, 10, 10), (Entity)this.getCaster()), me.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), me.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), me.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), (double)0.2f, (double)0.15f, (double)radius);
        }
    }

    @Override
    protected void spawnEarthParticles(SpellRecipe recipe) {
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        for (int i = 0; i < 50; ++i) {
            double angle = Math.random() * 2.0 * Math.PI;
            Vec3 offset = new Vec3(Math.cos(angle) * (double)radius, 0.0, Math.sin(angle) * (double)radius);
            Vec3 pos = me.add(offset);
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)this.getCaster()), pos.x, pos.y, pos.z, 0.0, 0.1, 0.0);
        }
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        if (!lightning) {
            for (int i = 0; i < 50; ++i) {
                double angle = Math.random() * 2.0 * Math.PI;
                Vec3 offset = new Vec3(Math.cos(angle) * (double)radius, 0.0, Math.sin(angle) * (double)radius);
                Vec3 pos = me.add(offset);
                this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)(hellfire ? (ParticleType)ParticleInit.HELLFIRE.get() : (ParticleType)ParticleInit.FLAME.get())), (Entity)this.getCaster()), pos.x, pos.y, pos.z, 0.0, 0.1, 0.0);
            }
        } else {
            Vec3 offset = new Vec3((double)(-radius) + Math.random() * (double)radius * 2.0, (double)(-radius) + Math.random() * (double)radius * 2.0, (double)(-radius) + Math.random() * (double)radius * 2.0);
            Vec3 pos = me.add(offset);
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)this.getCaster()), me.x, me.y, me.z, pos.x, pos.y, pos.z);
        }
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean frost) {
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        for (int i = 0; i < 50; ++i) {
            double angle = Math.random() * 2.0 * Math.PI;
            Vec3 offset = new Vec3(Math.cos(angle) * (double)radius, 0.0, Math.sin(angle) * (double)radius);
            Vec3 pos = me.add(offset);
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)(frost ? (ParticleType)ParticleInit.FROST.get() : (ParticleType)ParticleInit.WATER.get())), (Entity)this.getCaster()), pos.x, pos.y, pos.z, 0.0, 0.1, 0.0);
        }
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        for (int i = 0; i < 50; ++i) {
            double angle = Math.random() * 2.0 * Math.PI;
            Vec3 offset = new Vec3(Math.cos(angle) * (double)radius * (double)MathUtils.RandomBetween(0.9f, 1.0f), (double)0.1f + Math.random() * 2.0, Math.sin(angle) * (double)radius * (double)MathUtils.RandomBetween(0.9f, 1.0f));
            Vec3 pos = me.add(offset);
            if (blood) {
                this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD), (Entity)this.getCaster()).setMaxAge(10 + (int)radius * 2).setPhysics(true).setGravity(0.05f), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
                continue;
            }
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)this.getCaster()), me.x, pos.y, me.z, pos.x, pos.y, pos.z);
        }
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        for (int i = 0; i < 50; ++i) {
            double angle = Math.random() * 2.0 * Math.PI;
            Vec3 offset = new Vec3(Math.cos(angle) * (double)radius, (double)0.1f + Math.abs(Math.sin((float)this.tickCount / 0.02f)) * 0.75, -Math.sin(angle) * (double)radius);
            Vec3 pos = me.add(offset);
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), (Entity)this.getCaster()).setMaxAge(10 + (int)radius * 2).setMover(new ParticleLerpMover(me.x, pos.y, me.z, pos.x, pos.y, pos.z)), me.x, pos.y, me.z, 0.0, 0.0, 0.0);
        }
    }
}

