 
package com.mna.entities.faction;

import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.math.MathUtils;
import java.util.EnumSet;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class HulkingZombie
extends BaseFactionMob<HulkingZombie> {
    private static final String INSTRUCTION_DAMAGE = "damage";
    private static final String INSTRUCTION_ROAR = "roar";
    private static final int LEAP_ATTACK_CD = 300;
    private static final int ROAR_CD = 300;
    private int leapAttackCooldown = 0;
    private int roarCooldown = 0;
    private boolean isActing = false;
    private boolean isMeleeAttacking = false;
    private boolean leftHandPunch = false;
    private boolean isLeapAttacking = false;
    private boolean isRoaring = false;

    public HulkingZombie(EntityType<HulkingZombie> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public float getStepHeight() {
        return 1.2f;
    }

    public HulkingZombie(Level worldIn) {
        this((EntityType<HulkingZombie>)((EntityType)EntityInit.HULKING_ZOMBIE.get()), worldIn);
    }

    public static boolean canSpawnPredicate(EntityType<HulkingZombie> p_234351_0_, LevelAccessor p_234351_1_, MobSpawnType p_234351_2_, BlockPos p_234351_3_, RandomSource p_234351_4_) {
        if (p_234351_1_ instanceof ServerLevelAccessor) {
            return p_234351_3_.getY() <= 45 && Monster.checkMonsterSpawnRules(p_234351_0_, (ServerLevelAccessor)((ServerLevelAccessor)p_234351_1_), (MobSpawnType)p_234351_2_, (BlockPos)p_234351_3_, (RandomSource)p_234351_4_);
        }
        return false;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> state) {
        if (!this.isActing) {
            if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.hulking_zombie.run"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.hulking_zombie.idle"));
        }
        if (this.isLeapAttacking) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.hulking_zombie.ground_slam_loop"));
        }
        if (this.isRoaring) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.hulking_zombie.roar"));
        }
        if (this.leftHandPunch) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.hulking_zombie.punch_left"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation.hulking_zombie.punch_right"));
    }

    @Override
    public void tick() {
        super.tick();
        --this.leapAttackCooldown;
        --this.roarCooldown;
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
        this.goalSelector.addGoal(1, (Goal)new FactionTierWrapperGoal(1, this, new LerpLeap(16.0f, 1, 10)));
        this.goalSelector.addGoal(1, (Goal)new FactionTierWrapperGoal(2, this, new RoarGoal()));
        this.goalSelector.addGoal(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), false));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.35f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal(this, Player.class, 10, true, false, (java.util.function.Predicate<net.minecraft.world.entity.LivingEntity>)(t -> this.factionTargetPlayerPredicate(t))));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, (java.util.function.Predicate<net.minecraft.world.entity.LivingEntity>)(t -> this.factionTargetHelpPredicate(t))));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public SoundEvent getAmbientSound() {
        return SFX.Entity.HulkingZombie.IDLE;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.HulkingZombie.DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SFX.Entity.HulkingZombie.HURT;
    }

    public void swing(InteractionHand p_226292_1_, boolean p_226292_2_) {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 40, "", this::handleDelayCallback));
        this.isActing = true;
        this.isMeleeAttacking = true;
        this.leftHandPunch = Math.random() < 0.5;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.playSound(SFX.Entity.HulkingZombie.ATTACK, 1.0f, (float)(0.9 + Math.random() * 0.2));
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (!this.isMeleeAttacking) {
            return true;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>(INSTRUCTION_DAMAGE, 20, entityIn, this::handleDelayCallback));
        this.swing(InteractionHand.MAIN_HAND, true);
        this.isMeleeAttacking = false;
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FREEZING)) {
            amount *= 0.75f;
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
        if (this.isLeapAttacking) {
            f += 4.0f;
        }
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entityIn instanceof LivingEntity) {
            f += com.mna.api.enchantments.MNAEnchantmentHelper.getDamageBonus((ItemStack)this.getMainHandItem(), (LivingEntity)entityIn);
            f1 += (float)com.mna.api.enchantments.MNAEnchantmentHelper.getKnockbackBonus((LivingEntity)this);
        }
        if ((i = com.mna.api.enchantments.MNAEnchantmentHelper.getFireAspect((LivingEntity)this)) > 0 || this.isOnFire()) {
            entityIn.igniteForSeconds((float)(i * 4));
        }
        if (flag = entityIn.hurt(this.damageSources().mobAttack((LivingEntity)this), f)) {
            if (f1 > 0.0f && entityIn instanceof LivingEntity) {
                ((LivingEntity)entityIn).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                if (!this.leftHandPunch && this.getTier() > 0) {
                    ((LivingEntity)entityIn).push(0.0, 1.0, 0.0);
                }
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if (entityIn instanceof Player) {
                Player playerentity = (Player)entityIn;
                this.maybeDisableShield(playerentity, this.getMainHandItem(), playerentity.isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY);
            }
            com.mna.tools.MATools.doEnchantDamageEffects(this, entityIn);
            this.setLastHurtMob(entityIn);
            if (this.random.nextFloat() < 0.2f && entityIn instanceof Mob) {
                ((Mob)entityIn).setTarget((LivingEntity)this);
            }
        }
        return flag;
    }

    private void maybeDisableShield(Player p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_) {
        if (!p_233655_2_.isEmpty() && !p_233655_3_.isEmpty() && p_233655_2_.getItem() instanceof AxeItem && p_233655_3_.getItem() == Items.SHIELD) {
            float f = 0.25f + (float)com.mna.api.enchantments.MNAEnchantmentHelper.getBlockEfficiency((LivingEntity)this) * 0.05f;
            if (this.random.nextFloat() < f) {
                p_233655_1_.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level().broadcastEntityEvent((Entity)p_233655_1_, (byte)30);
            }
        }
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, (double)0.6f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, (double)0.8f);
    }

    private void handleDelayCallback(String identifier, Entity entity) {
        if (!this.level().isClientSide() && this.isAlive()) {
            switch (identifier) {
                case "damage": {
                    this.damageEntity(entity);
                    break;
                }
                case "roar": {
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, false));
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, false, false));
                    this.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()), 200, 4, false, false));
                    this.playSound(SFX.Entity.HulkingZombie.ROAR, 1.0f, 1.0f);
                }
            }
        }
    }

    private void handleDelayCallback(String identifier, String data) {
        if (!this.level().isClientSide()) {
            this.isActing = false;
            if (this.isLeapAttacking) {
                this.leapAttackCooldown = 20;
                this.isLeapAttacking = false;
            } else if (this.isRoaring) {
                this.roarCooldown = 300;
                this.isRoaring = false;
            }
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isActing);
        nbt.putBoolean("leap_attacking", this.isLeapAttacking);
        nbt.putBoolean("left_hand_punch", this.leftHandPunch);
        nbt.putBoolean("roaring", this.isRoaring);
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.isActing = nbt.getBoolean("attacking");
        this.isLeapAttacking = nbt.getBoolean("leap_attacking");
        this.leftHandPunch = nbt.getBoolean("left_hand_punch");
        this.isRoaring = nbt.getBoolean("roaring");
    }

    public class LerpLeap
    extends Goal {
        private float maxDist;
        private int lerpTicks;
        private int initialDelay;
        private int lerpCount = 0;
        private Vec3 start;
        private Vec3 end;
        private Vec3 control_1;
        private Vec3 control_2;

        public LerpLeap(float maxDist, int initialDelay, int lerpTime) {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
            this.maxDist = maxDist;
            this.lerpTicks = lerpTime;
            this.initialDelay = initialDelay;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canUse() {
            LivingEntity livingentity;
            if (HulkingZombie.this.getTarget() == null || !HulkingZombie.this.isAlive() || !HulkingZombie.this.getTarget().isAlive() || HulkingZombie.this.leapAttackCooldown > 0) {
                return false;
            }
            double dist = HulkingZombie.this.distanceToSqr((Entity)HulkingZombie.this.getTarget());
            return dist >= 4.0 && dist <= (double)(this.maxDist * this.maxDist) && (livingentity = HulkingZombie.this.getTarget()) != null && livingentity.isAlive() && (livingentity instanceof FlyingMob || livingentity.hasImpulse || !livingentity.onGround() || livingentity instanceof Player && ((Player)livingentity).getAbilities().flying) && this.getEntityHeightOffGround(livingentity) > 2.0f;
        }

        private float getEntityHeightOffGround(LivingEntity living) {
            BlockPos livingPos = living.blockPosition();
            int count = 0;
            while (count < 10 && living.level().isEmptyBlock(livingPos)) {
                ++count;
                livingPos = livingPos.below();
            }
            return count;
        }

        public void stop() {
            this.lerpCount = 0;
            HulkingZombie.this.setNoGravity(false);
            HulkingZombie.this.isActing = false;
            HulkingZombie.this.isLeapAttacking = false;
            ServerMessageDispatcher.sendEntityStateMessage(HulkingZombie.this);
        }

        public boolean canContinueToUse() {
            return HulkingZombie.this.getTarget() != null && this.lerpCount < this.lerpTicks + this.initialDelay;
        }

        public void start() {
            HulkingZombie.this.setNoGravity(true);
            HulkingZombie.this.getNavigation().stop();
            HulkingZombie.this.setDeltaMovement(Vec3.ZERO);
            HulkingZombie.this.isActing = true;
            HulkingZombie.this.isLeapAttacking = true;
            ServerMessageDispatcher.sendEntityStateMessage(HulkingZombie.this);
        }

        public void tick() {
            if (HulkingZombie.this.getTarget() == null) {
                return;
            }
            LivingEntity ent = HulkingZombie.this.getTarget();
            float lerpPct = (float)(this.lerpCount++ - this.initialDelay) / (float)this.lerpTicks;
            if (this.lerpCount == this.initialDelay) {
                this.start = HulkingZombie.this.position();
                HulkingZombie.this.playSound(SFX.Entity.HulkingZombie.LEAP, 1.0f, (float)(0.9 + Math.random() * 0.2));
            }
            if (this.lerpCount > this.initialDelay && this.lerpCount - this.initialDelay <= this.lerpTicks) {
                Vec3 direction = ent.position().subtract(this.start).normalize();
                this.end = ent.position().add(ent.getDeltaMovement()).subtract(direction);
                Vec3 difference = this.end.subtract(this.start);
                this.control_1 = this.start.add(difference.scale(0.3)).add(0.0, 1.0, 0.0);
                this.control_2 = this.start.add(difference.scale(0.6)).add(0.0, 1.0, 0.0);
                Vec3 position = MathUtils.bezierVector3d(this.start, this.end, this.control_1, this.control_2, lerpPct);
                HulkingZombie.this.lookAt(EntityAnchorArgument.Anchor.FEET, ent.position());
                HulkingZombie.this.setPos(position.x, position.y, position.z);
                if (this.lerpCount - this.initialDelay == this.lerpTicks) {
                    HulkingZombie.this.leapAttackCooldown = 300;
                    double dist = HulkingZombie.this.distanceToSqr((Entity)ent);
                    if (dist < 16.0) {
                        HulkingZombie.this.damageEntity((Entity)ent);
                        ent.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()), 20));
                    }
                }
            }
        }
    }

    public class RoarGoal
    extends Goal {
        public RoarGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return HulkingZombie.this.roarCooldown <= 0 && HulkingZombie.this.getTarget() != null && HulkingZombie.this.getHealth() < HulkingZombie.this.getMaxHealth() * 0.75f && !HulkingZombie.this.hasEffect(MobEffects.MOVEMENT_SPEED) && !HulkingZombie.this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !HulkingZombie.this.hasEffect(MobEffects.DAMAGE_BOOST) && !HulkingZombie.this.isActing;
        }

        public void start() {
            HulkingZombie.this.getNavigation().stop();
            HulkingZombie.this.isRoaring = true;
            HulkingZombie.this.isActing = true;
            DelayedEventQueue.pushEvent(HulkingZombie.this.level(), new TimedDelayedEvent<LivingEntity>(HulkingZombie.INSTRUCTION_ROAR, 20, (LivingEntity)HulkingZombie.this, HulkingZombie.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(HulkingZombie.this.level(), new TimedDelayedEvent<String>("resetattack", 40, "", HulkingZombie.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(HulkingZombie.this);
        }

        public boolean canContinueToUse() {
            return HulkingZombie.this.isRoaring;
        }
    }
}

