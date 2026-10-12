/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.sorcery.targeting;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.spells.targeting.SpellTargetHelper;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.base.ChanneledSpellEntity;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.BlockUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.LineSegment;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpellBeam
extends ChanneledSpellEntity {
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    private boolean lastTickMissed = true;
    private Vec3 lastTickImpact = null;
    private Vec3 lastTickTarget = null;
    private BlockPos lastTickBlockPos = null;
    private Direction lastTickBlockFace = Direction.UP;

    public SpellBeam(EntityType<? extends SpellBeam> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public SpellBeam(LivingEntity caster, ISpellDefinition spell, Level world) {
        super((EntityType<? extends ChanneledSpellEntity>)((EntityType)EntityInit.SPELL_BEAM.get()), caster, spell, world);
        this.setPos(caster.getX(), caster.getY(), caster.getZ());
        this.setRot(this.getCaster().getYRot(), this.getCaster().getXRot());
    }

    @Override
    public void tick() {
        LivingEntity caster = this.getCaster();
        SpellRecipe recipe = this.getSpell();
        if (caster != null && recipe != null && recipe.isValid()) {
            List<Affinity> pfx_affs;
            float range = this.getShapeAttributeByAge(Attribute.RANGE);
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Vec3 newPos = caster.position().add(0.0, (double)(caster.getEyeHeight() - 0.1f), 0.0);
            Pair<Vec3, Vec3> lookCross = MathUtils.getLookCross(caster);
            HumanoidArm casterHandedness = caster.getMainArm();
            newPos = casterHandedness == HumanoidArm.RIGHT && caster.getUsedItemHand() == InteractionHand.MAIN_HAND || casterHandedness == HumanoidArm.LEFT && caster.getUsedItemHand() == InteractionHand.OFF_HAND ? newPos.add(((Vec3)lookCross.getSecond()).scale((double)0.1f)) : newPos.add(((Vec3)lookCross.getSecond()).scale((double)-0.1f));
            this.setPos(newPos.x, newPos.y, newPos.z);
            this.setXRot(caster.getXRot());
            this.xRotO = caster.xRotO;
            this.setYRot(caster.yHeadRot);
            this.yRotO = caster.yHeadRotO;
            HitResult result = SpellTargetHelper.rayTrace(this, this.level(), this.position(), caster.getLookAngle(), true, false, ClipContext.Block.COLLIDER, entity -> entity.isPickable() && entity.isAlive() && entity != caster, caster.getBoundingBox().inflate((double)range, (double)range, (double)range), range);
            this.lastTickImpact = result.getLocation();
            if (result.getType() == HitResult.Type.BLOCK) {
                this.lastTickBlockPos = ((BlockHitResult)result).getBlockPos();
                this.lastTickBlockFace = ((BlockHitResult)result).getDirection();
                this.lastTickTarget = this.lastTickImpact;
            } else if (result.getType() == HitResult.Type.ENTITY) {
                this.lastTickTarget = ((EntityHitResult)result).getEntity().position();
                this.lastTickBlockPos = BlockPos.containing((Position)this.lastTickTarget);
            }
            this.lastTickMissed = result.getType() == HitResult.Type.MISS;
            if (this.level().isClientSide() && (pfx_affs = Arrays.asList(Affinity.FIRE, Affinity.ARCANE, Affinity.HELLFIRE, Affinity.LIGHTNING, Affinity.WIND, Affinity.ENDER, Affinity.ICE)).contains(recipe.getHighestAffinity())) {
                Vec3 target = this.lastTickImpact != null ? this.lastTickImpact : this.getLookAngle().normalize().scale((double)range);
                int[] clr = this.getBeamColor();
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())).setColor(clr[0], clr[1], clr[2]).setScale(0.01f).setMaxAge(5).setAgePadding(5).setGravity(0.02f), this.position().x, this.position().y, this.position().z, target.x, target.y, target.z);
            }
        } else {
            this.lastTickMissed = true;
        }
        super.tick();
    }

    @Override
    protected void applyEffect(ItemStack stack, SpellRecipe recipe, LivingEntity caster, ServerLevel world) {
        if (this.lastTickMissed || caster == null) {
            return;
        }
        IModifiedSpellPart s = recipe.getShape();
        float radius_h = ((ModifiedSpellPart)s).getValue(Attribute.WIDTH) / 2.0f;
        float radius_v = ((ModifiedSpellPart)s).getValue(Attribute.HEIGHT) / 2.0f;
        float radius_d = ((ModifiedSpellPart)s).getValue(Attribute.DEPTH) * Math.max((float)this.tickCount / 20.0f, 1.0f) / 2.0f;
        SpellSource source = new SpellSource(caster, caster.getUsedItemHand());
        List<Entity> entities = this.targetEntities(this.lastTickTarget, radius_h * 2.0f, radius_v * 2.0f, radius_d);
        BlockPos[] blocks = this.targetBlocks(this.lastTickBlockPos, radius_h, radius_v, radius_d);
        SpellContext context = new SpellContext((Level)world, recipe, this);
        for (Entity e : entities) {
            SpellCaster.ApplyComponents(recipe, source, new SpellTarget(e), context);
        }
        for (BlockPos b : blocks) {
            SpellCaster.ApplyComponents(recipe, source, new SpellTarget(b, this.lastTickBlockFace), context);
        }
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
        this.spawnParticles(recipe, (MAParticleType)(ParticleInit.AIR_VELOCITY.get()), 10, 0.1f);
    }

    @Override
    protected void spawnEarthParticles(SpellRecipe recipe) {
        this.spawnParticles(recipe, (MAParticleType)(ParticleInit.DUST.get()), 5, 0.01f);
        float particle_spread = 0.2f;
        this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())).setGravity(0.02f), (Entity)this.getCaster()), this.lastTickImpact.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.lastTickImpact.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.lastTickImpact.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), -0.06 + Math.random() * 0.12, 0.05 + Math.random() * 0.05, -0.06 + Math.random() * 0.12);
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
        if (!lightning) {
            this.spawnParticles(recipe, hellfire ? (MAParticleType)(ParticleInit.HELLFIRE.get()) : (MAParticleType)(ParticleInit.FLAME.get()), 20, 0.01f);
        } else {
            this.spawnLightningParticles(recipe, (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()), 1);
        }
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean frost) {
        this.spawnParticles(recipe, frost ? (MAParticleType)(ParticleInit.FROST.get()) : (MAParticleType)(ParticleInit.WATER.get()), 10, 0.01f);
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
        if (blood) {
            this.spawnParticles(recipe, (MAParticleType)(ParticleInit.DROPLET.get()), 10, 0.01f, p -> {
                p.setColor(Affinity.BLOOD);
                p.setPhysics(true);
                p.setGravity(0.05f);
                p.setMaxAge(5);
            });
        } else {
            this.spawnParticles(recipe, (MAParticleType)(ParticleInit.ENDER_VELOCITY.get()), 10, 0.01f);
        }
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
        this.spawnParticles(recipe, (MAParticleType)(ParticleInit.ARCANE.get()), 10, 0.01f);
    }

    private void spawnParticles(SpellRecipe recipe, MAParticleType type, int amount, float velocity) {
        this.spawnParticles(recipe, type, amount, velocity, null);
    }

    private void spawnParticles(SpellRecipe recipe, MAParticleType type, int amount, float velocity, Consumer<MAParticleType> particleAdjuster) {
        if (this.lastTickMissed) {
            return;
        }
        float particle_spread = 0.2f;
        for (int i = 0; i < amount; ++i) {
            MAParticleType particle = new MAParticleType(type);
            if (particleAdjuster != null) {
                particleAdjuster.accept(particle);
            }
            this.level().addParticle((ParticleOptions)recipe.colorParticle(particle, (Entity)this.getCaster()), this.lastTickImpact.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.lastTickImpact.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.lastTickImpact.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), (double)(-velocity) + Math.random() * (double)velocity * 2.0, (double)(-velocity) + Math.random() * (double)velocity * 2.0, (double)(-velocity) + Math.random() * (double)velocity * 2.0);
        }
    }

    private void spawnLightningParticles(SpellRecipe recipe, MAParticleType type, int amount) {
        if (this.lastTickMissed) {
            return;
        }
        float particle_spread = 0.5f;
        for (int i = 0; i < amount; ++i) {
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType(type), (Entity)this.getCaster()), this.lastTickImpact.x, this.lastTickImpact.y, this.lastTickImpact.z, this.lastTickImpact.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.lastTickImpact.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.lastTickImpact.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0));
        }
    }

    protected BlockPos[] targetBlocks(BlockPos impactPoint, float radius_h, float radius_v, float radius_d) {
        BlockPos projectedDepthPoint = BlockUtils.Vector3dToBlockPosRound(this.getLookAngle().normalize().scale((double)radius_d).add((double)impactPoint.getX(), (double)impactPoint.getY(), (double)impactPoint.getZ()));
        return this.getAllBlockLocationsDepth(impactPoint, projectedDepthPoint, radius_h, radius_v);
    }

    private BlockPos[] getAllBlockLocationsDepth(BlockPos startPoint, BlockPos endPoint, float radius_h, float radius_v) {
        double radiusOffsetX = Math.cos(Math.toRadians(this.getYRot())) * (double)radius_h;
        double radiusOffsetZ = Math.sin(Math.toRadians(this.getYRot())) * (double)radius_h;
        ArrayList vecList = new ArrayList();
        BlockUtils.stepThroughBlocksLinear(startPoint, endPoint, pos -> {
            BlockPos[] positions;
            Vec3 a = new Vec3((double)pos.getX() + radiusOffsetX, (double)pos.getY(), (double)pos.getZ() + radiusOffsetZ);
            Vec3 b = new Vec3((double)pos.getX() - radiusOffsetX, (double)pos.getY(), (double)pos.getZ() - radiusOffsetZ);
            for (BlockPos position : positions = this.getAllBlockLocationsHorizontal(BlockUtils.Vector3dToBlockPosRound(a), BlockUtils.Vector3dToBlockPosRound(b))) {
                int y = -((int)Math.floor(radius_v));
                while ((double)y <= Math.floor(radius_v)) {
                    BlockPos vOffset = position.offset(0, y, 0);
                    if (!vecList.contains(vOffset)) {
                        vecList.add(vOffset);
                    }
                    ++y;
                }
            }
        });
        return (BlockPos[])((vecList).toArray(new BlockPos[0]));
    }

    private BlockPos[] getAllBlockLocationsHorizontal(BlockPos p1, BlockPos p2) {
        ArrayList allPoints = new ArrayList();
        BlockUtils.stepThroughBlocksLinear(p1, p2, pos -> {
            if (!allPoints.contains(pos)) {
                allPoints.add(pos);
            }
        });
        return (BlockPos[])((allPoints).toArray(new BlockPos[0]));
    }

    public int[] getBeamColor() {
        SpellRecipe recipe = this.getSpell();
        if (recipe == null) {
            return new int[]{255, 255, 255};
        }
        int override = this.getOverrideColor();
        if (override == -1) {
            return recipe.getHighestAffinity().getColor();
        }
        return new int[]{FastColor.ARGB32.red((int)override), FastColor.ARGB32.green((int)override), FastColor.ARGB32.blue((int)override)};
    }

    protected List<Entity> targetEntities(Vec3 impactPoint, float width, float height, float depth) {
        Vec3 forward = this.getLookAngle().normalize();
        Vec3 center = impactPoint.add(forward.scale((double)(depth / 2.0f)));
        AABB bb = new AABB(BlockPos.containing((Position)center)).inflate((double)Math.max(width, Math.max(height, depth)));
        List possibleTargets = this.level().getEntities((Entity)this.getCaster(), bb);
        ArrayList<Entity> targets = new ArrayList<Entity>();
        float r_width = width / 2.0f;
        float r_height = height / 2.0f;
        float r_depth = depth / 2.0f;
        Vec3 depth_a = impactPoint;
        Vec3 depth_b = impactPoint.add(forward.scale((double)depth));
        Vec3 horizontal_a = center.add(forward.cross(UP).scale((double)r_width));
        Vec3 horizontal_b = center.add(forward.cross(UP).scale((double)(-r_width)));
        LineSegment line_horizontal = new LineSegment(horizontal_a, horizontal_b);
        LineSegment line_depth = new LineSegment(depth_a, depth_b);
        for (Entity e : possibleTargets) {
            if (e == this || e == this.getCaster()) continue;
            Vec3 target = e.position();
            Vec3 closest_horizontal = line_horizontal.closestPointOnLine(target);
            Vec3 closest_depth = line_depth.closestPointOnLine(target);
            target = target.subtract(0.0, target.y, 0.0);
            closest_horizontal = closest_horizontal.subtract(0.0, closest_horizontal.y, 0.0);
            closest_depth = closest_depth.subtract(0.0, closest_depth.y, 0.0);
            double depthDist = Math.abs(closest_horizontal.distanceTo(target));
            double heightDist = Math.abs(center.y - e.getY());
            double widthDist = closest_depth.distanceTo(target);
            if (!(widthDist >= (double)(-r_width)) || !(widthDist <= (double)r_width) || !(heightDist >= (double)(-r_height)) || !(heightDist <= (double)r_height) || !(depthDist >= (double)(-r_depth)) || !(depthDist <= (double)r_depth)) continue;
            targets.add(e);
        }
        return targets;
    }

    public Vec3 getLastTickImpact() {
        return this.lastTickImpact;
    }
}

