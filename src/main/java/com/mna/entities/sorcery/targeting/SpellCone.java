/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
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
import com.mna.tools.EntityUtil;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class SpellCone
extends ChanneledSpellEntity {
    public SpellCone(EntityType<? extends SpellCone> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public SpellCone(LivingEntity caster, ISpellDefinition spell, Level world) {
        super((EntityType<? extends ChanneledSpellEntity>)((EntityType)EntityInit.SPELL_CONE.get()), caster, spell, world);
        this.setPos(caster.getX(), caster.getY(), caster.getZ());
    }

    @Override
    public void tick() {
        LivingEntity c = this.getCaster();
        if (c != null) {
            Vec3 side = c.getLookAngle().cross(new Vec3(0.0, 1.0, 0.0)).scale(0.5);
            double offsetX = 0.0;
            HumanoidArm casterHandedness = c.getMainArm();
            offsetX = casterHandedness == HumanoidArm.RIGHT && c.getUsedItemHand() == InteractionHand.MAIN_HAND || casterHandedness == HumanoidArm.LEFT && c.getUsedItemHand() == InteractionHand.OFF_HAND ? side.x : -side.x;
            this.setPos(c.getX() + offsetX, c.getY() + (double)c.getEyeHeight() * 0.75, c.getZ());
        }
        super.tick();
    }

    @Override
    protected void applyEffect(ItemStack stack, SpellRecipe recipe, LivingEntity caster, ServerLevel world) {
        float width;
        if (caster == null) {
            return;
        }
        float depth = this.getShapeAttributeByAge(Attribute.DEPTH);
        float r = width = this.getShapeAttributeByAge(Attribute.WIDTH);
        float angle = (float)Math.abs(Math.sin(depth / r) * 180.0 / Math.PI);
        this.setYRot(caster.getYRot());
        this.setXRot(caster.getXRot());
        SpellSource source = new SpellSource(caster, caster.getUsedItemHand());
        SpellContext context = new SpellContext((Level)world, recipe, this);
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
            EntityUtil.getEntitiesWithinCone(this.level(), this.position(), this.getCaster().getForward(), depth, -angle, angle, e -> e.isAlive() && e != this.getCaster()).forEach(e -> {
                if (this.losCheck((Entity)e)) {
                    SpellCaster.ApplyComponents(recipe, source, new SpellTarget((Entity)e), context);
                }
            });
        }
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks())) {
            int min = 0;
            int max = (int)depth;
            Vec3 pos = this.position().subtract(0.0, 1.0, 0.0);
            Vec3 step = this.getCaster().getForward().normalize();
            HashSet<BlockPos> allBlocks = new HashSet<BlockPos>();
            for (int i = min; i < max; ++i) {
                pos = pos.add(step);
                float radiusAtStep = r * ((float)i / (float)max);
                BlockPos[] positions = this.targetBlocks(pos, radiusAtStep, radiusAtStep);
                allBlocks.addAll(Arrays.asList(positions));
            }
            for (BlockPos bp : allBlocks) {
                SpellCaster.ApplyComponents(recipe, source, new SpellTarget(bp, Direction.UP).doNotOffsetFace(), context);
            }
        }
    }

    protected BlockPos[] targetBlocks(Vec3 pos, float radius_h, float radius_v) {
        double radiusOffsetX = Math.cos(Math.toRadians(this.getYRot())) * (double)radius_h;
        double radiusOffsetZ = Math.sin(Math.toRadians(this.getYRot())) * (double)radius_h;
        Vec3 a = new Vec3(pos.x() + radiusOffsetX, pos.y(), pos.z() + radiusOffsetZ);
        Vec3 b = new Vec3(pos.x() - radiusOffsetX, pos.y(), pos.z() - radiusOffsetZ);
        return this.getAllBlockLocationsBetween(a, b, radius_v);
    }

    protected BlockPos[] getAllBlockLocationsBetween(Vec3 a, Vec3 b, float radius_v) {
        ArrayList allPoints = new ArrayList();
        BlockUtils.stepThroughBlocksLinear(BlockUtils.Vector3dToBlockPosRound(a), BlockUtils.Vector3dToBlockPosRound(b), pos -> {
            int i = 0;
            while ((float)i < radius_v) {
                if (!allPoints.contains(pos.offset(0, i, 0))) {
                    allPoints.add(pos.offset(0, i, 0));
                }
                ++i;
            }
        });
        return allPoints.toArray(new BlockPos[0]);
    }

    private void spawnConeParticles(int count, MAParticleType particle) {
        Vec3 forward = Vec3.directionFromRotation((float)this.getCaster().getXRot(), (float)this.getCaster().getYHeadRot()).scale(0.25);
        Vec3 origin = this.position().add(forward.x, forward.y, forward.z);
        float depth = this.getShapeAttributeByAge(Attribute.DEPTH);
        for (int i = 0; i < count; ++i) {
            Vector3f direction = new Vector3f((float)forward.x, (float)forward.y, (float)forward.z);
            direction.rotate((Quaternionfc)Axis.YP.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
            direction.rotate((Quaternionfc)Axis.XN.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
            direction.rotate((Quaternionfc)Axis.ZN.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
            direction.normalize();
            float speed = (float)(0.5 + Math.random());
            direction.mul(speed);
            this.level().addParticle((ParticleOptions)particle.setMaxAge((int)Math.ceil(depth / speed)), origin.x + (double)direction.x(), origin.y + (double)direction.y(), origin.z + (double)direction.z(), (double)direction.x(), (double)direction.y(), (double)direction.z());
        }
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
        int count = 25;
        this.spawnConeParticles(count, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), (Entity)this.getCaster()));
    }

    @Override
    protected void spawnEarthParticles(SpellRecipe recipe) {
        int count = 25;
        this.spawnConeParticles(count, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)this.getCaster()));
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
        int count = 25;
        if (!lightning) {
            this.spawnConeParticles(count, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)(hellfire ? (ParticleType)ParticleInit.HELLFIRE.get() : (ParticleType)ParticleInit.FLAME.get())), (Entity)this.getCaster()));
        } else {
            Vec3 forward = Vec3.directionFromRotation((float)this.getCaster().getXRot(), (float)this.getCaster().getYHeadRot()).scale(0.25);
            Vec3 origin = this.position().add(forward.x, forward.y, forward.z);
            float depth = this.getShapeAttributeByAge(Attribute.DEPTH);
            for (int i = 0; i < 5; ++i) {
                Vector3f direction = new Vector3f((float)forward.x, (float)forward.y, (float)forward.z);
                direction.rotate((Quaternionfc)Axis.YP.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
                direction.rotate((Quaternionfc)Axis.XN.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
                direction.rotate((Quaternionfc)Axis.ZN.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
                direction.normalize();
                direction.mul(depth);
                this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)this.getCaster()).setMaxAge(1 + (int)depth * 2), origin.x, origin.y, origin.z, origin.x + (double)direction.x(), origin.y + (double)direction.y(), origin.z + (double)direction.z());
            }
        }
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean frost) {
        int count = 25;
        this.spawnConeParticles(count, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)(frost ? (ParticleType)ParticleInit.FROST.get() : (ParticleType)ParticleInit.WATER.get())), (Entity)this.getCaster()));
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
        int count = 25;
        if (blood) {
            this.spawnConeParticles(2, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setColor(182, 28, 28).setScale(0.25f), (Entity)this.getCaster()));
            this.spawnConeParticles(count, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD), (Entity)this.getCaster()));
        } else {
            this.spawnConeParticles(count, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get())), (Entity)this.getCaster()));
        }
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
        int count = 25;
        this.spawnConeParticles(count, recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), (Entity)this.getCaster()));
    }
}

