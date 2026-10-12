 
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
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.base.ChanneledSpellEntity;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class SpellFocus
extends ChanneledSpellEntity {
    public SpellFocus(EntityType<? extends SpellFocus> entityType, Level world) {
        super(entityType, world);
    }

    public SpellFocus(LivingEntity caster, ISpellDefinition spell, Level world, Vec3 position) {
        super((EntityType<? extends ChanneledSpellEntity>)((EntityType)EntityInit.SPELL_FOCUS.get()), caster, spell, world);
        this.setPos(position);
    }

    @Override
    public void tick() {
        if (this.getCaster() == null) {
            if (!this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        MNACap.get(this.getCaster(), PlayerMagicProvider.MAGIC).ifPresent(m -> {
            Vec3 pos = this.getCaster().getEyePosition().add(this.getCaster().getForward().normalize().scale((double)m.getFocusDistance()));
            ClipContext ctx = new ClipContext(this.getCaster().getEyePosition(), pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this);
            BlockHitResult bhr = this.level().clip(ctx);
            if (bhr.getType() == HitResult.Type.BLOCK) {
                pos = bhr.getLocation();
            }
            this.setPos(pos.x, pos.y, pos.z);
        });
        super.tick();
    }

    @Override
    protected int getApplicationRate() {
        return 1;
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
            if (radius > 0) {
                for (int i = -radius; i <= radius; ++i) {
                    for (int j = -radius; j <= radius; ++j) {
                        for (int k = -radius; k <= radius; ++k) {
                            BlockPos adjusted = this.blockPosition().offset(i, j, k);
                            results.putAll(SpellCaster.ApplyComponents(recipe, source, new SpellTarget(adjusted, face).doNotOffsetFace(), context));
                        }
                    }
                }
            }
        }
        if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
            float radius = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RADIUS);
            List targets = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)radius), e -> !e.isInvulnerable() && e.isAlive() && e instanceof LivingEntity);
            for (Entity target : (java.util.List<Entity>)targets) {
                if (target == null || target == caster) continue;
                results.putAll(SpellCaster.ApplyComponents(recipe, source, new SpellTarget(target), context));
            }
        }
        if (results.size() > 0) {
            List<SpellEffect> appliedEffects = results.entrySet().stream().map(e -> e.getValue() == ComponentApplicationResult.SUCCESS ? (SpellEffect)e.getKey() : null).filter(e -> e != null).collect(Collectors.toList());
            SpellCaster.spawnClientFX(this.level(), this.position(), Vec3.ZERO, source, appliedEffects);
        }
    }

    private void spawnParticle(MAParticleType particle) {
        float radius = Math.max(1.0f, ((ModifiedSpellPart)this.getSpell().getShape()).getValue(Attribute.RADIUS));
        Vec3 end = this.position();
        int i = 0;
        while ((float)i < 3.0f * radius + 1.0f) {
            Vec3 start = this.position().add((double)(-radius) + (double)(2.0f * radius) * Math.random(), (double)(-radius) + (double)(2.0f * radius) * Math.random(), (double)(-radius) + (double)(2.0f * radius) * Math.random());
            this.level().addParticle((ParticleOptions)particle, start.x, start.y, start.z, end.x, end.y, end.z);
            ++i;
        }
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
        this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_LERP.get())).setScale(0.05f).setColor(10, 10, 10), (Entity)this.getCaster()));
    }

    @Override
    protected void spawnEarthParticles(SpellRecipe recipe) {
        this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST_LERP.get())), (Entity)this.getCaster()));
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
        if (lightning) {
            this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)this.getCaster()));
        } else if (hellfire) {
            this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())).setMover(new ParticleVelocityMover(0.0, 0.1f, 0.0, true)), (Entity)this.getCaster()));
        } else {
            this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_LERP.get())), (Entity)this.getCaster()));
        }
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean ice) {
        this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER_LERP.get())), (Entity)this.getCaster()));
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
        if (blood) {
            Vec3 pos = this.position();
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setColor(192, 28, 28).setGravity(0.0f).setScale(0.25f).setMaxAge(8), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
            this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET_LERP.get())).setColor(Affinity.BLOOD).setMaxAge(7), (Entity)this.getCaster()));
        } else {
            this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)this.getCaster()));
        }
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
        this.spawnParticle(recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_LERP.get())), (Entity)this.getCaster()));
    }
}

