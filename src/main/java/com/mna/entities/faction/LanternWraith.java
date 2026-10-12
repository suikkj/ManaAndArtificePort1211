 
package com.mna.entities.faction;

import com.mna.api.config.GeneralConfigValues;
import com.mna.api.entities.DamageHelper;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.faction.base.BaseFlyingFactionMob;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import com.mna.api.tools.MNACap;

public class LanternWraith
extends BaseFlyingFactionMob<LanternWraith>
implements RangedAttackMob {
    private static final String INSTRUCTION_BOLT = "bolt";
    private static final String INSTRUCTION_AFFECT = "affect";
    private boolean isAttacking = false;

    public LanternWraith(EntityType<LanternWraith> type, Level worldIn) {
        super(type, worldIn);
    }

    public static boolean canSpawnPredicate(EntityType<LanternWraith> type, LevelAccessor worldIn, MobSpawnType reason, BlockPos pos, RandomSource randomIn) {
        if (worldIn.getDifficulty() == Difficulty.PEACEFUL || !IFactionEnemy.canMnASpawn(worldIn, pos)) {
            return false;
        }
        if (randomIn.nextBoolean()) {
            return false;
        }
        if (randomIn.nextInt(15) > 7) {
            return false;
        }
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        do {
            blockpos$mutableblockpos.move(Direction.UP);
        } while (worldIn.getFluidState((BlockPos)blockpos$mutableblockpos).is(FluidTags.LAVA));
        if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos).isAir()) {
            int existingWraiths;
            return GeneralConfigValues.LanternWraithSpawnBoundsSize <= 0 || (existingWraiths = worldIn.getEntitiesOfClass(LanternWraith.class, AABB.ofSize((Vec3)Vec3.atCenterOf((Vec3i)blockpos$mutableblockpos), (double)GeneralConfigValues.LanternWraithSpawnBoundsSize, (double)GeneralConfigValues.LanternWraithSpawnBoundsSize, (double)GeneralConfigValues.LanternWraithSpawnBoundsSize)).size()) <= GeneralConfigValues.LanternWraithGroupingLimit;
        }
        return false;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFlyingFactionMob<?>> state) {
        if (!this.isAttacking) {
            if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ModelLanternWraith.move"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ModelLanternWraith.idle"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.ModelLanternWraith.attack"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(2, (Goal)new RangedAttackGoal((RangedAttackMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), 40, 80, 12.0f));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SFX.Entity.LanternWraith.HURT;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.LanternWraith.DEATH;
    }

    public SoundEvent getAmbientSound() {
        return SFX.Entity.LanternWraith.IDLE;
    }

    @Override
    public IFaction getFaction() {
        return Factions.DEMONS;
    }

    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.isAttacking) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_BOLT, 20, target, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 40, "", this::handleDelayCallback));
        this.isAttacking = true;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 1.0f, (float)(0.9 + Math.random() * 0.2));
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    public void spawnSoulFireBolt(Entity target) {
        if (!target.isAlive()) {
            return;
        }
        ServerMessageDispatcher.sendParticleSpawn(this.getX(), this.getY() + (double)1.1f, this.getZ(), target.getX(), target.getY() + (double)target.getEyeHeight(), target.getZ(), 0, 64.0f, (ResourceKey<Level>)this.level().dimension(), (MAParticleType)(ParticleInit.WRAITH_BOLT.get()));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Vec3>(INSTRUCTION_AFFECT, 20, target.position(), this::tryAffectTarget));
    }

    public void tryAffectTarget(String identifier, Vec3 position) {
        LivingEntity entity = this.getTarget();
        if (entity == null) {
            return;
        }
        net.minecraft.core.Holder<MobEffect> goalEffect = MobEffects.MOVEMENT_SLOWDOWN;
        int duration = 60;
        boolean increaseMagnitude = true;
        if (this.tier >= 2 && entity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFE_TAP.get())) == null) {
            goalEffect = EffectInit.LIFE_TAP;
            duration = 200;
            increaseMagnitude = false;
        } else if (this.tier >= 3 && !entity.onGround()) {
            goalEffect = EffectInit.GRAVITY_WELL;
            duration = 100;
            increaseMagnitude = false;
        }
        double dist = entity.distanceToSqr(position);
        if (!entity.isAlive() || dist > 4.0) {
            return;
        }
        if (entity instanceof Player) {
            MNACap.get(entity, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().consume(entity, 100.0f));
        }
        int magnitude = 0;
        int damage = 5;
        MobEffectInstance inst = entity.getEffect(goalEffect);
        if (inst != null) {
            if (increaseMagnitude) {
                damage += inst.getAmplifier() * 2;
                if ((magnitude += inst.getAmplifier() + 1) > 2) {
                    magnitude = 2;
                }
            } else {
                damage += 5;
            }
        }
        entity.addEffect(new MobEffectInstance(goalEffect, duration, magnitude));
        entity.hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.MAGIC, this.level().registryAccess(), (Entity)this), (float)damage);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, (double)0.55f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    private void handleDelayCallback(String identifier, LivingEntity entity) {
        if (!this.level().isClientSide() && this.isAlive()) {
            switch (identifier) {
                case "bolt": {
                    this.spawnSoulFireBolt((Entity)entity);
                }
            }
        }
    }

    private void handleDelayCallback(String identifier, String data) {
        if (!this.level().isClientSide()) {
            this.isAttacking = false;
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isAttacking);
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.isAttacking = nbt.getBoolean("attacking");
    }

    @Override
    protected float attackTargetRunawayDistance() {
        return 10.0f;
    }

    @Override
    protected float attackTargetRunawayThreshold() {
        return 4.0f;
    }
}

