 
package com.mna.entities.boss;

import com.mna.api.sound.SFX;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.boss.attacks.OrangeChickenProjectile;
import com.mna.tools.math.MathUtils;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class PigDragon
extends BossMonster<PigDragon> {
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
    private static final int COOLDOWN_SHOOT_CHICKEN = 0;
    private static final int COOLDOWN_DIVEBOMB = 0;
    private int spawnTicks = 90;
    private int flapCount = 0;
    private Vec3 targetPosition;

    public PigDragon(EntityType<PigDragon> type, Level world) {
        super(type, world);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setAction(Action.SUMMON);
    }

    public PigDragon(Level world) {
        this((EntityType<PigDragon>)((EntityType)EntityInit.PIG_DRAGON.get()), world);
        this.setAction(Action.SUMMON);
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BossMonster<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        switch (this.getAction()) {
            case DIVEBOMBING: {
                builder.thenLoop("animation.PigDragon.dive");
                break;
            }
            case SHOOTING: {
                builder.thenPlay("animation.PigDragon.fly_breath");
                break;
            }
            case SUMMON: {
                builder.thenPlay("animation.PigDragon.summon");
                break;
            }
            default: {
                if (this.flapCount > 0) {
                    builder.thenLoop("animation.PigDragon.fly");
                    break;
                }
                builder.thenLoop("animation.PigDragon.glide_gentle");
            }
        }
        return state.setAndContinue(builder);
    }

    @Override
    protected ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    @Override
    public void setupSpawn() {
    }

    private double distSqToTarget() {
        if (this.targetPosition == null) {
            return 0.0;
        }
        return this.targetPosition.distanceToSqr(this.position());
    }

    protected void registerGoals() {
        this.targetSelector.addGoal(1, (Goal)new ShootChicken());
        this.targetSelector.addGoal(2, (Goal)new DiveBomb());
        this.targetSelector.addGoal(3, (Goal)new PigDragonFly());
        this.targetSelector.addGoal(1, (Goal)new BossMonster.ThreatTableHurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal(this, Player.class, 10, true, false, le -> true));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal(this, BossMonster.class, 10, true, false, le -> le != this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.targetPosition != null) {
            Vec3 myPos = this.position();
            if (this.getDeltaMovement().x == 0.0 && this.getDeltaMovement().y == 0.0 && this.getDeltaMovement().z == 0.0) {
                this.setDeltaMovement(Math.random(), Math.random(), Math.random());
            }
            float tickTheta = 5.135f;
            Vec3 desiredHeading = this.targetPosition.subtract(myPos).normalize();
            Vec3 calculatedHeading = MathUtils.rotateTowards(this.getDeltaMovement(), desiredHeading, tickTheta).normalize().scale((double)0.35f);
            this.setDeltaMovement(calculatedHeading);
        }
        if (this.getDeltaMovement().length() > (double)0.1f) {
            Vec3 target = this.position().add(this.getDeltaMovement().normalize().scale(30.0));
            Vec3 vec3 = EntityAnchorArgument.Anchor.FEET.apply((Entity)this);
            double d0 = target.x - vec3.x;
            double d1 = target.y - vec3.y;
            double d2 = target.z - vec3.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            this.setXRot(Mth.wrapDegrees((float)((float)(-(Mth.atan2((double)d1, (double)d3) * 57.2957763671875)))));
            this.setYRot(Mth.wrapDegrees((float)((float)(Mth.atan2((double)d2, (double)d0) * 57.2957763671875) - 90.0f)));
            this.setYHeadRot(this.getYRot());
        }
        if (this.level().isClientSide()) {
            if (this.flapCount > 0) {
                --this.flapCount;
                if (this.flapCount == 0) {
                    this.flapCount = -((int)(60.0 + Math.random() * 100.0));
                }
            } else if (this.flapCount <= 0) {
                ++this.flapCount;
                if (this.flapCount == 0) {
                    this.flapCount = (int)(60.0 + Math.random() * 100.0);
                }
            }
        } else if (this.spawnTicks > 0) {
            this.setNoAi(true);
            --this.spawnTicks;
            this.move(MoverType.SELF, new Vec3(0.0, 0.25, 0.0));
            this.setAction(Action.SUMMON);
            this.setDeltaMovement(Vec3.ZERO);
            if (this.spawnTicks == 0) {
                this.setNoAi(false);
                this.setAction(Action.IDLE);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource type, float amount) {
        if (type.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return super.hurt(type, amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.checkWalls(this.getBoundingBox());
        if (!this.level().isClientSide() && this.hurtTime == 0) {
            this.knockBack(this.level().getEntities((Entity)this, this.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            this.hurtNearby(this.level().getEntities((Entity)this, this.getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        }
    }

    private void setAction(Action action) {
        byte ordinal = (byte)((action.ordinal() & 0xFF) << 3);
        int flag = this.getStateFlag();
        flag &= 0xFFFFF807;
        this.setState(flag |= ordinal);
    }

    private Action getAction() {
        int ordinal = (this.getStateFlag() & 0x7F8) >> 3 & 0xFF;
        if (ordinal >= Action.values().length) {
            return Action.IDLE;
        }
        return Action.values()[ordinal];
    }

    @Override
    public ResourceLocation getArenaStructureID() {
        return null;
    }

    @Override
    public int getArenaStructureSegment() {
        return -1;
    }

    private void hurtNearby(List<Entity> pEntities) {
        for (Entity entity : (java.util.List<Entity>)pEntities) {
            if (!(entity instanceof LivingEntity)) continue;
            entity.hurt(this.damageSources().mobAttack((LivingEntity)this), (float)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
            com.mna.tools.MATools.doEnchantDamageEffects(this, entity);
        }
    }

    private void knockBack(List<Entity> pEntities) {
        double d0 = (this.getBoundingBox().minX + this.getBoundingBox().maxX) / 2.0;
        double d1 = (this.getBoundingBox().minZ + this.getBoundingBox().maxZ) / 2.0;
        for (Entity entity : (java.util.List<Entity>)pEntities) {
            if (!(entity instanceof LivingEntity)) continue;
            double d2 = entity.getX() - d0;
            double d3 = entity.getZ() - d1;
            double d4 = Math.max(d2 * d2 + d3 * d3, 0.1);
            entity.push(d2 / d4 * 4.0, (double)0.2f, d3 / d4 * 4.0);
            if (((LivingEntity)entity).getLastHurtByMobTimestamp() >= entity.tickCount - 2) continue;
            entity.hurt(this.damageSources().mobAttack((LivingEntity)this), 5.0f);
            com.mna.tools.MATools.doEnchantDamageEffects(this, entity);
        }
    }

    private boolean checkWalls(AABB pArea) {
        int i = Mth.floor((double)pArea.minX);
        int j = Mth.floor((double)pArea.minY);
        int k = Mth.floor((double)pArea.minZ);
        int l = Mth.floor((double)pArea.maxX);
        int i1 = Mth.floor((double)pArea.maxY);
        int j1 = Mth.floor((double)pArea.maxZ);
        boolean flag = false;
        boolean flag1 = false;
        for (int k1 = i; k1 <= l; ++k1) {
            for (int l1 = j; l1 <= i1; ++l1) {
                for (int i2 = k; i2 <= j1; ++i2) {
                    BlockPos blockpos = new BlockPos(k1, l1, i2);
                    BlockState blockstate = this.level().getBlockState(blockpos);
                    if (blockstate.isAir() || blockstate.is(BlockTags.DRAGON_TRANSPARENT)) continue;
                    if (CommonHooks.canEntityDestroy((Level)this.level(), (BlockPos)blockpos, (LivingEntity)this) && !blockstate.is(BlockTags.DRAGON_IMMUNE)) {
                        flag1 = this.level().removeBlock(blockpos, false) || flag1;
                        continue;
                    }
                    flag = true;
                }
            }
        }
        if (flag1) {
            BlockPos blockpos1 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(i1 - j + 1), k + this.random.nextInt(j1 - k + 1));
            this.level().levelEvent(2008, blockpos1, 0);
        }
        return flag;
    }

    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.Oink.OINK;
    }

    protected float getSoundVolume() {
        return 5.0f;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 600.0).add(Attributes.MOVEMENT_SPEED, (double)0.7f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 14.0).add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.GRAVITY, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public static enum Action {
        IDLE,
        SHOOTING,
        DIVEBOMBING,
        SUMMON;

    }

    private class ShootChicken
    extends Goal {
        int tickCount = 0;

        private ShootChicken() {
        }

        public boolean canUse() {
            return !PigDragon.this.isOnCooldown(0) && PigDragon.this.getTarget() != null;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void start() {
            PigDragon.this.setAction(Action.SHOOTING);
            Vec3 delta = PigDragon.this.getTarget().position().subtract(PigDragon.this.position());
            Vec3 target = PigDragon.this.position().add(delta.scale(2.0));
            PigDragon.this.targetPosition = new Vec3(target.x, PigDragon.this.position().y, target.z);
            this.tickCount = 0;
        }

        public void stop() {
            PigDragon.this.setCooldown(0, 40);
            PigDragon.this.setAction(Action.IDLE);
        }

        public boolean canContinueToUse() {
            return this.tickCount <= 40 && this.canUse();
        }

        public void tick() {
            if (this.tickCount++ % 20 == 0 && PigDragon.this.getTarget() != null) {
                PigDragon.this.level().playSound(null, (Entity)PigDragon.this.getTarget(), SoundEvents.CHICKEN_HURT, PigDragon.this.getSoundSource(), 1.0f, (float)(0.9 + Math.random() * 0.2));
                Vec3 delta = PigDragon.this.getTarget().position().subtract(PigDragon.this.position()).normalize();
                OrangeChickenProjectile proj = new OrangeChickenProjectile(PigDragon.this.level(), PigDragon.this.getTarget());
                proj.setPos(PigDragon.this.position().add(PigDragon.this.getDeltaMovement().normalize()));
                proj.shoot(delta.x, delta.y, delta.z, 0.5f, 0.0f);
                PigDragon.this.level().addFreshEntity((Entity)proj);
            }
        }
    }

    private class DiveBomb
    extends Goal {
        private static final AttributeModifier DIVE_BONUS = new AttributeModifier(ResourceLocation.fromNamespaceAndPath("mna", "22b8d8eb-16a3-4ee3-80b7-73b19b8e9006"), 1.0, AttributeModifier.Operation.ADD_VALUE);

        private DiveBomb() {
        }

        public boolean canUse() {
            return !PigDragon.this.isOnCooldown(0) && PigDragon.this.getTarget() != null;
        }

        public void start() {
            PigDragon.this.setAction(Action.DIVEBOMBING);
            PigDragon.this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(DIVE_BONUS);
            PigDragon.this.targetPosition = PigDragon.this.getTarget().getEyePosition();
        }

        public void stop() {
            PigDragon.this.setCooldown(0, 300);
            PigDragon.this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(DIVE_BONUS);
            PigDragon.this.setAction(Action.IDLE);
        }

        public boolean canContinueToUse() {
            return PigDragon.this.distSqToTarget() >= 4.0;
        }
    }

    class PigDragonFly
    extends Goal {
        public PigDragonFly() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            Action action = PigDragon.this.getAction();
            if (action == Action.SHOOTING || action == Action.DIVEBOMBING) {
                return false;
            }
            return PigDragon.this.distSqToTarget() < 2.0;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            RandomSource random = PigDragon.this.getRandom();
            double d0 = PigDragon.this.getX() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double d1 = PigDragon.this.getY() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double d2 = PigDragon.this.getZ() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            int y = (int)d1;
            for (int adjustments = 0; adjustments < 32; ++adjustments) {
                BlockPos bpTestPos = BlockPos.containing((double)d0, (double)y, (double)d2);
                if (!PigDragon.this.level().isEmptyBlock(bpTestPos)) break;
                --y;
            }
            d1 = y + 16;
            PigDragon.this.targetPosition = new Vec3(d0, d1, d2);
        }
    }
}


