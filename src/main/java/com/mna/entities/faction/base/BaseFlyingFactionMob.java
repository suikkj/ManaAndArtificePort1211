 
package com.mna.entities.faction.base;

import com.google.common.collect.Maps;
import com.mna.api.entities.IFactionEnemy;
import com.mna.effects.EffectInit;
import com.mna.entities.IAnimPacketSync;
import com.mna.entities.ai.RetaliateOnAttackGoal;
import com.mna.tools.SummonUtils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseFlyingFactionMob<T extends BaseFlyingFactionMob<?>>
extends FlyingMob
implements Enemy,
IFactionEnemy<T>,
GeoEntity,
IAnimPacketSync<T> {
    private static final EntityDataAccessor<OptionalInt> TARGET_ID = SynchedEntityData.defineId(BaseFlyingFactionMob.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(BaseFlyingFactionMob.class, (EntityDataSerializer)EntityDataSerializers.INT);
    protected static final int RANDOM_FLY_ID = 999;
    protected Player raidTarget;
    protected int tier;
    protected HashMap<String, Integer> damageResists;
    protected AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    protected LivingEntity localTarget;
    protected int randomFlyCooldown = 0;
    protected boolean disableFlyAway = false;
    private final Map<String, Tuple<MutableInt, Runnable>> timers = Maps.newHashMap();
    private final Map<Integer, Integer> cooldowns = Maps.newHashMap();

    protected BaseFlyingFactionMob(EntityType<T> mobType, Level world) {
        super(mobType, world);
        this.setNoGravity(true);
        this.moveControl = new MoveHelperController(this);
        this.damageResists = new HashMap();
    }

    protected void setTimer(String id, int delay, Runnable callback) {
        this.setTimer(id, delay, callback, true);
    }

    protected void setTimer(String id, int delay, Runnable callback, boolean runIfExists) {
        if (runIfExists && this.timers.containsKey(id)) {
            ((Runnable)this.timers.get(id).getB()).run();
        }
        this.timers.put(id, (Tuple<MutableInt, Runnable>)new Tuple(new MutableInt(delay), callback));
    }

    protected void setCooldown(int id, int ticks) {
        this.cooldowns.put(id, ticks);
    }

    protected boolean isOnCooldown(int id) {
        return this.cooldowns.getOrDefault(id, 0) > 0;
    }

    protected int getStateFlag() {
        return (Integer)this.entityData.get(DATA_STATE);
    }

    protected void setState(int ... flags) {
        int finalFlag = 0;
        for (int i : flags) {
            finalFlag |= i;
        }
        this.entityData.set(DATA_STATE, finalFlag);
    }

    public void tick() {
        Vec3 wanted;
        if (this.getTarget() != null && this.getTarget().isAlive()) {
            this.getLookControl().setLookAt((Entity)this.getTarget());
            this.lookAt(EntityAnchorArgument.Anchor.FEET, this.getTarget().getEyePosition());
        } else if (this.getDeltaMovement().length() > (double)0.1f) {
            this.lookAt(EntityAnchorArgument.Anchor.FEET, this.position().add(this.getDeltaMovement().normalize().scale(30.0)));
        }
        if (this.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()))) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.2, 0.0));
        }
        this.cooldowns.keySet().forEach(c -> {
            int cd = this.cooldowns.get(c);
            if (cd > 0) {
                this.cooldowns.put((Integer)c, cd - 1);
            }
        });
        ArrayList timersToRemove = new ArrayList();
        this.timers.forEach((k, v) -> {
            ((MutableInt)this.timers.get(k).getA()).subtract(1);
            if (((MutableInt)this.timers.get(k).getA()).getValue() <= 0) {
                timersToRemove.add(k);
            }
        });
        timersToRemove.forEach(id -> {
            ((Runnable)this.timers.get(id).getB()).run();
            this.timers.remove(id);
        });
        if (this.randomFlyCooldown > 0 && this.isOnCooldown(999) && this.getDeltaMovement().length() > 0.0 && this.distanceToSqr(wanted = new Vec3(this.getMoveControl().getWantedX(), this.getMoveControl().getWantedY(), this.getMoveControl().getWantedZ())) < 1.0) {
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 1.0);
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_ID, OptionalInt.empty());
        builder.define(DATA_STATE, 0);
    }

    public void setTarget(LivingEntity pLivingEntity) {
        super.setTarget(pLivingEntity);
        if (!this.level().isClientSide()) {
            this.entityData.set(TARGET_ID, (pLivingEntity != null ? OptionalInt.of(pLivingEntity.getId()) : OptionalInt.empty()));
        }
    }

    public LivingEntity getTarget() {
        Entity e;
        if (!this.level().isClientSide()) {
            return super.getTarget();
        }
        OptionalInt targetId = (OptionalInt)this.entityData.get(TARGET_ID);
        if (!targetId.isPresent()) {
            return null;
        }
        if ((this.localTarget == null || this.localTarget.getId() != targetId.getAsInt()) && (e = this.level().getEntity(targetId.getAsInt())) instanceof LivingEntity) {
            this.localTarget = (LivingEntity)e;
        }
        return this.localTarget;
    }

    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
    }

    public boolean isPushable() {
        return false;
    }

    protected void doPush(Entity entityIn) {
    }

    protected void pushEntities() {
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(5, (Goal)new BaseFlyingFactionMobFlyGoal(this));
        this.goalSelector.addGoal(7, (Goal)new LookAroundGoal(this));
        this.targetSelector.addGoal(1, (Goal)new RetaliateOnAttackGoal((Mob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, (java.util.function.Predicate<net.minecraft.world.entity.LivingEntity>)(t -> this.factionTargetPlayerPredicate(t))));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal<Mob>(this, Mob.class, 10, true, false, (java.util.function.Predicate<net.minecraft.world.entity.LivingEntity>)(t -> this.factionTargetHelpPredicate(t))));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal<Mob>(this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction() && e.isAlive()));
    }

    public float getStepHeight() {
        return 1.2f;
    }

    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, SpawnGroupData spawnDataIn, CompoundTag dataTag) {
        this.applyInitialSpawnTier((LevelAccessor)worldIn);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    public boolean canBeLeashed(Player player) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> this.handleAnimState(state));
        this.addControllerListeners(controller);
        registrar.add(new AnimationController[]{controller});
    }

    protected void addControllerListeners(AnimationController<T> controller) {
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    @Override
    public void setRaidTarget(Player player) {
        this.raidTarget = player;
        this.setTarget((LivingEntity)player);
    }

    @Override
    public Player getRaidTarget() {
        return this.raidTarget;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
        this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(ResourceLocation.parse("tier_health_bonus"), (double)(tier + 1), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        this.getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(ResourceLocation.parse("tier_armor_bonus"), (double)(5 * tier), AttributeModifier.Operation.ADD_VALUE));
        this.setHealth(this.getMaxHealth());
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.writeFactionData(compound);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readFactionData(compound);
    }

    @Override
    public HashMap<String, Integer> getDamageResists() {
        return this.damageResists;
    }

    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        amount = this.applyDamageResists(source, amount);
        return super.hurt(source, amount);
    }

    public void checkDespawn() {
        super.checkDespawn();
        this.raidTargetDespawn();
        if (this.level() != null && !this.level().isClientSide() && this.level().getServer() != null && this.level().getServer().getWorldData().getDifficulty() == Difficulty.PEACEFUL) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void die(DamageSource cause) {
        super.die(cause);
        this.onKilled(cause);
    }

    public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf additionalData) {
        this.tier = additionalData.readInt();
    }

    public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.tier);
    }

    protected abstract PlayState handleAnimState(AnimationState<? extends BaseFlyingFactionMob<?>> var1);

    protected float keepDistanceFromAttackTarget() {
        return 10.0f;
    }

    protected float heightAboveAttackTarget() {
        return 3.0f;
    }

    protected float attackTargetRunawayThreshold() {
        return 0.0f;
    }

    protected float attackTargetRunawayDistance() {
        return 0.0f;
    }

    protected boolean canFlyInWater() {
        return false;
    }

    static class MoveHelperController
    extends MoveControl {
        private final BaseFlyingFactionMob<?> parentEntity;
        private int courseChangeCooldown;

        public MoveHelperController(BaseFlyingFactionMob<?> parent) {
            super(parent);
            this.parentEntity = parent;
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO && this.courseChangeCooldown-- <= 0) {
                this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
                Vec3 vector3d = new Vec3(this.wantedX - this.parentEntity.getX(), this.wantedY - this.parentEntity.getY(), this.wantedZ - this.parentEntity.getZ());
                double d0 = vector3d.length();
                if (this.isCollided(vector3d = vector3d.normalize(), Mth.ceil((double)d0))) {
                    this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add(vector3d.scale(0.1)));
                } else {
                    this.operation = MoveControl.Operation.WAIT;
                }
            }
        }

        private boolean isCollided(Vec3 p_220673_1_, int p_220673_2_) {
            AABB axisalignedbb = this.parentEntity.getBoundingBox();
            for (int i = 1; i < p_220673_2_; ++i) {
                axisalignedbb = axisalignedbb.move(p_220673_1_);
                if (this.parentEntity.level().noCollision(this.parentEntity, axisalignedbb)) continue;
                return false;
            }
            return true;
        }
    }

    static class BaseFlyingFactionMobFlyGoal
    extends Goal {
        private final BaseFlyingFactionMob<?> parentEntity;

        public BaseFlyingFactionMobFlyGoal(BaseFlyingFactionMob<?> parent) {
            this.parentEntity = parent;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            MoveControl movementcontroller = this.parentEntity.getMoveControl();
            if (!movementcontroller.hasWanted()) {
                return !this.parentEntity.isOnCooldown(999);
            }
            if (this.parentEntity.getTarget() == null) {
                double d2;
                double d1;
                if (this.parentEntity.isOnCooldown(999)) {
                    return false;
                }
                double d0 = movementcontroller.getWantedX() - this.parentEntity.getX();
                double d3 = d0 * d0 + (d1 = movementcontroller.getWantedY() - this.parentEntity.getY()) * d1 + (d2 = movementcontroller.getWantedZ() - this.parentEntity.getZ()) * d2;
                return d3 < 4.0 || d3 > 3600.0;
            }
            double dist = this.parentEntity.getTarget().distanceToSqr(this.parentEntity);
            return !this.parentEntity.getSensing().hasLineOfSight((Entity)this.parentEntity.getTarget()) || dist > 25.0 || dist < 4.0;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            if (this.parentEntity.getTarget() == null) {
                LivingEntity summoner;
                boolean setRandom = true;
                if (SummonUtils.isSummon(this.parentEntity) && (summoner = SummonUtils.getSummoner(this.parentEntity)) != null && summoner.distanceTo(this.parentEntity) > 16.0f) {
                    setRandom = false;
                    this.parentEntity.getMoveControl().setWantedPosition(summoner.getX(), summoner.getY(), summoner.getZ(), this.parentEntity.getAttributeValue(Attributes.MOVEMENT_SPEED));
                }
                if (setRandom) {
                    RandomSource random = this.parentEntity.getRandom();
                    double d0 = this.parentEntity.getX() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
                    double d1 = this.parentEntity.getY() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
                    double d2 = this.parentEntity.getZ() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
                    int count = 0;
                    int y = (int)d1;
                    boolean ground = false;
                    while (count < 16) {
                        BlockPos bpTestPos = BlockPos.containing((double)d0, (double)y, (double)d2);
                        if (this.parentEntity.level().isEmptyBlock(bpTestPos)) {
                            ++count;
                            --y;
                            continue;
                        }
                        if (this.parentEntity.level().isWaterAt(bpTestPos)) {
                            ++count;
                            if (!this.parentEntity.canFlyInWater()) {
                                ++y;
                            }
                            ground = true;
                            continue;
                        }
                        ground = true;
                        break;
                    }
                    if (!ground) {
                        d1 -= 16.0;
                    }
                    this.parentEntity.getMoveControl().setWantedPosition(d0, d1, d2, 1.0);
                    if (this.parentEntity.randomFlyCooldown > 0) {
                        this.parentEntity.setCooldown(999, this.parentEntity.randomFlyCooldown);
                    }
                }
            } else if (!this.parentEntity.disableFlyAway) {
                Vec3 direction = this.parentEntity.position().subtract(this.parentEntity.getTarget().position()).normalize();
                direction = direction.add(0.0, -direction.y, 0.0);
                Vec3 offset = direction.scale((double)this.parentEntity.keepDistanceFromAttackTarget());
                Vec3 target = this.parentEntity.getTarget().position().add(offset);
                if (this.parentEntity.getTarget().onGround()) {
                    target = target.add(0.0, (double)this.parentEntity.heightAboveAttackTarget(), 0.0);
                }
                Vec3 vec3 = new Vec3(target.x, 0.0, target.z);
                Vec3 vec32 = new Vec3(this.parentEntity.getTarget().getX(), 0.0, this.parentEntity.getTarget().getZ());
                if (vec3.distanceTo(vec32) < (double)this.parentEntity.attackTargetRunawayThreshold()) {
                    float dist = this.parentEntity.attackTargetRunawayDistance();
                    target = target.add(Math.random() * (double)dist / 2.0 - (double)dist, 0.0, Math.random() * (double)dist / 2.0 - (double)dist);
                }
                this.parentEntity.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.0);
            }
        }
    }

    static class LookAroundGoal
    extends Goal {
        private final BaseFlyingFactionMob<?> parentEntity;

        public LookAroundGoal(BaseFlyingFactionMob<?> parent) {
            this.parentEntity = parent;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return true;
        }

        public void tick() {
            if (this.parentEntity.getTarget() == null) {
                Vec3 vector3d = this.parentEntity.getDeltaMovement();
                this.parentEntity.setXRot(-((float)Mth.atan2((double)vector3d.x, (double)vector3d.z)) * 57.295776f);
                this.parentEntity.yBodyRot = this.parentEntity.getYRot();
            } else {
                LivingEntity livingentity = this.parentEntity.getTarget();
                if (livingentity.distanceToSqr(this.parentEntity) < 4096.0) {
                    double d1 = livingentity.getX() - this.parentEntity.getX();
                    double d2 = livingentity.getZ() - this.parentEntity.getZ();
                    this.parentEntity.setYRot(-((float)Mth.atan2((double)d1, (double)d2)) * 57.295776f);
                    this.parentEntity.yBodyRot = this.parentEntity.getYRot();
                }
            }
        }
    }
}


