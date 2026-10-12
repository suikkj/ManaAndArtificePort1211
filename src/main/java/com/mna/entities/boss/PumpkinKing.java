/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobType
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.common.ToolActions
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.PlayState
 */
package com.mna.entities.boss;

import com.mna.api.sound.SFX;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.entities.EntityInit;
import com.mna.entities.IAnimPacketSync;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.boss.attacks.PumpkinKingEntangle;
import com.mna.entities.boss.attacks.PumpkinKingIncinerate;
import com.mna.network.ServerMessageDispatcher;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class PumpkinKing
extends BossMonster<PumpkinKing>
implements IAnimPacketSync<PumpkinKing> {
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
    private static final Predicate<Player> randomPlayerFlyable = p -> p.getAbilities().flying || !p.onGround();
    private static final String INSTRUCTION_DAMAGE = "damage";
    private static final String INSTRUCTION_ENTANGLE = "entangle";
    private static final String INSTRUCTION_INCINERATE = "incinerate";
    private static final int ENTANGLE_CD = 300;
    private static final int INCINERATE_CD = 300;
    private static final int ATTACK_CD = 20;
    private int entangleCooldown = 0;
    private int incinerateCooldown = 0;
    private int attackCooldown = 0;
    private boolean isAttacking = false;
    private boolean leftHandPunch = false;
    private boolean isEntangling = false;
    private boolean isIncinerating = false;

    public PumpkinKing(EntityType<? extends PumpkinKing> type, Level world) {
        super(type, world);
    }

    public PumpkinKing(Level world) {
        this((EntityType<? extends PumpkinKing>)((EntityType)EntityInit.PUMPKIN_KING.get()), world);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new BossMonster.DoNothingGoal());
        this.goalSelector.addGoal(1, (Goal)new EntangleGoal());
        this.goalSelector.addGoal(1, (Goal)new IncinerateGoal());
        this.goalSelector.addGoal(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), false));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.35f));
        this.goalSelector.addGoal(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, le -> true));
    }

    @Override
    public void tick() {
        super.tick();
        --this.entangleCooldown;
        --this.incinerateCooldown;
        --this.attackCooldown;
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (this.isAttacking || this.attackCooldown > 0) {
            return true;
        }
        this.playSound(SFX.Entity.PumpkinKing.ATTACK, this.getSoundVolume(), this.getVoicePitch());
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>(INSTRUCTION_DAMAGE, 10, entityIn, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 15, "", this::handleDelayCallback));
        this.isAttacking = true;
        this.leftHandPunch = Math.random() < 0.5;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        return true;
    }

    @Override
    protected ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    private void handleDelayCallback(String identifier, Entity entity) {
        if (!this.level().isClientSide() && this.isAlive()) {
            block5 : switch (identifier) {
                case "damage": {
                    this.damageEntity(entity);
                    break;
                }
                case "incinerate": {
                    Player player;
                    ArrayList<Player> attacked = new ArrayList<Player>();
                    for (int i = 0; i < 10 && (player = this.getRandomNearbyPlayer(null)) != null; ++i) {
                        if (!attacked.contains(player)) {
                            this.spawnIncinerate(player);
                            attacked.add(player);
                        }
                        if (attacked.size() >= 4 || attacked.size() == this.level().players().size()) break block5;
                    }
                    break;
                }
                case "entangle": {
                    Player player = this.getRandomNearbyPlayer(randomPlayerFlyable);
                    if (player == null) break;
                    this.spawnEntangle(player);
                }
            }
        }
    }

    private void spawnIncinerate(Player player) {
        PumpkinKingIncinerate incinerate = new PumpkinKingIncinerate(this.level());
        incinerate.setPos(player.getX(), player.getY(), player.getZ());
        this.level().addFreshEntity((Entity)incinerate);
    }

    private void spawnEntangle(Player player) {
        BlockPos spawnPos = player.blockPosition();
        int offsetAmount = 10;
        spawnPos = spawnPos.offset(-offsetAmount + (int)(Math.random() * (double)offsetAmount * 2.0), 0, -offsetAmount + (int)(Math.random() * (double)offsetAmount * 2.0));
        while (spawnPos.getY() > 0 && this.level().isEmptyBlock(spawnPos)) {
            spawnPos = spawnPos.below();
        }
        spawnPos = spawnPos.above();
        PumpkinKingEntangle entangle = new PumpkinKingEntangle(this.level());
        entangle.setPos((float)spawnPos.getX() + 0.5f, spawnPos.getY(), (float)spawnPos.getZ() + 0.5f);
        entangle.setTarget(player);
        this.level().addFreshEntity((Entity)entangle);
    }

    public boolean damageEntity(Entity entityIn) {
        boolean flag;
        int i;
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entityIn instanceof LivingEntity) {
            f += com.mna.api.enchantments.MNAEnchantmentHelper.getDamageBonus((ItemStack)this.getMainHandItem(), (LivingEntity)entityIn);
            f1 += (float)EnchantmentHelper.getKnockbackBonus((LivingEntity)this);
        }
        if ((i = EnchantmentHelper.getFireAspect((LivingEntity)this)) > 0) {
            entityIn.igniteForSeconds((float)(i * 4));
        }
        if (flag = entityIn.hurt(this.damageSources().mobAttack((LivingEntity)this), f)) {
            if (f1 > 0.0f && entityIn instanceof LivingEntity) {
                ((LivingEntity)entityIn).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if (entityIn instanceof Player) {
                Player playerentity;
                this.maybeDisableShield(playerentity, (playerentity = (Player)entityIn).isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY);
            }
            com.mna.tools.MATools.doEnchantDamageEffects(this, entityIn);
            this.setLastHurtMob(entityIn);
            if (this.random.nextFloat() < 0.4f && entityIn instanceof Mob) {
                ((Mob)entityIn).setTarget((LivingEntity)this);
            }
        }
        return flag;
    }

    private void maybeDisableShield(Player player, ItemStack playerItem) {
        if (!playerItem.isEmpty() && playerItem.getItem().canPerformAction(playerItem, ItemAbilities.SHIELD_BLOCK)) {
            float f = 0.25f + (float)com.mna.api.enchantments.MNAEnchantmentHelper.getBlockEfficiency((LivingEntity)this) * 0.05f;
            if (this.random.nextFloat() < f) {
                player.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level().broadcastEntityEvent((Entity)player, (byte)30);
            }
        }
    }

    private void handleDelayCallback(String identifier, String data) {
        if (!this.level().isClientSide()) {
            this.isAttacking = false;
            if (this.isEntangling) {
                this.entangleCooldown = 300;
                if (this.incinerateCooldown <= 0) {
                    this.incinerateCooldown = 37;
                }
                this.isEntangling = false;
            } else if (this.isIncinerating) {
                this.incinerateCooldown = 300;
                this.isIncinerating = false;
            } else {
                this.attackCooldown = 20;
            }
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isAttacking);
        nbt.putBoolean("entangling", this.isEntangling);
        nbt.putBoolean("incinerating", this.isIncinerating);
        nbt.putBoolean("left_hand_punch", this.leftHandPunch);
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.isAttacking = nbt.getBoolean("attacking");
        this.isEntangling = nbt.getBoolean("entangling");
        this.isIncinerating = nbt.getBoolean("incinerating");
        this.leftHandPunch = nbt.getBoolean("left_hand_punch");
    }

    @Override
    public void setupSpawn() {
        this.setInvulnerableTicks(79);
        this.setHealth(this.getMaxHealth() / 3.0f);
        this.playSound(SFX.Entity.PumpkinKing.SPAWN, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    public ResourceLocation getArenaStructureID() {
        return null;
    }

    @Override
    public int getArenaStructureSegment() {
        return -1;
    }

    protected SoundEvent getAmbientSound() {
        return SFX.Entity.PumpkinKing.IDLE;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.PumpkinKing.DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return SFX.Entity.PumpkinKing.HURT;
    }

    @Override
    public boolean hurt(DamageSource type, float amount) {
        if (this.getInvulnerableTicks() > 0 && type.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        if (this.isDamageCheaty(type)) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return false;
        }
        if (type.is(DamageTypeTags.IS_FREEZING)) {
            amount *= 2.0f;
        }
        if (type.is(DamageTypeTags.IS_FIRE)) {
            amount = (float)((double)amount * 0.5);
        }
        return super.hurt(type, amount);
    }

    @Override
    protected void customServerAiStep() {
        if (this.getInvulnerableTicks() > 0) {
            int j1 = this.getInvulnerableTicks() - 1;
            if (j1 <= 0) {
                if (!this.isSilent()) {
                    this.level().globalLevelEvent(1023, this.blockPosition(), 0);
                }
                this.heal(this.getMaxHealth());
            }
            this.setInvulnerableTicks(j1);
            if (this.tickCount % 10 == 0) {
                this.heal(25.0f);
            }
        }
        super.customServerAiStep();
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BossMonster<?>> state) {
        if (this.getInvulnerableTicks() > 0) {
            state.getController().transitionLength(0);
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.PumpkinKing.spawn"));
        }
        state.getController().transitionLength(2);
        if (!this.isAttacking) {
            if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.PumpkinKing.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.PumpkinKing.idle"));
        }
        if (this.isIncinerating) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.PumpkinKing.incinerate"));
        }
        if (this.isEntangling) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.PumpkinKing.entangle"));
        }
        if (this.leftHandPunch) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.PumpkinKing.swipe_left").thenLoop("animation.PumpkinKing.idle"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation.PumpkinKing.swipe_right").thenLoop("animation.PumpkinKing.idle"));
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, (double)0.6f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 4.0);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.setYRot(additionalData.readFloat());
        this.yRotO = additionalData.readFloat();
        this.setXRot(additionalData.readFloat());
        this.xRotO = additionalData.readFloat();
        this.yBodyRot = this.getYRot();
        this.yBodyRotO = this.yRotO;
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.getYRot());
        buffer.writeFloat(this.yRotO);
        buffer.writeFloat(this.getXRot());
        buffer.writeFloat(this.xRotO);
    }

    public class EntangleGoal
    extends Goal {
        public EntangleGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return PumpkinKing.this.entangleCooldown <= 0 && PumpkinKing.this.getTarget() != null && !PumpkinKing.this.isAttacking && PumpkinKing.this.incinerateCooldown <= 0;
        }

        public void start() {
            PumpkinKing.this.getNavigation().stop();
            PumpkinKing.this.isEntangling = true;
            PumpkinKing.this.isAttacking = true;
            PumpkinKing.this.playSound(SFX.Entity.PumpkinKing.ENTANGLE, PumpkinKing.this.getSoundVolume(), PumpkinKing.this.getVoicePitch());
            DelayedEventQueue.pushEvent(PumpkinKing.this.level(), new TimedDelayedEvent<LivingEntity>(PumpkinKing.INSTRUCTION_ENTANGLE, 15, (LivingEntity)PumpkinKing.this, PumpkinKing.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(PumpkinKing.this.level(), new TimedDelayedEvent<LivingEntity>(PumpkinKing.INSTRUCTION_ENTANGLE, 25, (LivingEntity)PumpkinKing.this, PumpkinKing.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(PumpkinKing.this.level(), new TimedDelayedEvent<LivingEntity>(PumpkinKing.INSTRUCTION_ENTANGLE, 35, (LivingEntity)PumpkinKing.this, PumpkinKing.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(PumpkinKing.this.level(), new TimedDelayedEvent<LivingEntity>(PumpkinKing.INSTRUCTION_ENTANGLE, 45, (LivingEntity)PumpkinKing.this, PumpkinKing.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(PumpkinKing.this.level(), new TimedDelayedEvent<String>("resetattack", 60, "", PumpkinKing.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(PumpkinKing.this);
        }

        public boolean canContinueToUse() {
            return PumpkinKing.this.isEntangling;
        }
    }

    public class IncinerateGoal
    extends Goal {
        public IncinerateGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return PumpkinKing.this.incinerateCooldown <= 0 && PumpkinKing.this.getTarget() != null && !PumpkinKing.this.isAttacking;
        }

        public void start() {
            PumpkinKing.this.getNavigation().stop();
            PumpkinKing.this.isIncinerating = true;
            PumpkinKing.this.isAttacking = true;
            PumpkinKing.this.playSound(SFX.Entity.PumpkinKing.INCINERATE, PumpkinKing.this.getSoundVolume(), PumpkinKing.this.getVoicePitch());
            DelayedEventQueue.pushEvent(PumpkinKing.this.level(), new TimedDelayedEvent<LivingEntity>(PumpkinKing.INSTRUCTION_INCINERATE, 65, (LivingEntity)PumpkinKing.this, PumpkinKing.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(PumpkinKing.this.level(), new TimedDelayedEvent<String>("resetattack", 82, "", PumpkinKing.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(PumpkinKing.this);
        }

        public boolean canContinueToUse() {
            return PumpkinKing.this.isIncinerating;
        }
    }
}


