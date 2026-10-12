 
package com.mna.entities.boss;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ai.CastSpellAtTargetGoal;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.boss.attacks.ThrownWeapon;
import com.mna.entities.boss.effects.DemonReturnPortal;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.spells.components.ComponentFling;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.EntityUtil;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import org.apache.commons.lang3.mutable.MutableBoolean;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;

public class DemonLord
extends BossMonster<DemonLord>
implements IFactionEnemy<DemonLord> {
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
    protected static final EntityDataAccessor<String> SUMMONER_UUID = SynchedEntityData.defineId(DemonLord.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Boolean> IS_JOINING = SynchedEntityData.defineId(DemonLord.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Byte> PHASE = SynchedEntityData.defineId(DemonLord.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Integer> INCINERATE_ID = SynchedEntityData.defineId(DemonLord.class, (EntityDataSerializer)EntityDataSerializers.INT);
    protected static final EntityDataAccessor<BlockPos> PULL_POS = SynchedEntityData.defineId(DemonLord.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    private static final float LIFT_SPEED = 0.055f;
    private static final int COOLDOWN_LEAP_ID = 1024;
    private static final int COOLDOWN_SPARKS_ID = 1025;
    private static final int COOLDOWN_THUNDER_ID = 1026;
    private static final int COOLDOWN_GRASP_OF_FIRE_ID = 1027;
    private static final int COOLDOWN_HELLBALL_ID = 1028;
    private Player summonedBy;
    private LivingEntity _incinerateTarget;
    @Nullable
    private BlockPos vortexTarget = null;
    private int demonDeathTime;
    private float forceYRot = 0.0f;
    private int globalSpellCooldown = 0;
    private int queuedPhaseTransition = 0;

    public DemonLord(EntityType<? extends DemonLord> type, Level worldIn) {
        super(type, worldIn);
        this.setWeaponState(WeaponState.AXE);
    }

    public DemonLord(Level worldIn) {
        this((EntityType<? extends DemonLord>)((EntityType)EntityInit.DEMON_LORD.get()), worldIn);
    }

    public DemonLord(Level level, Vec3 position) {
        this(level);
        this.setPos(position);
    }

    private void lockTargetInPlace(LivingEntity target, boolean raise) {
        int duration = 200;
        target.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()));
        target.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()), duration, raise ? 1 : 0));
        if (raise && target instanceof Player) {
            EntityUtil.SetLiftSpeed((Player)target, 0.055f);
        }
    }

    private void setupPhase2Transition() {
        this.setInvulnerable(true);
        this.setTarget(null);
        this.setAction(Action.PHASE_2_TRANSITION);
        this.setTimer("remove_weapon", 10, () -> {
            this.setWeaponState(WeaponState.NOTHING);
            if (!this.level().isClientSide()) {
                ThrownWeapon thrown = new ThrownWeapon((EntityType<? extends AbstractHurtingProjectile>)((EntityType)EntityInit.THROWN_WEAPON.get()), (LivingEntity)this, this.level(), new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE.get()));
                thrown.setScale(2.0f);
                Vec3 direction = Vec3.directionFromRotation((Vec2)new Vec2(0.0f, this.getYRot()));
                thrown.shoot(direction.x, direction.y, direction.z, 2.5f, 0.0f);
                this.level().addFreshEntity((Entity)thrown);
            }
        });
        this.setTimer("set_swords", 38, () -> {
            this.setWeaponState(WeaponState.SWORDS);
            this.playSound(SFX.Entity.DemonLord.DRAW_SWORDS, 1.0f, 1.0f);
        });
        this.setTimer("fire_shield", 80, () -> {
            this.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.FIRE_SHIELD.get()), 99999, 2));
            this.playSound(SFX.Spell.Buff.FIRE, 1.0f, (float)(0.6 + Math.random() * 0.8));
            this.playSound(SFX.Entity.DemonLord.SCREAM, 1.0f, (float)(0.6 + Math.random() * 0.8));
        });
    }

    private void setupPhase3Transition() {
        this.setInvulnerable(true);
        this.setAction(Action.PHASE_3_TRANSITION);
        this.setTarget(null);
        this.setTimer("remove_weapon", 10, () -> {
            this.setWeaponState(WeaponState.NOTHING);
            if (!this.level().isClientSide()) {
                float degreeDifference = -25.0f;
                for (int i = 0; i < 2; ++i) {
                    ThrownWeapon thrown = new ThrownWeapon((EntityType<? extends AbstractHurtingProjectile>)((EntityType)EntityInit.THROWN_WEAPON.get()), (LivingEntity)this, this.level(), new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE.get()));
                    thrown.setScale(2.0f);
                    Vec3 direction = Vec3.directionFromRotation((Vec2)new Vec2(0.0f, this.getYRot() + degreeDifference));
                    thrown.shoot(direction.x, direction.y, direction.z, 2.5f, 0.0f);
                    this.level().addFreshEntity((Entity)thrown);
                    degreeDifference += 50.0f;
                }
            }
        });
        this.setTimer("set_staff", 64, () -> {
            this.setWeaponState(WeaponState.STAFF);
            this.playSound(SFX.Entity.DemonLord.SUMMON_STAFF, 1.0f, 1.0f);
        });
    }

    @Nullable
    private LivingEntity getIncinerateTarget() {
        int id = (Integer)this.entityData.get(INCINERATE_ID);
        if (this._incinerateTarget == null) {
            Entity e;
            if (id > -1 && (e = this.level().getEntity(id)) != null && e instanceof LivingEntity) {
                this._incinerateTarget = (LivingEntity)e;
            }
        } else if (id == -1) {
            this._incinerateTarget = null;
        }
        return this._incinerateTarget;
    }

    @Nullable
    private Vec3 getPullPos() {
        BlockPos pos = (BlockPos)this.entityData.get(PULL_POS);
        if (pos == null || BlockPos.ZERO.equals(pos)) {
            return null;
        }
        return Vec3.atCenterOf((Vec3i)pos);
    }

    private UUID getSummonerGuid() {
        try {
            return UUID.fromString((String)this.entityData.get(SUMMONER_UUID));
        }
        catch (Throwable t) {
            return null;
        }
    }

    private Player getSummonedBy() {
        if (this.summonedBy == null && this.getSummonerGuid() != null) {
            this.summonedBy = this.level().getPlayerByUUID(this.getSummonerGuid());
        }
        return this.summonedBy;
    }

    private boolean naturalSpawn() {
        return (Boolean)this.entityData.get(IS_JOINING) == false;
    }

    public WeaponState getWeaponState() {
        int ordinal = this.getStateFlag() & 3;
        if (ordinal < WeaponState.values().length) {
            return WeaponState.values()[ordinal];
        }
        return WeaponState.NOTHING;
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

    private boolean isPhaseTransitioning() {
        return this.getAction() == Action.PHASE_2_TRANSITION || this.getAction() == Action.PHASE_3_TRANSITION;
    }

    public void setSummoner(Player player) {
        if (player == null || player.getGameProfile() == null) {
            return;
        }
        this.entityData.set(IS_JOINING, true);
        this.entityData.set(SUMMONER_UUID, player.getGameProfile().getId().toString());
        this.summonedBy = player;
        this.setInvulnerable(true);
        com.mna.tools.MNAGoalSelectorAccess.availableGoals(this.goalSelector).clear();
        com.mna.tools.MNAGoalSelectorAccess.availableGoals(this.targetSelector).clear();
        this.setAction(Action.FJ_WAITING_ITEM);
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

    private void setIncinerateTarget(@Nullable LivingEntity target) {
        this._incinerateTarget = target;
        if (!this.level().isClientSide()) {
            this.entityData.set(INCINERATE_ID, (target != null ? target.getId() : -1));
        }
    }

    private void setPullPos(@Nullable BlockPos pos) {
        this.entityData.set(PULL_POS, (pos == null ? BlockPos.ZERO : pos));
    }

    @Override
    protected void spawnParticles() {
        Vec3 graspPos;
        for (int i = 0; i < 10; ++i) {
            LivingEntity incinerateTarget = this.getIncinerateTarget();
            if (incinerateTarget == null) continue;
            float bbOffset = incinerateTarget.getBbWidth();
            float bbHeightOffset = incinerateTarget.getBbHeight() / 2.0f;
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), incinerateTarget.getX() - (double)(bbOffset / 2.0f) + Math.random() * (double)bbOffset, incinerateTarget.getY() - (double)bbHeightOffset, incinerateTarget.getZ() - (double)(bbOffset / 2.0f) + Math.random() * (double)bbOffset, 0.0, 0.165 + Math.random() * 0.165, 0.0);
        }
        if (this.getAction() == Action.GRASP_OF_FIRE && (graspPos = this.getPullPos()) != null) {
            for (int i = 0; i < 10; ++i) {
                Vec3 pos = graspPos.add(-2.0 + Math.random() * 4.0, -2.0 + Math.random() * 4.0, -2.0 + Math.random() * 4.0);
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_LERP.get())).setScale(0.2f).setColor(10, 10, 10).setMaxAge(5 + (int)(Math.random() * 5.0)), pos.x, pos.y, pos.z, graspPos.x, graspPos.y, graspPos.z);
            }
        }
    }

    @Override
    public void setupSpawn() {
        this.setInvulnerableTicks(80);
        this.setHealth(this.getMaxHealth() / 3.0f);
    }

    @Override
    protected int defaultState() {
        int flags = WeaponState.AXE.ordinal() & 3;
        return flags;
    }

    @Override
    public ResourceLocation getArenaStructureID() {
        return RLoc.create("faction/boss/demon_arena");
    }

    @Override
    public int getArenaStructureSegment() {
        return 0;
    }

    @Override
    public void tick() {
        if (this.getHealth() < 1.0f && (this.queuedPhaseTransition > 0 || this.getAction() == Action.PHASE_2_TRANSITION || this.getAction() == Action.PHASE_3_TRANSITION)) {
            this.setInvulnerable(true);
            this.setHealth(1.0f);
        }
        if (this.getAction() == Action.IDLE) {
            if (this.queuedPhaseTransition == 2) {
                this.setupPhase2Transition();
                this.queuedPhaseTransition = -1;
            } else if (this.queuedPhaseTransition == 3) {
                this.setupPhase3Transition();
                this.queuedPhaseTransition = -1;
            }
        }
        super.tick();
        if (this.globalSpellCooldown > 0) {
            --this.globalSpellCooldown;
        }
        if (this.getAction() == Action.DEATH) {
            this.setYRot(this.forceYRot);
        }
        boolean remove = false;
        if (this._incinerateTarget != null && !this.level().isClientSide() && this.level().getGameTime() % 10L == 0L) {
            float incinerateDamage = this.naturalSpawn() ? 6.0f : (float)(6 + 2 * (Byte)this.entityData.get(PHASE));
            this._incinerateTarget.hurt(this.damageSources().inFire(), incinerateDamage);
        }
        if (!this.naturalSpawn()) {
            if (this.getSummonedBy() == null) {
                this.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            this.lookAt(EntityAnchorArgument.Anchor.FEET, this.getSummonedBy().getEyePosition());
            if (this.getSummonedBy().isDeadOrDying() || this.getSummonedBy().distanceToSqr((Entity)this) > 256.0) {
                remove = true;
            } else {
                boolean bl = remove = this.tickCount > 700;
            }
            if (remove && !this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        } else if (this.getAction() == Action.PHASE_2_TRANSITION || this.getAction() == Action.PHASE_3_TRANSITION) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            if (this.getTarget() != null) {
                this.lookAt(EntityAnchorArgument.Anchor.EYES, this.getTarget().position());
            }
            this.heal(2.5f);
            if (this.getHealth() == this.getMaxHealth()) {
                this.entityData.set(PHASE, ((byte)((Byte)this.entityData.get(PHASE) + 1)));
                this.setInvulnerable(false);
                this.setAction(Action.IDLE);
            }
        } else if (this.getAction() == Action.IDLE && this.isInvulnerable()) {
            this.setInvulnerable(false);
        }
        if (this.getHealth() < 0.0f) {
            this.setAction(Action.DEATH);
        }
        if (this.getAction() == Action.ATTACKING && this.getTarget() != null) {
            this.lookAt(EntityAnchorArgument.Anchor.FEET, this.getTarget().position());
        }
    }

    protected void registerGoals() {
        ItemStack sparks = new ItemStack((ItemLike)ItemInit.SPELL.get());
        ItemStack thunder = new ItemStack((ItemLike)ItemInit.SPELL.get());
        ItemStack hellball = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe sparksSpell = new SpellRecipe();
        sparksSpell.setShape(Shapes.PROJECTILE);
        ((ModifiedSpellPart)sparksSpell.getShape()).setValue(Attribute.MAGNITUDE, 5.0f);
        sparksSpell.addComponent(Components.LIGHTNING_DAMAGE);
        sparksSpell.addComponent(Components.LIVING_BOMB);
        ((ModifiedSpellPart)sparksSpell.getComponent(0)).setValue(Attribute.DAMAGE, 10.0f);
        ((ModifiedSpellPart)sparksSpell.getComponent(1)).setValue(Attribute.DURATION, 5.0f);
        ((ModifiedSpellPart)sparksSpell.getComponent(1)).setValue(Attribute.MAGNITUDE, 3.0f);
        sparksSpell.writeToNBT(sparks.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        SpellRecipe thunderSpell = new SpellRecipe();
        thunderSpell.setShape(Shapes.SMITE);
        ((ModifiedSpellPart)thunderSpell.getShape()).setValue(Attribute.HEIGHT, 3.0f);
        ((ModifiedSpellPart)thunderSpell.getShape()).setValue(Attribute.RADIUS, 5.0f);
        thunderSpell.addComponent(Components.LIGHTNING_DAMAGE);
        ((ModifiedSpellPart)thunderSpell.getComponent(0)).setValue(Attribute.DAMAGE, 20.0f);
        ((ModifiedSpellPart)thunderSpell.getComponent(0)).setValue(Attribute.LESSER_MAGNITUDE, 10.0f);
        ((ModifiedSpellPart)thunderSpell.getComponent(0)).setValue(Attribute.RADIUS, 10.0f);
        thunderSpell.addComponent(Components.GRAVITY_WELL);
        ((ModifiedSpellPart)thunderSpell.getComponent(1)).setValue(Attribute.DURATION, 5.0f);
        thunderSpell.writeToNBT(com.mna.api.tools.MNAItemData.tag(thunder));
        SpellRecipe hellballSpell = new SpellRecipe();
        hellballSpell.setShape(Shapes.PROJECTILE);
        ((ModifiedSpellPart)hellballSpell.getShape()).setValue(Attribute.RADIUS, 10.0f);
        hellballSpell.addComponent(Components.EXPLOSION);
        ((ModifiedSpellPart)hellballSpell.getComponent(0)).setValue(Attribute.DAMAGE, 20.0f);
        ((ModifiedSpellPart)hellballSpell.getComponent(0)).setValue(Attribute.RADIUS, 10.0f);
        ((ModifiedSpellPart)hellballSpell.getComponent(0)).setValue(Attribute.PRECISION, 2.0f);
        hellballSpell.addComponent(Components.FLING);
        ((ModifiedSpellPart)hellballSpell.getComponent(1)).setValue(Attribute.SPEED, 3.0f);
        hellballSpell.setOverrideAffinity(Affinity.HELLFIRE);
        hellballSpell.writeToNBT(com.mna.api.tools.MNAItemData.tag(hellball));
        double moveSpeed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(0, (Goal)new PhaseTransitionGoal(this));
        this.goalSelector.addGoal(1, (Goal)new HellballGoal(hellball));
        this.goalSelector.addGoal(2, (Goal)new GraspOfFireGoal());
        this.goalSelector.addGoal(3, (Goal)new CastSpellAtTargetGoal(sparks, moveSpeed, 300, 32.0f, 8.0f, true, 1025, 10, 20, null, () -> {
            this.setAction(Action.IDLE);
            this.setAttackAction(AttackAction.NONE);
            this.globalSpellCooldown = (Byte)this.entityData.get(PHASE) == 3 ? 10 + (int)(Math.random() * 30.0) : 40 + (int)(Math.random() * 60.0);
        }, me -> {
            switch ((Byte)this.entityData.get(PHASE)) {
                case 3: {
                    if (Math.random() < 0.5) {
                        this.setAttackAction(AttackAction.STAFF_CAST_1);
                        break;
                    }
                    this.setAttackAction(AttackAction.STAFF_CAST_2);
                    break;
                }
                default: {
                    this.setAttackAction(AttackAction.CAST_1);
                }
            }
            this.setAction(Action.ATTACKING);
            return true;
        }).setUsePredicate(e -> this.getAction() == Action.IDLE && this.globalSpellCooldown == 0));
        this.goalSelector.addGoal(3, (Goal)new CastSpellAtTargetGoal(thunder, moveSpeed, 300, 16.0f, 4.0f, true, 1026, 10, 20, null, () -> {
            this.setAction(Action.IDLE);
            this.setAttackAction(AttackAction.NONE);
            this.globalSpellCooldown = (Byte)this.entityData.get(PHASE) == 3 ? 10 + (int)(Math.random() * 30.0) : 40 + (int)(Math.random() * 60.0);
        }, me -> {
            switch ((Byte)this.entityData.get(PHASE)) {
                case 3: {
                    this.setAttackAction(AttackAction.STAFF_CAST_2);
                    break;
                }
                default: {
                    this.setAttackAction(AttackAction.CAST_2);
                }
            }
            this.setAction(Action.ATTACKING);
            return true;
        }).setUsePredicate(e -> this.getAction() == Action.IDLE && this.globalSpellCooldown == 0));
        this.goalSelector.addGoal(4, (Goal)new LerpLeap(4.0f, 32.0f, 5));
        this.goalSelector.addGoal(5, (Goal)new MeleeAttackGoal((PathfinderMob)this, moveSpeed, true));
        this.goalSelector.addGoal(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.targetSelector.addGoal(1, (Goal)new BossMonster.ThreatTableHurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, le -> true));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SUMMONER_UUID, "");
        builder.define(PHASE, (byte)1);
        builder.define(INCINERATE_ID, -1);
        builder.define(PULL_POS, BlockPos.ZERO);
        builder.define(IS_JOINING, false);
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (!this.naturalSpawn() && this.level().isClientSide()) {
            for (int i = 0; i < 75; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), this.getX() - 0.5 + Math.random(), this.getY() + Math.random() * 1.5, this.getZ() - 0.5 + Math.random(), -0.01 + Math.random() * 0.02, (double)0.15f, -0.01 + Math.random() * 0.02);
            }
            Player player = this.getSummonedBy();
            if (player != null && player.distanceTo((Entity)this) < 32.0f) {
                for (int i = 0; i < 75; ++i) {
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), player.getX(), player.getY() + 1.0, player.getZ(), -0.25 + Math.random() * 0.5, (double)0.15f, -0.25 + Math.random() * 0.5);
                }
            }
        }
    }

    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (this.naturalSpawn()) {
            return InteractionResult.FAIL;
        }
        if (player != this.getSummonedBy()) {
            return InteractionResult.FAIL;
        }
        if (this.getAction() == Action.FJ_WAITING_ITEM && player.getItemInHand(hand).getItem() == ItemInit.MOTE_FIRE.get()) {
            EntityUtil.SetLiftSpeed(player, 0.055f);
            if (!this.level().isClientSide()) {
                player.getUseItem().shrink(1);
                this.setAction(Action.FJ_IMBUING);
                this.level().playSound(null, this.blockPosition(), SFX.Event.Ritual.DEMON_SUMMON_CHANNEL, SoundSource.PLAYERS, 1.0f, 1.0f);
                this.lockTargetInPlace((LivingEntity)player, false);
                this.setTimer("raise", 10, () -> this.lockTargetInPlace((LivingEntity)player, true));
                this.setTimer("advance", 202, () -> {
                    IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
                    if (progression != null && progression.getTier() < 5) {
                        if (progression.getAlliedFaction() == null) {
                            progression.setAlliedFaction(Factions.DEMONS, player);
                            player.sendSystemMessage((Component)Component.translatable((String)"event.mna.faction_ally_demons"));
                        }
                        if (progression.getAlliedFaction() == Factions.DEMONS) {
                            progression.setTier(progression.getTier() + 1, player);
                            player.sendSystemMessage((Component)Component.translatable((String)"mna:progresscondition.advanced", (Object[])new Object[]{progression.getTier()}));
                        }
                    }
                    player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()));
                    this.remove(Entity.RemovalReason.DISCARDED);
                });
            }
            this.setTimer("incinerate", 62, () -> {
                this.lockTargetInPlace((LivingEntity)player, false);
                this.setIncinerateTarget((LivingEntity)player);
            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (this.isOnCooldown(1) || this.getAction() != Action.IDLE) {
            return true;
        }
        ArrayList<AttackAction> actionPool = new ArrayList<AttackAction>();
        byte phase = (Byte)this.entityData.get(PHASE);
        if (phase == 1 || phase == 3) {
            actionPool.add(AttackAction.LEFT_OVERHEAD);
            actionPool.add(AttackAction.LEFT_SWEEP);
            actionPool.add(AttackAction.TWOHAND_OVERHEAD);
        } else if (phase == 2) {
            actionPool.add(AttackAction.LEFT_OVERHEAD);
            actionPool.add(AttackAction.LEFT_SWEEP);
            actionPool.add(AttackAction.RIGHT_OVERHEAD);
            actionPool.add(AttackAction.RIGHT_SWEEP);
            actionPool.add(AttackAction.DUALWIELD_OVERHEAD);
            actionPool.add(AttackAction.DUALWIELD_SWEEP);
        }
        AttackAction selected = (AttackAction)(actionPool.get((int)((double)actionPool.size() * Math.random())));
        this.setAction(Action.ATTACKING);
        this.setAttackAction(selected);
        this.setTimer("", selected.animLength, () -> {
            this.damageEntity(entityIn);
            this.playSound(SFX.Entity.Odin.ATTACK, 1.0f, (float)(0.6 + Math.random() * 0.8));
            this.playSound(SFX.Entity.DemonLord.ATTACK, 1.0f, (float)(0.6 + Math.random() * 0.8));
        });
        this.setTimer("resetattack", selected.resetTime, () -> {
            this.setAttackAction(AttackAction.NONE);
            this.setAction(Action.IDLE);
            this.setCooldown(1, phase == 2 ? 10 : 20);
        });
        return true;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 375.0).add(Attributes.MOVEMENT_SPEED, (double)0.7f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 14.0).add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public boolean damageEntity(Entity entityIn) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        ArrayList<Entity> targets = new ArrayList<Entity>();
        if (this.getAttackAction() == AttackAction.LEFT_SWEEP || this.getAttackAction() == AttackAction.RIGHT_SWEEP || this.getAttackAction() == AttackAction.DUALWIELD_SWEEP) {
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
        if (!playerItem.isEmpty() && playerItem.getItem().canPerformAction(playerItem, ItemAbilities.SHIELD_BLOCK) && (Byte)this.entityData.get(PHASE) == 1) {
            player.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level().broadcastEntityEvent((Entity)player, (byte)30);
        }
    }

    @Override
    public void die(DamageSource pCause) {
        if (pCause.is(DamageTypes.FELL_OUT_OF_WORLD) && pCause.getEntity() == null) {
            super.die(pCause);
            return;
        }
        if (this.queuedPhaseTransition > 0) {
            if (this.getHealth() < 1.0f) {
                this.setHealth(1.0f);
            }
            return;
        }
        byte phase = (Byte)this.entityData.get(PHASE);
        if (phase == 1) {
            this.setInvulnerable(true);
            this.setHealth(1.0f);
            this.queuedPhaseTransition = 2;
        } else if (phase == 2) {
            this.setInvulnerable(true);
            this.setHealth(1.0f);
            this.queuedPhaseTransition = 3;
        } else {
            this.setAction(Action.DEATH);
            if (!this.level().isClientSide()) {
                this.forceYRot = this.getYRot();
                Vec3 direction = Vec3.directionFromRotation((float)0.0f, (float)this.getYHeadRot()).normalize().scale(-11.5);
                Vec3 portalPos = this.position().add(direction.x, 0.25, direction.z);
                this.setTimer("death_portal", 30, () -> {
                    this.playSound(SFX.Entity.DemonLord.DEATH, 1.0f, 1.0f);
                    DemonReturnPortal portal = new DemonReturnPortal((EntityType)EntityInit.DEMON_RETURN_PORTAL.get(), this.level());
                    portal.setPos(portalPos);
                    portal.setFocus(this);
                    this.level().addFreshEntity((Entity)portal);
                });
                BlockPos myPos = this.blockPosition();
                for (int i = -4; i <= 4; ++i) {
                    for (int j = -2; j <= 2; ++j) {
                        for (int k = -4; k <= 4; ++k) {
                            BlockPos offsetPos = myPos.offset(i, j, k);
                            BlockState bs2 = this.level().getBlockState(offsetPos);
                            Block block = bs2.getBlock();
                            if (block != BlockInit.HELLFIRE.get() && block != Blocks.FIRE) continue;
                            this.level().setBlock(offsetPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
                this.level().getBlockStates(this.getBoundingBox().inflate(5.0)).forEach(bs -> {});
            }
            this.removeAllEffects();
            super.die(pCause);
        }
    }

    protected void tickDeath() {
        ++this.demonDeathTime;
        if (this.demonDeathTime == 155 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    protected SoundEvent getAmbientSound() {
        return SFX.Entity.DemonLord.IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SFX.Entity.DemonLord.HIT;
    }

    @Override
    public boolean hurt(DamageSource type, float amount) {
        if (!(type.is(DamageTypes.FELL_OUT_OF_WORLD) || this.getAction() != Action.PHASE_2_TRANSITION && this.getAction() != Action.PHASE_3_TRANSITION && this.queuedPhaseTransition <= 0)) {
            if (this.getHealth() < 1.0f) {
                this.setHealth(1.0f);
            }
            return false;
        }
        if (type.is(DamageTypeTags.IS_FIRE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
            if (this.level().getGameTime() % 5L == 0L) {
                this.heal(amount);
            }
            return false;
        }
        return super.hurt(type, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("phase", ((Byte)this.entityData.get(PHASE)).byteValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setAction(Action.IDLE);
        this.entityData.set(PHASE, ((byte)MathUtils.clamp(nbt.getByte("phase"), 1, 3)));
    }

    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    protected ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BossMonster<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        if (!this.naturalSpawn()) {
            if (this.getAction() == Action.FJ_WAITING_ITEM) {
                builder.thenLoop("animation.DemonLord.item_wait");
            } else if (this.getAction() == Action.FJ_IMBUING) {
                builder.thenPlay("animation.DemonLord.grasp_of_fire_no_throw");
                builder.thenLoop("animation.DemonLord.grasp_of_fire_no_throw_end_loop");
            }
        } else if (this.getAction() == Action.DEATH) {
            builder.thenPlay("animation.DemonLord.death");
        } else if (this.getAction() == Action.PHASE_2_TRANSITION) {
            builder.thenPlay("animation.DemonLord.phase_transition_2");
            builder.thenLoop("animation.DemonLord.regenerating");
        } else if (this.getAction() == Action.PHASE_3_TRANSITION) {
            builder.thenPlay("animation.DemonLord.phase_transition_3");
            builder.thenLoop("animation.DemonLord.regenerating");
        } else if (this.getAction() == Action.LEAPING) {
            builder.thenPlay("animation.DemonLord.axe_leap");
            builder.thenLoop("animation.DemonLord.axe_leap_loop");
        } else if (this.getAction() == Action.LEAP_LANDING) {
            builder.thenPlay("animation.DemonLord.axe_leap_land");
        } else if (this.getAction() == Action.SUMMON) {
            builder.thenPlay("animation.DemonLord.summon");
        } else if (this.getAction() == Action.GRASP_OF_FIRE) {
            builder.thenPlay("animation.DemonLord.grasp_of_fire");
        } else if (this.getAction() == Action.ATTACKING) {
            switch (this.getAttackAction()) {
                case CAST_1: {
                    builder.thenPlay("animation.DemonLord.cast");
                    break;
                }
                case CAST_2: {
                    builder.thenPlay("animation.DemonLord.cast_2");
                    break;
                }
                case DUALWIELD_OVERHEAD: {
                    builder.thenPlay("animation.DemonLord.dual_wield_overhead");
                    break;
                }
                case DUALWIELD_SWEEP: {
                    builder.thenPlay("animation.DemonLord.dual_weapon_sweep");
                    break;
                }
                case LEFT_OVERHEAD: {
                    builder.thenPlay("animation.DemonLord.left_weapon_overhead");
                    break;
                }
                case LEFT_SWEEP: {
                    builder.thenPlay("animation.DemonLord.left_weapon_sweep");
                    break;
                }
                case RIGHT_OVERHEAD: {
                    builder.thenPlay("animation.DemonLord.right_weapon_overhead");
                    break;
                }
                case RIGHT_SWEEP: {
                    builder.thenPlay("animation.DemonLord.right_weapon_sweep");
                    break;
                }
                case STAFF_CAST_1: {
                    builder.thenPlay("animation.DemonLord.staff_cast_1");
                    break;
                }
                case STAFF_CAST_2: {
                    builder.thenPlay("animation.DemonLord.staff_cast_2");
                    break;
                }
                case STAFF_CAST_3: {
                    builder.thenPlay("animation.DemonLord.staff_cast_3");
                    break;
                }
                case TWOHAND_OVERHEAD: {
                    builder.thenPlay("animation.DemonLord.two_handed_overhead");
                    break;
                }
                default: {
                    builder.thenLoop("animation.DemonLord.idle");
                    break;
                }
            }
        } else {
            double movementSpeed = this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length();
            if (movementSpeed > 0.1) {
                builder.thenLoop("animation.DemonLord.run");
            } else if (movementSpeed > 0.02) {
                builder.thenLoop("animation.DemonLord.walk");
            } else {
                builder.thenLoop("animation.DemonLord.idle");
            }
        }
        return state.setAndContinue(builder);
    }

    @Override
    public void handle(CustomInstructionKeyframeEvent<BossMonster<?>> event) {
        String instruction = event.getKeyframeData().getInstructions();
        if (instruction == "axe_throw" && (this.getAction() == Action.PHASE_2_TRANSITION || this.getAction() == Action.PHASE_3_TRANSITION)) {
            this.setWeaponState(WeaponState.NOTHING);
        } else if (instruction == "sword_draw") {
            this.setWeaponState(WeaponState.SWORDS);
        } else if (instruction == "staff_draw") {
            this.setWeaponState(WeaponState.STAFF);
        }
    }

    @Override
    public HashMap<String, Integer> getDamageResists() {
        return new HashMap<String, Integer>();
    }

    @Override
    public IFaction getFaction() {
        return Factions.DEMONS;
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
        AXE,
        NOTHING,
        SWORDS,
        STAFF;

    }

    public static enum Action {
        IDLE,
        FJ_WAITING_ITEM,
        FJ_IMBUING,
        ATTACKING,
        LEAPING,
        LEAP_LANDING,
        GRASP_OF_FIRE,
        PHASE_2_TRANSITION,
        PHASE_3_TRANSITION,
        SUMMON,
        DEATH;

    }

    public static enum AttackAction {
        NONE(0, 0),
        LEFT_OVERHEAD(10, 14),
        RIGHT_OVERHEAD(10, 14),
        LEFT_SWEEP(12, 16),
        RIGHT_SWEEP(12, 16),
        TWOHAND_OVERHEAD(10, 14),
        DUALWIELD_OVERHEAD(10, 14),
        DUALWIELD_SWEEP(10, 14),
        CAST_1(25, 30),
        CAST_2(16, 20),
        STAFF_CAST_1(22, 26),
        STAFF_CAST_2(16, 20),
        STAFF_CAST_3(72, 80);

        public final int animLength;
        public final int resetTime;

        private AttackAction(int animLength, int resetTime) {
            this.animLength = animLength;
            this.resetTime = resetTime;
        }
    }

    private class PhaseTransitionGoal
    extends Goal {
        private DemonLord entity;

        public PhaseTransitionGoal(DemonLord entity) {
            this.entity = entity;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canUse() {
            return this.entity.getAction() == Action.PHASE_2_TRANSITION || this.entity.getAction() == Action.PHASE_3_TRANSITION;
        }

        public boolean canContinueToUse() {
            return this.entity.getHealth() < this.entity.getMaxHealth();
        }
    }

    public class HellballGoal
    extends com.mna.api.entities.ai.CastSpellAtTargetGoal<DemonLord> {
        private boolean casting;
        private int warmupCounter;
        private int resetCounter;
        private ItemStack hellball;

        public HellballGoal(ItemStack hellball) {
            super(DemonLord.this, hellball, 0.6, 300, 32.0f, false);
            this.casting = false;
            this.warmupCounter = 0;
            this.resetCounter = 0;
            this.hellball = hellball;
            this.setPrecastCallback(e -> {
                this.casting = true;
                e.setAction(Action.ATTACKING);
                e.setAttackAction(AttackAction.STAFF_CAST_3);
                return false;
            });
        }

        @Override
        public boolean canUse() {
            if ((Byte)((DemonLord)this.entity).entityData.get(PHASE) != 3 || ((DemonLord)this.entity).isOnCooldown(1028) || ((DemonLord)this.entity).globalSpellCooldown > 0) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public void start() {
            super.start();
            this.target = ((DemonLord)this.entity).getRandomThreatTableTarget(32.0);
            if (this.target == null) {
                this.target = ((DemonLord)this.entity).getTarget();
            }
            this.casting = false;
            this.warmupCounter = 0;
            this.resetCounter = 0;
        }

        private void fireHellball() {
            SpellProjectile projectile = new SpellProjectile((LivingEntity)this.entity, DemonLord.this.level());
            projectile.setSpellRecipe(this.hellball.getTag());
            projectile.setHellball((Entity)this.target, 0.1f);
            projectile.shoot((Entity)this.entity, ((DemonLord)this.entity).getForward(), 0.1f, 0.0f);
            DemonLord.this.level().addFreshEntity((Entity)projectile);
        }

        @Override
        public void tick() {
            if (!this.casting) {
                super.tick();
            } else {
                ++this.warmupCounter;
                if (this.warmupCounter == 55) {
                    this.fireHellball();
                } else if (this.warmupCounter > 55 && this.resetCounter++ >= 20) {
                    ((DemonLord)this.entity).setAction(Action.IDLE);
                    ((DemonLord)this.entity).setAttackAction(AttackAction.NONE);
                    ((DemonLord)this.entity).setCooldown(1028, 600);
                    this.hasCast = true;
                }
            }
        }
    }

    public class GraspOfFireGoal
    extends Goal {
        private static final float pullStrength = 0.2f;
        private Optional<LivingEntity> grabbed = Optional.empty();
        private int tickCount = 0;

        public GraspOfFireGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public boolean canUse() {
            boolean isCorrectPhase = (Byte)DemonLord.this.entityData.get(PHASE) == 1 || (Byte)DemonLord.this.entityData.get(PHASE) == 3;
            boolean isOffCD = !DemonLord.this.isOnCooldown(1027);
            boolean isIdle = DemonLord.this.getAction() == Action.IDLE;
            boolean hasTarget = DemonLord.this.getTarget() != null;
            boolean onGround = DemonLord.this.onGround();
            return isOffCD && isIdle && hasTarget && onGround && isCorrectPhase;
        }

        public boolean canContinueToUse() {
            boolean actionIsGraspOfFire = DemonLord.this.getAction() == Action.GRASP_OF_FIRE;
            boolean hasTarget = DemonLord.this.getTarget() != null;
            return actionIsGraspOfFire && hasTarget;
        }

        public boolean isInterruptable() {
            return !DemonLord.this.isPhaseTransitioning();
        }

        public void start() {
            LivingEntity target = DemonLord.this.getTarget();
            BlockPos targetPos = BlockPos.containing((Position)target.getEyePosition());
            DemonLord.this.setPullPos(targetPos);
            DemonLord.this.setAction(Action.GRASP_OF_FIRE);
            DemonLord.this.getNavigation().stop();
            this.tickCount = 0;
            this.grabbed = Optional.empty();
        }

        public void stop() {
            if (!DemonLord.this.isPhaseTransitioning()) {
                DemonLord.this.setAction(Action.IDLE);
            }
            DemonLord.this.setCooldown(1027, 300);
        }

        public void tick() {
            if (DemonLord.this.getTarget() == null) {
                return;
            }
            ++this.tickCount;
            DemonLord.this.lookAt(EntityAnchorArgument.Anchor.FEET, DemonLord.this.getTarget().position());
            if (this.tickCount < 46) {
                this.pullEntities();
            } else if (this.tickCount == 46 && this.grabbed.isEmpty()) {
                this.grabbed = this.tryGrab();
                DemonLord.this.setPullPos(null);
                if (this.grabbed.isEmpty()) {
                    this.stop();
                    return;
                }
                LivingEntity living = this.grabbed.get();
                DemonLord.this.lockTargetInPlace(living, true);
                DemonLord.this.setTimer("burn", 10, () -> DemonLord.this.setIncinerateTarget(living));
            } else if (this.tickCount == 108) {
                LivingEntity living = this.grabbed.get();
                living.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()));
                Vec3 direction = Vec3.directionFromRotation((Vec2)new Vec2(0.0f, DemonLord.this.getYRot())).cross(new Vec3(0.0, 1.0, 0.0)).normalize();
                ComponentFling.flingTarget(living, direction, 3.0f, 3.0f, 0.0f);
                DemonLord.this.setIncinerateTarget(null);
            } else if (this.tickCount == 118) {
                this.stop();
            }
        }

        private void pullEntities() {
            Vec3 graspPoint = DemonLord.this.getPullPos();
            if (graspPoint == null) {
                return;
            }
            DemonLord.this.level().getEntities((Entity)DemonLord.this, new AABB(BlockPos.containing((Position)graspPoint)).inflate(256.0), e -> {
                if (!(e instanceof LivingEntity) || !e.isAlive() || e.distanceToSqr(graspPoint) > 256.0) {
                    return false;
                }
                return !(e instanceof Player) || !((Player)e).isCreative() && !((Player)e).isSpectator();
            }).stream().map(e -> (LivingEntity)e).forEach(e -> {
                Vec3 delta = graspPoint.subtract(e.position()).normalize();
                ComponentFling.flingTarget(e, delta, 0.04f, 0.2f, 0.5f);
            });
        }

        private Optional<LivingEntity> tryGrab() {
            Vec3 graspPoint = DemonLord.this.getPullPos();
            if (graspPoint == null) {
                return Optional.empty();
            }
            return DemonLord.this.level().getEntities((Entity)DemonLord.this, new AABB(BlockPos.containing((Position)graspPoint)).inflate(16.0), e -> {
                if (!(e instanceof LivingEntity) || !e.isAlive() || e.distanceToSqr(graspPoint) > 6.25) {
                    return false;
                }
                if (!DemonLord.this.getSensing().hasLineOfSight(e)) {
                    return false;
                }
                return DemonLord.this.getTarget() == e || DemonLord.this.threat.isOn((LivingEntity)e);
            }).stream().map(e -> (LivingEntity)e).sorted((a, b) -> (int)(a.distanceToSqr(graspPoint) - b.distanceToSqr(graspPoint))).findFirst();
        }
    }

    public class LerpLeap
    extends Goal {
        private float minDist;
        private float maxDist;
        private int lerpTicks;
        private int initialDelay;
        private int phaseTicks = 0;
        private int phase = 0;
        private Vec3 start;
        private Vec3 end;
        private Vec3 control_1;
        private Vec3 control_2;

        public LerpLeap(float minDist, float maxDist, int initialDelay) {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
            this.minDist = minDist;
            this.maxDist = maxDist;
            this.initialDelay = initialDelay;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public boolean canUse() {
            if (DemonLord.this.getTarget() == null || !DemonLord.this.isAlive() || !DemonLord.this.getTarget().isAlive() || DemonLord.this.isOnCooldown(1024) || (Byte)DemonLord.this.entityData.get(PHASE) == 3 || DemonLord.this.getAction() != Action.IDLE) {
                return false;
            }
            double dist = DemonLord.this.distanceToSqr((Entity)DemonLord.this.getTarget());
            return dist >= (double)(this.minDist * this.minDist) && dist <= (double)(this.maxDist * this.maxDist);
        }

        public void stop() {
            this.setPhase(0);
            DemonLord.this.setNoGravity(false);
            DemonLord.this.setCooldown(1024, 300);
            if (!DemonLord.this.isPhaseTransitioning()) {
                DemonLord.this.setAction(Action.IDLE);
            }
        }

        public boolean canContinueToUse() {
            return DemonLord.this.getTarget() != null && DemonLord.this.getAction() != Action.IDLE && this.phase < 4;
        }

        public void start() {
            DemonLord.this.setNoGravity(true);
            DemonLord.this.getNavigation().stop();
            DemonLord.this.setDeltaMovement(Vec3.ZERO);
            DemonLord.this.setAction(Action.LEAPING);
            this.setPhase(0);
        }

        private void setPhase(int phase) {
            this.phase = phase;
            this.phaseTicks = 0;
        }

        public void tick() {
            if (DemonLord.this.getTarget() == null) {
                return;
            }
            ++this.phaseTicks;
            switch (this.phase) {
                case 0: {
                    if (this.phaseTicks < this.initialDelay) break;
                    Vec3 direction = DemonLord.this.getTarget().position().subtract(DemonLord.this.position()).normalize();
                    this.start = DemonLord.this.position();
                    this.end = DemonLord.this.getTarget().position().subtract(direction);
                    float speed = 2.0f;
                    double distance = this.start.distanceTo(this.end);
                    this.lerpTicks = (int)(distance / (double)speed);
                    Vec3 difference = this.end.subtract(this.start);
                    this.control_1 = this.start.add(difference.scale(0.3)).add(0.0, 3.0, 0.0);
                    this.control_2 = this.start.add(difference.scale(0.6)).add(0.0, 3.0, 0.0);
                    DemonLord.this.playSound(SFX.Entity.DemonLord.LEAP, 1.0f, (float)(0.9 + Math.random() * 0.2));
                    this.setPhase(1);
                    break;
                }
                case 1: {
                    if (this.phaseTicks <= this.lerpTicks) {
                        if (this.lerpTicks < 5 || this.phaseTicks == this.lerpTicks - 5) {
                            DemonLord.this.setAction(Action.LEAP_LANDING);
                        }
                        float lerpPct = (float)this.phaseTicks / (float)this.lerpTicks;
                        Vec3 position = MathUtils.bezierVector3d(this.start, this.end, this.control_1, this.control_2, lerpPct);
                        DemonLord.this.lookAt(EntityAnchorArgument.Anchor.FEET, this.end);
                        DemonLord.this.setPos(position.x, position.y, position.z);
                        break;
                    }
                    this.setPhase(2);
                    break;
                }
                case 2: {
                    double dist = DemonLord.this.distanceToSqr((Entity)DemonLord.this.getTarget());
                    if (dist < 16.0) {
                        DemonLord.this.damageEntity((Entity)DemonLord.this.getTarget());
                    }
                    this.setPhase(3);
                    break;
                }
                case 3: {
                    if (this.phaseTicks < 10) break;
                    this.setPhase(4);
                }
            }
        }
    }
}

