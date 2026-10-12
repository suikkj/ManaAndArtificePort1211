 
package com.mna.entities.sorcery.targeting;

import com.google.common.collect.Lists;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.entities.ISpellInteractibleEntity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.Odin;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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

public class SpellProjectile
extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> SPECIAL_RENDER = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> SPECIAL_RENDER_PARAM = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> FORCED_AFFINITY = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> FORCED_DAMAGE = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DISABLE_WATER_CHECK = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HOMING_STRENGTH = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VELOCITY = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SpellProjectile.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static ItemStack[] halloween_stacks = new ItemStack[]{new ItemStack((ItemLike)Blocks.ZOMBIE_HEAD), new ItemStack((ItemLike)Blocks.CREEPER_HEAD), new ItemStack((ItemLike)Blocks.SKELETON_SKULL), new ItemStack((ItemLike)Blocks.WITHER_SKELETON_SKULL), new ItemStack((ItemLike)Blocks.CARVED_PUMPKIN), new ItemStack((ItemLike)Blocks.JACK_O_LANTERN)};
    private IntOpenHashSet piercedEntities;
    private List<Entity> hitEntities;
    public UUID shootingEntity;
    private int ticksInAir;
    private ArrayList<Affinity> affinities;
    private Optional<Integer> forcedTargetID = Optional.empty();
    private boolean trackForcedTarget = false;
    private float trackingSpeed = 0.0f;
    private SpellRecipe _cachedSpellRecipe = null;
    private LivingEntity target;
    private HashMap<Affinity, BiConsumer<Integer, Float>> particleFunctions = new HashMap();
    private HashMap<Affinity, BiConsumer<Integer, Float>> impactFunctions = new HashMap();

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level world) {
        super((EntityType)EntityInit.SPELL_PROJECTILE.get(), world);
        this.setNoGravity(true);
        this.forcedTargetID = Optional.empty();
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
        this.impactFunctions.put(Affinity.WATER, this::spawnWaterImpactParticles);
        this.impactFunctions.put(Affinity.FIRE, this::spawnFireImpactParticles);
        this.impactFunctions.put(Affinity.HELLFIRE, this::spawnFireImpactParticles);
        this.impactFunctions.put(Affinity.EARTH, this::spawnEarthImpactParticles);
        this.impactFunctions.put(Affinity.WIND, this::spawnWindImpactParticles);
        this.impactFunctions.put(Affinity.ARCANE, this::spawnArcaneImpactParticles);
        this.impactFunctions.put(Affinity.ENDER, this::spawnEnderImpactParticles);
        this.impactFunctions.put(Affinity.ICE, this::spawnFrostImpactParticles);
        this.impactFunctions.put(Affinity.BLOOD, this::spawnBloodImpactParticles);
    }

    public SpellProjectile(LivingEntity shooter, Level worldIn) {
        this(worldIn, shooter.getX(), shooter.getEyeY() - (double)0.1f, shooter.getZ());
        this.setOwner((Entity)shooter);
    }

    public SpellProjectile(Level worldIn, double x, double y, double z) {
        super((EntityType)EntityInit.SPELL_PROJECTILE.get(), worldIn);
        this.setPos(x, y, z);
        this.setNoGravity(true);
    }

    public ArrayList<Affinity> getAffinity() {
        if (this.affinities == null) {
            if ((Byte)this.entityData.get(FORCED_AFFINITY) > 0) {
                this.affinities = new ArrayList();
                this.affinities.add(Affinity.values()[(Byte)this.entityData.get(FORCED_AFFINITY)]);
            } else {
                SpellRecipe recipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE));
                this.affinities = new ArrayList<Affinity>(recipe.getAffinity().keySet());
            }
        }
        return this.affinities;
    }

    public SpecialRenderType getSpecialRenderType() {
        return SpecialRenderType.values()[(Byte)this.entityData.get(SPECIAL_RENDER)];
    }

    public void spawnParticles(int particleCount, float partialTick) {
        Affinity aff;
        if (this.getAffinity() != null && this.particleFunctions.containsKey((aff = this.affinities.get((int)(Math.random() * (double)this.affinities.size()))))) {
            this.particleFunctions.get(aff).accept(particleCount, Float.valueOf(partialTick));
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(SPELL_RECIPE, new CompoundTag());
        builder.define(PIERCE_LEVEL, (byte)0);
        builder.define(FORCED_AFFINITY, (byte)0);
        builder.define(FORCED_DAMAGE, (byte)0);
        builder.define(SPECIAL_RENDER, (byte)0);
        builder.define(SPECIAL_RENDER_PARAM, (byte)0);
        builder.define(DISABLE_WATER_CHECK, false);
        builder.define(VELOCITY, Float.valueOf(0.2f));
        builder.define(HOMING_STRENGTH, Float.valueOf(0.0f));
        builder.define(TARGET_ID, -1);
        builder.define(OWNER_ID, -1);
    }

    public void setForcedDamageAffinityAndTarget(Affinity affinity, float damage, @Nullable Entity target) {
        this.entityData.set(FORCED_DAMAGE, ((byte)damage));
        this.entityData.set(FORCED_AFFINITY, ((byte)affinity.ordinal()));
        this.forcedTargetID = target != null ? Optional.of(target.getId()) : Optional.empty();
    }

    public void setHellball(Entity target, float speed) {
        this.forcedTargetID = Optional.of(target.getId());
        this.trackForcedTarget = true;
        this.trackingSpeed = speed;
        this.setSpecialRender(SpecialRenderType.HELLBALL);
    }

    public void setHomingStrength(float strength) {
        this.entityData.set(HOMING_STRENGTH, Float.valueOf(MathUtils.clamp01(strength)));
    }

    public void setSpecialRender(SpecialRenderType type) {
        this.entityData.set(SPECIAL_RENDER, ((byte)type.ordinal()));
        switch (type) {
            case HALLOWEEN: {
                this.entityData.set(SPECIAL_RENDER_PARAM, ((byte)(Math.random() * (double)halloween_stacks.length)));
                break;
            }
        }
    }

    public void setNoWaterFizzle() {
        this.entityData.set(DISABLE_WATER_CHECK, true);
    }

    public ItemStack getSpecialRenderStack() {
        return halloween_stacks[(Byte)this.entityData.get(SPECIAL_RENDER_PARAM)];
    }

    public void setSpellRecipe(CompoundTag recipe) {
        this.entityData.set(SPELL_RECIPE, recipe);
    }

    protected SpellRecipe getRecipe() {
        if (this._cachedSpellRecipe == null) {
            this._cachedSpellRecipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE));
        }
        return this._cachedSpellRecipe;
    }

    public void shoot(@Nullable Entity shooter, Vec3 look, float velocity, float inaccuracy) {
        this.shoot(look.x(), look.y(), look.z(), velocity, inaccuracy);
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 Vector3d = new Vec3(x, y, z).normalize().scale((double)velocity);
        this.setDeltaMovement(Vector3d);
        double f = Vector3d.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)Vector3d.x, (double)Vector3d.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)Vector3d.y, (double)f) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.entityData.set(VELOCITY, Float.valueOf(velocity));
    }

    protected void entityHit(Entity entity) {
        if (entity instanceof SpellProjectile) {
            return;
        }
        if (!this.level().isClientSide()) {
            if ((Byte)this.entityData.get(FORCED_DAMAGE) > 0) {
                Entity shooter = this.getShooter();
                if (shooter != null && shooter instanceof LivingEntity) {
                    entity.hurt(this.damageSources().mobAttack((LivingEntity)shooter), (float)((Byte)this.entityData.get(FORCED_DAMAGE)).byteValue());
                } else {
                    entity.hurt(entity.damageSources().magic(), (float)((Byte)this.entityData.get(FORCED_DAMAGE)).byteValue());
                }
            } else {
                this.applyEffect(this.blockPosition(), this.getDirection(), entity);
            }
        }
        this.remove(Entity.RemovalReason.KILLED);
    }

    protected void blockHit(BlockPos pos, Direction face) {
        if ((Byte)this.entityData.get(FORCED_DAMAGE) <= 0 && !this.level().isClientSide()) {
            this.applyEffect(pos, face, null);
        }
        this.remove(Entity.RemovalReason.KILLED);
    }

    private void applyEffect(BlockPos impactPoint, Direction face, @Nullable Entity hitEntity) {
        if (!this.isAlive()) {
            return;
        }
        this.remove(Entity.RemovalReason.KILLED);
        if (this.getShooter() == null || !(this.getShooter() instanceof LivingEntity)) {
            return;
        }
        SpellSource source = new SpellSource((LivingEntity)this.getShooter(), InteractionHand.MAIN_HAND);
        SpellRecipe recipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE));
        if (recipe.isValid() && !this.level().isClientSide()) {
            boolean sphere;
            HashMap<SpellEffect, ComponentApplicationResult> results = new HashMap<SpellEffect, ComponentApplicationResult>();
            BlockState state = this.level().getBlockState(impactPoint);
            if (state.getBlock() instanceof ISpellInteractibleBlock && ((ISpellInteractibleBlock)state.getBlock()).onHitBySpell(this.level(), impactPoint, recipe)) {
                return;
            }
            if (hitEntity != null && hitEntity instanceof ISpellInteractibleEntity && ((ISpellInteractibleEntity)hitEntity).onShapeTarget(recipe, source)) {
                return;
            }
            float spellRadius = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RADIUS) * 2.0f;
            SpellContext context = new SpellContext(this.level(), recipe, (Entity)this);
            boolean bl = sphere = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.PRECISION) == 1.0f;
            if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks())) {
                int rSquared;
                int radius = (int)((Math.floor(spellRadius) - 1.0) / 2.0);
                if (radius < 0) {
                    radius = 0;
                }
                int n = rSquared = sphere ? radius * radius : 0;
                if (radius > 0 || hitEntity == null) {
                    for (int i = -radius; i <= radius; ++i) {
                        int tx_squared = sphere ? i * i : 0;
                        for (int j = -radius; j <= radius; ++j) {
                            int ty_squared = sphere ? j * j : 0;
                            for (int k = -radius; k <= radius; ++k) {
                                int tz_squared;
                                int n2 = tz_squared = sphere ? k * k : 0;
                                if (sphere && tx_squared + ty_squared + tz_squared > rSquared) continue;
                                BlockPos adjusted = impactPoint.offset(i, j, k);
                                HashMap<SpellEffect, ComponentApplicationResult> loopRes = SpellCaster.ApplyComponents(recipe, source, new SpellTarget(adjusted, face).doNotOffsetFace(), context);
                                SpellCaster.mergeComponentResults(results, loopRes);
                            }
                        }
                    }
                }
            }
            if (recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
                float radius = ((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RADIUS);
                List targets = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)radius), e -> !e.isInvulnerable() && (!sphere || e.distanceTo((Entity)this) <= radius) && !(e instanceof SpellProjectile) && e.isAlive() && e != this);
                if (hitEntity != null && !targets.contains(hitEntity)) {
                    targets.add(hitEntity);
                }
                for (Entity target : targets) {
                    if (target == null || target == this.getShooter()) continue;
                    HashMap<SpellEffect, ComponentApplicationResult> loopRes = SpellCaster.ApplyComponents(recipe, source, new SpellTarget(target), context);
                    SpellCaster.mergeComponentResults(results, loopRes);
                }
            }
            if (results.size() > 0) {
                List<SpellEffect> appliedEffects = results.entrySet().stream().map(e -> e.getValue() == ComponentApplicationResult.SUCCESS ? (SpellEffect)e.getKey() : null).filter(e -> e != null).collect(Collectors.toList());
                SpellCaster.spawnClientFX(this.level(), this.position(), this.getDeltaMovement(), source, appliedEffects);
            }
        }
    }

    protected void onHit(HitResult raytraceResultIn) {
        if (raytraceResultIn == null) {
            return;
        }
        HitResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)raytraceResultIn);
        } else if (raytraceresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockraytraceresult = (BlockHitResult)raytraceResultIn;
            this.blockHit(blockraytraceresult.getBlockPos(), blockraytraceresult.getDirection());
        }
    }

    protected float getWaterDrag() {
        return this.affinities.contains(Affinity.WATER) ? 1.0f : 0.6f;
    }

    public byte getPierceLevel() {
        return (Byte)this.entityData.get(PIERCE_LEVEL);
    }

    public void setPierceLevel(byte level) {
        this.entityData.set(PIERCE_LEVEL, level);
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
        if (this.level() instanceof ServerLevel) {
            if (this.shootingEntity != null) {
                return ((ServerLevel)this.level()).getEntity(this.shootingEntity);
            }
            return null;
        }
        int shooterID = (Integer)this.entityData.get(OWNER_ID);
        return this.level().getEntity(shooterID);
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

    public void setOwner(@Nullable Entity entityIn) {
        UUID uUID = this.shootingEntity = entityIn == null ? null : entityIn.getUUID();
        if (entityIn != null) {
            this.entityData.set(OWNER_ID, entityIn.getId());
        } else {
            this.entityData.set(OWNER_ID, -1);
        }
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putByte("PierceLevel", this.getPierceLevel());
        if (this.shootingEntity != null) {
            compound.putUUID("OwnerUUID", this.shootingEntity);
        }
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        this.setPierceLevel(compound.getByte("PierceLevel"));
        if (compound.hasUUID("OwnerUUID")) {
            this.shootingEntity = compound.getUUID("OwnerUUID");
        }
    }

    @Nullable
    protected EntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec) {
        if (this.getShooter() instanceof Odin) {
            return ProjectileUtil.getEntityHitResult((Level)this.level(), (Entity)this, (Vec3)startVec, (Vec3)endVec, (AABB)this.getBoundingBox().inflate(3.0).expandTowards(this.getDeltaMovement()), candidate -> !(candidate.isSpectator() || !candidate.isAlive() || !candidate.isPickable() || candidate == this.getShooter() && this.ticksInAir < 5 || this.piercedEntities != null && this.piercedEntities.contains(candidate.getId())));
        }
        return ProjectileUtil.getEntityHitResult((Level)this.level(), (Entity)this, (Vec3)startVec, (Vec3)endVec, (AABB)this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), candidate -> !(candidate.isSpectator() || !candidate.isAlive() || !candidate.isPickable() || candidate == this.getShooter() && this.ticksInAir < 5 || this.piercedEntities != null && this.piercedEntities.contains(candidate.getId())));
    }

    protected void onHitEntity(EntityHitResult p_213868_1_) {
        Entity entity = p_213868_1_.getEntity();
        if (this.getShooter() == null || entity == this.getShooter()) {
            return;
        }
        if (this.getPierceLevel() > 0) {
            if (this.piercedEntities == null) {
                this.piercedEntities = new IntOpenHashSet(5);
            }
            if (this.hitEntities == null) {
                this.hitEntities = Lists.newArrayListWithCapacity((int)5);
            }
            if (this.piercedEntities.size() >= this.getPierceLevel() + 1) {
                this.remove(Entity.RemovalReason.KILLED);
                return;
            }
            this.piercedEntities.add(entity.getId());
        }
        if (this.forcedTargetID != null && this.forcedTargetID.isPresent() && this.forcedTargetID.get().intValue() != entity.getId()) {
            return;
        }
        this.entityHit(entity);
        if (!entity.isAlive() && this.hitEntities != null) {
            this.hitEntities.add(entity);
        }
        if (this.getPierceLevel() <= 0) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public void tick() {
        float homingStrength;
        Vec3 motion = this.getDeltaMovement();
        LivingEntity target = this.getTarget();
        if (this.forcedTargetID.isPresent()) {
            target = this.level().getEntity(this.forcedTargetID.get().intValue());
        }
        if (this.trackForcedTarget && this.forcedTargetID.isPresent()) {
            if (target == null) {
                this.applyEffect(this.blockPosition(), this.getDirection(), null);
                this.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            Vec3 direction = target.position().subtract(this.position()).normalize().scale((double)this.trackingSpeed);
            this.setDeltaMovement(direction);
            float closeEnough = 2.0f;
            if (this.position().distanceTo(target.position()) <= (double)closeEnough) {
                EntityHitResult ehr = new EntityHitResult((Entity)target);
                this.onHitEntity(ehr);
            }
        }
        if ((homingStrength = ((Float)this.entityData.get(HOMING_STRENGTH)).floatValue()) > 0.0f) {
            if (target == null) {
                if (this.level().getGameTime() % 5L == 0L) {
                    this.target();
                }
            } else {
                Vec3 myPos = this.position();
                Vec3 theirPos = target.position().add(0.0, (double)(target.getBbHeight() / 2.0f), 0.0);
                float maxRotationRadians = 0.261799f;
                float tickTheta = maxRotationRadians * MathUtils.clamp01(homingStrength);
                if (tickTheta > 0.0f) {
                    Vec3 desiredHeading = theirPos.subtract(myPos).normalize();
                    Vec3 calculatedHeading = MathUtils.rotateTowards(this.getDeltaMovement().normalize(), desiredHeading, tickTheta).normalize().scale((double)((Float)this.entityData.get(VELOCITY)).floatValue());
                    this.setDeltaMovement(calculatedHeading);
                }
            }
        }
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            double f = motion.horizontalDistance();
            this.setYRot((float)(Mth.atan2((double)motion.x, (double)motion.z) * 57.2957763671875));
            this.setXRot((float)(Mth.atan2((double)motion.y, (double)f) * 57.2957763671875));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        this.wasTouchingWater = this.level().isWaterAt(this.blockPosition());
        if (this.isInWaterOrRain()) {
            this.clearFire();
        }
        if (this.isInWaterOrBubble() && this.getAffinity().contains(Affinity.FIRE) && !((Boolean)this.entityData.get(DISABLE_WATER_CHECK)).booleanValue()) {
            if (!this.level().isClientSide()) {
                this.level().playSound(null, this.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        ++this.ticksInAir;
        Vec3 Vector3d2 = this.position();
        Vec3 Vector3d3 = Vector3d2.add(motion);
        BlockHitResult raytraceresult = this.level().clip(new ClipContext(Vector3d2, Vector3d3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
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
                if (!(entity instanceof LivingEntity)) break;
                Entity entity1 = this.getShooter();
                if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                    raytraceresult = null;
                    entityraytraceresult = null;
                }
            }
            if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS && true) {
                this.onHit((HitResult)raytraceresult);
                this.hasImpulse = true;
                break;
            }
            if (entityraytraceresult == null || this.getPierceLevel() <= 0) break;
            raytraceresult = null;
        }
        motion = this.getDeltaMovement();
        double d3 = motion.x;
        double d4 = motion.y;
        double d0 = motion.z;
        double d5 = this.getX() + d3;
        double d1 = this.getY() + d4;
        double d2 = this.getZ() + d0;
        double f1 = motion.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)d3, (double)d0) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)d4, (double)f1) * 57.2957763671875));
        while (this.getXRot() - this.xRotO < -180.0f) {
            this.xRotO -= 360.0f;
        }
        while (this.getXRot() - this.xRotO >= 180.0f) {
            this.xRotO += 360.0f;
        }
        while (this.getYRot() - this.yRotO < -180.0f) {
            this.yRotO -= 360.0f;
        }
        while (this.getYRot() - this.yRotO >= 180.0f) {
            this.yRotO += 360.0f;
        }
        this.setXRot(Mth.lerp((float)0.2f, (float)this.xRotO, (float)this.getXRot()));
        this.setYRot(Mth.lerp((float)0.2f, (float)this.yRotO, (float)this.getYRot()));
        float f2 = 0.99f;
        if (this.isInWater()) {
            for (int j = 0; j < 4; ++j) {
                this.level().addParticle((ParticleOptions)ParticleTypes.BUBBLE, d5 - d3 * 0.25, d1 - d4 * 0.25, d2 - d0 * 0.25, d3, d4, d0);
            }
        }
        this.setDeltaMovement(motion.scale((double)f2));
        if (!this.isNoGravity()) {
            Vec3 Vector3d4 = this.getDeltaMovement();
            this.setDeltaMovement(Vector3d4.x, Vector3d4.y - (double)0.05f, Vector3d4.z);
        }
        this.setPos(d5, d1, d2);
        this.checkInsideBlocks();
        if (this.tickCount > 200 || this.ticksInAir > 200) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void lookAt(Entity pEntity, float pMaxYRotIncrease, float pMaxXRotIncrease) {
        double d1;
        double d0 = pEntity.getX() - this.getX();
        double d2 = pEntity.getZ() - this.getZ();
        if (pEntity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)pEntity;
            d1 = livingentity.getEyeY() - this.getEyeY();
        } else {
            d1 = (pEntity.getBoundingBox().minY + pEntity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(Mth.atan2((double)d2, (double)d0) * 57.2957763671875) - 90.0f;
        float f1 = (float)(-(Mth.atan2((double)d1, (double)d3) * 57.2957763671875));
        this.setXRot(this.rotlerp(this.getXRot(), f1, pMaxXRotIncrease));
        this.setYRot(this.rotlerp(this.getYRot(), f, pMaxYRotIncrease));
    }

    private float rotlerp(float pAngle, float pTargetAngle, float pMaxIncrease) {
        float f = Mth.wrapDegrees((float)(pTargetAngle - pAngle));
        if (f > pMaxIncrease) {
            f = pMaxIncrease;
        }
        if (f < -pMaxIncrease) {
            f = -pMaxIncrease;
        }
        return pAngle + f;
    }

    private void target() {
        if (this.getShooter() == null || !(this.getShooter() instanceof LivingEntity)) {
            return;
        }
        this.target = this.level().getEntities((Entity)this, this.getBoundingBox().inflate(16.0), e -> {
            ClipContext ctx = new ClipContext(this.position(), e.position().add(0.0, (double)(e.getBbHeight() / 2.0f), 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this);
            if (this.level().clip(ctx).getType() == HitResult.Type.BLOCK) {
                return false;
            }
            return e.isAlive() && e instanceof LivingEntity && !SummonUtils.isTargetFriendly(e, (LivingEntity)this.getShooter());
        }).stream().map(e -> (LivingEntity)e).findFirst().orElse(null);
        if (this.target != null) {
            this.entityData.set(TARGET_ID, this.target.getId());
            this.tickCount = 0;
        }
    }

    @Nullable
    private LivingEntity getTarget() {
        Entity e;
        if (this.target == null && (e = this.level().getEntity(((Integer)this.entityData.get(TARGET_ID)).intValue())) instanceof LivingEntity) {
            this.target = (LivingEntity)e;
        }
        return this.target;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            float f = Mth.sqrt((float)((float)(x * x + z * z)));
            this.setXRot((float)(Mth.atan2((double)y, (double)f) * 57.2957763671875));
            this.setYRot((float)(Mth.atan2((double)x, (double)z) * 57.2957763671875));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
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
        return distance < (d0 = d0 * 64.0 * SpellProjectile.getViewScale()) * d0;
    }

    public void onRemovedFromLevel() {
        Entity.RemovalReason reason;
        super.onRemovedFromLevel();
        if (this.level().isClientSide() && (reason = this.getRemovalReason()) == Entity.RemovalReason.DISCARDED) {
            this.spawnImpactParticles();
        }
    }

    private void spawnImpactParticles() {
        if (!this.getRecipe().isValid() || this.affinities == null) {
            return;
        }
        float radius = ((ModifiedSpellPart)this.getRecipe().getShape()).getValue(Attribute.RADIUS) + 1.0f;
        for (Affinity aff : this.affinities) {
            if (!this.impactFunctions.containsKey(aff)) continue;
            this.impactFunctions.get(aff).accept((int)(75.0f * radius), Float.valueOf(radius));
        }
    }

    private void spawnWaterImpactParticles(int particleCount, float velocityModifier) {
        Vec3 pos = this.position();
        float spread = 0.2f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)));
            Vec3 velocity = particlePos.subtract(pos).normalize().scale((Math.random() * 0.5 + 0.5) * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DRIP.get())), this.getShooter()).setGravity(0.05f).setPhysics(true).setScale(0.125f).setMaxAge(40), particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnFireImpactParticles(int particleCount, float velocityModifier) {
        Vec3 velocity;
        Vec3 particlePos;
        int j;
        Vec3 pos = this.position();
        float spread = 0.1f;
        for (j = 0; j < particleCount / 8; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)).scale(0.25));
            velocity = particlePos.subtract(pos).normalize().scale((double)0.1f).scale((double)(spread * velocityModifier));
            this.level().addParticle((ParticleOptions)ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
        for (j = 0; j < particleCount / 4; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)).scale((double)(velocityModifier / 2.0f)));
            velocity = particlePos.subtract(pos).normalize().scale(Math.random() * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)ParticleTypes.LAVA, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnWindImpactParticles(int particleCount, float velocityModifier) {
        Vec3 particlePos;
        int j;
        Vec3 pos = this.position();
        float spread = 0.2f;
        for (j = 0; j < particleCount / 6; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)));
            Vec3 velocity = particlePos.subtract(pos).normalize().scale((Math.random() * 0.5 + 0.5) * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())), this.getShooter()).setScale(0.2f).setColor(10, 10, 10).setScale(0.125f).setMaxAge(4 + (int)(Math.random() * 4.0 * (double)velocityModifier)), particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
        for (j = 0; j < particleCount / 2; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)));
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())), this.getShooter()).setScale(0.2f).setColor(10, 10, 10).setScale(0.125f).setMaxAge(4 + (int)(Math.random() * 4.0 * (double)velocityModifier)), particlePos.x, particlePos.y, particlePos.z, (double)(0.05f * velocityModifier), (double)(0.05f * velocityModifier), (double)(1.0f * velocityModifier));
        }
    }

    private void spawnArcaneImpactParticles(int particleCount, float velocityModifier) {
        Vec3 pos = this.position();
        float spread = 0.2f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)));
            Vec3 velocity = particlePos.subtract(pos).normalize().scale((Math.random() * 0.5 + 0.5) * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())), this.getShooter()).setGravity(0.05f).setPhysics(true).setScale(0.125f).setMaxAge(40), particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnEnderImpactParticles(int particleCount, float velocityModifier) {
        Vec3 pos = this.position();
        for (int j = 0; j < particleCount; ++j) {
            Vec3 particlePos = pos.add(new Vec3(1.0, 1.0, 1.0).scale(1.0 + Math.random() * 3.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)));
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), this.getShooter()).setMaxAge(10 + (int)(Math.random() * 10.0)), particlePos.x, particlePos.y, particlePos.z, pos.x, pos.y, pos.z);
        }
    }

    private void spawnEarthImpactParticles(int particleCount, float velocityModifier) {
        Vec3 pos = this.position();
        float spread = 0.1f;
        for (int j = 0; j < particleCount / 4; ++j) {
            Vec3 particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)).scale(Math.random() * (double)velocityModifier));
            Vec3 velocity = particlePos.subtract(pos).normalize().scale(Math.random() * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())), this.getShooter()).setScale(0.125f).setMaxAge(40).setGravity(0.01f).setPhysics(true), particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnFrostImpactParticles(int particleCount, float velocityModifier) {
        Vec3 velocity;
        Vec3 particlePos;
        int j;
        Vec3 pos = this.position();
        float spread = 0.1f;
        for (j = 0; j < particleCount / 4; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)).scale(Math.random() * (double)velocityModifier));
            velocity = particlePos.subtract(pos).normalize().scale(Math.random() * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), this.getShooter()).setScale(0.05f).setMaxAge(60).setGravity(0.01f).setPhysics(true), particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
        for (j = 0; j < particleCount / 4; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)).scale(Math.random() * (double)velocityModifier));
            velocity = particlePos.subtract(pos).normalize().scale(Math.random() * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)ParticleTypes.SNOWFLAKE, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
        for (j = 0; j < particleCount / 16; ++j) {
            particlePos = pos.add(new Vec3(-0.5 + Math.random(), 0.0, -0.5 + Math.random()).scale((double)velocityModifier));
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), this.getShooter()).setScale(0.1f * velocityModifier).setColor(140, 150, 160, 64).setMaxAge(60), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
        }
    }

    private void spawnBloodImpactParticles(int particleCount, float velocityModifier) {
        Vec3 velocity;
        Vec3 particlePos;
        int j;
        Vec3 pos = this.position();
        float spread = 0.1f;
        for (j = 0; j < 5; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)).scale(0.25));
            velocity = particlePos.subtract(pos).normalize().scale((double)0.1f).scale((double)(spread * velocityModifier));
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setColor(182, 28, 28).setScale(0.25f), particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
        for (j = 0; j < particleCount; ++j) {
            particlePos = pos.add(this.getForward().multiply(-1.0, -1.0, -1.0).xRot((float)(Math.random() * 360.0)).yRot((float)(Math.random() * 360.0)).zRot((float)(Math.random() * 360.0)).scale((double)(velocityModifier / 4.0f)));
            velocity = particlePos.subtract(pos).normalize().scale(Math.random() * (double)spread * (double)velocityModifier);
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(91, 14, 14).setGravity(0.05f).setPhysics(true), particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnArcaneParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float spread = 0.05f;
        float spread_2 = 0.1f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)(-spread) + Math.random() * (double)spread_2, (double)(-spread) + Math.random() * (double)spread, (double)(-spread) + Math.random() * (double)spread_2);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), this.getShooter()).setGravity(0.005f).setPhysics(true).setScale(0.025f), pos.x + ((double)(-spread) + Math.random() * (double)spread * 2.0), pos.y + ((double)(-spread) + Math.random() * (double)spread * 2.0), pos.z + ((double)(-spread) + Math.random() * (double)spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnEnderParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        for (int j = 0; j < particleCount; ++j) {
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())).setMaxAge(15), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0));
        }
    }

    private void spawnEarthParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())).setColor(0.12156863f, 0.44313726f, 0.12156863f), pos.x + (double)(-particle_spread + this.level().getRandom().nextFloat() * particle_spread * 2.0f), pos.y + (double)(-particle_spread + this.level().getRandom().nextFloat() * particle_spread * 2.0f), pos.z + (double)(-particle_spread + this.level().getRandom().nextFloat() * particle_spread * 2.0f), 0.0, 0.0, 0.0);
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnWaterParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        float velocity_spread = 0.025f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, Math.random() * (double)velocity_spread, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DRIP.get())).setGravity(0.025f).setPhysics(true).setScale(0.05f), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnFireParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        float velocity_spread = 0.025f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setScale(0.01f), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnHellfireParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        float velocity_spread = 0.025f;
        float scale = this.getSpecialRenderType() == SpecialRenderType.HELLBALL ? 0.05f : 0.01f;
        for (int j = 0; j < particleCount; ++j) {
            Vec3 velocity = new Vec3((double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())).setScale(scale), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnLightningParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        double radius = 0.25;
        Vec3 motion = this.getDeltaMovement();
        int particleLife = (int)(5.0 + Math.random() * 5.0);
        this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())).setMaxAge(particleLife), this.getShooter()), pos.x - radius + Math.random() * radius * 2.0, pos.y - radius + Math.random() * radius * 2.0, pos.z - radius + Math.random() * radius * 2.0, pos.x + motion.x * (double)particleLife / 2.0, pos.y + motion.y * (double)particleLife / 2.0, pos.z + motion.z * (double)particleLife / 2.0);
    }

    private void spawnWindParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        float velocity_spread = 0.025f;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 vSpread = new Vec3((double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0);
            Vec3 velocity = this.getDeltaMovement().scale((double)-0.2f).add(vSpread);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setMaxAge(10).setScale(0.2f).setColor(3, 3, 3), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0) + this.getDeltaMovement().x * Math.random(), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0) + this.getDeltaMovement().y * Math.random(), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0) + this.getDeltaMovement().z * Math.random(), velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnFrostParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.05f;
        for (int j = 0; j < particleCount / 2; ++j) {
            Vec3 velocity = new Vec3((double)-0.01f + Math.random() * (double)0.02f, (double)-0.05f + Math.random() * (double)0.05f, (double)-0.01f + Math.random() * (double)0.02f);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setScale(0.01f).setGravity(0.005f).setPhysics(true), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    private void spawnBloodParticles(int particleCount, float partialTick) {
        Vec3 pos = this.position().add(this.getDeltaMovement().scale((double)partialTick));
        float particle_spread = 0.1f;
        float velocity_spread = 0.025f;
        for (int j = 0; j < particleCount / 2; ++j) {
            Vec3 velocity = new Vec3((double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0, Math.random() * (double)velocity_spread, (double)(-velocity_spread) + Math.random() * (double)velocity_spread * 2.0);
            this.level().addParticle((ParticleOptions)this.getRecipe().colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(91, 14, 14).setGravity(0.05f).setPhysics(true).setScale(0.05f), this.getShooter()), pos.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), pos.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    public static enum SpecialRenderType {
        NONE,
        HALLOWEEN,
        HELLBALL;

    }
}