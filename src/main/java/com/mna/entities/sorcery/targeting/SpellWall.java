/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
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
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.BlockUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.LineSegment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SpellWall
extends ChanneledSpellEntity {
    protected Vec3 forward;
    protected Vec3 perpendicular;

    public SpellWall(EntityType<? extends SpellWall> type, Level world) {
        super(type, world);
    }

    public SpellWall(LivingEntity caster, ISpellDefinition spell, Level world, boolean axisLock) {
        this((EntityType<? extends SpellWall>)((EntityType)EntityInit.SPELL_WALL.get()), caster, spell, world, axisLock);
    }

    protected SpellWall(EntityType<? extends SpellWall> entityType, LivingEntity caster, ISpellDefinition spell, Level world, boolean axisLock) {
        super(entityType, caster, spell, world);
        if (!axisLock) {
            this.setYRot(caster.getYRot());
            this.setXRot(caster.getXRot());
        } else {
            this.setYRot(caster.getDirection().toYRot());
        }
    }

    @Override
    protected void applyEffect(ItemStack stack, SpellRecipe recipe, LivingEntity caster, ServerLevel world) {
        float radius_h = this.getShapeAttributeByAge(Attribute.WIDTH);
        float radius_v = this.getShapeAttributeByAge(Attribute.HEIGHT);
        SpellSource source = new SpellSource(caster, caster.getUsedItemHand());
        SpellContext context = new SpellContext((Level)world, recipe, this);
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities() && ((SpellEffect)c.getPart()).canBeChanneled())) {
            for (Entity e : this.targetEntities(radius_h, radius_v)) {
                SpellCaster.ApplyComponents(recipe, source, new SpellTarget(e), context);
            }
        }
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks() && ((SpellEffect)c.getPart()).canBeChanneled())) {
            for (BlockPos p : this.targetBlocks(radius_h, radius_v)) {
                SpellCaster.ApplyComponents(recipe, source, new SpellTarget(p, null).doNotOffsetFace(), context);
            }
        }
    }

    protected BlockPos[] targetBlocks(float radius_h, float radius_v) {
        double radiusOffsetX = Math.cos(Math.toRadians(this.getYRot())) * (double)radius_h;
        double radiusOffsetZ = Math.sin(Math.toRadians(this.getYRot())) * (double)radius_h;
        Vec3 a = new Vec3(this.getX() + radiusOffsetX, this.getY(), this.getZ() + radiusOffsetZ);
        Vec3 b = new Vec3(this.getX() - radiusOffsetX, this.getY(), this.getZ() - radiusOffsetZ);
        return this.getAllBlockLocationsBetween(a, b, radius_v);
    }

    protected BlockPos[] getAllBlockLocationsBetween(Vec3 a, Vec3 b, float radius_v) {
        ArrayList allPoints = new ArrayList();
        BlockUtils.stepThroughBlocksLinear(BlockUtils.Vector3dToBlockPosRound(a), BlockUtils.Vector3dToBlockPosRound(b), pos -> {
            int i = 0;
            while ((float)i < radius_v) {
                if (!allPoints.contains(pos.offset(0, 1 + i, 0))) {
                    allPoints.add(pos.offset(0, 1 + i, 0));
                }
                ++i;
            }
        });
        return allPoints.toArray(new BlockPos[0]);
    }

    protected List<Entity> targetEntities(float radius_h, float radius_v) {
        List<Entity> possibleTargets = this.level().getEntitiesOfClass(Entity.class, new AABB(this.getX() - (double)radius_h, this.getY(), this.getZ() - (double)radius_h, this.getX() + (double)radius_h, this.getY() + (double)radius_v, this.getZ() + (double)radius_h));
        ArrayList<Entity> targets = new ArrayList<Entity>();
        double dirX = Math.cos(Math.PI / 180 * (double)this.getXRot());
        double dirZ = Math.sin(Math.PI / 180 * (double)this.getYRot());
        Vec3 a = new Vec3(this.getX() - dirX * (double)radius_h, this.getY(), this.getZ() - dirZ * (double)radius_h);
        Vec3 b = new Vec3(this.getX() - dirX * (double)(-radius_h), this.getY(), this.getZ() - dirZ * (double)(-radius_h));
        LineSegment wallLine = new LineSegment(a, b);
        for (Entity e : possibleTargets) {
            if (e == this || e == this.getCaster() || !(e instanceof LivingEntity) || !e.isAlive()) continue;
            Vec3 target = e.position();
            Vec3 closest = wallLine.closestPointOnLine(target);
            target = target.subtract(0.0, target.y, 0.0);
            closest = closest.subtract(0.0, closest.y, 0.0);
            double hDist = Math.abs(closest.distanceTo(target));
            double vDist = Math.abs(this.getY() - e.getY());
            if (!(hDist < 0.75) || !(vDist < (double)radius_v)) continue;
            targets.add(e);
        }
        return targets;
    }

    @Override
    public void tick() {
        if (this.actAsChanneled()) {
            super.tick();
            return;
        }
        if (!this.level().isClientSide() && this.tickCount >= this.getMaxAge()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        SpellRecipe recipe = this.getSpell();
        if (!this.level().isClientSide() && !recipe.isValid()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        LivingEntity caster = this.getCaster();
        if (caster == null) {
            if (!this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        if (!this.level().isClientSide()) {
            if (this.tickCount > 0 && (this.getApplicationRate() == 1 || this.tickCount % this.getApplicationRate() == 0)) {
                this.applyEffect(ItemStack.EMPTY, recipe, caster, (ServerLevel)this.level());
            }
        } else {
            this.spawnParticles();
            this.playSounds();
        }
    }

    protected boolean actAsChanneled() {
        return false;
    }

    @Override
    public void setCaster(LivingEntity player) {
        this.setXRot(player.getXRot());
        this.setYRot(player.getYRot());
        super.setCaster(player);
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
        this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.AIR_ORBIT.get()), new Vec3((double)0.3f, (double)0.02f, (double)0.3f), p -> p.setScale(0.2f).setColor(10, 10, 10));
    }

    @Override
    protected void spawnEarthParticles(SpellRecipe recipe) {
        this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.DUST.get()), new Vec3(0.0, (double)0.05f, 0.0), null);
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
        if (!lightning) {
            this.spawnParticlesAlongRadius(recipe, hellfire ? (MAParticleType)(ParticleInit.HELLFIRE.get()) : (MAParticleType)(ParticleInit.FLAME.get()), new Vec3(0.0, (double)(this.getShapeAttributeByAge(Attribute.HEIGHT) / 30.0f), 0.0), null);
        } else {
            this.spawnLightningParticlesAlongRadius(recipe, new Vec3(-0.5 + Math.random(), Math.random(), -0.5 + Math.random()));
        }
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean frost) {
        this.spawnParticlesAlongRadius(recipe, frost ? (MAParticleType)(ParticleInit.FROST.get()) : (MAParticleType)(ParticleInit.WATER.get()), new Vec3(0.0, (double)0.025f, 0.0), null);
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
        if (blood) {
            this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.DROPLET.get()), new Vec3(0.0, (double)0.025f, 0.0), p -> {
                p.setColor(Affinity.BLOOD);
                p.setGravity(0.05f);
                p.setPhysics(true);
                p.setMaxAge(12);
            });
        } else {
            this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.ENDER_VELOCITY.get()), new Vec3(0.0, (double)0.025f, 0.0), null);
        }
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
        this.spawnParticlesAlongRadius(recipe, (MAParticleType)(ParticleInit.ARCANE.get()), new Vec3(0.0, (double)0.025f, 0.0), null);
    }

    protected void spawnParticlesAlongRadius(SpellRecipe recipe, MAParticleType particleData, Vec3 motionData, Consumer<MAParticleType> particleAdjuster) {
        this.spawnParticlesAlongRadius(recipe, particleData, motionData, true, false, particleAdjuster);
    }

    protected void spawnParticlesAlongRadius(SpellRecipe recipe, MAParticleType particleData, Vec3 motionData, boolean randomY, boolean addPositionToMotionData, Consumer<MAParticleType> particleAdjuster) {
        if (this.forward == null) {
            float x = -Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
            float y = -Mth.sin((float)(this.getXRot() * ((float)Math.PI / 180)));
            float z = Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
            this.forward = new Vec3((double)x, (double)y, (double)z).normalize();
        }
        if (this.perpendicular == null) {
            this.perpendicular = this.forward.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        }
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        float height = this.getShapeAttributeByAge(Attribute.HEIGHT);
        int i = 0;
        while ((float)i < 20.0f * radius * (height / 2.0f)) {
            double radiusMid = Math.random() * (double)radius * 2.0 - (double)radius;
            Vec3 particleOrigin = new Vec3(me.x - this.perpendicular.x * radiusMid + (double)(MathUtils.RandomBetween((float)(-this.forward.x), (float)this.forward.x) / 2.0f), randomY ? me.y + Math.random() * (double)height : me.y, me.z - this.perpendicular.z * radiusMid + (double)(MathUtils.RandomBetween((float)(-this.forward.z), (float)this.forward.z) / 2.0f));
            Vec3 particleMotion = addPositionToMotionData ? motionData.add(particleOrigin) : motionData;
            MAParticleType pfxAdj = new MAParticleType(particleData);
            if (particleAdjuster != null) {
                particleAdjuster.accept(pfxAdj);
            }
            recipe.colorParticle(pfxAdj, (Entity)this.getCaster());
            this.level().addParticle((ParticleOptions)pfxAdj, particleOrigin.x, particleOrigin.y, particleOrigin.z, particleMotion.x, particleMotion.y, particleMotion.z);
            ++i;
        }
    }

    protected void spawnLightningParticlesAlongRadius(SpellRecipe recipe, Vec3 motionData) {
        if (this.forward == null) {
            float x = -Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
            float y = -Mth.sin((float)(this.getXRot() * ((float)Math.PI / 180)));
            float z = Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
            this.forward = new Vec3((double)x, (double)y, (double)z);
        }
        if (this.perpendicular == null) {
            this.perpendicular = this.forward.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        }
        Vec3 me = this.position();
        float radius = this.getShapeAttributeByAge(Attribute.WIDTH);
        float height = this.getShapeAttributeByAge(Attribute.HEIGHT);
        int i = 0;
        while ((float)i < 2.0f * radius) {
            double radiusMid = Math.random() * (double)radius * 2.0 - (double)radius;
            Vec3 particleOrigin = new Vec3(me.x - this.perpendicular.x * radiusMid, me.y, me.z - this.perpendicular.z * radiusMid);
            Vec3 particleMotion = motionData.add(particleOrigin);
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)this.getCaster()), particleOrigin.x, particleOrigin.y, particleOrigin.z, particleMotion.x, particleMotion.y + (double)height, particleMotion.z);
            ++i;
        }
    }
}

