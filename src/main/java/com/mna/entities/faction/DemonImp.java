/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.Difficulty
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.entity.MobType
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.HitResult$Type
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.PlayState
 */
package com.mna.entities.faction;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ai.CastSpellAtTargetGoal;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.entities.EntityInit;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.ai.LerpLeap;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.network.ServerMessageDispatcher;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class DemonImp
extends BaseFactionMob<DemonImp> {
    private static final int ACTION_LEAP = 1000;
    private static final int ACTION_CAST_SPELL = 1001;
    private static final ItemStack Spell_T1 = new ItemStack((ItemLike)ItemInit.SPELL.get());
    private static final ItemStack Spell_T2 = new ItemStack((ItemLike)ItemInit.SPELL.get());
    private static final ItemStack Spell_T3 = new ItemStack((ItemLike)ItemInit.SPELL.get());
    private int castTimer = 0;

    public DemonImp(EntityType<DemonImp> type, Level worldIn) {
        super(type, worldIn);
    }

    public DemonImp(Level worldIn) {
        this((EntityType<DemonImp>)((EntityType)EntityInit.DEMON_IMP.get()), worldIn);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, (double)0.6f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 5; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), -0.5 + this.getX() + Math.random(), this.getY() + Math.random() * (double)this.getEyeHeight(), -0.5 + this.getZ() + Math.random(), this.getDeltaMovement().x, (double)0.1f, this.getDeltaMovement().z);
            }
        }
    }

    private Action getAction() {
        int ordinal = (this.getStateFlag() & 0x7F8) >> 3 & 0xFF;
        if (ordinal >= Action.values().length) {
            return Action.IDLE;
        }
        return Action.values()[ordinal];
    }

    private AttackAction getAttackAction() {
        int ordinal = (this.getStateFlag() & 0xFFFF) >> 12 & 0xF;
        if (ordinal >= AttackAction.values().length) {
            return AttackAction.NONE;
        }
        return AttackAction.values()[ordinal];
    }

    private void setAction(Action action) {
        byte ordinal = (byte)((action.ordinal() & 0xFF) << 3);
        int flag = this.getStateFlag();
        flag &= 0xFFFFF807;
        this.setState(flag |= ordinal);
    }

    private void setAttackAction(AttackAction action) {
        int ordinal = (action.ordinal() & 0xF) << 12;
        int flag = this.getStateFlag();
        flag &= 0xFFFF0FFF;
        this.setState(flag |= ordinal);
    }

    public static boolean canSpawnPredicate(EntityType<DemonImp> mobType, LevelAccessor levelAccessor, MobSpawnType spawnType, BlockPos pos, RandomSource rand) {
        if (IFactionEnemy.canMnASpawn(levelAccessor, pos)) {
            return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && levelAccessor.getBlockState(pos.below()).getBlock() != Blocks.NETHER_WART_BLOCK;
        }
        return false;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.getAction() != Action.IDLE) {
            block0 : switch (this.getAction()) {
                case ATTACKING: {
                    switch (this.getAttackAction()) {
                        case LEFT: {
                            builder.thenPlay("animation.model.swipe_left");
                            break block0;
                        }
                        case RIGHT: {
                            builder.thenPlay("animation.model.swipe_right");
                            break block0;
                        }
                    }
                    builder.thenPlay("animation.model.bite");
                    break;
                }
                case CAST_SPELL: {
                    builder.thenPlay("animation.model.swipe_left");
                    break;
                }
                case LEAP_START: {
                    builder.thenPlay("animation.model.leap_start");
                    break;
                }
                case LEAPING: {
                    builder.thenPlay("animation.model.leap_loop");
                    break;
                }
                case LEAP_LANDING: {
                    builder.thenPlay("animation.model.leap_land");
                    break;
                }
            }
        }
        if (builder.getAnimationStages().size() == 0) {
            double movementSpeed = this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length();
            if (movementSpeed > 0.1) {
                builder.thenLoop("animation.model.run");
            } else if (movementSpeed > 0.02) {
                builder.thenLoop("animation.model.walk");
            } else {
                builder.thenLoop("animation.model.idle");
            }
        }
        return state.setAndContinue(builder);
    }

    @Override
    public IFaction getFaction() {
        return Factions.DEMONS;
    }

    @Override
    public void setTier(int tier) {
        super.setTier(tier);
        this.goalSelector.removeAllGoals(g -> g instanceof CastSpellAtTargetGoal);
        ItemStack spell = ItemStack.EMPTY;
        switch (tier) {
            case 1: {
                spell = Spell_T2;
                break;
            }
            case 2: {
                spell = Spell_T3;
                break;
            }
            default: {
                spell = Spell_T1;
            }
        }
        if (!spell.isEmpty()) {
            CastSpellAtTargetGoal<DemonImp> spellGoal = new CastSpellAtTargetGoal<DemonImp>(this, spell, 0.35, 0, 16.0f).setAttackCooldown(0).setStopCallback(DemonImp::onCastEnd).setStartCallback(DemonImp::onTargetCastStart).setResetCallback(DemonImp::onCastEnd).setCanUseCallback(DemonImp::canCast).setPrecastCallback(DemonImp::onTargetCasting);
            this.goalSelector.addGoal(1, spellGoal);
        }
    }

    private static void onCastEnd(DemonImp entity) {
        entity.setAction(Action.IDLE);
        entity.castTimer = 0;
        entity.setCooldown(1001, 300);
        entity.setCooldown(1000, 60);
    }

    private static void onTargetCastStart(DemonImp entity) {
        entity.setAction(Action.CAST_SPELL);
    }

    private static boolean canCast(DemonImp entity) {
        if (entity.isOnCooldown(1001)) {
            return false;
        }
        if (entity.getTarget() == null || entity.getTarget().isDeadOrDying()) {
            return false;
        }
        if (entity.distanceToSqr((Entity)entity.getTarget()) < 9.0) {
            return false;
        }
        return entity.getAction() == Action.IDLE || entity.getAction() == Action.CAST_SPELL;
    }

    private static boolean onTargetCasting(DemonImp entity) {
        entity.setAction(Action.CAST_SPELL);
        if (entity.castTimer++ < 5) {
            return false;
        }
        entity.setAction(Action.IDLE);
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(2, (Goal)new FactionTierWrapperGoal(1, this, new LerpLeap<DemonImp>(this, 10, 3, t -> this.getTarget() != null && !this.isOnCooldown(1000) && this.distanceTo((Entity)this.getTarget()) > 8.0f && this.getSensing().hasLineOfSight((Entity)this.getTarget()) && this.level().clip(new ClipContext(this.getEyePosition(), this.getTarget().getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null)).getType() == HitResult.Type.MISS, e -> {
            switch (e) {
                case DAMAGE: {
                    this.setAction(Action.LEAP_LANDING);
                    if (this.getTarget() == null) break;
                    this.damageEntity((Entity)this.getTarget());
                    break;
                }
                case LEAP: {
                    this.setAction(Action.LEAPING);
                    break;
                }
                case START: {
                    this.setAction(Action.LEAP_START);
                    this.playSound(SFX.Entity.Imp.LEAP, 1.0f, 1.0f);
                    break;
                }
                case STOP: {
                    this.setAction(Action.IDLE);
                    this.setCooldown(1000, 300);
                    this.setCooldown(1001, 60);
                    break;
                }
            }
        }).setLeapSpeed(4.0f).setEndWaitTicks(4)));
        this.goalSelector.addGoal(3, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), false));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.35f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, this::factionTargetPlayerPredicate));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, this::factionTargetHelpPredicate));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (this.getAction() != Action.IDLE) {
            return false;
        }
        ArrayList<AttackAction> actionPool = new ArrayList<AttackAction>();
        actionPool.add(AttackAction.LEFT);
        actionPool.add(AttackAction.RIGHT);
        actionPool.add(AttackAction.BITE);
        AttackAction selected = (AttackAction)(actionPool.get((int)((double)actionPool.size() * Math.random())));
        this.setAction(Action.ATTACKING);
        this.setAttackAction(selected);
        this.setTimer("", selected.animLength, () -> {
            this.damageEntity(entityIn);
            this.playSound(SFX.Entity.Imp.ATTACK, 1.0f, (float)(0.6 + Math.random() * 0.8));
        });
        this.setTimer("resetattack", selected.resetTime, () -> {
            this.setAttackAction(AttackAction.NONE);
            this.setAction(Action.IDLE);
        });
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.playSound(SFX.Entity.Imp.ATTACK, 1.0f, (float)(0.9 + Math.random() * 0.2));
        return true;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!this.level().isClientSide() && cause.getEntity() instanceof LivingEntity && this.tier >= 3) {
            this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), 5.0f, Level.ExplosionInteraction.NONE);
        }
    }

    public SoundEvent getAmbientSound() {
        return SFX.Entity.Imp.IDLE;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.Imp.DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SFX.Entity.Imp.ATTACK;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypeTags.IS_LIGHTNING)) {
            amount *= 0.5f;
        }
        amount = this.applyDamageResists(source, amount);
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
        if (this.getAction() == Action.LEAP_LANDING) {
            f += 4.0f;
        }
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entityIn instanceof LivingEntity) {
            f += EnchantmentHelper.getDamageBonus((ItemStack)this.getMainHandItem(), (LivingEntity)entityIn);
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

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
    }

    static {
        SpellRecipe spell = new SpellRecipe(Shapes.PROJECTILE, Components.FIRE_DAMAGE);
        spell.writeToNBT(Spell_T1.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        spell = new SpellRecipe(Shapes.PROJECTILE, Components.FROST_DAMAGE);
        ((ModifiedSpellPart)spell.getShape()).setValue(Attribute.SPEED, 2.0f);
        ((ModifiedSpellPart)spell.getComponent(0)).setValue(Attribute.DAMAGE, 10.0f);
        spell.addComponent(Components.LIFE_TAP);
        ((ModifiedSpellPart)spell.getComponent(1)).setValue(Attribute.DURATION, 30.0f);
        spell.setOverrideAffinity(Affinity.ICE);
        spell.writeToNBT(Spell_T2.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        spell = new SpellRecipe(Shapes.PROJECTILE, Components.FIRE_DAMAGE);
        ((ModifiedSpellPart)spell.getShape()).setValue(Attribute.SPEED, 3.0f);
        ((ModifiedSpellPart)spell.getComponent(0)).setValue(Attribute.DAMAGE, 10.0f);
        spell.addComponent(Components.LIFE_TAP);
        ((ModifiedSpellPart)spell.getComponent(1)).setValue(Attribute.DURATION, 30.0f);
        spell.addComponent(Components.BACKDRAFT);
        spell.setOverrideAffinity(Affinity.HELLFIRE);
        spell.writeToNBT(Spell_T3.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    public static enum Action {
        IDLE,
        ATTACKING,
        LEAP_START,
        LEAPING,
        LEAP_LANDING,
        CAST_SPELL;

    }

    public static enum AttackAction {
        NONE(0, 0),
        LEFT(10, 14),
        RIGHT(10, 14),
        BITE(10, 14);

        public final int animLength;
        public final int resetTime;

        private AttackAction(int animLength, int resetTime) {
            this.animLength = animLength;
            this.resetTime = resetTime;
        }
    }
}

