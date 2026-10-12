 
package com.mna.entities.sorcery.targeting;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.entities.ISpellInteractibleEntity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedSpellEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class Smite
extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(Smite.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final Vec3 DOWN = new Vec3(0.0, -1.0, 0.0);
    private List<Entity> hitEntities;
    public UUID shootingEntity;
    private int ticksInAir;
    private ArrayList<Affinity> affinities;
    private SpellRecipe _cachedSpellRecipe = null;
    private float bonusDamagePctFromHeight = 0.0f;
    private HashMap<Affinity, BiConsumer<Integer, Float>> particleFunctions = new HashMap();

    public Smite(EntityType<? extends Smite> type, Level world) {
        super((EntityType)EntityInit.SMITE_PROJECTILE.get(), world);
        this.setNoGravity(true);
        this.particleFunctions.put(Affinity.ARCANE, this::spawnArcaneParticles);
        this.particleFunctions.put(Affinity.ENDER, this::spawnEnderParticles);
        this.particleFunctions.put(Affinity.FIRE, this::spawnFireParticles);
        this.particleFunctions.put(Affinity.HELLFIRE, this::spawnHellfireParticles);
        this.particleFunctions.put(Affinity.LIGHTNING, this::spawnLightningParticles);
        this.particleFunctions.put(Affinity.WATER, this::spawnWaterParticles);
        this.particleFunctions.put(Affinity.ICE, this::spawnFrostParticles);
        this.particleFunctions.put(Affinity.WIND, this::spawnWindParticles);
        this.particleFunctions.put(Affinity.EARTH, this::spawnEarthParticles);
        this.particleFunctions.put(Affinity.BLOOD, this::spawnBloodParticles);
    }

    public Smite(Level worldIn, Vec3 position, CompoundTag recipe, LivingEntity shooter) {
        super((EntityType)EntityInit.SMITE_PROJECTILE.get(), worldIn);
        this.setPos(position.x(), position.y(), position.z());
        this.setNoGravity(true);
        this.setOwner((Entity)shooter);
        this.setSpellRecipe(recipe);
    }

    public ArrayList<Affinity> getAffinity() {
        if (this.affinities == null) {
            SpellRecipe recipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE));
            this.affinities = new ArrayList<Affinity>(recipe.getAffinity().keySet());
        }
        return this.affinities;
    }

    public void setBonusDamagePctFromHeight(float bonus) {
        this.bonusDamagePctFromHeight = bonus;
    }

    public void spawnParticles(int particleCount, float partialTick) {
        Affinity aff;
        if (this.affinities == null) {
            SpellRecipe recipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE));
            this.affinities = new ArrayList<Affinity>(recipe.getAffinity().keySet());
        }
        if (this.affinities != null && this.particleFunctions.containsKey((aff = this.affinities.get((int)(Math.random() * (double)this.affinities.size()))))) {
            this.particleFunctions.get(aff).accept(particleCount, Float.valueOf(partialTick));
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(SPELL_RECIPE, new CompoundTag());
    }

    public void setSpellRecipe(CompoundTag recipe) {
        this.entityData.set(SPELL_RECIPE, recipe);
    }

    protected void entityHit(Entity entity) {
        this.applyEffect(this.blockPosition(), this.getDirection(), entity);
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    protected SpellRecipe getRecipe() {
        if (this._cachedSpellRecipe == null) {
            this._cachedSpellRecipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE));
        }
        return this._cachedSpellRecipe;
    }

    protected void blockHit(BlockPos pos, Direction face) {
        this.applyEffect(pos, Direction.DOWN, null);
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    private void applyEffect(BlockPos impactPoint, Direction face, @Nullable Entity hitEntity) {
        if (this.getShooterAsLiving() == null) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        SpellSource source = new SpellSource(this.getShooterAsLiving(), InteractionHand.MAIN_HAND);
        SpellRecipe recipe = this.getRecipe();
        if (recipe.isValid() && !this.level().isClientSide()) {
            int vRadius;
            BlockState state = this.level().getBlockState(impactPoint);
            if (state.getBlock() instanceof ISpellInteractibleBlock && ((ISpellInteractibleBlock)state.getBlock()).onHitBySpell(this.level(), impactPoint, recipe)) {
                this.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            if (hitEntity != null && hitEntity instanceof ISpellInteractibleEntity && ((ISpellInteractibleEntity)hitEntity).onShapeTarget(recipe, source)) {
                this.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            float spellRadius = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RADIUS);
            float spellVerticalRadius = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.HEIGHT);
            SpellContext context = new SpellContext(this.level(), recipe, (Entity)this);
            int fallTicks = Math.min(Math.max(this.ticksInAir - 2, 0), 20);
            float dmgBoost = (float)fallTicks * 0.03f * (1.0f + this.bonusDamagePctFromHeight);
            recipe.iterateComponents(c -> {
                if (((SpellEffect)c.getPart()).getModifiableAttributes().stream().anyMatch(a -> a.getAttribute() == Attribute.DAMAGE)) {
                    float base = c.getValue(Attribute.DAMAGE);
                    c.setValue(Attribute.DAMAGE, base + base * dmgBoost);
                }
            });
            int radius = (int)Math.floor(spellRadius);
            if (radius < 0) {
                radius = 0;
            }
            if ((vRadius = (int)Math.floor(spellVerticalRadius) - 1) < 0) {
                vRadius = 0;
            }
            if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks())) {
                int delay = 1;
                if (radius > 0 || hitEntity == null) {
                    Spiral s = new Spiral(radius * 2, radius * 2);
                    for (Point p : s.spiral()) {
                        for (int j = vRadius; j >= -vRadius; --j) {
                            BlockPos adjusted = impactPoint.offset(p.x, j, p.y);
                            int loopDelay = delay;
                            recipe.iterateComponents(c -> {
                                if (((SpellEffect)c.getPart()).targetsBlocks() && !context.hasBlockBeenAffected((SpellEffect)c.getPart(), adjusted)) {
                                    DelayedEventQueue.pushEvent(this.level(), new TimedDelayedSpellEffect("smiteLoop", (int)((float)loopDelay + c.getValue(Attribute.DELAY) * 20.0f), source, new SpellTarget(adjusted, face), (IModifiedSpellPart<SpellEffect>)c, context, false));
                                }
                            });
                        }
                        ++delay;
                    }
                }
            }
            if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
                List targets = this.level().getEntities((Entity)this, new AABB(impactPoint).expandTowards((double)spellRadius, (double)spellVerticalRadius, (double)spellRadius).expandTowards((double)(-spellRadius), 0.0, (double)(-spellRadius)), e -> !e.isInvulnerable());
                if (hitEntity != null && !targets.contains(hitEntity)) {
                    targets.add(hitEntity);
                }
                for (Entity target : (java.util.List<Entity>)targets) {
                    if (target == null || target == this.getShooterAsLiving()) continue;
                    SpellCaster.ApplyComponents(recipe, source, new SpellTarget(target).doNotOffsetFace(), context);
                }
            }
            switch (recipe.getHighestAffinity()) {
                case ARCANE: {
                    this.level().playSound(null, impactPoint, SFX.Spell.Impact.AoE.ARCANE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    this.level().playSound(null, impactPoint, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.0f);
                    break;
                }
                case EARTH: {
                    this.level().playSound(null, impactPoint, SFX.Spell.Impact.AoE.EARTH, SoundSource.PLAYERS, 1.0f, 1.0f);
                    break;
                }
                case ENDER: {
                    this.level().playSound(null, impactPoint, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.0f);
                    break;
                }
                case FIRE: 
                case HELLFIRE: {
                    this.level().playSound(null, impactPoint, SFX.Spell.Impact.AoE.EARTH, SoundSource.PLAYERS, 0.5f, 1.0f);
                    this.level().playSound(null, impactPoint, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    break;
                }
                case LIGHTNING: {
                    break;
                }
                case WATER: {
                    this.level().playSound(null, impactPoint, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.0f, 1.0f);
                    break;
                }
                case ICE: {
                    this.level().playSound(null, impactPoint, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                    this.level().playSound(null, impactPoint, SoundEvents.SNOW_PLACE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    break;
                }
                case WIND: {
                    this.level().playSound(null, impactPoint, SFX.Spell.Impact.AoE.WIND, SoundSource.PLAYERS, 1.0f, 1.0f);
                    break;
                }
            }
        }
    }

    protected void onHit(HitResult raytraceResultIn) {
        HitResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)raytraceResultIn);
        } else if (raytraceresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockraytraceresult = (BlockHitResult)raytraceResultIn;
            this.blockHit(blockraytraceresult.getBlockPos(), blockraytraceresult.getDirection());
        }
    }

    public void onRemovedFromLevel() {
        SpellRecipe recipe;
        super.onRemovedFromLevel();
        if (this.level().isClientSide() && (recipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE))).isValid()) {
            this.spawnParticleBurst("", Float.valueOf(((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RADIUS)));
        }
    }

    private LivingEntity getShooterAsLiving() {
        Entity shooter = this.getShooter();
        if (shooter != null && shooter instanceof LivingEntity) {
            return (LivingEntity)shooter;
        }
        return null;
    }

    public boolean isAttackable() {
        return false;
    }

    protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return 0.0f;
    }

    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
    }

    @Nullable
    public Entity getShooter() {
        return this.shootingEntity != null && this.level() instanceof ServerLevel ? ((ServerLevel)this.level()).getEntity(this.shootingEntity) : null;
    }

    public void setOwner(@Nullable Entity entityIn) {
        this.shootingEntity = entityIn == null ? null : entityIn.getUUID();
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        if (this.shootingEntity != null) {
            compound.putUUID("OwnerUUID", this.shootingEntity);
        }
        compound.putFloat("bonusFallDamagePct", this.bonusDamagePctFromHeight);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("OwnerUUID")) {
            this.shootingEntity = compound.getUUID("OwnerUUID");
        }
        if (compound.contains("bonusFallDamagePct")) {
            this.bonusDamagePctFromHeight = compound.getFloat("bonusFallDamagePct");
        }
    }

    @Nullable
    protected EntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec) {
        return ProjectileUtil.getEntityHitResult((Level)this.level(), (Entity)this, (Vec3)startVec, (Vec3)endVec, (AABB)this.getBoundingBox().expandTowards(DOWN).inflate(1.0), p_213871_1_ -> !p_213871_1_.isSpectator() && p_213871_1_.isAlive() && p_213871_1_.isPickable() && (p_213871_1_ != this.getShooter() || this.ticksInAir >= 5));
    }

    protected void onHitEntity(EntityHitResult p_213868_1_) {
        Entity entity = p_213868_1_.getEntity();
        this.entityHit(entity);
        if (!entity.isAlive() && this.hitEntities != null) {
            this.hitEntities.add(entity);
        }
    }

    public void tick() {
        SpellRecipe recipe;
        if (this.tickCount == 3 && !this.level().isClientSide() && (recipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE))) != null && recipe.isValid() && recipe.getHighestAffinity() == Affinity.LIGHTNING) {
            this.level().playSound(null, this.blockPosition(), SFX.Spell.Impact.AoE.LIGHTNING, SoundSource.PLAYERS, 0.5f, (float)(0.6 + Math.random() * 0.8));
        }
        if (this.isInWaterOrRain()) {
            this.clearFire();
        }
        ++this.ticksInAir;
        Vec3 Vector3d2 = this.position();
        Vec3 Vector3d3 = Vector3d2.add(DOWN);
        net.minecraft.world.phys.HitResult raytraceresult = this.level().clip(new ClipContext(Vector3d2, Vector3d3, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)this));
        if (raytraceresult.getType() != HitResult.Type.MISS) {
            Vector3d3 = raytraceresult.getLocation();
        }
        while (this.isAlive()) {
            EntityHitResult entityraytraceresult = this.rayTraceEntities(Vector3d2, Vector3d3);
            if (entityraytraceresult != null) {
                raytraceresult = entityraytraceresult;
            }
            if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult)raytraceresult).getEntity();
                Entity entity1 = this.getShooter();
                if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                    raytraceresult = null;
                    entityraytraceresult = null;
                }
            }
            if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS && true) {
                this.onHit((HitResult)raytraceresult);
                this.hasImpulse = true;
            }
            if (entityraytraceresult == null) break;
            raytraceresult = null;
        }
        float speedFactor = 0.99f;
        if (this.isInWater()) {
            if (this.affinities.contains(Affinity.FIRE)) {
                if (!this.level().isClientSide()) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                }
                return;
            }
            for (int j = 0; j < 4; ++j) {
                this.level().addParticle((ParticleOptions)ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
            if (!this.affinities.contains(Affinity.WATER)) {
                speedFactor = 0.6f;
            }
        }
        Vec3 newPos = this.position().add(DOWN.scale((double)speedFactor));
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.setPos(newPos.x, newPos.y, newPos.z);
        this.checkInsideBlocks();
        if (this.tickCount > 200) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void lerpMotion(double x, double y, double z) {
    }

    @OnlyIn(value=Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    @OnlyIn(value=Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(d0)) {
            d0 = 1.0;
        }
        return distance < (d0 = d0 * 64.0 * Smite.getViewScale()) * d0;
    }

    public final int getOverrideColor() {
        Entity caster = this.getShooter();
        SpellRecipe recipe = this.getRecipe();
        if (caster == null || recipe == null) {
            return -1;
        }
        MutableInt color = new MutableInt(-1);
        MNACap.get(caster, PlayerMagicProvider.MAGIC).ifPresent(m -> color.setValue(m.getParticleColorOverride()));
        if (color.getValue() == -1) {
            color.setValue(recipe.getParticleColorOverride());
        }
        return color.getValue();
    }

    public void spawnParticleBurst(String identifier, Float radius) {
        if (this.affinities == null || this.affinities.size() == 0) {
            return;
        }
        MAParticleType particle = null;
        int count = 180;
        double angleRads = 0.0;
        double step = Math.PI * 2 / (double)count;
        block12: for (int i = 0; i < count; ++i) {
            Vec3 vel;
            Vec3 dir;
            angleRads += step;
            Affinity affinity = this.affinities.get((int)Math.random() * this.affinities.size());
            switch (affinity) {
                case ARCANE: {
                    particle = (MAParticleType)(ParticleInit.ARCANE.get());
                    break;
                }
                case EARTH: {
                    particle = (MAParticleType)(ParticleInit.DUST.get());
                    break;
                }
                case ENDER: {
                    dir = new Vec3(Math.cos(angleRads), Math.random() * 0.1, Math.sin(angleRads)).normalize().scale((double)(radius.floatValue() * 5.0f)).add(this.position());
                    this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)this.getShooterAsLiving()), dir.x, dir.y, dir.z, this.getX(), this.getY() + 0.25, this.getZ());
                    continue block12;
                }
                case FIRE: {
                    particle = (MAParticleType)(ParticleInit.FLAME.get());
                    break;
                }
                case HELLFIRE: {
                    particle = (MAParticleType)(ParticleInit.HELLFIRE.get());
                    break;
                }
                case LIGHTNING: {
                    dir = new Vec3(Math.cos(angleRads), (double)0.1f, Math.sin(angleRads)).normalize().scale((double)(radius.floatValue() * 5.0f)).add(this.position());
                    this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)this.getShooterAsLiving()), this.getX(), this.getY() + 1.0, this.getZ(), dir.x, dir.y + 1.0, dir.z);
                    i += count / 10;
                    angleRads += step * (double)(count / 10);
                    continue block12;
                }
                case WATER: {
                    particle = (MAParticleType)(ParticleInit.WATER.get());
                    break;
                }
                case ICE: {
                    particle = (MAParticleType)(ParticleInit.FROST.get());
                    break;
                }
                case WIND: {
                    dir = new Vec3(Math.cos(angleRads), Math.random() * 0.1, Math.sin(angleRads)).normalize();
                    vel = dir.scale((double)(0.3f * radius.floatValue()));
                    dir = dir.scale(0.2);
                    this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), (Entity)this.getShooterAsLiving()), this.getX() + dir.x, this.getY() + Math.random(), this.getZ() + dir.z, vel.x, vel.y, vel.z);
                    break;
                }
                case BLOOD: {
                    dir = new Vec3(Math.cos(angleRads), Math.random() * 0.2, Math.sin(angleRads)).normalize().scale((double)(radius.floatValue() * 0.15f) + 0.05 * Math.random());
                    this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setGravity(0.05f).setMaxAge(16).setPhysics(true), (Entity)this.getShooterAsLiving()), this.getX(), this.getY() + 0.25, this.getZ(), dir.x, dir.y, dir.z);
                    continue block12;
                }
                default: {
                    particle = (MAParticleType)(ParticleInit.SPARKLE_GRAVITY.get());
                }
            }
            if (particle == null) continue;
            dir = new Vec3(Math.cos(angleRads), Math.random() * 0.1, Math.sin(angleRads)).normalize();
            vel = dir.scale((double)(0.3f * radius.floatValue()));
            dir = dir.scale(0.2);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType(particle), (Entity)this.getShooterAsLiving()), this.getX() + dir.x, this.getY() + Math.random(), this.getZ() + dir.z, vel.x, vel.y, vel.z);
        }
    }

    public void spawnArcaneParticles(int particleCount, float partialTick) {
        for (int j = 0; j < particleCount; ++j) {
            Vec3 pos = this.position().add(DOWN.scale((double)partialTick * Math.random()));
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), (Entity)this.getShooterAsLiving()), pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    public void spawnEnderParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(DOWN.scale((double)partialTick * Math.random()));
        float particle_spread = 0.5f;
        for (int j = 0; j < particleCount; ++j) {
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)this.getShooterAsLiving()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + -2.0, pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.x, pos.y, pos.z);
        }
    }

    public void spawnEarthParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(DOWN.scale((double)partialTick * Math.random()));
        float particle_spread = 0.05f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, -0.5, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)this.getShooterAsLiving()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    public void spawnWaterParticles(int particleCount, float partialTick) {
        float particle_spread = 0.05f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 pos = this.position().add(DOWN.scale(Math.random()));
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())), (Entity)this.getShooterAsLiving()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    public void spawnBloodParticles(int particleCount, float partialTick) {
        float particle_spread = 0.25f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 pos = this.position().add(DOWN.scale(Math.random()));
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, 0.0, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setGravity(0.05f).setMaxAge(16).setPhysics(true), (Entity)this.getShooterAsLiving()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    public void spawnFireParticles(int particleCount, float partialTick) {
        for (int j = 0; j < particleCount; ++j) {
            Vec3 pos = this.position().add(0.0, -Math.random(), 0.0);
            Vec3 velocity = new Vec3(0.0, 0.0, 0.0);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), (Entity)this.getShooterAsLiving()), pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    public void spawnHellfireParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(DOWN.scale((double)partialTick * Math.random()));
        float particle_spread = 0.05f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), (Entity)this.getShooterAsLiving()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    public void spawnWindParticles(int particleCount, float partialTick) {
        for (int i = 0; i < particleCount; ++i) {
            Vec3 pos = this.position().add(DOWN.scale((double)partialTick * Math.random()));
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.2f).setColor(10, 10, 10), (Entity)this.getShooterAsLiving()), pos.x, pos.y, pos.z, (double)0.6f * Math.random() + (double)0.1f, (double)-0.2f, 0.25);
        }
    }

    public void spawnFrostParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(DOWN.scale((double)partialTick * Math.random()));
        float particle_spread = 0.05f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), (Entity)this.getShooterAsLiving()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    public void spawnLightningParticles(int particleCount, float partialTick) {
        BlockPos projected_impact = BlockPos.containing((Position)this.position());
        for (int count = 0; this.level().isEmptyBlock(projected_impact) && count < 30; ++count) {
            projected_impact = projected_impact.below();
        }
        this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)this.getShooterAsLiving()), this.getX(), this.getY(), this.getZ(), this.getX(), (double)projected_impact.getY(), this.getZ());
    }

    private static class Spiral {
        private static final Point ORIGIN = new Point(0, 0);
        private final int width;
        private final int height;
        private Point point;
        private Direction direction = Direction.E;
        private List<Point> list = new ArrayList<Point>();

        public Spiral(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public List<Point> spiral() {
            this.point = ORIGIN;
            int steps = 1;
            while (this.list.size() < this.width * this.height) {
                this.advance(steps);
                this.advance(steps);
                ++steps;
            }
            return this.list;
        }

        private void advance(int n) {
            for (int i = 0; i < n; ++i) {
                if (this.inBounds(this.point)) {
                    this.list.add(this.point);
                }
                this.point = this.direction.advance(this.point);
            }
            this.direction = this.direction.next();
        }

        private boolean inBounds(Point p) {
            return Spiral.between(-this.width / 2, this.width / 2, p.x) && Spiral.between(-this.height / 2, this.height / 2, p.y);
        }

        private static boolean between(int low, int high, int n) {
            return low <= n && n <= high;
        }

         
        private static enum Direction {
            E(1, 0){

                @Override
                Direction next() {
                    return N;
                }
            }
            ,
            N(0, 1){

                @Override
                Direction next() {
                    return W;
                }
            }
            ,
            W(-1, 0){

                @Override
                Direction next() {
                    return S;
                }
            }
            ,
            S(0, -1){

                @Override
                Direction next() {
                    return E;
                }
            };

            private int dx;
            private int dy;

            Point advance(Point point) {
                return new Point(point.x + this.dx, point.y + this.dy);
            }

            abstract Direction next();

            private Direction(int dx, int dy) {
                this.dx = dx;
                this.dy = dy;
            }
        }
    }
}


