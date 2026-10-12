 
package com.mna.entities.faction;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.DamageHelper;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.faction.base.BaseFlyingFactionMob;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import com.mna.api.tools.MNACap;

public class LivingWard
extends BaseFlyingFactionMob<LivingWard> {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(LivingWard.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DETONATING = SynchedEntityData.defineId(LivingWard.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);

    public LivingWard(EntityType<LivingWard> mobType, Level world) {
        super(mobType, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.spawnParticles();
        }
    }

    private void spawnParticles() {
        if (this.isDeadOrDying()) {
            return;
        }
        boolean detonating = (Boolean)this.entityData.get(DETONATING);
        int numParticles = detonating ? 10 : 2;
        MAParticleType particleType = detonating ? (MAParticleType)(ParticleInit.BLUE_SPARKLE_VELOCITY.get()) : (MAParticleType)(ParticleInit.BLUE_SPARKLE_GRAVITY.get());
        float scale = detonating ? 0.1f : 0.01f;
        for (int i = 0; i < numParticles; ++i) {
            Vec3 particleVelocity = Vec3.ZERO;
            if (detonating) {
                particleVelocity = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
                particleVelocity.scale(0.25);
            }
            this.level().addParticle((ParticleOptions)new MAParticleType(particleType).setScale(scale), this.getX() - 0.3 + Math.random() * 0.6, this.getY() + Math.random() * 1.2, this.getZ() - 0.3 + Math.random() * 0.6, particleVelocity.x, particleVelocity.y, particleVelocity.z);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, (Goal)new DetonateGoal(this));
        this.goalSelector.addGoal(7, (Goal)new BoltAttackGoal(this));
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(DETONATING, false);
    }

    public void onRemovedFromLevel() {
        if (this.level().isClientSide() && ((Boolean)this.entityData.get(DETONATING)).booleanValue()) {
            for (int i = 0; i < 150; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get())), this.getX(), this.getY(), this.getZ(), -0.5 + Math.random(), -0.5 + Math.random(), -0.5 + Math.random());
            }
        }
    }

    @Override
    public IFaction getFaction() {
        return Factions.COUNCIL;
    }

    @Override
    public CompoundTag getPacketData() {
        return new CompoundTag();
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFlyingFactionMob<?>> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.arcane_eye.idle"));
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, (double)0.6f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 0.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    public static boolean canSpawnPredicate(EntityType<LivingWard> type, ServerLevelAccessor worldIn, MobSpawnType reason, BlockPos pos, RandomSource randomIn) {
        if (IFactionEnemy.canMnASpawn((LevelAccessor)worldIn, pos)) {
            return worldIn.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn((ServerLevelAccessor)worldIn, (BlockPos)pos, (RandomSource)randomIn) && LivingWard.checkMobSpawnRules(type, (LevelAccessor)worldIn, (MobSpawnType)reason, (BlockPos)pos, (RandomSource)randomIn);
        }
        return false;
    }

    static class DetonateGoal
    extends Goal {
        private final LivingWard parentEntity;
        private int updateCount = 0;

        public DetonateGoal(LivingWard ward) {
            this.parentEntity = ward;
        }

        public boolean canUse() {
            if (this.parentEntity.getTarget() == null || !(this.parentEntity.getTarget() instanceof Player)) {
                return false;
            }
            boolean lowHealth = this.parentEntity.getHealth() < this.parentEntity.getHealth() * 0.35f;
            boolean targetTooClose = this.parentEntity.getTarget().distanceTo((Entity)this.parentEntity) <= 3.0f;
            return lowHealth || targetTooClose;
        }

        public boolean canContinueToUse() {
            return this.parentEntity.getTarget() != null;
        }

        public void start() {
            this.parentEntity.entityData.set(DETONATING, true);
            this.parentEntity.disableFlyAway = true;
        }

        public void tick() {
            LivingEntity target = this.parentEntity.getTarget();
            if (target == null) {
                return;
            }
            this.parentEntity.getMoveControl().setWantedPosition(target.getX(), target.getEyeY(), target.getZ(), 0.5);
            if (this.updateCount > 40) {
                this.parentEntity.level().getEntities((Entity)this.parentEntity, this.parentEntity.getBoundingBox().inflate(7.5), e -> {
                    if (!(e instanceof Player) || !e.isAlive()) {
                        return false;
                    }
                    IPlayerProgression progression = MNACap.get((IPlayerProgression)((Player)e), PlayerProgressionProvider.PROGRESSION).orElse(null);
                    return progression != null && progression.getAlliedFaction() != null && progression.getAlliedFaction() != Factions.COUNCIL;
                }).stream().map(e -> (Player)e).forEach(p -> MNACap.get(p, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().consume((LivingEntity)p, m.getCastingResource().getMaxAmount() * 0.5f)));
                this.parentEntity.remove(Entity.RemovalReason.KILLED);
            }
            ++this.updateCount;
        }
    }

    static class BoltAttackGoal
    extends Goal {
        private final LivingWard parentEntity;
        public int attackTimer;

        public BoltAttackGoal(LivingWard ward) {
            this.parentEntity = ward;
        }

        public boolean canUse() {
            return this.parentEntity.getTarget() != null;
        }

        public void start() {
            this.attackTimer = 0;
        }

        public void stop() {
            this.attackTimer = 0;
            this.parentEntity.setAttacking(false);
        }

        public boolean canContinueToUse() {
            return this.parentEntity.getTarget() != null && this.parentEntity.getTarget().isAlive() && this.attackTimer < 65;
        }

        public void tick() {
            LivingEntity livingentity = this.parentEntity.getTarget();
            if (livingentity == null) {
                return;
            }
            if (livingentity.distanceToSqr((Entity)this.parentEntity) < 144.0 && this.parentEntity.getSensing().hasLineOfSight((Entity)livingentity)) {
                ++this.attackTimer;
                if (this.attackTimer == 40) {
                    this.parentEntity.setAttacking(true);
                }
                if (this.attackTimer == 42) {
                    if (!this.parentEntity.isSilent()) {
                        this.parentEntity.playSound(SFX.Spell.Cast.ARCANE, 1.0f, (float)((double)0.9f + Math.random() * (double)0.2f));
                    }
                    boolean isMiss = Math.random() > (double)(0.3f + (float)this.parentEntity.getTier() * 0.5f);
                    Vec3 offset = new Vec3(0.0, 0.0, 0.0);
                    if (isMiss) {
                        offset = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
                    }
                    int color = -10545027;
                    ServerMessageDispatcher.sendParticleSpawn(this.parentEntity.getX(), this.parentEntity.getY(), this.parentEntity.getZ(), this.parentEntity.getTarget().getX() + offset.x(), this.parentEntity.getTarget().getY() + (double)this.parentEntity.getTarget().getEyeHeight() + offset.y(), this.parentEntity.getTarget().getZ() + offset.z(), color, 64.0f, (ResourceKey<Level>)this.parentEntity.level().dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
                    if (!isMiss) {
                        if (this.parentEntity.getTarget().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DAMPEN_MAGIC.get())) == null) {
                            this.parentEntity.getTarget().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.DAMPEN_MAGIC.get()), 100));
                        }
                        DamageSource damageType = DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.MAGIC, this.parentEntity.level().registryAccess(), (Entity)this.parentEntity);
                        this.parentEntity.getTarget().hurt(damageType, 8.0f);
                        LivingEntity livingEntity = this.parentEntity.getTarget();
                        if (livingEntity instanceof Player) {
                            Player playerTarget = (Player)livingEntity;
                            MNACap.get(playerTarget, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().consume((LivingEntity)playerTarget, m.getCastingResource().getMaxAmount() * 0.05f));
                        }
                    }
                } else if (this.attackTimer >= 62) {
                    this.parentEntity.setAttacking(false);
                }
            } else if (this.attackTimer > 0) {
                --this.attackTimer;
            }
        }
    }
}

