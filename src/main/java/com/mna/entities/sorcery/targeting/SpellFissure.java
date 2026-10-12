/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.commands.arguments.EntityAnchorArgument$Anchor
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.sorcery.targeting;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.base.ChanneledSpellEntity;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.math.MathUtils;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpellFissure
extends ChanneledSpellEntity {
    public SpellFissure(EntityType<? extends SpellFissure> type, Level world) {
        super(type, world);
    }

    public SpellFissure(LivingEntity caster, ISpellDefinition spell, Level world) {
        super((EntityType<? extends ChanneledSpellEntity>)((EntityType)EntityInit.SPELL_FISSURE.get()), caster, spell, world);
        this.setPos(caster.getX(), caster.getY(), caster.getZ());
    }

    public float getStepHeight() {
        return 2.0f;
    }

    @Override
    public void tick() {
        SpellRecipe recipe = this.getSpell();
        if (!this.level().isClientSide() && !recipe.isValid()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        LivingEntity caster = this.getCaster();
        if (caster == null || !caster.isAlive() || !caster.level().dimension().equals(this.level().dimension())) {
            if (!this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        if (!this.level().isClientSide() && this.tickCount >= this.getMaxAge()) {
            if (!this.level().isClientSide()) {
                this.applyEffect(caster.getUseItem(), recipe, caster, (ServerLevel)this.level());
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        if (caster.getUseItemRemainingTicks() <= 0 || caster.getUseItem().isEmpty()) {
            if (!this.level().isClientSide()) {
                this.applyEffect(caster.getUseItem(), recipe, caster, (ServerLevel)this.level());
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        if (!this.snapToGround()) {
            return;
        }
        this.updateRotation(caster);
        this.setOnGround(true);
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level().isClientSide()) {
            this.spawnParticles();
            this.playSounds();
        }
    }

    private boolean snapToGround() {
        BlockPos curPos = this.blockPosition();
        int count = 0;
        if (this.level().isEmptyBlock(curPos)) {
            do {
                ++count;
                curPos = curPos.below();
                if (this.level().isEmptyBlock(curPos)) continue;
                this.setPos(this.getX(), curPos.getY() + 1, this.getZ());
                return true;
            } while (count < 3);
            this.remove(Entity.RemovalReason.DISCARDED);
            return false;
        }
        return true;
    }

    private void updateRotation(LivingEntity caster) {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 source = caster.getEyePosition();
        Vec3 target = caster.getEyePosition().add(caster.getForward().normalize().scale(64.0));
        ClipContext clipContext = new ClipContext(source, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster);
        BlockHitResult bhr = this.level().clip(clipContext);
        if (bhr.getType() == HitResult.Type.BLOCK) {
            this.lookAt(EntityAnchorArgument.Anchor.FEET, bhr.getLocation());
        } else {
            this.turn(caster.getXRot(), caster.getYRot());
        }
        this.setDeltaMovement(this.getForward().normalize().scale((double)this.getShapeAttribute(Attribute.SPEED)));
    }

    @Override
    protected void applyEffect(ItemStack stack, SpellRecipe recipe, LivingEntity caster, ServerLevel world) {
        float spellRadius = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RADIUS);
        SpellContext context = new SpellContext(this.level(), recipe, this);
        SpellSource source = new SpellSource(caster, InteractionHand.MAIN_HAND);
        Direction face = Direction.UP;
        HashMap<SpellEffect, ComponentApplicationResult> results = new HashMap<SpellEffect, ComponentApplicationResult>();
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks())) {
            int radius = (int)Math.floor(spellRadius);
            if (radius < 0) {
                radius = 0;
            }
            for (int i = -radius; i <= radius; ++i) {
                for (int j = -radius; j <= radius; ++j) {
                    for (int k = -radius; k <= radius; ++k) {
                        BlockPos adjusted = this.blockPosition().below().offset(i, j, k);
                        HashMap<SpellEffect, ComponentApplicationResult> loopRes = SpellCaster.ApplyComponents(recipe, source, new SpellTarget(adjusted, face).doNotOffsetFace(), context);
                        SpellCaster.mergeComponentResults(results, loopRes);
                    }
                }
            }
        }
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
            float radius = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RADIUS);
            List targets = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)radius), e -> !e.isInvulnerable());
            for (Entity target : (java.util.List<Entity>)targets) {
                if (target == null || target == caster) continue;
                HashMap<SpellEffect, ComponentApplicationResult> loopRes = SpellCaster.ApplyComponents(recipe, source, new SpellTarget(target), context);
                SpellCaster.mergeComponentResults(results, loopRes);
            }
        }
        if (results.size() > 0) {
            List<SpellEffect> appliedEffects = results.entrySet().stream().map(e -> ((ComponentApplicationResult)((e.getValue()))).is_success ? (SpellEffect)e.getKey() : null).filter(e -> e != null).collect(Collectors.toList());
            SpellCaster.spawnClientFX(this.level(), this.position(), new Vec3(0.0, 1.0, 0.0), source, appliedEffects);
        }
    }

    @Override
    protected void spawnParticles() {
        block3: {
            Vec3 pos;
            BlockPos belowPos;
            block4: {
                super.spawnParticles();
                belowPos = this.blockPosition().below();
                if (this.level().isEmptyBlock(belowPos)) break block3;
                pos = this.position();
                FluidState flState = this.level().getFluidState(belowPos);
                if (flState.isEmpty()) break block4;
                ParticleOptions particle = flState.getDripParticle();
                if (particle == null) break block3;
                for (int i = 0; i < 2; ++i) {
                    this.level().addParticle(particle, pos.x - 1.0 + 2.0 * Math.random(), pos.y, pos.z - 1.0 + 2.0 * Math.random(), 0.0, 0.0, 0.0);
                }
                break block3;
            }
            BlockState below = this.level().getBlockState(belowPos);
            ItemStack belowStack = new ItemStack((ItemLike)below.getBlock());
            if (belowStack.isEmpty()) {
                belowStack = new ItemStack((ItemLike)Blocks.STONE);
            }
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ITEM.get())).setStack(belowStack).setScale(0.15f).setMover(new ParticleVelocityMover(0.0, 0.2f, 0.0, true)).setGravity(0.05f), pos.x - 1.0 + 2.0 * Math.random(), pos.y, pos.z - 1.0 + 2.0 * Math.random(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
        Vec3 pos = this.position();
        for (int i = 0; i < 5; ++i) {
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setColor(10, 10, 10).setScale(0.1f), (Entity)this.getCaster()), pos.x - 1.0 + 2.0 * Math.random(), pos.y, pos.z - 1.0 + 2.0 * Math.random(), (double)0.1f, (double)0.1f, 1.25);
        }
    }

    @Override
    protected void spawnEarthParticles(SpellRecipe recipe) {
        Vec3 pos = this.position();
        for (int i = 0; i < 5; ++i) {
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)this.getCaster()).setScale(0.1f).setGravity(0.05f), pos.x - 1.0 + 2.0 * Math.random(), pos.y, pos.z - 1.0 + 2.0 * Math.random(), 0.0, (double)0.1f, 0.0);
        }
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
        Vec3 pos = this.position();
        for (int i = 0; i < 5; ++i) {
            Vec3 pStart;
            if (hellfire) {
                pStart = new Vec3(pos.x - 1.0 + 2.0 * Math.random(), pos.y, pos.z - 1.0 + 2.0 * Math.random());
                this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType(((MAParticleType)(ParticleInit.HELLFIRE.get())).setScale(0.1f).setMover(new ParticleOrbitMover(pStart.x, pStart.y, pStart.z, 0.1f, 0.1f, 1.25))), (Entity)this.getCaster()), pStart.x, pStart.y, pStart.z, 0.0, 0.0, 0.0);
                continue;
            }
            if (lightning) {
                pStart = new Vec3(pos.x - 1.0 + 2.0 * Math.random(), pos.y, pos.z - 1.0 + 2.0 * Math.random());
                Vec3 pEnd = pStart.add(0.0, Math.random() + 0.5, 0.0);
                this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)this.getCaster()), pStart.x, pStart.y, pStart.z, pEnd.x, pEnd.y, pEnd.z);
                continue;
            }
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType(((MAParticleType)(ParticleInit.FLAME_ORBIT.get())).setScale(0.1f)), (Entity)this.getCaster()), pos.x - 1.0 + 2.0 * Math.random(), pos.y, pos.z - 1.0 + 2.0 * Math.random(), (double)0.1f, (double)0.1f, 1.25);
        }
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean ice) {
        Vec3 pos = this.position();
        for (int i = 0; i < 5; ++i) {
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)(ice ? (ParticleType)ParticleInit.FROST.get() : (ParticleType)ParticleInit.WATER.get())), (Entity)this.getCaster()), pos.x - 0.5 + Math.random(), pos.y + Math.random() * 0.1, pos.z - 0.5 + Math.random(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
        Vec3 pos = this.position();
        for (int i = 0; i < 5; ++i) {
            if (blood) {
                Vec3 velocity = new Vec3((double)MathUtils.RandomBetween(-0.05f, 0.05f), (double)MathUtils.RandomBetween(0.15f, 0.25f), (double)MathUtils.RandomBetween(-0.05f, 0.05f));
                this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setPhysics(true).setGravity(0.05f), (Entity)this.getCaster()), pos.x - 0.5 + Math.random(), pos.y + Math.random() * 0.1, pos.z - 0.5 + Math.random(), velocity.x, velocity.y, velocity.z);
                continue;
            }
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get())), (Entity)this.getCaster()), pos.x - 0.5 + Math.random(), pos.y + Math.random() * 0.1, pos.z - 0.5 + Math.random(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
        Vec3 pos = this.position();
        for (int i = 0; i < 2; ++i) {
            this.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get())), (Entity)this.getCaster()), pos.x - 0.5 + Math.random(), pos.y + Math.random() * 0.1, pos.z - 0.5 + Math.random(), 0.0, 0.0, 0.0);
        }
    }
}

