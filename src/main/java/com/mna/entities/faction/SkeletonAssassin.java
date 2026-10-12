 
package com.mna.entities.faction;

import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.ai.AIPredicateWrapperGoal;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mna.entities.projectile.SkeletonAssassinBolo;
import com.mna.entities.projectile.SkeletonAssassinShuriken;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.math.Vector3;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class SkeletonAssassin
extends BaseFactionMob<SkeletonAssassin>
implements RangedAttackMob {
    private static final String INSTRUCTION_SHURIKEN = "shuriken";
    private static final String INSTRUCTION_MELEE = "melee";
    private static final String INSTRUCTION_DAMAGE = "damage";
    private static final String INSTRUCTION_BOLO = "bolo";
    private static final String INSTRUCTION_SMOKEBOMB = "smokebomb";
    private static final String INSTRUCTION_BACKFLIP = "backflip";
    private static final int COOLDOWN_BOLO = 300;
    private static final int COOLDOWN_BACKFLIP = 200;
    private static final int COOLDOWN_SMOKEBOMB = 1200;
    private boolean isActing = false;
    private boolean throwingShuriken = false;
    private boolean smokeBomb = false;
    private boolean throwingBolo = false;
    private boolean backflipping = false;
    private int boloCooldown = 0;
    private int backflipCooldown = 0;
    private int smokeBombCooldown = 0;
    private boolean instantTransition = false;

    public SkeletonAssassin(EntityType<SkeletonAssassin> type, Level worldIn) {
        super(type, worldIn);
    }

    public SkeletonAssassin(Level worldIn) {
        this((EntityType<SkeletonAssassin>)((EntityType)EntityInit.SKELETON_ASSASSIN.get()), worldIn);
    }

    public static boolean canSpawnPredicate(EntityType<SkeletonAssassin> p_234351_0_, LevelAccessor p_234351_1_, MobSpawnType p_234351_2_, BlockPos p_234351_3_, RandomSource p_234351_4_) {
        if (p_234351_1_ instanceof ServerLevelAccessor) {
            return p_234351_3_.getY() <= 60 && Monster.checkMonsterSpawnRules(p_234351_0_, (ServerLevelAccessor)((ServerLevelAccessor)p_234351_1_), (MobSpawnType)p_234351_2_, (BlockPos)p_234351_3_, (RandomSource)p_234351_4_);
        }
        return false;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> state) {
        if (this.instantTransition) {
            state.getController().transitionLength(0);
            this.instantTransition = false;
        } else {
            state.getController().transitionLength(5);
        }
        if (!this.isActing) {
            if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.skeleton_assassin.run"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.skeleton_assassin.idle"));
        }
        if (this.throwingShuriken) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.skeleton_assassin.quick_throw"));
        }
        if (this.smokeBomb) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.skeleton_assassin.smoke_bomb"));
        }
        if (this.throwingBolo) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.skeleton_assassin.outerhand_throw"));
        }
        if (this.backflipping) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.skeleton_assassin.backflip"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation.skeleton_assassin.slash"));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.boloCooldown > 0) {
            --this.boloCooldown;
        }
        if (this.backflipCooldown > 0) {
            --this.backflipCooldown;
        }
        if (this.smokeBombCooldown > 0) {
            --this.smokeBombCooldown;
        }
        if (this.isAlive() && this.isSunBurnTick()) {
            this.igniteForSeconds((float)(8));
        }
    }

    @Override
    public IFaction getFaction() {
        return Factions.UNDEAD;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, new AIPredicateWrapperGoal<SkeletonAssassin>(this, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), false)).executionPredicate(me -> me.getTarget() != null && me.getTarget().isAlive() && me.isInvisible()));
        this.goalSelector.addGoal(2, (Goal)new FleeSunGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        this.goalSelector.addGoal(2, (Goal)new FactionTierWrapperGoal(2, this, new SmokeBombGoal()));
        this.goalSelector.addGoal(3, (Goal)new FactionTierWrapperGoal(1, this, new BoloGoal(this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), 20, 15, 24.0f)));
        this.goalSelector.addGoal(4, (Goal)new FactionTierWrapperGoal(1, this, new BackflipGoal()));
        this.goalSelector.addGoal(5, (Goal)new ShootGoal(this.getAttributeValue(Attributes.MOVEMENT_SPEED), 15, 30, 12.0f));
        this.goalSelector.addGoal(6, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), false));
        this.goalSelector.addGoal(7, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.35f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, this::factionTargetPlayerPredicate));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, this::factionTargetHelpPredicate));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_DAMAGE, 12, target, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 20, INSTRUCTION_SHURIKEN, this::handleDelayCallback));
        this.isActing = true;
        this.throwingShuriken = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    private void throwBolo(LivingEntity target, float distanceFactor) {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_BOLO, 17, target, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 22, INSTRUCTION_BOLO, this::handleDelayCallback));
        this.isActing = true;
        this.throwingBolo = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    private void throwSmokeBomb() {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_SMOKEBOMB, 28, (LivingEntity)this, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 30, INSTRUCTION_SMOKEBOMB, this::handleDelayCallback));
        this.isActing = true;
        this.smokeBomb = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    private void doBackflip() {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_BACKFLIP, 31, (LivingEntity)this, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 32, INSTRUCTION_BACKFLIP, this::handleDelayCallback));
        this.isActing = true;
        this.backflipping = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    public boolean spawnShuriken(Entity target) {
        Vec3 direction = target == this ? Vec3.directionFromRotation((Vec2)this.getRotationVector()).normalize() : target.position().add(0.0, (double)(target.getBbHeight() * 0.5f), 0.0).subtract(this.position()).normalize();
        SkeletonAssassinShuriken shuriken = new SkeletonAssassinShuriken(this.level(), (LivingEntity)this);
        shuriken.setOwner((Entity)this);
        shuriken.shoot(direction.x, direction.y, direction.z, 1.6f, 0.0f);
        this.level().addFreshEntity((Entity)shuriken);
        this.playSound(SFX.Entity.SkeletonAssassin.SHURIKEN_THROW, 1.0f, (float)(0.9 + Math.random() * 0.2));
        return true;
    }

    public boolean spawnBolo(Entity target) {
        Vec3 direction = target.position().add(target.getDeltaMovement()).add(0.0, (double)(target.getBbHeight() * 0.5f), 0.0).subtract(this.position()).normalize();
        SkeletonAssassinBolo bolo = new SkeletonAssassinBolo(this.level(), (LivingEntity)this);
        bolo.setOwner((Entity)this);
        bolo.shoot(direction.x, direction.y, direction.z, 2.5f, 0.0f);
        this.level().addFreshEntity((Entity)bolo);
        this.playSound(SFX.Entity.SkeletonAssassin.BOLO_THROW, 1.0f, (float)(0.9 + Math.random() * 0.2));
        return true;
    }

    public SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.SKELETON_HURT;
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (this.isActing) {
            return true;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>(INSTRUCTION_DAMAGE, 10, entityIn, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 20, INSTRUCTION_MELEE, this::handleDelayCallback));
        this.isActing = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.backflipping && !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.OUTSIDE_BORDER)) {
            return false;
        }
        if (source.is(DamageTypes.FALL)) {
            return false;
        }
        if (source.is(DamageTypes.FREEZE)) {
            amount *= 0.5f;
        }
        return super.hurt(source, amount);
    }

    
    public boolean damageEntity(Entity entityIn) {
        boolean flag;
        int i;
        if (this.getTarget() == null) {
            return false;
        }
        double dist = this.distanceToSqr((Entity)this.getTarget());
        if (dist > 9.0) {
            return false;
        }
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entityIn instanceof LivingEntity) {
            f += EnchantmentHelper.getDamageBonus((ItemStack)this.getMainHandItem(), (LivingEntity)entityIn);
            f1 += (float)EnchantmentHelper.getKnockbackBonus((LivingEntity)this);
            if (this.isInvisible() || ((LivingEntity)entityIn).hasEffect(MobEffects.BLINDNESS)) {
                if (!((LivingEntity)entityIn).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_SIGHT.get()))) {
                    f *= (float)(this.getTier() + 2);
                }
                this.removeEffect(MobEffects.INVISIBILITY);
            }
        }
        if ((i = EnchantmentHelper.getFireAspect((LivingEntity)this)) > 0 || this.isOnFire()) {
            entityIn.igniteForSeconds((float)(i * 4));
        }
        if (flag = entityIn.hurt(this.damageSources().mobAttack((LivingEntity)this), f)) {
            if (f1 > 0.0f && entityIn instanceof LivingEntity) {
                ((LivingEntity)entityIn).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if (entityIn instanceof Player) {
                Player playerentity = (Player)entityIn;
                this.maybeDisableShield(playerentity, this.getMainHandItem(), playerentity.isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY);
            }
            this.doEnchantDamageEffects((LivingEntity)this, entityIn);
            this.setLastHurtMob(entityIn);
        }
        return flag;
    }

    private void maybeDisableShield(Player p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_) {
        if (!p_233655_2_.isEmpty() && !p_233655_3_.isEmpty() && p_233655_2_.getItem() instanceof AxeItem && p_233655_3_.getItem() == Items.SHIELD) {
            float f = 0.25f + (float)EnchantmentHelper.getBlockEfficiency((LivingEntity)this) * 0.05f;
            if (this.random.nextFloat() < f) {
                p_233655_1_.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level().broadcastEntityEvent((Entity)p_233655_1_, (byte)30);
            }
        }
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, (double)0.6f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    private void handleDelayCallback(String identifier, Entity entity) {
        if (this.isAlive()) {
            if (!this.level().isClientSide()) {
                switch (identifier) {
                    case "damage": {
                        if (this.throwingShuriken) {
                            this.spawnShuriken(entity);
                            break;
                        }
                        this.damageEntity(entity);
                        break;
                    }
                    case "bolo": {
                        this.spawnBolo(entity);
                        this.boloCooldown = 300;
                        break;
                    }
                    case "smokebomb": {
                        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200));
                        this.playSound(SFX.Entity.SkeletonAssassin.SMOKE_BOMB, 1.0f, (float)(0.8 + Math.random() * 0.2));
                        this.smokeBombCooldown = 1200;
                        break;
                    }
                    case "backflip": {
                        this.backflipCooldown = 200;
                    }
                }
            } else {
                switch (identifier) {
                    case "smokebomb": {
                        for (int i = 0; i < 50; ++i) {
                            this.level().addParticle((ParticleOptions)ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX() - 0.25 + Math.random() * 0.5, this.getY() + Math.random() * 2.0, this.getZ() - 0.25 + Math.random() * 0.5, 0.0, 0.0, 0.0);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void handleDelayCallback(String identifier, String data) {
        if (!this.level().isClientSide()) {
            boolean wasFlipping = this.backflipping;
            switch (data) {
                case "shuriken": {
                    this.throwingShuriken = false;
                    break;
                }
                case "bolo": {
                    this.throwingBolo = false;
                    break;
                }
                case "smokebomb": {
                    this.smokeBomb = false;
                    break;
                }
                case "backflip": {
                    this.backflipping = false;
                }
            }
            boolean bl = this.isActing = this.throwingShuriken || this.throwingBolo || this.smokeBomb || this.backflipping;
            if (!this.backflipping && wasFlipping) {
                this.instantTransition = true;
            }
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isActing);
        nbt.putBoolean(INSTRUCTION_SHURIKEN, this.throwingShuriken);
        nbt.putBoolean(INSTRUCTION_BOLO, this.throwingBolo);
        nbt.putBoolean("smoke", this.smokeBomb);
        nbt.putBoolean(INSTRUCTION_BACKFLIP, this.backflipping);
        nbt.putBoolean("instant", this.instantTransition);
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.isActing = nbt.getBoolean("attacking");
        this.throwingShuriken = nbt.getBoolean(INSTRUCTION_SHURIKEN);
        this.throwingBolo = nbt.getBoolean(INSTRUCTION_BOLO);
        this.smokeBomb = nbt.getBoolean("smoke");
        this.backflipping = nbt.getBoolean(INSTRUCTION_BACKFLIP);
        this.instantTransition = nbt.getBoolean("instant");
        if (this.smokeBomb) {
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_SMOKEBOMB, 22, (LivingEntity)this, this::handleDelayCallback));
        }
    }

    public class SmokeBombGoal
    extends Goal {
        public SmokeBombGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        public boolean canUse() {
            return SkeletonAssassin.this.getTarget() != null && !SkeletonAssassin.this.isActing && SkeletonAssassin.this.smokeBombCooldown == 0;
        }

        public boolean canContinueToUse() {
            return SkeletonAssassin.this.isActing && SkeletonAssassin.this.throwingBolo;
        }

        public void start() {
            SkeletonAssassin.this.throwSmokeBomb();
        }
    }

    public class BoloGoal
    extends Goal {
        private int rangedAttackTime = -1;
        private final int attackIntervalMin;
        private final int maxRangedAttackTime;
        private final float attackRadius;
        private final float maxAttackDistance;

        public BoloGoal(RangedAttackMob attacker, double movespeed, int maxAttackTime, float maxAttackDistanceIn) {
            this(attacker, movespeed, maxAttackTime, maxAttackTime, maxAttackDistanceIn);
        }

        public BoloGoal(RangedAttackMob attacker, double movespeed, int p_i1650_4_, int maxAttackTime, float maxAttackDistanceIn) {
            this.attackIntervalMin = p_i1650_4_;
            this.maxRangedAttackTime = maxAttackTime;
            this.attackRadius = maxAttackDistanceIn;
            this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (SkeletonAssassin.this.isActing || SkeletonAssassin.this.boloCooldown > 0) {
                return false;
            }
            LivingEntity livingentity = SkeletonAssassin.this.getTarget();
            return livingentity != null && livingentity.isAlive() && livingentity.getY() > SkeletonAssassin.this.getY() + 2.0 && (livingentity.hasImpulse || !livingentity.onGround() || livingentity instanceof Player && ((Player)livingentity).getAbilities().flying);
        }

        public boolean canContinueToUse() {
            return this.canUse() && !SkeletonAssassin.this.getNavigation().isStuck();
        }

        public void stop() {
            this.rangedAttackTime = -1;
        }

        public void tick() {
            LivingEntity e = SkeletonAssassin.this.getTarget();
            if (e == null) {
                return;
            }
            double distance = SkeletonAssassin.this.distanceToSqr(e.getX(), e.getY(), e.getZ());
            boolean canSeeTarget = SkeletonAssassin.this.getSensing().hasLineOfSight((Entity)e);
            if (distance <= (double)this.maxAttackDistance && canSeeTarget) {
                SkeletonAssassin.this.getNavigation().stop();
            } else {
                SkeletonAssassin.this.getNavigation().moveTo((Entity)e, SkeletonAssassin.this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            }
            SkeletonAssassin.this.getLookControl().setLookAt((Entity)e, 30.0f, 30.0f);
            if (--this.rangedAttackTime == 0) {
                if (!canSeeTarget) {
                    return;
                }
                float f = Mth.sqrt((float)((float)distance)) / this.attackRadius;
                float lvt_5_1_ = Mth.clamp((float)f, (float)0.1f, (float)1.0f);
                SkeletonAssassin.this.throwBolo(e, lvt_5_1_);
                this.rangedAttackTime = Mth.floor((float)(f * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin));
            } else if (this.rangedAttackTime < 0) {
                float f2 = Mth.sqrt((float)((float)distance)) / this.attackRadius;
                this.rangedAttackTime = Mth.floor((float)(f2 * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin));
            }
        }
    }

    public class BackflipGoal
    extends Goal {
        private Vector3 start;
        private Vector3 end;
        private int tick_count;
        private float move_time = 31.0f;

        public boolean canUse() {
            if (SkeletonAssassin.this.isInvisible()) {
                return false;
            }
            LivingEntity target = SkeletonAssassin.this.getTarget();
            if (target != null && !SkeletonAssassin.this.isActing && SkeletonAssassin.this.backflipCooldown == 0) {
                double dist = SkeletonAssassin.this.distanceToSqr((Entity)target);
                if (dist > 9.0) {
                    return false;
                }
                Vec3 startPos = SkeletonAssassin.this.position().add(0.0, 0.5, 0.0);
                Vec3 delta = startPos.subtract(target.position().add(0.0, 0.5, 0.0)).normalize();
                delta = new Vec3(delta.x, 0.0, delta.z);
                float searchDist = 20.0f;
                Vec3 endPos = startPos.add(delta.scale((double)searchDist));
                BlockPos bp = BlockPos.containing((Position)endPos);
                int vCount = 0;
                int count = 0;
                int vSearch = 5;
                int tSearch = 50;
                while (!SkeletonAssassin.this.level().getBlockState(bp).getCollisionShape((BlockGetter)SkeletonAssassin.this.level(), bp).isEmpty() && count < tSearch && searchDist > 10.0f) {
                    ++count;
                    if (++vCount == vSearch) {
                        vCount = 0;
                        endPos = startPos.add(delta.scale((double)(searchDist -= 1.0f)));
                    } else {
                        endPos = endPos.add(0.0, 1.0, 0.0);
                    }
                    bp = BlockPos.containing((Position)endPos);
                }
                if (count >= tSearch) {
                    return false;
                }
                ClipContext ctx = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)SkeletonAssassin.this);
                BlockHitResult brtr = SkeletonAssassin.this.level().clip(ctx);
                if (brtr.getType() == HitResult.Type.MISS) {
                    this.start = new Vector3(SkeletonAssassin.this.position());
                    this.end = new Vector3(endPos.subtract(0.0, 0.5, 0.0));
                } else {
                    this.start = new Vector3(SkeletonAssassin.this.position());
                    this.end = new Vector3(brtr.getLocation().add(delta));
                }
                return this.start != null && this.end != null && !(this.start.distanceSqTo(this.end) < 5.0);
            }
            return false;
        }

        public void start() {
            super.start();
            SkeletonAssassin.this.doBackflip();
        }

        public boolean canContinueToUse() {
            return SkeletonAssassin.this.isActing && SkeletonAssassin.this.backflipping;
        }

        public void stop() {
            super.stop();
            this.start = null;
            this.end = null;
            this.tick_count = 0;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public boolean isInterruptable() {
            return false;
        }

        public void tick() {
            ++this.tick_count;
            if ((float)this.tick_count <= this.move_time) {
                Vector3 interp_pos = Vector3.lerp(this.start, this.end, (float)this.tick_count / this.move_time);
                SkeletonAssassin.this.setPos(interp_pos.x, interp_pos.y, interp_pos.z);
                SkeletonAssassin.this.setOnGround(true);
            }
        }
    }

    public class ShootGoal
    extends Goal {
        private final double speedModifier;
        private int attackIntervalMin;
        private int attackIntervalMax;
        private final float attackRadiusSqr;
        private int attackTime = -1;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;

        public ShootGoal(double pSpeedModifier, int pAttackIntervalMin, int attackIntervalMax, float pAttackRadius) {
            this.speedModifier = pSpeedModifier;
            this.attackIntervalMin = pAttackIntervalMin;
            this.attackIntervalMax = attackIntervalMax;
            this.attackRadiusSqr = pAttackRadius * pAttackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (SkeletonAssassin.this.backflipping) {
                this.attackTime = -1;
                return false;
            }
            if (SkeletonAssassin.this.throwingBolo || SkeletonAssassin.this.isInvisible() || SkeletonAssassin.this.smokeBomb) {
                return false;
            }
            LivingEntity livingentity = SkeletonAssassin.this.getTarget();
            return livingentity != null && !livingentity.isDeadOrDying();
        }

        public boolean canContinueToUse() {
            if (!this.canUse()) {
                return false;
            }
            return !SkeletonAssassin.this.getNavigation().isDone();
        }

        public void stop() {
            SkeletonAssassin.this.getNavigation().stop();
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = SkeletonAssassin.this.getTarget();
            if (livingentity != null) {
                double distance = SkeletonAssassin.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                boolean hasLineOfSight = SkeletonAssassin.this.getSensing().hasLineOfSight((Entity)livingentity);
                if (!SkeletonAssassin.this.isActing) {
                    if (distance <= (double)this.attackRadiusSqr && hasLineOfSight) {
                        SkeletonAssassin.this.getNavigation().stop();
                        ++this.strafingTime;
                    } else {
                        SkeletonAssassin.this.getNavigation().moveTo((Entity)livingentity, this.speedModifier);
                        this.strafingTime = -1;
                    }
                    if (this.strafingTime >= 40) {
                        if ((double)SkeletonAssassin.this.getRandom().nextFloat() < 0.3) {
                            boolean bl = this.strafingClockwise = !this.strafingClockwise;
                        }
                        if ((double)SkeletonAssassin.this.getRandom().nextFloat() < 0.3) {
                            this.strafingBackwards = !this.strafingBackwards;
                        }
                        this.strafingTime = 0;
                    }
                    if (this.strafingTime > -1) {
                        if (distance > (double)(this.attackRadiusSqr * 0.75f)) {
                            this.strafingBackwards = false;
                        } else if (distance < (double)(this.attackRadiusSqr * 0.25f)) {
                            this.strafingBackwards = true;
                        }
                        SkeletonAssassin.this.getMoveControl().strafe(this.strafingBackwards ? -0.5f : 0.5f, this.strafingClockwise ? 0.5f : -0.5f);
                        Entity entity = SkeletonAssassin.this.getControlledVehicle();
                        if (entity instanceof Mob) {
                            Mob mob = (Mob)entity;
                            mob.lookAt((Entity)livingentity, 30.0f, 30.0f);
                        }
                    }
                } else {
                    SkeletonAssassin.this.getNavigation().stop();
                }
                SkeletonAssassin.this.lookAt((Entity)livingentity, 30.0f, 30.0f);
                SkeletonAssassin.this.getLookControl().setLookAt((Entity)livingentity, 30.0f, 30.0f);
                --this.attackTime;
                if (this.attackTime <= 0) {
                    if (!hasLineOfSight) {
                        return;
                    }
                    SkeletonAssassin.this.performRangedAttack(livingentity, 0.0f);
                    this.attackTime = this.attackIntervalMax - this.attackIntervalMin + this.attackIntervalMin;
                }
            }
        }
    }
}

