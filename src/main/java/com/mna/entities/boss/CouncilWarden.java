 
package com.mna.entities.boss;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ai.CastSpellOnSelfGoal;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.tools.RLoc;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.ai.AIPredicateWrapperGoal;
import com.mna.entities.ai.LerpLeap;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.utility.EldrinFlight;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_client.SpawnParticleEffectMessage;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.spells.SpellCaster;
import com.mna.spells.components.ComponentFling;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.EntityUtil;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import org.apache.commons.lang3.mutable.MutableBoolean;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class CouncilWarden
extends BossMonster<CouncilWarden>
implements IFactionEnemy<CouncilWarden> {
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
    private static final int COOLDOWN_ID_LEAP = 1000;
    private static final int COOLDOWN_ID_GRAVITY_WELL = 1001;
    private static final int COOLDOWN_ID_PULL = 1002;
    private static final int COOLDOWN_ID_GREATER_ANIMUS = 1003;
    private static final int COOLDOWN_ID_ARCANE_EMBERS = 1004;
    private static final int COOLDOWN_ID_ARCANE_SMITE = 1005;
    private static final int COOLDOWN_ID_SHIELD = 1006;
    private static final int COOLDOWN_ID_LEAP_AWAY = 1007;
    private int globalSpellCooldown = 0;
    private int wardenDeathTime = 0;

    public CouncilWarden(EntityType<? extends Monster> p_i48553_1_, Level p_i48553_2_) {
        super(p_i48553_1_, p_i48553_2_);
    }

    public CouncilWarden(Level level, Vec3 position) {
        this((EntityType<? extends Monster>)((EntityType)EntityInit.COUNCIL_WARDEN.get()), level);
        this.setPos(position);
    }

    public WeaponState getWeaponState() {
        int ordinal = this.getStateFlag() & 3;
        if (ordinal < WeaponState.values().length) {
            return WeaponState.values()[ordinal];
        }
        return WeaponState.SWORD_ON_BACK;
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

    private void setWeaponState(WeaponState state) {
        byte ordinal = (byte)(state.ordinal() & 3);
        int flag = this.getStateFlag();
        flag &= 0xFFFFFFFC;
        this.setState(flag |= ordinal);
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

    @Override
    protected ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BossMonster<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.getInvulnerableTicks() > 0) {
            builder.thenPlay("animation.CouncilWarden.summon");
        } else if (this.getAction() == Action.SWORD_LEAP_UP || this.getAction() == Action.LEAP_AWAY) {
            builder.thenPlay("animation.CouncilWarden.swordspike_jump");
        } else if (this.getAction() == Action.ATTACKING) {
            switch (this.getAttackAction()) {
                case LEFT_CROSS: {
                    builder.thenPlay("animation.CouncilWarden.cross_left");
                    break;
                }
                case RIGHT_CROSS: {
                    builder.thenPlay("animation.CouncilWarden.cross_right");
                    break;
                }
                case LEFT_CAST: {
                    builder.thenPlay("animation.CouncilWarden.offhand_cast_left");
                    break;
                }
                case RIGHT_CAST: {
                    builder.thenPlay("animation.CouncilWarden.offhand_cast_right");
                    break;
                }
                case SELF_CAST: {
                    builder.thenPlay("animation.CouncilWarden.self_cast");
                    break;
                }
                case SLAM_CAST: {
                    builder.thenPlay("animation.CouncilWarden.cast_slam");
                    break;
                }
                default: {
                    if (this.getTarget() != null) {
                        builder.thenLoop("animation.CouncilWarden.idle_combat");
                        break;
                    }
                    builder.thenLoop("animation.CouncilWarden.idle");
                    break;
                }
            }
        } else if (this.getAction() == Action.DEATH) {
            builder.thenPlayAndHold("animation.CouncilWarden.death");
        } else {
            double movementSpeed = this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length();
            if (movementSpeed > 0.1) {
                builder.thenLoop("animation.CouncilWarden.run");
            } else if (movementSpeed > 0.02) {
                if (this.getTarget() != null) {
                    builder.thenLoop("animation.CouncilWarden.walk_combat");
                } else {
                    builder.thenLoop("animation.CouncilWarden.walk");
                }
            } else if (this.getTarget() != null) {
                builder.thenLoop("animation.CouncilWarden.idle_combat");
            } else {
                builder.thenLoop("animation.CouncilWarden.idle");
            }
        }
        return state.setAndContinue(builder);
    }

    private void setCastAnimation() {
        this.setAction(Action.ATTACKING);
        this.setAttackAction(this.getWeaponState() != WeaponState.SWORD_IN_HAND && Math.random() < 0.5 ? AttackAction.RIGHT_CAST : AttackAction.LEFT_CAST);
    }

    private void setSelfCastAnimation() {
        this.setAction(Action.ATTACKING);
        this.setAttackAction(AttackAction.SELF_CAST);
    }

    private void setSlamCastAnimation() {
        this.setAction(Action.ATTACKING);
        this.setAttackAction(AttackAction.SLAM_CAST);
    }

    private void resetAfterSpellcast(int cooldownModifier) {
        this.setAction(Action.IDLE);
        this.setAttackAction(AttackAction.NONE);
        this.globalSpellCooldown = 10 + (int)(Math.random() * (double)cooldownModifier);
    }

    @Override
    public void setupSpawn() {
        this.setInvulnerableTicks(40);
        this.setInvulnerable(true);
        this.setHealth(this.getMaxHealth() / 3.0f);
        this.setNoAi(true);
    }

    @Override
    public ResourceLocation getArenaStructureID() {
        return RLoc.create("faction/boss/council_island");
    }

    @Override
    public int getArenaStructureSegment() {
        return 1;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getInvulnerableTicks() > 0) {
            if (this.getInvulnerableTicks() > 1) {
                this.heal(5.0f);
                if (this.getInvulnerableTicks() == 20) {
                    this.setWeaponState(WeaponState.SWORD_IN_HAND);
                }
            } else if (this.getInvulnerableTicks() == 1) {
                this.setNoAi(false);
                this.setInvulnerable(false);
                this.setHealth(this.getMaxHealth());
            }
            this.setInvulnerableTicks(this.getInvulnerableTicks() - 1);
            return;
        }
        if (this.globalSpellCooldown > 0) {
            --this.globalSpellCooldown;
        }
        if (this.getAction() == Action.ATTACKING && this.getTarget() != null) {
            this.lookAt(EntityAnchorArgument.Anchor.FEET, this.getTarget().position());
        }
    }

    protected void registerGoals() {
        double moveSpeed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        ItemStack earthquake = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe quakeSpell = new SpellRecipe(Shapes.TOUCH, Components.EARTHQUAKE);
        ((ModifiedSpellPart)quakeSpell.getShape()).setValue(Attribute.RADIUS, 1.0f);
        ((ModifiedSpellPart)quakeSpell.getComponent(0)).setValue(Attribute.DURATION, 10.0f);
        quakeSpell.writeToNBT(earthquake.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack bolt_gravity_well = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe gravWellSpell = new SpellRecipe(Shapes.BOLT, Components.GRAVITY_WELL);
        gravWellSpell.addComponent(Components.SLOW);
        ((ModifiedSpellPart)gravWellSpell.getShape()).setValue(Attribute.RANGE, 48.0f);
        ((ModifiedSpellPart)gravWellSpell.getComponent(0)).setValue(Attribute.DURATION, 10.0f);
        ((ModifiedSpellPart)gravWellSpell.getComponent(1)).setValue(Attribute.DURATION, 10.0f);
        ((ModifiedSpellPart)gravWellSpell.getComponent(1)).setValue(Attribute.MAGNITUDE, 3.0f);
        gravWellSpell.writeToNBT(bolt_gravity_well.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack pull = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe pullSpell = new SpellRecipe(Shapes.PROJECTILE, Components.PULL);
        ((ModifiedSpellPart)pullSpell.getShape()).setValue(Attribute.RADIUS, 9.0f);
        ((ModifiedSpellPart)pullSpell.getShape()).setValue(Attribute.SPEED, 2.0f);
        ((ModifiedSpellPart)pullSpell.getComponent(0)).setValue(Attribute.SPEED, 3.0f);
        pullSpell.writeToNBT(pull.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack greater_animus = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe animusSpell = new SpellRecipe(Shapes.SELF, Components.GREATER_ANIMUS);
        ((ModifiedSpellPart)animusSpell.getComponent(0)).setValue(Attribute.DURATION, 30.0f);
        ((ModifiedSpellPart)animusSpell.getComponent(0)).setValue(Attribute.LESSER_MAGNITUDE, 1000.0f);
        animusSpell.writeToNBT(greater_animus.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack arcane_embers = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe embersSpell = new SpellRecipe(Shapes.EMBERS, Components.MAGIC_DAMAGE);
        ((ModifiedSpellPart)embersSpell.getComponent(0)).setValue(Attribute.DAMAGE, 20.0f);
        ((ModifiedSpellPart)embersSpell.getShape()).setValue(Attribute.MAGNITUDE, 3.0f);
        embersSpell.writeToNBT(arcane_embers.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack arcane_smite = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe smiteSpell = new SpellRecipe(Shapes.SMITE, Components.MAGIC_DAMAGE);
        ((ModifiedSpellPart)smiteSpell.getComponent(0)).setValue(Attribute.DAMAGE, 20.0f);
        ((ModifiedSpellPart)smiteSpell.getComponent(0)).setValue(Attribute.RADIUS, 3.0f);
        smiteSpell.addComponent(Components.DAMPEN_MAGIC);
        ((ModifiedSpellPart)smiteSpell.getComponent(1)).setValue(Attribute.DURATION, 20.0f);
        smiteSpell.writeToNBT(arcane_smite.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack shield = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe shieldSpell = new SpellRecipe(Shapes.SELF, Components.SHIELD);
        ((ModifiedSpellPart)shieldSpell.getComponent(0)).setValue(Attribute.DURATION, 60.0f);
        ((ModifiedSpellPart)shieldSpell.getComponent(0)).setValue(Attribute.MAGNITUDE, 1.0f);
        shieldSpell.writeToNBT(shield.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        this.registerShield(1, shield);
        this.registerEmbers(1, arcane_embers);
        this.registerBoltGravWell(2, bolt_gravity_well);
        this.registerGreaterAnimus(2, greater_animus);
        this.registerLeapAwayGoal(3);
        this.registerSmite(4, arcane_smite);
        this.registerPull(5, pull);
        this.registerLeapGoal(5, earthquake);
        this.goalSelector.addGoal(7, new AIPredicateWrapperGoal<CouncilWarden>(this, new BossMonster.AdjustableSpeedMeleeAttackGoal((PathfinderMob)this, moveSpeed, true, 10).setReachMultiplier(1.25f)).executionPredicate(e -> e.getWeaponState() == WeaponState.SWORD_IN_HAND).continuationPredicate(e -> e.getWeaponState() == WeaponState.SWORD_IN_HAND));
        this.goalSelector.addGoal(8, (Goal)new RandomStrollGoal((PathfinderMob)this, moveSpeed));
        this.goalSelector.addGoal(9, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.targetSelector.addGoal(1, (Goal)new BossMonster.ThreatTableHurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal(this, Player.class, 10, true, false, le -> true));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    private void registerLeapGoal(int priority, ItemStack spell) {
        this.goalSelector.addGoal(priority, new LerpLeap<CouncilWarden>(this, 10, 8, e -> e.onGround() && e.getTarget() != null && e.getTarget().distanceTo((Entity)e) > 8.0f && e.getTarget().onGround() && e.getTarget().isAlive() && !e.isOnCooldown(1000) && e.getWeaponState() == WeaponState.SWORD_IN_HAND, evt -> {
            switch (evt) {
                case DAMAGE: {
                    this.level().getEntities((Entity)this, this.getBoundingBox().inflate(4.0), tgt -> tgt instanceof LivingEntity && tgt.isAlive() && tgt != this && !this.isAlliedTo((Entity)tgt)).stream().map(e -> (LivingEntity)e).forEach(target -> {
                        float mX = (float)(this.getX() - target.getX());
                        float mZ = (float)(this.getZ() - target.getZ());
                        float speed = 1.9499999f;
                        ComponentFling.flingTarget(target, new Vec3((double)mX, (double)speed, (double)mZ), speed, 3.0f);
                        target.hurt(this.damageSources().mobAttack((LivingEntity)this), 30.0f);
                        target.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.EARTHQUAKE.get()), 100, 1));
                    });
                    SpellSource me = new SpellSource((LivingEntity)this, InteractionHand.MAIN_HAND);
                    for (int i = -5; i <= 5; ++i) {
                        for (int j = -5; j <= 5; ++j) {
                            if (Math.random() < 0.5 || i == 0 & j == 0) continue;
                            SpellTarget hint = new SpellTarget(this.blockPosition().below().offset(i, 0, j), Direction.UP);
                            SpellCaster.Affect(spell, SpellRecipe.fromNBT(spell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()), this.level(), me, hint);
                        }
                    }
                    if (this.level().isClientSide()) break;
                    ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.level().dimension(), 32.0f, this.getX(), this.getY(), this.getZ(), SpawnParticleEffectMessage.ParticleTypes.CONSTRUCT_HAMMER_SMASH);
                    break;
                }
                case START: {
                    this.setAction(Action.SWORD_LEAP_UP);
                    break;
                }
                case STOP: {
                    this.setAction(Action.IDLE);
                    this.setAttackAction(AttackAction.NONE);
                    this.setCooldown(1000, 1500);
                    break;
                }
            }
        }).setForceLerpTicks(38).setEndWaitTicks(20));
    }

    private void registerLeapAwayGoal(int priority) {
        this.goalSelector.addGoal(priority, new LerpLeap<CouncilWarden>(this, 10, 8, e -> e.onGround() && e.getTarget() != null && e.getTarget().isAlive() && e.getTarget().distanceTo((Entity)e) < 6.0f && !e.isOnCooldown(1007) && (!e.isOnCooldown(1003) || !e.isOnCooldown(1005) || !e.isOnCooldown(1000) || !e.isOnCooldown(1002)), evt -> {
            switch (evt) {
                case START: {
                    this.setAction(Action.LEAP_AWAY);
                    break;
                }
                case STOP: {
                    this.setAction(Action.IDLE);
                    this.setAttackAction(AttackAction.NONE);
                    this.setCooldown(1007, 2000);
                    break;
                }
            }
        }).setForceLerpTicks(38).setEndWaitTicks(20).setEndAdjuster(end -> {
            Vec3 rPos = EntityUtil.getRetreatPos((Entity)this, this.getForward().scale(-1.0), 16);
            if (rPos == null) {
                return end;
            }
            return rPos;
        }));
    }

    private void registerBoltGravWell(int priority, ItemStack bolt_gravity_well) {
        this.goalSelector.addGoal(priority, (Goal)new CastSpellAtTargetGoal(bolt_gravity_well, this.getAttributeValue(Attributes.MOVEMENT_SPEED), 100, 42.0f, 2.0f, false, 1001, 7, 5, null, () -> this.resetAfterSpellcast(30), me -> {
            this.setCastAnimation();
            return true;
        }).setUsePredicate(e -> this.getAction() == Action.IDLE && this.globalSpellCooldown == 0 && !this.getTarget().onGround()));
    }

    private void registerPull(int priority, ItemStack pull) {
        this.goalSelector.addGoal(priority, (Goal)new CastSpellAtTargetGoal(pull, this.getAttributeValue(Attributes.MOVEMENT_SPEED), 100, 42.0f, 2.0f, true, 1002, 7, 5, null, () -> this.resetAfterSpellcast(30), me -> {
            this.setCastAnimation();
            return true;
        }).setUsePredicate(e -> this.getAction() == Action.IDLE && this.globalSpellCooldown == 0 && !this.getTarget().onGround()).setTargetPredicate(e -> e.distanceTo((Entity)this) > 8.0f));
    }

    private void registerSmite(int priority, ItemStack arcane_smite) {
        this.goalSelector.addGoal(priority, (Goal)new CastSpellAtTargetGoal(arcane_smite, this.getAttributeValue(Attributes.MOVEMENT_SPEED), 200, 42.0f, 2.0f, true, 1005, 7, 5, null, () -> this.resetAfterSpellcast(30), me -> {
            this.setSlamCastAnimation();
            return true;
        }).setUsePredicate(e -> this.getAction() == Action.IDLE && this.globalSpellCooldown == 0 && !this.getTarget().onGround()).setTargetPredicate(e -> e.distanceTo((Entity)this) > 2.0f));
    }

    private void registerShield(int priority, ItemStack shield) {
        this.goalSelector.addGoal(priority, new CastSpellOnSelfGoal<CouncilWarden>(this, shield, e -> !e.isOnCooldown(1006) && !e.hasEffect(MobEffects.DAMAGE_RESISTANCE) && this.globalSpellCooldown <= 0, me -> me.setSelfCastAnimation(), me -> {
            me.resetAfterSpellcast(30);
            me.setCooldown(1006, 300);
        }, 21));
    }

    private void registerEmbers(int priority, ItemStack arcane_embers) {
        this.goalSelector.addGoal(priority, new CastSpellOnSelfGoal<CouncilWarden>(this, arcane_embers, e -> !e.isOnCooldown(1004) && this.globalSpellCooldown <= 0, me -> me.setSelfCastAnimation(), me -> {
            me.resetAfterSpellcast(30);
            me.setCooldown(1004, 300);
        }, 21));
    }

    private void registerGreaterAnimus(int priority, ItemStack greater_animus) {
        this.goalSelector.addGoal(priority, new CastSpellOnSelfGoal<CouncilWarden>(this, greater_animus, e -> !e.isOnCooldown(1003) && this.globalSpellCooldown <= 0 && (double)this.getHealth() < (double)this.getMaxHealth() * 0.65 && this.getWeaponState() == WeaponState.SWORD_IN_HAND, me -> {
            me.setSelfCastAnimation();
            SpellRecipe recipe = new SpellRecipe(Shapes.PROJECTILE, Components.IMPALE).addComponent(Components.SLOW).addComponent(Components.WEAKNESS);
            ((ModifiedSpellPart)recipe.getComponent(0)).setValue(Attribute.DAMAGE, 20.0f);
            ((ModifiedSpellPart)recipe.getComponent(1)).setValue(Attribute.MAGNITUDE, 2.0f);
            ((ModifiedSpellPart)recipe.getComponent(1)).setValue(Attribute.DURATION, 40.0f);
            ((ModifiedSpellPart)recipe.getComponent(2)).setValue(Attribute.MAGNITUDE, 2.0f);
            ((ModifiedSpellPart)recipe.getComponent(2)).setValue(Attribute.DURATION, 40.0f);
            me.setItemInHand(InteractionHand.OFF_HAND, recipe.createAsSpell());
        }, me -> {
            me.resetAfterSpellcast(30);
            me.setCooldown(1003, 300);
        }, 21));
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (this.isOnCooldown(1) || this.getAction() != Action.IDLE) {
            return true;
        }
        ArrayList<AttackAction> actionPool = new ArrayList<AttackAction>();
        actionPool.add(AttackAction.LEFT_CROSS);
        actionPool.add(AttackAction.RIGHT_CROSS);
        AttackAction selected = (AttackAction)(actionPool.get((int)((double)actionPool.size() * Math.random())));
        this.setAction(Action.ATTACKING);
        this.setAttackAction(selected);
        this.setTimer("", selected.animLength, () -> {
            for (LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, entityIn.getBoundingBox().inflate(1.0, 0.25, 1.0))) {
                if (livingentity == this) continue;
                this.damageEntity((Entity)livingentity);
            }
            this.playSound(SFX.Entity.Odin.ATTACK, 1.0f, (float)(0.6 + Math.random() * 0.8));
        });
        this.setTimer("resetattack", selected.resetTime, () -> {
            this.setAttackAction(AttackAction.NONE);
            this.setAction(Action.IDLE);
            this.setCooldown(1, 5);
        });
        return true;
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        this.setupSpawn();
    }

    public boolean damageEntity(Entity entityIn) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        ArrayList<Entity> targets = new ArrayList<Entity>();
        if (this.getAttackAction() == AttackAction.LEFT_CROSS || this.getAttackAction() == AttackAction.RIGHT_CROSS) {
            EntityUtil.getEntitiesWithinCone(this.level(), this.position(), Vec3.directionFromRotation((float)this.getXRot(), (float)this.getYHeadRot()), 6.0f, -60.0f, 60.0f, e -> e instanceof LivingEntity && e.isAlive() && e != this && e != entityIn);
        }
        targets.add(entityIn);
        MutableBoolean hurtTarget = new MutableBoolean(false);
        targets.forEach(t -> {
            boolean hurt = t.hurt(this.damageSources().mobAttack((LivingEntity)this), f);
            if (hurt) {
                hurtTarget.setTrue();
                if (f1 > 0.0f && t instanceof LivingEntity) {
                    ((LivingEntity)t).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                }
                if (t instanceof Player) {
                    Player playerentity;
                    this.maybeDisableShield(playerentity, (playerentity = (Player)t).isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY);
                    if (!this.lowerBuffs(playerentity)) {
                        this.suppressEnchantments(playerentity);
                    }
                }
                this.setLastHurtMob((Entity)t);
                if (this.random.nextFloat() < 0.4f && t instanceof Mob) {
                    ((Mob)t).setTarget((LivingEntity)this);
                }
            }
        });
        return hurtTarget.getValue();
    }

    private void maybeDisableShield(Player player, ItemStack playerItem) {
        if (!playerItem.isEmpty() && playerItem.getItem().canPerformAction(playerItem, ItemAbilities.SHIELD_BLOCK)) {
            player.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level().broadcastEntityEvent((Entity)player, (byte)30);
        }
    }

    private void suppressEnchantments(Player target) {
        if (!target.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DISJUNCTION.get()))) {
            target.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.DISJUNCTION.get()), 100, 5));
        }
    }

    private boolean lowerBuffs(Player target) {
        List<MobEffectInstance> beneficialEffects = target.getActiveEffects().stream().filter(e -> e.getEffect().value().isBeneficial()).toList();
        if (beneficialEffects.size() == 0) {
            return false;
        }
        int numToLower = MathUtils.weightedRandomNumber(beneficialEffects.size() + 1);
        for (int i = 0; i < numToLower; ++i) {
            MobEffectInstance effect = beneficialEffects.get(i);
            if (effect.getAmplifier() == 0) {
                target.removeEffect(effect.getEffect());
                --i;
                --numToLower;
                continue;
            }
            int duration = effect.getDuration();
            int amp = effect.getAmplifier() - 1;
            target.removeEffect(effect.getEffect());
            target.addEffect(new MobEffectInstance(effect.getEffect(), duration, amp));
        }
        return true;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 850.0).add(Attributes.MOVEMENT_SPEED, (double)0.7f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 20.0).add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.75);
    }

    @Override
    public void die(DamageSource pCause) {
        if (pCause.is(DamageTypes.FELL_OUT_OF_WORLD) && pCause.getEntity() == null) {
            super.die(pCause);
            return;
        }
        if (this.getAction() == Action.DEATH) {
            if (this.getHealth() < 1.0f) {
                this.setHealth(1.0f);
            }
            return;
        }
        this.setAction(Action.DEATH);
        this.removeAllEffects();
        super.die(pCause);
    }

    protected void tickDeath() {
        ++this.wardenDeathTime;
        if (!this.level().isClientSide()) {
            if (this.wardenDeathTime == 60) {
                this.level().playSound(null, this.blockPosition(), SFX.Entity.Warden.ESCAPE, this.getSoundSource());
            }
            if (this.wardenDeathTime >= 74) {
                EldrinFlight efBossLeave = new EldrinFlight(this.level(), null, this.position(), this.position().add((double)MathUtils.RandomBetween(-500.0f, 500.0f), 50.0, (double)MathUtils.RandomBetween(-500.0f, 500.0f)), 10);
                this.level().addFreshEntity((Entity)efBossLeave);
                this.level().playSound(null, this.blockPosition(), SFX.Spell.Cast.ARCANE, this.getSoundSource());
                this.remove(Entity.RemovalReason.KILLED);
            }
        } else if (this.level().isClientSide()) {
            Vec3 particlePos;
            int i;
            if (this.wardenDeathTime > 60) {
                for (i = 0; i < 200; ++i) {
                    particlePos = new Vec3(this.getX() + (double)MathUtils.RandomBetween(-1.0f, 1.0f), this.getY() + (double)MathUtils.RandomBetween(-1.0f, 1.0f), this.getZ() + (double)MathUtils.RandomBetween(-1.0f, 1.0f));
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get())).setScale(MathUtils.RandomBetween(0.25f, 1.0f)).setMaxAge((int)MathUtils.RandomBetween(5.0f, 20.0f)), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.25, 0.0);
                }
            }
            if (this.wardenDeathTime == 73) {
                Vec3 particleVel;
                for (i = 0; i < 150; ++i) {
                    particlePos = new Vec3(this.getX() + (double)MathUtils.RandomBetween(-1.0f, 1.0f), this.getY() + (double)MathUtils.RandomBetween(0.0f, 2.0f), this.getZ() + (double)MathUtils.RandomBetween(-1.0f, 1.0f));
                    particleVel = particlePos.subtract(this.position()).normalize().scale(MathUtils.RandomBetween(0.05, 0.2));
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setScale(0.05f).setGravity(MathUtils.RandomBetween(0.005f, 0.02f)), particlePos.x, particlePos.y, particlePos.z, particleVel.x, particleVel.y, particleVel.z);
                }
                for (i = 0; i < 25; ++i) {
                    particlePos = new Vec3(this.getX() + (double)MathUtils.RandomBetween(-1.0f, 1.0f), this.getY() + (double)MathUtils.RandomBetween(0.0f, 2.0f), this.getZ() + (double)MathUtils.RandomBetween(-1.0f, 1.0f));
                    particleVel = particlePos.subtract(this.position()).normalize().scale(MathUtils.RandomBetween(0.25, 0.5));
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENCHANT.get())).setMover(new ParticleOrbitMover(particlePos, 0.005, 0.0, MathUtils.RandomBetween(0.75, 1.25))).setScale(0.05f).setMaxAge((int)MathUtils.RandomBetween(120.0f, 240.0f)), particlePos.x, particlePos.y, particlePos.z, particleVel.x, particleVel.y, particleVel.z);
                }
                for (i = 0; i < 50; ++i) {
                    particlePos = new Vec3(this.getX() + (double)MathUtils.RandomBetween(-2.0f, 2.0f), this.getY() + (double)MathUtils.RandomBetween(2.0f, 4.0f), this.getZ() + (double)MathUtils.RandomBetween(-2.0f, 2.0f));
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setGravity(MathUtils.RandomBetween(0.005f, 0.02f)).setPhysics(true).setMaxAge((int)MathUtils.RandomBetween(120.0f, 240.0f)), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public HashMap<String, Integer> getDamageResists() {
        return new HashMap<String, Integer>();
    }

    @Override
    public IFaction getFaction() {
        return Factions.COUNCIL;
    }

    @Override
    public Player getRaidTarget() {
        return null;
    }

    @Override
    public int getTier() {
        return 3;
    }

    @Override
    public void setRaidTarget(Player player) {
    }

    @Override
    public void setTier(int tier) {
    }

    public static enum WeaponState {
        SWORD_ON_BACK,
        SWORD_IN_HAND,
        NO_SWORD;

    }

    public static enum Action {
        IDLE,
        ATTACKING,
        SWORD_LEAP_UP,
        SWORD_LEAP_LAND,
        SWORD_THROW_START,
        SWORD_THROW_LOOP,
        SWORD_THROW_END,
        LEAP_AWAY,
        DEATH;

    }

    public static enum AttackAction {
        NONE(0, 0),
        LEFT_CROSS(8, 16),
        RIGHT_CROSS(8, 16),
        LEFT_CAST(8, 16),
        RIGHT_CAST(8, 16),
        SELF_CAST(8, 12),
        SLAM_CAST(11, 15);

        public final int animLength;
        public final int resetTime;

        private AttackAction(int animLength, int resetTime) {
            this.animLength = animLength;
            this.resetTime = resetTime;
        }
    }
}


