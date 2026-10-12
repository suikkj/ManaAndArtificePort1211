 
package com.mna.entities.boss;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.entities.DamageHelper;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.boss.attacks.ThrownAllfatherAxe;
import com.mna.entities.boss.effects.Bifrost;
import com.mna.items.ItemInit;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.spells.crafting.SpellRecipe;
import java.util.EnumSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import net.minecraft.core.registries.BuiltInRegistries;

public class Odin
extends BossMonster<Odin> {
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
    private static final EntityDataAccessor<Byte> DATA_AXE_SLOT = SynchedEntityData.defineId(BossMonster.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(BossMonster.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    protected static final TargetingConditions TARGETING_CONDITIONS_SNOWBLIND = TargetingConditions.forCombat().range(40.0).selector(LIVING_ENTITY_SELECTOR);
    public static final byte AXE_SLOT_BACK = 0;
    public static final byte AXE_SLOT_HAND = 1;
    public static final byte AXE_SLOT_THROW = 2;
    private static final int ACTION_FROST_SPELL = 2;
    private static final int ACTION_LIGHTNING_SPELL = 4;
    private static final int ACTION_THROW_AXE = 8;
    private static final int ACTION_ENRAGE = 16;
    private static final int ACTION_OFFER = 32;
    private static final int ACTION_LEAVE = 64;
    private static final int ACTION_WRATH = 128;
    private static final int ACTION_SMITE = 256;

    public Odin(EntityType<? extends Monster> p_i48553_1_, Level p_i48553_2_) {
        super(p_i48553_1_, p_i48553_2_);
    }

    public Odin(Level world) {
        this((EntityType<? extends Monster>)((EntityType)EntityInit.ODIN.get()), world);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 850.0).add(Attributes.MOVEMENT_SPEED, (double)0.6f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 20.0).add(Attributes.ATTACK_DAMAGE, 15.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.75);
    }

    public float getStepHeight() {
        return 1.6f;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.level().getNearbyPlayers(TARGETING_CONDITIONS_SNOWBLIND, (LivingEntity)this, this.getBoundingBox().inflate(40.0)).forEach(p -> p.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.SNOWBLIND.get()), 100, 0)));
            BlockPos search = this.blockPosition().below();
            for (int i = -2; i <= 2; ++i) {
                for (int j = -2; j <= 2; ++j) {
                    BlockPos offset = search.offset(i, 0, j);
                    BlockState belowState = this.level().getBlockState(offset);
                    if (belowState.is(Blocks.WATER)) {
                        this.level().setBlock(offset, Blocks.FROSTED_ICE.defaultBlockState(), 3);
                        continue;
                    }
                    if (!belowState.is(Blocks.LAVA)) continue;
                    this.level().setBlock(offset, Blocks.COBBLESTONE.defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AXE_SLOT, (byte)0);
        builder.define(ENRAGED, false);
    }

    public byte getAxeSlot() {
        return (Byte)this.entityData.get(DATA_AXE_SLOT);
    }

    public void setAxeSlot(byte slot) {
        this.entityData.set(DATA_AXE_SLOT, slot);
    }

    @Override
    protected ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BossMonster<?>> state) {
        if (this.getInvulnerableTicks() > 0) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.odin.summon"));
        }
        if (this.flagSet(32)) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.odin.offerAxe"));
        }
        if (this.flagSet(64)) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.odin.bifrostLeave"));
        }
        if (!this.flagSet(1)) {
            if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.odin.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.odin.idle"));
        }
        if (this.flagSet(4) || this.flagSet(8)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.odin.axeThrow"));
        }
        if (this.flagSet(2) || this.flagSet(4)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.odin.spellCast"));
        }
        if (this.flagSet(16)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.odin.axeImbue"));
        }
        if (this.flagSet(128)) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.odin.axeImbue"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation.odin.axeHit"));
    }

    @Override
    public void setupSpawn() {
        this.setInvulnerableTicks(110);
        this.setHealth(this.getMaxHealth() / 3.0f);
    }

    @Override
    protected void customServerAiStep() {
        if (this.getInvulnerableTicks() > 0) {
            int j1 = this.getInvulnerableTicks() - 1;
            if (j1 <= 0) {
                this.heal(this.getMaxHealth());
            }
            this.setInvulnerableTicks(j1);
            if (this.tickCount % 10 == 0) {
                this.heal(50.0f);
            }
            if (j1 == 11) {
                this.setAxeSlot((byte)1);
            }
        }
        super.customServerAiStep();
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (this.flagSet(1) || this.isOnCooldown(1) || (Byte)this.entityData.get(DATA_AXE_SLOT) == 2) {
            return true;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>("", 14, entityIn, this::handleDelayCallback));
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 18, "", this::handleDelayCallback));
        this.setState(1);
        return true;
    }

    @Override
    protected void spawnParticles() {
        int i;
        if (this.flagSet(16)) {
            for (i = 0; i < 20; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setMover(new ParticleOrbitMover(this.getX(), this.getY() + 3.0, this.getZ(), 0.1f, -0.01f, 1.0)), this.getX(), this.getY() + 3.0, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
        if (((Boolean)this.entityData.get(ENRAGED)).booleanValue()) {
            for (i = 0; i < 4; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setMover(new ParticleOrbitMover(this.getX(), this.getY() + 0.25, this.getZ(), 0.5, 0.065f, 0.8f)), this.getX(), this.getY() + 0.25, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource type, float amount) {
        if (this.flagSet(32)) {
            return false;
        }
        if (this.flagSet(64)) {
            return false;
        }
        if (((Boolean)this.entityData.get(ENRAGED)).booleanValue()) {
            amount *= 0.4f;
        }
        return super.hurt(type, amount);
    }

    @Override
    public void die(DamageSource type) {
        this.setHealth(1.0f);
        this.heal(300.0f);
        if (type.getEntity() instanceof Player) {
            this.setOfferingAxe();
        } else {
            this.setLeaving();
        }
    }

    public InteractionResult interactAt(Player player, Vec3 vector, InteractionHand hand) {
        if (this.flagSet(32)) {
            this.giveAxe(player);
            this.setLeaving();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public ResourceLocation getArenaStructureID() {
        return null;
    }

    @Override
    public int getArenaStructureSegment() {
        return -1;
    }

    public boolean damageEntity(Entity entityIn) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        boolean flag = entityIn.hurt((Boolean)this.entityData.get(ENRAGED) != false ? DamageHelper.createSourcedType(DamageHelper.FROST, this.level().registryAccess(), (Entity)this) : this.damageSources().mobAttack((LivingEntity)this), f);
        if (flag) {
            if (f1 > 0.0f && entityIn instanceof LivingEntity) {
                ((LivingEntity)entityIn).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if (entityIn instanceof Player) {
                Player playerentity = (Player)entityIn;
                this.maybeDisableShield(playerentity, playerentity.isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY);
            }
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

    public void hideAxe() {
        this.entityData.set(DATA_AXE_SLOT, (byte)2);
    }

    public void showAxe() {
        this.entityData.set(DATA_AXE_SLOT, (byte)1);
    }

    private void setOfferingAxe() {
        if (this.level().isClientSide()) {
            return;
        }
        this.clearState();
        this.setState(32);
        this.hideBossBar();
        if (!this.level().isClientSide()) {
            this.threat.forEach((entityID, threat) -> {
                Entity e = this.level().getEntity(entityID.intValue());
                if (e instanceof ServerPlayer) {
                    ServerPlayer player = (ServerPlayer)e;
                    player.sendSystemMessage((Component)Component.translatable((String)"entity.mna.odin.defeated"));
                    Stat stat = Stats.ENTITY_KILLED.get(this.getType());
                    player.awardStat(stat);
                    int totalKills = player.getStats().getValue(stat);
                    ResourceLocation typeName = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
                    CustomAdvancementTriggers.DEFEAT_BOSS.trigger(player, typeName, totalKills);
                }
            });
        }
    }

    @Override
    public void setTarget(LivingEntity target) {
        this.threat.initializeThreat(target);
        super.setTarget(target);
    }

    private void setLeaving() {
        if (this.level().isClientSide()) {
            return;
        }
        this.clearState();
        this.setState(64);
        this.setAxeSlot((byte)2);
        Bifrost bifrost = new Bifrost(this.level(), this.position().add(0.0, 10.0, 0.0));
        bifrost.setNoSummon();
        this.level().addFreshEntity((Entity)bifrost);
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("leave", 130, "leave", this::handleDelayCallback));
    }

    private void giveAxe(Player player) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack reward = new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE.get());
        if (!player.addItem(reward)) {
            player.drop(reward, false, false);
        }
    }

    private void handleDelayCallback(String id, Entity e) {
        if (this.flagSet(1)) {
            if (this.flagSet(16)) {
                this.entityData.set(ENRAGED, true);
                this.playSound(SFX.Entity.Odin.ENRAGE, 1.0f, 1.0f);
            } else {
                this.damageEntity(e);
                this.playSound(SFX.Entity.Odin.ATTACK, 1.0f, (float)(0.6 + Math.random() * 0.8));
            }
        }
    }

    private void handleDelayCallback(String id, String command) {
        if (command == "leave") {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        if (this.flagSet(16)) {
            this.clearState(16);
        }
        if (this.flagSet(8)) {
            this.clearState(8);
        }
        this.clearState(1);
        this.setCooldown(1, (Boolean)this.entityData.get(ENRAGED) != false ? 10 : 20);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("axe_slot")) {
            this.setAxeSlot(nbt.getByte("axe_slot"));
        }
        this.setState(0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("axe_slot", this.getAxeSlot());
    }

    protected void registerGoals() {
        SpellRecipe frost = new SpellRecipe();
        frost.setShape(Shapes.PROJECTILE).addComponent(Components.FROST_DAMAGE).addComponent(Components.SLOW);
        frost.changeShapeAttributeValue(Attribute.SPEED, 1.75f);
        frost.changeShapeAttributeValue(Attribute.RADIUS, 1.75f);
        frost.changeComponentAttributeValue(0, Attribute.DAMAGE, 20.0f);
        frost.changeComponentAttributeValue(0, Attribute.DURATION, 7.0f);
        frost.changeComponentAttributeValue(1, Attribute.MAGNITUDE, 2.0f);
        SpellRecipe lightning = new SpellRecipe();
        lightning.setShape(Shapes.PROJECTILE).addComponent(Components.LIGHTNING_DAMAGE).addComponent(Components.GRAVITY_WELL);
        lightning.changeShapeAttributeValue(Attribute.SPEED, 1.75f);
        lightning.changeShapeAttributeValue(Attribute.RADIUS, 1.75f);
        lightning.changeComponentAttributeValue(0, Attribute.DAMAGE, 20.0f);
        lightning.changeComponentAttributeValue(1, Attribute.DURATION, 10.0f);
        SpellRecipe wrath = new SpellRecipe();
        wrath.setShape(Shapes.WRATH).addComponent(Components.FRAILTY).addComponent(Components.LIGHTNING_DAMAGE).addComponent(Components.GRAVITY_WELL).addComponent(Components.PULL).addComponent(Components.ICE_SPIKE);
        wrath.changeShapeAttributeValue(Attribute.DURATION, 5.0f);
        wrath.changeShapeAttributeValue(Attribute.WIDTH, 15.0f);
        wrath.changeShapeAttributeValue(Attribute.HEIGHT, 15.0f);
        wrath.changeShapeAttributeValue(Attribute.RADIUS, 3.0f);
        wrath.changeComponentAttributeValue(0, Attribute.DURATION, 30.0f);
        wrath.changeComponentAttributeValue(1, Attribute.DAMAGE, 20.0f);
        wrath.changeComponentAttributeValue(1, Attribute.LESSER_MAGNITUDE, 100.0f);
        wrath.changeComponentAttributeValue(1, Attribute.RADIUS, 5.0f);
        wrath.changeComponentAttributeValue(2, Attribute.DURATION, 10.0f);
        wrath.changeComponentAttributeValue(3, Attribute.SPEED, 3.0f);
        wrath.changeComponentAttributeValue(4, Attribute.DAMAGE, 20.0f);
        SpellRecipe smite = new SpellRecipe();
        smite.setShape(Shapes.SMITE).addComponent(Components.LIGHTNING_DAMAGE).addComponent(Components.GRAVITY_WELL).addComponent(Components.PULL).addComponent(Components.ICE_SPIKE);
        smite.changeShapeAttributeValue(Attribute.HEIGHT, 15.0f);
        smite.changeShapeAttributeValue(Attribute.RANGE, 32.0f);
        smite.changeShapeAttributeValue(Attribute.RADIUS, 3.0f);
        wrath.changeComponentAttributeValue(0, Attribute.DAMAGE, 20.0f);
        wrath.changeComponentAttributeValue(0, Attribute.LESSER_MAGNITUDE, 100.0f);
        wrath.changeComponentAttributeValue(0, Attribute.RADIUS, 5.0f);
        wrath.changeComponentAttributeValue(1, Attribute.DURATION, 10.0f);
        wrath.changeComponentAttributeValue(2, Attribute.SPEED, 3.0f);
        wrath.changeComponentAttributeValue(3, Attribute.DAMAGE, 20.0f);
        this.goalSelector.addGoal(0, (Goal)new BossMonster.DoNothingGoal());
        this.goalSelector.addGoal(0, (Goal)new BossMonster.DoNothingGoal(32));
        this.goalSelector.addGoal(0, (Goal)new BossMonster.DoNothingGoal(64));
        this.goalSelector.addGoal(1, (Goal)new Enrage());
        this.goalSelector.addGoal(1, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(2, (Goal)new CastSpellAtTargetGoal(wrath.createAsSpell(), 0.35f, 1200, 16.0f, 0.0f, false, 128, 12, 120, () -> {
            this.setState(1);
            this.setState(128);
        }, () -> {
            this.clearState(1);
            this.setCooldown(1, (Boolean)this.entityData.get(ENRAGED) != false ? 10 : 20);
        }, null).setUsePredicate(b -> (Boolean)this.entityData.get(ENRAGED)).setChannelTime(100));
        this.goalSelector.addGoal(3, (Goal)new CastSpellAtTargetGoal(frost.createAsSpell(), 0.35f, 200, 32.0f, 2.0f, true, 2, 12, 20, () -> {
            this.setState(1);
            this.setState(2);
        }, () -> {
            this.clearState(1);
            this.setCooldown(1, (Boolean)this.entityData.get(ENRAGED) != false ? 10 : 20);
        }, null));
        this.goalSelector.addGoal(3, (Goal)new CastSpellAtTargetGoal(smite.createAsSpell(), 0.35f, 300, 30.0f, 0.0f, true, 256, 12, 20, () -> {
            this.setState(1);
            this.setState(256);
        }, () -> {
            this.clearState(1);
            this.setCooldown(1, (Boolean)this.entityData.get(ENRAGED) != false ? 10 : 20);
        }, null));
        this.goalSelector.addGoal(4, (Goal)new CastSpellAtTargetGoal(lightning.createAsSpell(), 0.35f, 200, 32.0f, 2.0f, true, 4, 12, 20, () -> {
            this.setState(1);
            this.setState(4);
        }, () -> {
            this.clearState(1);
            this.setCooldown(1, (Boolean)this.entityData.get(ENRAGED) != false ? 10 : 20);
        }, null).setUsePredicate(b -> (Byte)this.entityData.get(DATA_AXE_SLOT) != 1));
        this.goalSelector.addGoal(4, (Goal)new ThrowAxeAtTargetGoal(0.35f, 200, 6.0f, 2.0f, true, 16, 4, this::handleDelayCallback));
        this.goalSelector.addGoal(4, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), false));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.35f));
        this.goalSelector.addGoal(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.targetSelector.addGoal(1, (Goal)new BossMonster.ThreatTableHurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, le -> true));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal<BossMonster>(this, BossMonster.class, 10, true, false, le -> le != this));
    }

    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return SFX.Entity.Odin.HIT;
    }

    @Override
    public void handle(SoundKeyframeEvent<BossMonster<?>> event) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Entity.Odin.DRAW, SoundSource.HOSTILE, 1.0f, 1.0f, false);
    }

    class Enrage
    extends Goal {
        public Enrage() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return (double)Odin.this.getHealth() < (double)Odin.this.getMaxHealth() * 0.5 && !Odin.this.flagSet(1) && (Boolean)Odin.this.entityData.get(ENRAGED) == false;
        }

        public void start() {
            DelayedEventQueue.pushEvent(Odin.this.level(), new TimedDelayedEvent<LivingEntity>("cast", 100, (LivingEntity)Odin.this, Odin.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(Odin.this.level(), new TimedDelayedEvent<String>("resetAttack", 100, "", Odin.this::handleDelayCallback));
            Odin.this.setState(1, 16);
        }

        public boolean canContinueToUse() {
            return Odin.this.flagSet(1);
        }
    }

    public class ThrowAxeAtTargetGoal
    extends Goal {
        private final double moveSpeedAmp;
        private final float maxAttackDistance;
        private final float minAttackDistance;
        private final int attackCooldown;
        private int seeTime;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;
        private final boolean randomThreatTarget;
        private LivingEntity target;
        private boolean isWaitingOnAnim = false;
        private final int effectDelayTicks;
        private final int resetTicks;
        private final BiConsumer<String, String> callbackFunction;

        public ThrowAxeAtTargetGoal(double moveSpeedAmpIn, int attackCooldownIn, float maxAttackDistanceIn, float minAttackDistanceIn, boolean randomTargetFromThreatTable, int delayTicks, int resetTicks, BiConsumer<String, String> resetCallback) {
            this.moveSpeedAmp = moveSpeedAmpIn;
            this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
            this.minAttackDistance = minAttackDistanceIn * minAttackDistanceIn;
            this.attackCooldown = attackCooldownIn;
            this.effectDelayTicks = delayTicks;
            this.resetTicks = resetTicks;
            this.callbackFunction = resetCallback;
            this.randomThreatTarget = randomTargetFromThreatTable;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return Odin.this.getTarget() != null && !Odin.this.isOnCooldown(8) && !Odin.this.isOnCooldown(1) && Odin.this.getTarget().distanceToSqr((Entity)Odin.this) >= (double)this.minAttackDistance && (Byte)Odin.this.entityData.get(DATA_AXE_SLOT) != 2;
        }

        public boolean canContinueToUse() {
            if (this.target == null || this.target.isDeadOrDying() || this.target.position().distanceToSqr(Odin.this.position()) > Odin.this.getAttributeValue(Attributes.FOLLOW_RANGE)) {
                return false;
            }
            if (!Odin.this.isOnCooldown(8)) {
                return false;
            }
            if (this.isWaitingOnAnim) {
                return true;
            }
            return this.canUse() || !Odin.this.getNavigation().isDone();
        }

        public void start() {
            super.start();
            this.target = this.randomThreatTarget ? Odin.this.getRandomThreatTableTarget(Odin.this.getAttributeValue(Attributes.FOLLOW_RANGE)) : Odin.this.getTarget();
        }

        public void stop() {
            super.stop();
            Odin.this.setCooldown(8, this.attackCooldown);
            this.target = null;
            this.seeTime = 0;
            this.isWaitingOnAnim = false;
        }

        private void throwCallback(String identifier, LivingEntity target) {
            Vec3 origin = Odin.this.position().add(0.0, (double)Odin.this.getEyeHeight(), 0.0);
            Vec3 targetPos = target.position().add(0.0, (double)target.getEyeHeight(), 0.0);
            Vec3 targetVelocity = target.position().subtract(target.xOld, target.yOld, target.zOld);
            targetVelocity = targetVelocity.subtract(0.0, target.getAttribute(Attributes.GRAVITY).getValue(), 0.0);
            if (target.onGround()) {
                targetVelocity = targetVelocity.add(0.0, -targetVelocity.y, 0.0);
            }
            double distance = origin.distanceTo(targetPos);
            double speed = 1.0;
            int ticksToArrive = (int)Math.round(distance / speed);
            Vec3 adjustedTargetPos = targetPos.add(targetVelocity.scale((double)ticksToArrive));
            Vec3 delta = adjustedTargetPos.subtract(origin).normalize();
            Odin.this.hideAxe();
            ThrownAllfatherAxe thrown = new ThrownAllfatherAxe((LivingEntity)Odin.this, Odin.this.level(), null);
            thrown.shoot(delta.x, delta.y, delta.z, 1.0f, 0.0f);
            Odin.this.level().addFreshEntity((Entity)thrown);
            this.stop();
        }

        public void tick() {
            if (this.target == null) {
                this.stop();
                return;
            }
            if (this.isWaitingOnAnim) {
                return;
            }
            LivingEntity livingentity = Odin.this.getTarget();
            if (livingentity != null) {
                boolean positiveSeeTime;
                double d0 = Odin.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                boolean canSee = Odin.this.getSensing().hasLineOfSight((Entity)livingentity);
                boolean bl = positiveSeeTime = this.seeTime > 0;
                if (canSee != positiveSeeTime) {
                    this.seeTime = 0;
                }
                this.seeTime = canSee ? ++this.seeTime : --this.seeTime;
                if (!(d0 > (double)this.maxAttackDistance) && this.seeTime >= 20) {
                    Odin.this.getNavigation().stop();
                    ++this.strafingTime;
                } else {
                    Odin.this.getNavigation().moveTo((Entity)livingentity, this.moveSpeedAmp);
                    this.strafingTime = -1;
                }
                if (this.strafingTime >= 20) {
                    if ((double)Odin.this.getRandom().nextFloat() < 0.3) {
                        boolean bl2 = this.strafingClockwise = !this.strafingClockwise;
                    }
                    if ((double)Odin.this.getRandom().nextFloat() < 0.3) {
                        this.strafingBackwards = !this.strafingBackwards;
                    }
                    this.strafingTime = 0;
                }
                if (this.strafingTime > -1) {
                    if (d0 > (double)(this.maxAttackDistance * 0.75f)) {
                        this.strafingBackwards = false;
                    } else if (d0 < (double)(this.maxAttackDistance * 0.25f)) {
                        this.strafingBackwards = true;
                    }
                    Odin.this.getMoveControl().strafe(this.strafingBackwards ? -0.5f : 0.5f, this.strafingClockwise ? 0.5f : -0.5f);
                    Odin.this.lookAt((Entity)livingentity, 30.0f, 30.0f);
                } else {
                    Odin.this.getLookControl().setLookAt((Entity)livingentity, 30.0f, 30.0f);
                }
                if (!canSee && this.seeTime < -60) {
                    this.stop();
                } else if (canSee) {
                    DelayedEventQueue.pushEvent(Odin.this.level(), new TimedDelayedEvent<LivingEntity>("throw", this.effectDelayTicks, livingentity, this::throwCallback));
                    DelayedEventQueue.pushEvent(Odin.this.level(), new TimedDelayedEvent<String>("resetAttack", this.resetTicks, "", this.callbackFunction));
                    Odin.this.setState(1, 8);
                    this.isWaitingOnAnim = true;
                }
            }
        }
    }
}

