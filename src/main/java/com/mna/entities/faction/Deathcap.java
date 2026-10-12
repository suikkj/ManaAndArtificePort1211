 
package com.mna.entities.faction;

import com.mna.ManaAndArtifice;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.SummonUtils;
import java.util.EnumSet;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;

public class Deathcap
extends BaseFactionMob<Deathcap>
implements AnimationController.CustomKeyframeHandler<Deathcap> {
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(Deathcap.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final String INSTRUCTION_DAMAGE = "damage";
    private static final String INSTRUCTION_FLING = "fling";
    private static final String INSTRUCTION_HEAL = "heal";
    private static final String INSTRUCTION_BURROW_POS = "burrow_position";
    private static final String INSTRUCTION_BURROW_FLING = "burrow_attack";
    private static final String INSTRUCTION_ROOTS_POISON = "roots_attack";
    private static final String INSTRUCTION_SPORE_BURST = "spore_burst";
    private static final String ACTION_MELEE = "melee";
    private static final String ACTION_BURROW = "burrow";
    private static final String ACTION_HEAL = "heal";
    private static final String ACTION_ROOTS = "roots";
    private static final String ACTION_SPORES = "spores";
    private static final int BURROW_CD = 300;
    private static final int HEAL_CD = 1200;
    private static final int ROOTS_CD = 400;
    private static final int SPORES_CD = 400;
    private Entity attackEntity;
    private int burrowCooldown = 0;
    private int rootsCooldown = 0;
    private int healCooldown = 0;
    private int sporesCooldown = 0;
    private Vec3 rootsPos = Vec3.ZERO;
    private boolean isActing = false;
    private boolean isMeleeAttacking = false;
    private boolean meleeLeftHand = false;
    private boolean isBurrowing = false;
    private boolean isSporeBursting = false;
    private boolean isRooting = false;
    private boolean isHealing = false;

    public Deathcap(EntityType<Deathcap> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_ID, -1);
    }

    @Override
    protected void addControllerListeners(AnimationController<Deathcap> controller) {
        controller.transitionLength(2);
        controller.setCustomInstructionKeyframeHandler((AnimationController.CustomKeyframeHandler)this);
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.isActing) {
            if (this.isRooting) {
                builder.thenPlay("animation.mushroom_soldier.roots");
            } else if (this.isBurrowing) {
                builder.thenPlay("animation.mushroom_soldier.burrow");
            } else if (this.isHealing) {
                builder.thenPlay("animation.mushroom_soldier.heal");
            } else if (this.isSporeBursting) {
                builder.thenPlay("animation.mushroom_soldier.spore_burst");
            } else if (this.meleeLeftHand) {
                builder.thenPlay("animation.mushroom_soldier.attack_left");
            } else {
                builder.thenPlay("animation.mushroom_soldier.attack_right");
            }
        }
        if (this.getDeltaMovement().length() > (double)0.05f) {
            if ((Integer)this.entityData.get(TARGET_ID) != -1) {
                builder.thenLoop("animation.mushroom_soldier.run");
            } else {
                builder.thenLoop("animation.mushroom_soldier.walk");
            }
        } else {
            builder.thenLoop("animation.mushroom_soldier.idle");
        }
        return state.setAndContinue(builder);
    }

    public SoundEvent getAmbientSound() {
        return SoundEvents.BAMBOO_SAPLING_PLACE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.BAMBOO_SAPLING_PLACE;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ROOTS_BREAK;
    }

    @Override
    public IFaction getFaction() {
        return Factions.FEY;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isBurrowing && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return false;
        }
        if (source.is(DamageTypes.WITHER)) {
            amount *= 2.0f;
        }
        return super.hurt(source, amount);
    }

    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        return pPotioneffect.getEffect() != MobEffects.POISON;
    }

    @Override
    public void tick() {
        super.tick();
        --this.burrowCooldown;
        --this.healCooldown;
        --this.rootsCooldown;
        --this.sporesCooldown;
    }

    public void setTarget(LivingEntity pLivingEntity) {
        super.setTarget(pLivingEntity);
        if (pLivingEntity == null) {
            this.entityData.set(TARGET_ID, -1);
        } else {
            this.entityData.set(TARGET_ID, pLivingEntity.getId());
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new FactionTierWrapperGoal(1, this, new HealGoal()));
        this.goalSelector.addGoal(3, (Goal)new FactionTierWrapperGoal(1, this, new SporeBurstGoal()));
        this.goalSelector.addGoal(2, (Goal)new FactionTierWrapperGoal(2, this, new BurrowGoal()));
        this.goalSelector.addGoal(2, (Goal)new FactionTierWrapperGoal(2, this, new RootsGoal()));
        this.goalSelector.addGoal(4, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.25, false));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.3f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, this::factionTargetPlayerPredicate));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, this::factionTargetHelpPredicate));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public void swing(InteractionHand p_226292_1_, boolean p_226292_2_) {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 15, ACTION_MELEE, this::handleDelayCallback));
        this.isMeleeAttacking = true;
        this.meleeLeftHand = Math.random() < 0.5;
        this.isActing = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (!this.isMeleeAttacking) {
            return true;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>(INSTRUCTION_DAMAGE, 5, entityIn, this::handleDelayCallback));
        this.isActing = true;
        this.isMeleeAttacking = false;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.attackEntity = entityIn;
        return true;
    }

    public boolean damageEntity(Entity entityIn) {
        boolean flag;
        int i;
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entityIn instanceof LivingEntity) {
            f += EnchantmentHelper.getDamageBonus((ItemStack)this.getMainHandItem(), (LivingEntity)entityIn);
            f1 += (float)EnchantmentHelper.getKnockbackBonus((LivingEntity)this);
        }
        if ((i = EnchantmentHelper.getFireAspect((LivingEntity)this)) > 0) {
            entityIn.igniteForSeconds((float)(i * 4));
        }
        if (flag = entityIn.hurt(this.damageSources().mobAttack((LivingEntity)this), f)) {
            if (!this.level().isClientSide()) {
                this.playSound(SFX.Entity.Generic.WOOSH, 1.0f, (float)((double)0.9f + Math.random() * (double)0.2f));
            }
            if (f1 > 0.0f && entityIn instanceof LivingEntity) {
                ((LivingEntity)entityIn).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
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
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, (double)0.55f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    private void handleDelayCallback(String identifier, Entity entity) {
        if (this.isAlive()) {
            if (!this.level().isClientSide()) {
                switch (identifier) {
                    case "fling": {
                        entity.push(0.0, 1.0, 0.0);
                        if (entity instanceof Player) {
                            ((ServerPlayer)entity).connection.send((Packet)new ClientboundSetEntityMotionPacket(entity));
                        }
                        this.playSound(SFX.Entity.Generic.WOOSH, 1.0f, (float)((double)0.9f + Math.random() * (double)0.2f));
                        break;
                    }
                    case "damage": {
                        this.damageEntity(entity);
                        if (!(entity instanceof LivingEntity)) break;
                        ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                        break;
                    }
                    case "heal": {
                        this.heal(this.getMaxHealth() * 0.5f);
                        break;
                    }
                    case "burrow_position": {
                        Vec3 pos = entity.position();
                        if (!(pos.distanceTo(this.position()) < 64.0)) break;
                        Vec3 newPos = this.position().subtract(pos).normalize();
                        this.setPos(pos.x + newPos.x, pos.y + newPos.y, pos.z + newPos.z);
                        this.lookAt(EntityAnchorArgument.Anchor.FEET, pos);
                        break;
                    }
                    case "burrow_attack": {
                        Vec3 pos = entity.position();
                        this.playSound(SFX.Spell.Impact.Single.EARTH, 1.0f, (float)((double)0.9f + Math.random() * (double)0.2f));
                        if (!(pos.distanceTo(this.position()) < 2.0)) break;
                        this.handleDelayCallback(INSTRUCTION_FLING, entity);
                        break;
                    }
                    case "roots_attack": {
                        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.rootsPos.x, this.rootsPos.y, this.rootsPos.z);
                        areaeffectcloud.setOwner((LivingEntity)this);
                        areaeffectcloud.setRadius(3.0f);
                        areaeffectcloud.setRadiusOnUse(-0.5f);
                        areaeffectcloud.setWaitTime(10);
                        areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
                        areaeffectcloud.setPotion(Potions.POISON);
                        this.level().addFreshEntity((Entity)areaeffectcloud);
                        break;
                    }
                    case "spore_burst": {
                        this.level().getEntities((Entity)this, this.getBoundingBox().inflate(3.0), e -> e.isAlive() && e instanceof LivingEntity && !SummonUtils.isTargetFriendly(e, (LivingEntity)this)).stream().map(e -> (LivingEntity)e).forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2)));
                    }
                }
            } else {
                switch (identifier) {
                    case "heal": {
                        int i;
                        for (i = 0; i < 5; ++i) {
                            double d0 = this.random.nextGaussian() * 0.02;
                            double d1 = this.random.nextGaussian() * 0.02;
                            double d2 = this.random.nextGaussian() * 0.02;
                            this.level().addParticle((ParticleOptions)ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d0, d1, d2);
                        }
                        for (i = 0; i < 5; ++i) {
                            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())), this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), 0.18, 0.3, 0.03);
                        }
                        break;
                    }
                    case "burrow_position": {
                        BlockPos pos = this.getOnPos();
                        BlockState state = this.level().getBlockState(pos);
                        for (int i = 0; i < 10; ++i) {
                            this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, state), this.getX() + Math.random(), this.getY() + 0.5, this.getZ() + Math.random(), 0.0, 0.05 * Math.random(), 0.0);
                        }
                        SoundType soundType = this.getFeetBlockState().getSoundType();
                        this.level().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), soundType.getBreakSound(), SoundSource.HOSTILE, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
                        break;
                    }
                    case "burrow_attack": {
                        BlockPos pos = this.getOnPos();
                        BlockState state = this.level().getBlockState(pos);
                        for (int i = 0; i < 10; ++i) {
                            this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, state), this.getX() + Math.random(), this.getY() + 0.5, this.getZ() + Math.random(), 0.0, 0.05 * Math.random(), 0.0);
                        }
                        break;
                    }
                    case "spore_burst": {
                        Vec3 particlePos = this.getEyePosition();
                        for (int i = 0; i < 50; ++i) {
                            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setColor(54, 69, 30).setScale((float)((double)0.2f + Math.random() * 0.1)), particlePos.x(), particlePos.y() - Math.random(), particlePos.z, -0.5 + Math.random(), 0.0, -0.5 + Math.random());
                        }
                        break;
                    }
                }
            }
        }
    }

    private void handleDelayCallback(String identifier, String data) {
        if (!this.level().isClientSide()) {
            switch (data) {
                case "melee": {
                    this.isMeleeAttacking = false;
                    break;
                }
                case "burrow": {
                    this.burrowCooldown = 300;
                    this.isBurrowing = false;
                    break;
                }
                case "heal": {
                    this.healCooldown = 1200;
                    this.isHealing = false;
                    break;
                }
                case "roots": {
                    this.rootsCooldown = 400;
                    this.isRooting = false;
                    break;
                }
                case "spores": {
                    this.sporesCooldown = 400;
                    this.isSporeBursting = false;
                }
            }
            this.isActing = this.isBurrowing || this.isHealing || this.isRooting || this.isSporeBursting || this.isMeleeAttacking;
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    public void handle(CustomInstructionKeyframeEvent<Deathcap> event) {
        if (this.attackEntity == ManaAndArtifice.instance.proxy.getClientPlayer() && event.getKeyframeData().getInstructions().indexOf(INSTRUCTION_FLING) > -1) {
            this.attackEntity.push(0.0, 1.0, 0.0);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isActing);
        nbt.putBoolean("burrowing", this.isBurrowing);
        nbt.putBoolean("rooting", this.isRooting);
        nbt.putBoolean("healing", this.isHealing);
        nbt.putBoolean("meleeLeftHand", this.meleeLeftHand);
        nbt.putBoolean("sporeBursting", this.isSporeBursting);
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.isActing = nbt.getBoolean("attacking");
        this.isBurrowing = nbt.getBoolean("burrowing");
        this.isHealing = nbt.getBoolean("healing");
        this.isRooting = nbt.getBoolean("rooting");
        this.meleeLeftHand = nbt.getBoolean("meleeLeftHand");
        this.isSporeBursting = nbt.getBoolean("sporeBursting");
        if (this.level().isClientSide()) {
            if (this.isHealing) {
                DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>("heal", 60, (LivingEntity)this, this::handleDelayCallback));
            } else if (this.isBurrowing) {
                DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_BURROW_POS, 50, (LivingEntity)this, this::handleDelayCallback));
                DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_BURROW_FLING, 75, (LivingEntity)this, this::handleDelayCallback));
            } else if (this.isSporeBursting) {
                DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<LivingEntity>(INSTRUCTION_SPORE_BURST, 10, (LivingEntity)this, this::handleDelayCallback));
            }
        }
    }

    public class HealGoal
    extends Goal {
        public HealGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return Deathcap.this.healCooldown <= 0 && Deathcap.this.getHealth() < Deathcap.this.getMaxHealth() * 0.75f && !Deathcap.this.isActing;
        }

        public void start() {
            Deathcap.this.getNavigation().stop();
            Deathcap.this.isHealing = true;
            Deathcap.this.isActing = true;
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<LivingEntity>("heal", 60, (LivingEntity)Deathcap.this, Deathcap.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<String>("resetattack", 90, "heal", Deathcap.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(Deathcap.this);
        }

        public boolean canContinueToUse() {
            return Deathcap.this.isHealing;
        }
    }

    public class SporeBurstGoal
    extends Goal {
        public SporeBurstGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return Deathcap.this.sporesCooldown <= 0 && Deathcap.this.getTarget() != null && Deathcap.this.getTarget().distanceTo((Entity)Deathcap.this) <= 3.0f && !Deathcap.this.isActing;
        }

        public void start() {
            Deathcap.this.getNavigation().stop();
            Deathcap.this.isSporeBursting = true;
            Deathcap.this.isActing = true;
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<LivingEntity>(Deathcap.INSTRUCTION_SPORE_BURST, 10, Deathcap.this.getTarget(), Deathcap.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<String>("resetattack", 60, Deathcap.ACTION_SPORES, Deathcap.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(Deathcap.this);
        }

        public boolean canContinueToUse() {
            return Deathcap.this.isSporeBursting;
        }
    }

    public class BurrowGoal
    extends Goal {
        public BurrowGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return Deathcap.this.burrowCooldown <= 0 && Deathcap.this.rootsCooldown > 0 && Deathcap.this.onGround() && Deathcap.this.level().loadedAndEntityCanStandOnFace(Deathcap.this.blockPosition().below(), (Entity)Deathcap.this, Direction.UP) && Deathcap.this.getTarget() != null && Deathcap.this.getTarget().onGround() && Deathcap.this.getTarget().distanceTo((Entity)Deathcap.this) > 8.0f && !Deathcap.this.isActing;
        }

        public void start() {
            Deathcap.this.getNavigation().stop();
            Deathcap.this.isBurrowing = true;
            Deathcap.this.isActing = true;
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<LivingEntity>(Deathcap.INSTRUCTION_BURROW_POS, 65, Deathcap.this.getTarget(), Deathcap.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<LivingEntity>(Deathcap.INSTRUCTION_BURROW_FLING, 80, Deathcap.this.getTarget(), Deathcap.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<String>("resetattack", 90, Deathcap.ACTION_BURROW, Deathcap.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(Deathcap.this);
        }

        public boolean canContinueToUse() {
            return Deathcap.this.isBurrowing;
        }
    }

    public class RootsGoal
    extends Goal {
        public RootsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return Deathcap.this.rootsCooldown <= 0 && Deathcap.this.onGround() && Deathcap.this.level().loadedAndEntityCanStandOnFace(Deathcap.this.blockPosition().below(), (Entity)Deathcap.this, Direction.UP) && Deathcap.this.getTarget() != null && Deathcap.this.getTarget().onGround() && !Deathcap.this.isActing;
        }

        public void start() {
            Deathcap.this.getNavigation().stop();
            Deathcap.this.isRooting = true;
            Deathcap.this.isActing = true;
            Deathcap.this.rootsPos = Deathcap.this.getTarget().position();
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<LivingEntity>(Deathcap.INSTRUCTION_ROOTS_POISON, 50, Deathcap.this.getTarget(), Deathcap.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(Deathcap.this.level(), new TimedDelayedEvent<String>("resetattack", 95, Deathcap.ACTION_ROOTS, Deathcap.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(Deathcap.this);
        }

        public boolean canContinueToUse() {
            return Deathcap.this.isRooting;
        }
    }
}

