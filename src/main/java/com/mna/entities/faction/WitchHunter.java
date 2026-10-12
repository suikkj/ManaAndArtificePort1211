 
package com.mna.entities.faction;

import com.mna.tools.MATools;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.effects.EffectInit;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mna.entities.faction.util.WitchHunterArrow;
import com.mna.entities.projectile.WitchhunterTrickshot;
import com.mna.entities.sorcery.base.ChanneledSpellEntity;
import com.mna.entities.sorcery.targeting.Smite;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.math.MathUtils;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import net.minecraft.core.registries.BuiltInRegistries;

public class WitchHunter
extends BaseFactionMob<WitchHunter>
implements RangedAttackMob,
AnimationController.CustomKeyframeHandler<WitchHunter> {
    private static final String INSTRUCTION_DAMAGE = "damage";
    private static final String INSTRUCTION_TRICKSHOT = "trickshot";
    private static final String INSTRUCTION_SMASH = "potionSmash";
    private static final String INSTRUCTION_RESET_ATTACK = "reset_attack";
    private static final String INSTRUCTION_RESET_ROLL = "reset_roll";
    private static final String INSTRUCTION_POTION_COOLDOWN = "potion_cooldown";
    private static final int TRICKSHOT_CD = 200;
    private static final int ROLL_CD = 60;
    private boolean isAttacking = false;
    private boolean isTrickshotting = false;
    private boolean isPotionSmashing = false;
    private boolean isRolling = false;
    private int trickshotCooldown = 0;
    private HashMap<String, Integer> trickshotCooldowns = new HashMap();
    private int rollCooldown = 0;
    private boolean instantTransition = false;
    private int nextTrickshotDuration = 200;
    private int nextTrickshotMagnitude = 200;
    private MobEffect nextTrickshotEffect = null;

    public WitchHunter(EntityType<WitchHunter> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();
        for (String e : this.trickshotCooldowns.keySet()) {
            this.trickshotCooldowns.put(e, this.trickshotCooldowns.get(e) - 1);
        }
        --this.trickshotCooldown;
        --this.rollCooldown;
    }

    @Override
    public IFaction getFaction() {
        return Factions.COUNCIL;
    }

    @Override
    protected void addControllerListeners(AnimationController<WitchHunter> controller) {
        controller.setCustomInstructionKeyframeHandler((AnimationController.CustomKeyframeHandler)this);
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> state) {
        if (this.instantTransition) {
            state.getController().transitionLength(0);
            this.instantTransition = false;
        } else {
            state.getController().transitionLength(5);
        }
        if (!this.isAttacking) {
            double generalMoveSpeed = this.getDeltaMovement().subtract(0.0, this.getDeltaMovement().y, 0.0).length();
            if (generalMoveSpeed > (double)0.01f) {
                double moveAngle = Math.atan2(this.getDeltaMovement().z, this.getDeltaMovement().x) * 180.0 / Math.PI;
                double lookAngle = Math.atan2(this.getForward().z, this.getForward().x) * 180.0 / Math.PI;
                double angleDelta = Math.abs(lookAngle - moveAngle);
                state.getController().transitionLength(10);
                if (angleDelta > 45.0 && angleDelta < 135.0) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.strafe"));
                }
                if (angleDelta >= 155.0) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.backpedal"));
                }
                if (generalMoveSpeed < (double)0.1f && generalMoveSpeed > 0.01) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.walk"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.run"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.idle"));
        }
        if (this.isTrickshotting) {
            if (this.isPotionSmashing) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.throw_short").thenLoop("animation.model.idle"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.throw").thenLoop("animation.model.idle"));
        }
        if (this.isRolling) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.roll_short").thenLoop("animation.model.idle"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.shoot"));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(0, (Goal)new DodgeGoal());
        this.goalSelector.addGoal(1, (Goal)new FactionTierWrapperGoal(2, this, new TrickshotGoal(EffectInit.SILENCE, 100, 0)));
        this.goalSelector.addGoal(1, (Goal)new FactionTierWrapperGoal(1, this, new TrickshotGoal(MobEffects.HARM, 1, 2, true)));
        this.goalSelector.addGoal(2, (Goal)new FactionTierWrapperGoal(0, this, new TrickshotGoal(MobEffects.MOVEMENT_SLOWDOWN, 240, 3, true)));
        this.goalSelector.addGoal(2, (Goal)new FactionTierWrapperGoal(0, this, new TrickshotGoal(MobEffects.WEAKNESS, 240, 3, true)));
        this.goalSelector.addGoal(2, (Goal)new FactionTierWrapperGoal(0, this, new TrickshotGoal(MobEffects.GLOWING, 160, 3)));
        this.goalSelector.addGoal(3, (Goal)new FactionTierWrapperGoal(1, this, new TrickshotGoal(EffectInit.INSTANT_CHILL, 1, 1)));
        this.goalSelector.addGoal(3, (Goal)new FactionTierWrapperGoal(1, this, new TrickshotGoal(EffectInit.MANA_STUNT, 400, 4)));
        this.goalSelector.addGoal(4, (Goal)new ShootGoal(1.2f, 40, 80, 12.0f));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, 0.75));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal(this, Player.class, 10, true, false, (java.util.function.Predicate<net.minecraft.world.entity.LivingEntity>)(t -> this.factionTargetPlayerPredicate(t))));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal(this, Witch.class, 10, true, false, e -> true));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, (java.util.function.Predicate<net.minecraft.world.entity.LivingEntity>)(t -> this.factionTargetHelpPredicate(t))));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.isAttacking) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_DAMAGE, 10, target, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>(INSTRUCTION_RESET_ATTACK, 30, "", this::handleDelayCallback));
        this.isAttacking = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    public boolean spawnBolt(Entity target) {
        Vec3 fwd = this.getForward();
        WitchHunterArrow arrow = new WitchHunterArrow(this.level(), this.getX() + fwd.x, this.getEyeY() + fwd.y, this.getZ() + fwd.z);
        arrow.setOwner((Entity)this);
        arrow.setCritArrow(target.isCurrentlyGlowing());
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333) - arrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * (double)0.2f, d2, 1.6f, 0.0f);
        this.level().addFreshEntity((Entity)arrow);
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, (float)(0.9 + Math.random() * 0.2));
        return true;
    }

    Vector3f getProjectileShotVector(LivingEntity pUser, Vec3 pVectorTowardsTarget, float pProjectileAngle) {
        Vector3f vector3f = pVectorTowardsTarget.toVector3f().normalize();
        Vector3f vector3f1 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f));
        if ((double)vector3f1.lengthSquared() <= 1.0E-7) {
            Vec3 vec3 = pUser.getUpVector(1.0f);
            vector3f1 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)vec3.toVector3f());
        }
        Vector3f vector3f2 = new Vector3f((Vector3fc)vector3f).rotateAxis(1.5707964f, vector3f1.x, vector3f1.y, vector3f1.z);
        return new Vector3f((Vector3fc)vector3f).rotateAxis(pProjectileAngle * ((float)Math.PI / 180), vector3f2.x, vector3f2.y, vector3f2.z);
    }

    public void spawnTrickshot(MobEffect effect, int duration, int magnitude, boolean smash) {
        if (this.getTarget() == null) {
            return;
        }
        WitchhunterTrickshot trick = new WitchhunterTrickshot(this, this.position().add(0.0, 1.0, 0.0), smash ? this.getTarget().position() : this.getTarget().position().add(0.0, 3.0, 0.0), effect, duration, magnitude, smash);
        this.level().addFreshEntity((Entity)trick);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 20.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    private void handleDelayCallback(String identifier, LivingEntity entity) {
        if (!this.level().isClientSide() && this.isAlive()) {
            switch (identifier) {
                case "damage": {
                    this.spawnBolt((Entity)entity);
                    break;
                }
                case "trickshot": {
                    this.spawnTrickshot(this.nextTrickshotEffect, this.nextTrickshotDuration, this.nextTrickshotMagnitude, false);
                    break;
                }
                case "potionSmash": {
                    this.spawnTrickshot(this.nextTrickshotEffect, this.nextTrickshotDuration, this.nextTrickshotMagnitude, true);
                }
            }
        }
    }

    private void handleDelayCallback(String identifier, String data) {
        if (!this.level().isClientSide()) {
            boolean wasRolling = this.isRolling;
            switch (identifier) {
                case "reset_attack": {
                    this.isAttacking = false;
                    break;
                }
                case "reset_roll": {
                    this.isRolling = false;
                    this.rollCooldown = 5;
                    break;
                }
                case "potion_cooldown": {
                    this.trickshotCooldowns.put(data, 600);
                    this.trickshotCooldown = this.isPotionSmashing ? 40 : 200;
                    this.isTrickshotting = false;
                    this.isPotionSmashing = false;
                }
            }
            boolean bl = this.isAttacking = this.isRolling || this.isTrickshotting;
            if (!this.isRolling && wasRolling) {
                this.instantTransition = true;
            }
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    public void handle(CustomInstructionKeyframeEvent<WitchHunter> event) {
        if (event.getKeyframeData().getInstructions().contains("sound:woosh")) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Entity.Generic.WOOSH, SoundSource.HOSTILE, 0.05f, (float)(0.9 + Math.random() * 0.2), false);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isAttacking);
        nbt.putBoolean(INSTRUCTION_TRICKSHOT, this.isTrickshotting);
        nbt.putBoolean("smash", this.isPotionSmashing);
        nbt.putBoolean("roll", this.isRolling);
        nbt.putBoolean("instant", this.instantTransition);
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.isAttacking = nbt.getBoolean("attacking");
        this.isTrickshotting = nbt.getBoolean(INSTRUCTION_TRICKSHOT);
        this.isPotionSmashing = nbt.getBoolean("smash");
        this.isRolling = nbt.getBoolean("roll");
        this.instantTransition = nbt.getBoolean("instant");
    }

    public class DodgeGoal
    extends Goal {
        private Vec3 dodgeDirection;
        private Entity dodgeEntity;

        public DodgeGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canUse() {
            if (WitchHunter.this.rollCooldown > 0) {
                return false;
            }
            Vec3 rt_startVec = WitchHunter.this.position();
            Vec3 thisFwd = Vec3.directionFromRotation((Vec2)WitchHunter.this.getRotationVector());
            Vec3 rt_endVec = rt_startVec.add(thisFwd.scale(10.0));
            List potentials = WitchHunter.this.level().getEntities((Entity)WitchHunter.this, WitchHunter.this.getBoundingBox().inflate(10.0)).stream().filter(e -> e instanceof SpellProjectile || e instanceof Smite || e instanceof ChanneledSpellEntity).filter(e -> {
                AABB axisalignedbb = e.getBoundingBox().inflate(2.0);
                Optional optional = axisalignedbb.clip(rt_startVec, rt_endVec);
                return optional.isPresent();
            }).collect(Collectors.toList());
            if (potentials.size() == 0) {
                return false;
            }
            potentials.sort(new Comparator<Entity>(){

                @Override
                public int compare(Entity o1, Entity o2) {
                    Double d1 = o1.distanceToSqr((Entity)WitchHunter.this);
                    Double d2 = o2.distanceToSqr((Entity)WitchHunter.this);
                    return d1.compareTo(d2);
                }
            });
            this.dodgeEntity = (Entity)potentials.get(0);
            Vec3 fwd = this.dodgeEntity.getDeltaMovement();
            this.dodgeDirection = new Vec3(fwd.x, 0.0, fwd.z).cross(new Vec3(0.0, 1.0, 0.0)).normalize();
            float angle = (float)(Math.acos(this.dodgeDirection.dot(thisFwd.normalize())) * 180.0 / Math.PI);
            if (angle > 90.0f) {
                this.dodgeDirection.scale(-1.0);
            }
            return true;
        }

        public void start() {
            WitchHunter.this.getNavigation().stop();
            WitchHunter.this.isRolling = true;
            WitchHunter.this.isAttacking = true;
            WitchHunter.this.getLookControl().setLookAt(this.dodgeDirection.x, this.dodgeDirection.y, this.dodgeDirection.z, 30.0f, 30.0f);
            WitchHunter.this.setDeltaMovement(this.dodgeDirection);
            DelayedEventQueue.pushEvent(WitchHunter.this.level(), new TimedDelayedEvent<String>(WitchHunter.INSTRUCTION_RESET_ROLL, 60, "", WitchHunter.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(WitchHunter.this);
        }

        public void tick() {
            WitchHunter.this.getLookControl().setLookAt(this.dodgeDirection.x, this.dodgeDirection.y, this.dodgeDirection.z, 30.0f, 30.0f);
        }

        public boolean canContinueToUse() {
            return WitchHunter.this.isRolling;
        }

        protected EntityHitResult rayTraceEntity(Vec3 startVec, Vec3 endVec, float size, Entity searchEntity) {
            return ProjectileUtil.getEntityHitResult((Level)WitchHunter.this.level(), (Entity)WitchHunter.this, (Vec3)startVec, (Vec3)endVec, (AABB)WitchHunter.this.getBoundingBox().inflate((double)size), entity -> searchEntity == entity);
        }
    }

    public class TrickshotGoal
    extends Goal {
        private net.minecraft.core.Holder<MobEffect> potionEffect;
        private int potionDuration;
        private int potionMagnitude;
        private static final float shortDistance = 25.0f;
        private static final float maxDistance = 256.0f;
        private boolean startedAction;
        private boolean closeRangeOnly = false;

        public TrickshotGoal(net.minecraft.core.Holder<MobEffect> effect, int duration, int magnitude) {
            this(effect, duration, magnitude, false);
        }

        public TrickshotGoal(net.minecraft.core.Holder<MobEffect> effect, int duration, int magnitude, boolean closeRangeOnly) {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
            this.potionEffect = effect;
            this.potionDuration = duration;
            this.potionMagnitude = magnitude;
            this.startedAction = false;
            this.closeRangeOnly = closeRangeOnly;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canUse() {
            boolean baseCheck;
            boolean bl = baseCheck = WitchHunter.this.trickshotCooldown <= 0 && WitchHunter.this.trickshotCooldowns.getOrDefault(BuiltInRegistries.MOB_EFFECT.getKey(this.potionEffect.value()).toString(), 0) <= 0 && !WitchHunter.this.isTrickshotting && !WitchHunter.this.isRolling && WitchHunter.this.getTarget() != null && !WitchHunter.this.getTarget().hasEffect(this.potionEffect) && WitchHunter.this.getTarget().canBeAffected(new MobEffectInstance(this.potionEffect, this.potionDuration, this.potionMagnitude));
            if (baseCheck && this.potionEffect == MobEffects.HARM && (MATools.isUndead(WitchHunter.this.getTarget()))) {
                return false;
            }
            if (baseCheck && (this.potionEffect == EffectInit.MANA_STUNT || this.potionEffect == EffectInit.SILENCE) && !(WitchHunter.this.getTarget() instanceof Player)) {
                return false;
            }
            if (this.closeRangeOnly) {
                return baseCheck && WitchHunter.this.getTarget().distanceToSqr((Entity)WitchHunter.this) < 25.0;
            }
            return baseCheck;
        }

        public void start() {
            this.startedAction = false;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity at = WitchHunter.this.getTarget();
            if (at == null) {
                return;
            }
            if (!this.startedAction && !WitchHunter.this.isTrickshotting) {
                if (this.tryMove(at)) {
                    this.startAction();
                }
            } else {
                WitchHunter.this.getLookControl().setLookAt((Entity)at, 30.0f, 30.0f);
            }
        }

        private void startAction() {
            WitchHunter.this.getNavigation().stop();
            LivingEntity target = WitchHunter.this.getTarget();
            WitchHunter.this.isTrickshotting = true;
            WitchHunter.this.isPotionSmashing = WitchHunter.this.distanceToSqr((Entity)target) < 25.0;
            WitchHunter.this.isAttacking = true;
            WitchHunter.this.nextTrickshotDuration = this.potionDuration;
            WitchHunter.this.nextTrickshotMagnitude = this.potionMagnitude;
            WitchHunter.this.nextTrickshotEffect = this.potionEffect.value();
            String effectID = BuiltInRegistries.MOB_EFFECT.getKey(this.potionEffect.value()).toString();
            if (WitchHunter.this.isPotionSmashing) {
                DelayedEventQueue.pushEvent(WitchHunter.this.level(), new TimedDelayedEvent<LivingEntity>(WitchHunter.INSTRUCTION_SMASH, 12, (LivingEntity)WitchHunter.this, WitchHunter.this::handleDelayCallback));
                DelayedEventQueue.pushEvent(WitchHunter.this.level(), new TimedDelayedEvent<String>(WitchHunter.INSTRUCTION_POTION_COOLDOWN, 40, effectID, WitchHunter.this::handleDelayCallback));
            } else {
                DelayedEventQueue.pushEvent(WitchHunter.this.level(), new TimedDelayedEvent<LivingEntity>(WitchHunter.INSTRUCTION_TRICKSHOT, 46, (LivingEntity)WitchHunter.this, WitchHunter.this::handleDelayCallback));
                DelayedEventQueue.pushEvent(WitchHunter.this.level(), new TimedDelayedEvent<String>(WitchHunter.INSTRUCTION_POTION_COOLDOWN, 100, effectID, WitchHunter.this::handleDelayCallback));
            }
            ServerMessageDispatcher.sendEntityStateMessage(WitchHunter.this);
            this.startedAction = true;
        }

        private boolean tryMove(LivingEntity at) {
            double distance = WitchHunter.this.distanceToSqr(at.getX(), at.getY(), at.getZ());
            boolean canSeeTarget = WitchHunter.this.getSensing().hasLineOfSight((Entity)at);
            if (distance <= 256.0 && canSeeTarget) {
                WitchHunter.this.getNavigation().stop();
                return true;
            }
            WitchHunter.this.getNavigation().moveTo((Entity)at, 1.5);
            WitchHunter.this.getLookControl().setLookAt((Entity)at, 30.0f, 30.0f);
            return false;
        }

        public boolean canContinueToUse() {
            return WitchHunter.this.getTarget() != null && !WitchHunter.this.getTarget().isDeadOrDying() && WitchHunter.this.trickshotCooldown <= 0 && (!this.startedAction || WitchHunter.this.isTrickshotting);
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
            LivingEntity livingentity = WitchHunter.this.getTarget();
            return livingentity != null && !livingentity.isDeadOrDying();
        }

        public boolean canContinueToUse() {
            if (!this.canUse()) {
                return false;
            }
            return !WitchHunter.this.getNavigation().isDone();
        }

        public void stop() {
            WitchHunter.this.getNavigation().stop();
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = WitchHunter.this.getTarget();
            if (livingentity != null) {
                boolean needsToStandStill;
                double distance = WitchHunter.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                boolean hasLineOfSight = WitchHunter.this.getSensing().hasLineOfSight((Entity)livingentity);
                boolean bl = needsToStandStill = WitchHunter.this.isAttacking || WitchHunter.this.isTrickshotting || WitchHunter.this.isRolling;
                if (!needsToStandStill) {
                    if (distance <= (double)this.attackRadiusSqr && hasLineOfSight) {
                        WitchHunter.this.getNavigation().stop();
                        ++this.strafingTime;
                    } else {
                        WitchHunter.this.getNavigation().moveTo((Entity)livingentity, this.speedModifier);
                        this.strafingTime = -1;
                    }
                    if (this.strafingTime >= 40) {
                        if ((double)WitchHunter.this.getRandom().nextFloat() < 0.3) {
                            boolean bl2 = this.strafingClockwise = !this.strafingClockwise;
                        }
                        if ((double)WitchHunter.this.getRandom().nextFloat() < 0.3) {
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
                        WitchHunter.this.getMoveControl().strafe(this.strafingBackwards ? -0.5f : 0.5f, this.strafingClockwise ? 0.5f : -0.5f);
                        Entity entity = WitchHunter.this.getControlledVehicle();
                        if (entity instanceof Mob) {
                            Mob mob = (Mob)entity;
                            mob.lookAt((Entity)livingentity, 30.0f, 30.0f);
                        }
                    }
                } else {
                    WitchHunter.this.getNavigation().stop();
                }
                WitchHunter.this.lookAt((Entity)livingentity, 30.0f, 30.0f);
                WitchHunter.this.getLookControl().setLookAt((Entity)livingentity, 30.0f, 30.0f);
                int tier = MathUtils.clamp(WitchHunter.this.getTier() + 1, 1, 3);
                --this.attackTime;
                if (this.attackTime <= 0) {
                    if (!hasLineOfSight) {
                        return;
                    }
                    WitchHunter.this.performRangedAttack(livingentity, 0.0f);
                    this.attackTime = (this.attackIntervalMax - this.attackIntervalMin + this.attackIntervalMin) / tier;
                }
            }
        }
    }
}


