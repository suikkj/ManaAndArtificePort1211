 
package com.mna.entities.summon;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.items.relic.CrystallizedWind;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidType;
import com.mna.api.tools.MNACap;

public class AnimusBlock
extends Mob
implements Enemy,
PlayerRideableJumping {
    protected static final EntityDataAccessor<BlockPos> ORIGIN = SynchedEntityData.defineId(AnimusBlock.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    protected static final EntityDataAccessor<Integer> BLOCK_STATE_ID = SynchedEntityData.defineId(AnimusBlock.class, (EntityDataSerializer)EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(AnimusBlock.class, (EntityDataSerializer)EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> RIDER_OFFSET_HEIGHT = SynchedEntityData.defineId(AnimusBlock.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> RIDER_SIT = SynchedEntityData.defineId(AnimusBlock.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> CAN_FLY = SynchedEntityData.defineId(AnimusBlock.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    public float squishAmount;
    public float squishFactor;
    public float prevSquishFactor;
    private boolean wasOnGround;
    private boolean fireImmune = false;
    protected float jumpPower;
    private boolean clearOrigin = true;
    private BlockState fallTile = Blocks.SAND.defaultBlockState();

    public AnimusBlock(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new MoveHelperController(this);
    }

    public AnimusBlock(Level worldIn, BlockState state, BlockPos pos, int duration) {
        this((EntityType<? extends Mob>)((EntityType)EntityInit.ANIMUS_BLOCK.get()), worldIn);
        this.setPos((float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f);
        this.fallTile = state;
        this.blocksBuilding = true;
        if (!this.level().isClientSide()) {
            VoxelShape collisionShape = state.getShape((BlockGetter)this.level(), pos);
            this.entityData.set(RIDER_OFFSET_HEIGHT, Float.valueOf((float)collisionShape.max(Direction.Axis.Y)));
            this.entityData.set(RIDER_SIT, state.isCollisionShapeFullBlock((BlockGetter)this.level(), pos));
            this.entityData.set(BLOCK_STATE_ID, Block.getId((BlockState)this.fallTile));
            this.entityData.set(DURATION, duration);
            this.entityData.set(ORIGIN, pos);
            if (MATags.isBlockIn(state.getBlock(), MATags.Blocks.ANIMUS_FLYING)) {
                this.entityData.set(CAN_FLY, true);
            }
        }
        this.fireImmune = this.fallTile.getFireSpreadSpeed((BlockGetter)worldIn, pos, Direction.DOWN) == 0;
    }

    public boolean fireImmune() {
        return this.fireImmune;
    }

    public AnimusBlock doNotClearOrigin() {
        this.clearOrigin = false;
        return this;
    }

    public void tick() {
        if (this.tickCount == 1 && this.clearOrigin && !this.level().isClientSide()) {
            BlockPos origin = (BlockPos)this.entityData.get(ORIGIN);
            if (!this.level().isEmptyBlock(origin)) {
                this.level().setBlock(origin, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5f;
        this.prevSquishFactor = this.squishFactor;
        super.tick();
        if (this.onGround() && !this.wasOnGround && this.getControllingPassenger() == null) {
            int i = 1;
            for (int j = 0; j < i * 8; ++j) {
                float f = this.random.nextFloat() * ((float)Math.PI * 2);
                float f1 = this.random.nextFloat() * 0.5f + 0.5f;
                float f2 = Mth.sin((float)f) * (float)i * 0.5f * f1;
                float f3 = Mth.cos((float)f) * (float)i * 0.5f * f1;
                this.level().addParticle(this.getSquishParticle(), this.getX() + (double)f2, this.getY(), this.getZ() + (double)f3, 0.0, 0.0, 0.0);
            }
            this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) / 0.8f);
            this.squishAmount = -0.5f;
        } else if (!this.onGround() && this.wasOnGround) {
            this.squishAmount = 1.0f;
        }
        this.wasOnGround = this.onGround();
        this.alterSquishAmount();
        if (!this.level().isClientSide()) {
            LivingEntity j;
            if (this.isNoGravity() && (j = this.getControllingPassenger()) instanceof Player) {
                Player player = (Player)j;
                this.resetFallDistance();
                if (this.isInWaterOrBubble() || this.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()))) {
                    this.ejectPassengers();
                    this.setNoGravity(false);
                } else if (!CrystallizedWind.FindOn((LivingEntity)player)) {
                    MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                        float amount = 0.75f;
                        if (!m.getCastingResource().hasEnough((LivingEntity)player, amount)) {
                            this.ejectPassengers();
                            this.setNoGravity(false);
                        } else {
                            m.getCastingResource().consume((LivingEntity)player, amount);
                        }
                    });
                }
            }
            if (this.tickCount >= (Integer)this.entityData.get(DURATION) && this.getPassengers().size() == 0 && (this.onGround() || this.isInWaterOrBubble())) {
                this.remove(Entity.RemovalReason.DISCARDED);
                BlockState target = this.level().getBlockState(this.blockPosition());
                FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)this.level()));
                ItemStack stack = new ItemStack((ItemLike)this.getMimicState().getBlock());
                if (this.level().isEmptyBlock(this.blockPosition()) || target.canBeReplaced(new BlockPlaceContext((Player)player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(this.position(), this.getMotionDirection(), this.blockPosition(), true)))) {
                    this.level().setBlockAndUpdate(this.blockPosition(), this.getMimicState());
                    this.getMimicState().getBlock().setPlacedBy(this.level(), this.blockPosition(), this.getMimicState(), (LivingEntity)this, stack);
                } else {
                    this.spawnAtLocation(stack);
                }
            }
        }
        if (this.level().isClientSide()) {
            int numParticles = this.getControllingPassenger() != null && this.isNoGravity() ? 10 : 2;
            for (int i = 0; i < numParticles; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setColor(255, 215, 0), this.getX() - 0.5 + Math.random(), this.getY() + Math.random() * (double)((Float)this.entityData.get(RIDER_OFFSET_HEIGHT)).floatValue(), this.getZ() - 0.5 + Math.random(), 0.0, 0.0, 0.0);
            }
        }
    }

    protected void onEffectAdded(MobEffectInstance pEffectInstance, Entity pEntity) {
        super.onEffectAdded(pEffectInstance, pEntity);
        MobEffect effect = pEffectInstance.getEffect();
        if (effect == EffectInit.LEVITATION.get() || effect == MobEffects.LEVITATION || effect == EffectInit.SOAR.get()) {
            this.setNoGravity(true);
        }
    }

    protected void onEffectRemoved(MobEffectInstance pEffectInstance) {
        super.onEffectRemoved(pEffectInstance);
        MobEffect effect = pEffectInstance.getEffect();
        if (!(effect != EffectInit.LEVITATION.get() && effect != MobEffects.LEVITATION && effect != EffectInit.SOAR.get() || this.fallTile != null && MATags.isBlockIn(this.fallTile.getBlock(), MATags.Blocks.ANIMUS_FLYING))) {
            this.setNoGravity(false);
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ORIGIN, BlockPos.ZERO);
        builder.define(BLOCK_STATE_ID, Block.getId((BlockState)Blocks.SAND.defaultBlockState()));
        builder.define(DURATION, 10);
        builder.define(RIDER_OFFSET_HEIGHT, Float.valueOf(1.0f));
        builder.define(RIDER_SIT, true);
        builder.define(CAN_FLY, false);
    }

    public void setOrigin(BlockPos origin) {
        this.entityData.set(ORIGIN, origin);
    }

    public BlockState getMimicState() {
        return Block.stateById((int)((Integer)this.entityData.get(BLOCK_STATE_ID)));
    }

    @OnlyIn(value=Dist.CLIENT)
    public BlockPos getOrigin() {
        return (BlockPos)this.entityData.get(ORIGIN);
    }

    protected void alterSquishAmount() {
        this.squishAmount *= 0.6f;
    }

    protected SoundEvent getSquishSound() {
        return this.getMimicState().getSoundType().getPlaceSound();
    }

    protected SoundEvent getJumpSound() {
        return this.getMimicState().getSoundType().getBreakSound();
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        this.fallTile = NbtUtils.readBlockState((HolderGetter)this.level().holderLookup(Registries.BLOCK), (CompoundTag)compound.getCompound("BlockState"));
        if (this.fallTile.isAir()) {
            this.fallTile = Blocks.SAND.defaultBlockState();
        }
        if (compound.contains("Origin")) {
            this.entityData.set(ORIGIN, NbtUtils.readBlockPos(compound, "Origin").orElse(null));
        }
        if (compound.contains("StateID")) {
            this.entityData.set(BLOCK_STATE_ID, compound.getInt("StateID"));
        }
        if (compound.contains("Duration")) {
            this.entityData.set(DURATION, compound.getInt("Duration"));
        }
        super.readAdditionalSaveData(compound);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("BlockState", (Tag)NbtUtils.writeBlockState((BlockState)this.fallTile));
        compound.put("Origin", (Tag)NbtUtils.writeBlockPos((BlockPos)((BlockPos)this.entityData.get(ORIGIN))));
        compound.putInt("StateID", ((Integer)this.entityData.get(BLOCK_STATE_ID)).intValue());
        compound.putInt("Duration", ((Integer)this.entityData.get(DURATION)).intValue());
        super.addAdditionalSaveData(compound);
    }

    protected ParticleOptions getSquishParticle() {
        return new BlockParticleOption(ParticleTypes.BLOCK, this.getMimicState());
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    public boolean shouldDiscardFriction() {
        return MATags.isBlockIn(this.getMimicState().getBlock(), MATags.Blocks.ANIMUS_NOFRICTION);
    }

    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        if (MATags.isBlockIn(this.fallTile.getBlock(), MATags.Blocks.ANIMUS_NOFALLDAMAGE)) {
            return 0;
        }
        return super.calculateFallDamage(pFallDistance, pDamageMultiplier);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    public void push(Entity entityIn) {
        super.push(entityIn);
        if (entityIn == this.getTarget()) {
            if (!this.level().isClientSide()) {
                if (this.getMimicState() == Blocks.TNT.defaultBlockState()) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                    this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), 4.0f, Level.ExplosionInteraction.TNT);
                    return;
                }
                if (this.getMimicState().getBlock() instanceof FireBlock) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                    entityIn.hurt(entityIn.damageSources().onFire(), 5.0f);
                    entityIn.igniteForSeconds((float)(10));
                    return;
                }
            }
            if (MATags.isBlockIn(this.getMimicState().getBlock(), MATags.Blocks.ANIMUS_TANGLES)) {
                ((LivingEntity)entityIn).addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.ENTANGLE.get()), 100, 0));
                this.remove(Entity.RemovalReason.DISCARDED);
            } else if (MATags.isBlockIn(this.getMimicState().getBlock(), MATags.Blocks.ANIMUS_POISONCLOUD)) {
                AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
                areaeffectcloud.setRadius(3.0f);
                areaeffectcloud.setRadiusOnUse(-0.5f);
                areaeffectcloud.setWaitTime(10);
                areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
                areaeffectcloud.setPotion(new Potion(new MobEffectInstance[]{new MobEffectInstance(MobEffects.POISON, 1200, 0)}));
                this.level().addFreshEntity((Entity)areaeffectcloud);
                this.remove(Entity.RemovalReason.DISCARDED);
            } else {
                this.dealDamage((LivingEntity)entityIn);
                if (MATags.isBlockIn(this.getMimicState().getBlock(), MATags.Blocks.ANIMUS_GLOW)) {
                    ((LivingEntity)entityIn).addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
                }
                if (MATags.isBlockIn(this.getMimicState().getBlock(), MATags.Blocks.ANIMUS_SLOWS)) {
                    ((LivingEntity)entityIn).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                }
                if (MATags.isBlockIn(this.getMimicState().getBlock(), MATags.Blocks.ANIMUS_KNOCKBACK)) {
                    float mX = (float)(this.getX() - entityIn.getX());
                    float mZ = (float)(this.getZ() - entityIn.getZ());
                    ((LivingEntity)entityIn).knockback(0.5, (double)mX, (double)mZ);
                }
            }
        }
    }

    protected void dealDamage(LivingEntity entityIn) {
        if (this.isAlive()) {
            int i = 3;
            if (this.distanceToSqr((Entity)entityIn) < 0.6 * (double)i * 0.6 * (double)i && this.getSensing().hasLineOfSight((Entity)entityIn) && entityIn.hurt(this.damageSources().mobAttack((LivingEntity)this), this.getAttackDamage())) {
                this.playSound(SoundEvents.SLIME_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                com.mna.tools.MATools.doEnchantDamageEffects(this, entityIn);
            }
        }
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void onPlayerJump(int jumpPowerIn) {
        if (jumpPowerIn < 0) {
            jumpPowerIn = 0;
        }
        MobEffectInstance effect = this.getEffect(MobEffects.JUMP);
        this.jumpPower = jumpPowerIn >= 90 ? 1.0f : 0.4f + 0.4f * (float)jumpPowerIn / 90.0f;
        if (effect != null) {
            this.jumpPower = (float)((double)this.jumpPower + 0.15 * (double)effect.getAmplifier());
        }
    }

    public boolean canJump() {
        return !this.isNoGravity();
    }

    public void handleStartJump(int pJumpPower) {
    }

    public void handleStopJump() {
    }

    public boolean canRiderInteract() {
        return true;
    }

    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player) {
            return (Player)entity;
        }
        return null;
    }

    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            LivingEntity livingentity = this.getControllingPassenger();
            if (this.isVehicle() && this.canRiderInteract() && livingentity != null) {
                boolean jump;
                this.setYRot(livingentity.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(livingentity.getXRot() * 0.5f);
                this.setRot(this.getYRot(), this.getXRot());
                this.yHeadRot = this.yBodyRot = this.getYRot();
                float f = livingentity.xxa * 0.5f;
                float f1 = livingentity.zza;
                if (f1 <= 0.0f) {
                    f1 *= 0.25f;
                }
                boolean bl = jump = this.jumpPower > 0.0f && !this.jumping && this.onGround();
                if (jump) {
                    double d0 = this.jumpPower * this.getBlockJumpFactor();
                    double d1 = this.hasEffect(MobEffects.JUMP) ? d0 + (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1f) : d0;
                    float horizontalScale = 3.0f;
                    Vec3 vector3d = this.getDeltaMovement();
                    this.setDeltaMovement(vector3d.x * (double)horizontalScale, d1, vector3d.z * (double)horizontalScale);
                    this.setJumping(true);
                    this.hasImpulse = true;
                    CommonHooks.onLivingJump((LivingEntity)this);
                    if (f1 > 0.0f) {
                        float f2 = Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180)));
                        float f3 = Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)));
                        this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4f * f2 * this.jumpPower), 0.0, (double)(0.4f * f3 * this.jumpPower)));
                    }
                    this.jumpPower = 0.0f;
                } else if (this.onGround()) {
                    f = 0.0f;
                    f1 = 0.0f;
                }
                if (this.isNoGravity()) {
                    travelVector = livingentity.getForward().normalize().scale(travelVector.length());
                }
                if (this.isControlledByLocalInstance() && (!this.onGround() || jump || this.isNoGravity())) {
                    this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    super.travel(new Vec3((double)f, travelVector.y * (double)livingentity.zza, (double)f1));
                } else if (livingentity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }
                if (this.onGround()) {
                    this.jumpPower = 0.0f;
                    this.setJumping(false);
                }
                this.calculateEntityAnimation(false);
            } else {
                this.jumpPower = 0.02f;
                super.travel(travelVector);
            }
        }
    }

    public boolean isControlledByLocalInstance() {
        LivingEntity livingentity = this.getControllingPassenger();
        if (livingentity instanceof Player) {
            Player player = (Player)livingentity;
            return player.isLocalPlayer() && (player.xxa > 0.0f || player.zza > 0.0f || !this.onGround() || this.jumping);
        }
        return this.isEffectiveAi();
    }

    public void aiStep() {
        LivingEntity livingentity;
        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        if (this.lerpSteps > 0) {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double d2 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double d4 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            double d6 = Mth.wrapDegrees((double)(this.lerpYRot - (double)this.getYRot()));
            this.setYRot(this.getYRot() + (float)d6 / (float)this.lerpSteps);
            this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(d0, d2, d4);
            this.setRot(this.getYRot(), this.getXRot());
        } else if (!this.isEffectiveAi()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
        if (this.lerpHeadSteps > 0) {
            this.yHeadRot += (float)Mth.wrapDegrees((double)(this.lyHeadRot - (double)this.yHeadRot)) / (float)this.lerpHeadSteps;
            --this.lerpHeadSteps;
        }
        Vec3 vec31 = this.getDeltaMovement();
        double d1 = vec31.x;
        double d3 = vec31.y;
        double d5 = vec31.z;
        if (Math.abs(vec31.x) < 0.003) {
            d1 = 0.0;
        }
        if (Math.abs(vec31.y) < 0.003) {
            d3 = 0.0;
        }
        if (Math.abs(vec31.z) < 0.003) {
            d5 = 0.0;
        }
        this.setDeltaMovement(d1, d3, d5);
        this.level().getProfiler().push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0f;
            this.zza = 0.0f;
        } else if (this.isEffectiveAi()) {
            this.level().getProfiler().push("newAi");
            this.serverAiStep();
            this.level().getProfiler().pop();
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("jump");
        if (this.jumping && this.isAffectedByFluids()) {
            FluidType fluidType = this.getMaxHeightFluidType();
            double d7 = !fluidType.isAir() ? this.getFluidTypeHeight(fluidType) : (this.isInLava() ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER));
            boolean flag = this.isInWater() && d7 > 0.0;
            double d8 = this.getFluidJumpThreshold();
            if (!flag || this.onGround() && !(d7 > d8)) {
                if (!this.isInLava() || this.onGround() && !(d7 > d8)) {
                    if (fluidType.isAir() || this.onGround() && !(d7 > d8)) {
                        if ((this.onGround() || flag && d7 <= d8) && this.noJumpDelay == 0) {
                            this.jumpFromGround();
                            this.noJumpDelay = 10;
                        }
                    } else {
                        this.jumpInFluid(fluidType);
                    }
                } else {
                    this.jumpInFluid((NeoForgeMod.LAVA_TYPE).value());
                }
            } else {
                this.jumpInFluid((NeoForgeMod.WATER_TYPE).value());
            }
        } else {
            this.noJumpDelay = 0;
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("travel");
        this.xxa *= 0.98f;
        this.zza *= 0.98f;
        this.updateFallFlying();
        AABB aabb = this.getBoundingBox();
        Vec3 vec3 = new Vec3((double)this.xxa, (double)this.yya, (double)this.zza);
        if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
            this.resetFallDistance();
        }
        if ((livingentity = this.getControllingPassenger()) instanceof Player) {
            Player player = (Player)livingentity;
            if (this.isAlive()) {
                this.travelRidden(player, vec3);
            }
        } else {
            this.travel(vec3);
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("freezing");
        if (!this.level().isClientSide() && !this.isDeadOrDying()) {
            int i = this.getTicksFrozen();
            if (this.isInPowderSnow && this.canFreeze()) {
                this.setTicksFrozen(Math.min(this.getTicksRequiredToFreeze(), i + 1));
            } else {
                this.setTicksFrozen(Math.max(0, i - 2));
            }
        }
        this.removeFrost();
        this.tryAddFrost();
        if (!this.level().isClientSide() && this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
            this.hurt(this.damageSources().freeze(), 1.0f);
        }
        this.level().getProfiler().pop();
        this.level().getProfiler().push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            this.checkAutoSpinAttack(aabb, this.getBoundingBox());
        }
        this.pushEntities();
        this.level().getProfiler().pop();
        if (!this.level().isClientSide() && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
            this.hurt(this.damageSources().drown(), 1.0f);
        }
    }

    private void travelRidden(Player pPlayer, Vec3 pTravelVector) {
        Vec3 vec3 = this.getRiddenInput(pPlayer, pTravelVector);
        this.tickRidden(pPlayer, vec3);
        if (this.isControlledByLocalInstance()) {
            this.setSpeed(this.getRiddenSpeed(pPlayer));
            this.travel(vec3);
        } else {
            this.travel(vec3);
        }
    }

    private void updateFallFlying() {
        ItemStack itemstack;
        boolean flag = this.getSharedFlag(7);
        flag = flag && !this.onGround() && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION) ? (itemstack = this.getItemBySlot(EquipmentSlot.CHEST)).canElytraFly((LivingEntity)this) && itemstack.elytraFlightTick((LivingEntity)this, this.fallFlyTicks) : false;
        if (!this.level().isClientSide()) {
            this.setSharedFlag(7, flag);
        }
    }

    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.canAddPassenger((Entity)player)) {
            this.mountTo(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected void playStepSound(BlockPos pPos, BlockState pState) {
    }

    protected void mountTo(Player player) {
        if (!this.level().isClientSide()) {
            player.setXRot(this.getXRot());
            player.setYRot(this.getYRot());
            player.startRiding((Entity)this);
            if (((Boolean)this.entityData.get(CAN_FLY)).booleanValue()) {
                this.setNoGravity(true);
            }
        }
    }

    protected void removePassenger(Entity pPassenger) {
        super.removePassenger(pPassenger);
        if (!this.hasControllingPassenger()) {
            this.setNoGravity(false);
        }
    }

    public boolean shouldRiderSit() {
        return (Boolean)this.entityData.get(RIDER_SIT);
    }

    public boolean shouldRiderFaceForward(Player player) {
        return true;
    }

    public double getPassengersRidingOffset() {
        if (((Boolean)this.entityData.get(RIDER_SIT)).booleanValue()) {
            return (double)((Float)this.entityData.get(RIDER_OFFSET_HEIGHT)).floatValue() * 0.75;
        }
        return (double)((Float)this.entityData.get(RIDER_OFFSET_HEIGHT)).floatValue() + 0.35;
    }

    protected void updateControlFlags() {
        boolean flag = !(this.getControllingPassenger() instanceof Mob);
        boolean flag1 = !(this.getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
    }

    protected float getAttackDamage() {
        float amt = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (MATags.isBlockIn(this.getMimicState().getBlock(), MATags.Blocks.ANIMUS_DAMAGEBOOST)) {
            amt += 2.0f;
        }
        return amt;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, (Goal)new FloatGoal(this));
        this.goalSelector.addGoal(2, (Goal)new AttackGoal(this));
        this.goalSelector.addGoal(3, (Goal)new FaceRandomGoal(this));
        this.goalSelector.addGoal(5, (Goal)new HopGoal(this));
        this.targetSelector.addGoal(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> !(e instanceof AnimusBlock) && e instanceof Enemy && Math.abs(e.getY() - this.getY()) <= 4.0));
    }

    static class MoveHelperController
    extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final AnimusBlock slime;
        private boolean isAggressive;

        public MoveHelperController(AnimusBlock slimeIn) {
            super((Mob)slimeIn);
            this.slime = slimeIn;
            this.yRot = 180.0f * slimeIn.getYRot() / (float)Math.PI;
        }

        public void setDirection(float yRotIn, boolean aggressive) {
            this.yRot = yRotIn;
            this.isAggressive = aggressive;
        }

        public void setSpeed(double speedIn) {
            this.speedModifier = speedIn;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        public void tick() {
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0f));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0f);
            } else {
                this.operation = MoveControl.Operation.WAIT;
                if (this.mob.onGround()) {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                    if (this.jumpDelay-- <= 0) {
                        this.jumpDelay = this.slime.getJumpDelay();
                        if (this.isAggressive) {
                            this.jumpDelay /= 3;
                        }
                        this.slime.getJumpControl().jump();
                        this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), 1.0f);
                    } else {
                        this.slime.xxa = 0.0f;
                        this.slime.zza = 0.0f;
                        this.mob.setSpeed(0.0f);
                    }
                } else {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }
            }
        }
    }

    static class FloatGoal
    extends Goal {
        private final AnimusBlock slime;

        public FloatGoal(AnimusBlock slimeIn) {
            this.slime = slimeIn;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            slimeIn.getNavigation().setCanFloat(true);
        }

        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof MoveHelperController;
        }

        public void tick() {
            if (this.slime.getRandom().nextFloat() < 0.8f) {
                this.slime.getJumpControl().jump();
            }
            ((MoveHelperController)this.slime.getMoveControl()).setSpeed(1.2);
        }
    }

    static class AttackGoal
    extends Goal {
        private final AnimusBlock slime;
        private int growTieredTimer;

        public AttackGoal(AnimusBlock slimeIn) {
            this.slime = slimeIn;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.slime.getTarget();
            if (livingentity == null) {
                return false;
            }
            if (!livingentity.isAlive()) {
                return false;
            }
            return livingentity instanceof Player && ((Player)livingentity).getAbilities().invulnerable ? false : this.slime.getMoveControl() instanceof MoveHelperController;
        }

        public void start() {
            this.growTieredTimer = 300;
            super.start();
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.slime.getTarget();
            if (livingentity == null) {
                return false;
            }
            if (!livingentity.isAlive()) {
                return false;
            }
            if (livingentity instanceof Player && ((Player)livingentity).getAbilities().invulnerable) {
                return false;
            }
            return --this.growTieredTimer > 0;
        }

        public void tick() {
            this.slime.lookAt((Entity)this.slime.getTarget(), 10.0f, 10.0f);
            ((MoveHelperController)this.slime.getMoveControl()).setDirection(this.slime.getYRot(), true);
        }
    }

    static class FaceRandomGoal
    extends Goal {
        private final AnimusBlock slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public FaceRandomGoal(AnimusBlock slimeIn) {
            this.slime = slimeIn;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION)) && this.slime.getMoveControl() instanceof MoveHelperController;
        }

        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = 40 + this.slime.getRandom().nextInt(60);
                this.chosenDegrees = this.slime.getRandom().nextInt(360);
            }
            ((MoveHelperController)this.slime.getMoveControl()).setDirection(this.chosenDegrees, false);
        }
    }

    static class HopGoal
    extends Goal {
        private final AnimusBlock slime;

        public HopGoal(AnimusBlock slimeIn) {
            this.slime = slimeIn;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        public void tick() {
            ((MoveHelperController)this.slime.getMoveControl()).setSpeed(1.0);
        }
    }
}


