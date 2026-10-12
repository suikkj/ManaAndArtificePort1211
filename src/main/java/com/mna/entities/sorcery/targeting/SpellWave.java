/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
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
import com.mna.entities.sorcery.targeting.SpellWall;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpellWave
extends SpellWall {
    public SpellWave(EntityType<? extends SpellWave> entityTypeIn, Level worldIn) {
        super((EntityType<? extends SpellWall>)entityTypeIn, worldIn);
    }

    public SpellWave(LivingEntity caster, ISpellDefinition spell, Level world, boolean axisLock) {
        super((EntityType<? extends SpellWall>)((EntityType)EntityInit.SPELL_WAVE.get()), caster, spell, world, axisLock);
        this.setPos(caster.getX(), caster.getY(), caster.getZ());
        if (axisLock) {
            Vec3 motion = Vec3.directionFromRotation((float)0.0f, (float)caster.getDirection().toYRot()).normalize().scale((double)this.getShapeAttribute(Attribute.SPEED));
            this.setDeltaMovement(motion);
            if (caster.isCrouching()) {
                this.setPos(this.position().add(motion.normalize()).add(0.0, -0.5, 0.0));
            } else {
                this.setPos(this.position().add(motion.normalize()));
            }
        } else {
            float x = -Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
            float y = -Mth.sin((float)(this.getXRot() * ((float)Math.PI / 180)));
            float z = Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
            Vec3 motion = new Vec3((double)x, (double)y, (double)z).normalize().scale((double)this.getShapeAttribute(Attribute.SPEED));
            this.setDeltaMovement(motion);
        }
    }

    @Override
    public void tick() {
        this.setPos(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
        super.tick();
    }

    @Override
    protected boolean actAsChanneled() {
        return true;
    }

    @Override
    protected void applyEffect(ItemStack stack, SpellRecipe recipe, LivingEntity caster, ServerLevel world) {
        float radius_h = this.getShapeAttributeByAge(Attribute.WIDTH);
        float radius_v = this.getShapeAttributeByAge(Attribute.HEIGHT);
        SpellSource source = new SpellSource(caster, caster.getUsedItemHand());
        SpellContext context = new SpellContext((Level)world, recipe, this);
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
            for (Entity e : this.targetEntities(radius_h, radius_v)) {
                SpellCaster.ApplyComponents(recipe, source, new SpellTarget(e), context);
            }
        }
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks())) {
            for (BlockPos p : this.targetBlocks(radius_h, radius_v)) {
                SpellCaster.ApplyComponents(recipe, source, new SpellTarget(p, null), context);
            }
        }
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
        Vec3 jitter = new Vec3((double)-0.1f + Math.random() * (double)0.2f, 0.0, (double)-0.1f + Math.random() * (double)0.2f);
        this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.AIR_VELOCITY.get()), this.getDeltaMovement().scale(0.75).add(jitter), p -> p.setScale(0.2f).setColor(10, 10, 10));
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
        this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.ARCANE.get()), this.getDeltaMovement().scale(0.75), null);
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
        if (blood) {
            this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.DROPLET.get()), this.getDeltaMovement().scale(0.75).add(0.0, 0.25, 0.0), p -> {
                p.setColor(Affinity.BLOOD);
                p.setGravity(0.05f);
                p.setPhysics(true);
                p.setMaxAge(14);
            });
        } else {
            this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.ENDER_VELOCITY.get()), this.getDeltaMovement().scale(0.75), null);
        }
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
        if (!lightning) {
            this.spawnParticlesAlongRadius(recipe, hellfire ? (MAParticleType)(ParticleInit.HELLFIRE.get()) : (MAParticleType)(ParticleInit.FLAME.get()), this.getDeltaMovement(), false, false, null);
        } else {
            this.spawnLightningParticlesAlongRadius(recipe, this.getDeltaMovement().add(new Vec3(-0.5 + Math.random(), Math.random(), -0.5 + Math.random())));
        }
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean frost) {
        this.spawnParticlesAlongRadius(recipe, frost ? (MAParticleType)(ParticleInit.FROST.get()) : (MAParticleType)(ParticleInit.WATER.get()), this.getDeltaMovement().scale(0.75), null);
    }

    @Override
    protected int getApplicationRate() {
        return 1;
    }
}

