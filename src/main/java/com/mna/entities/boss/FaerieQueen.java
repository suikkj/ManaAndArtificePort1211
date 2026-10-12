 
package com.mna.entities.boss;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ai.CastSpellOnSelfGoal;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.tools.RLoc;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.ai.ThreatTable;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.boss.attacks.IllusionCreeper;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.EntityUtil;
import com.mna.tools.ParticleConfigurations;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class FaerieQueen
extends BossMonster<FaerieQueen>
implements IFactionEnemy<FaerieQueen> {
    public final Predicate<Entity> AURA_SELECTOR = e -> !(!e.isAlive() || !(e instanceof LivingEntity) || e instanceof FaerieQueen || e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() == Factions.FEY || SummonUtils.isSummon(e) && SummonUtils.isTargetFriendly(e, (LivingEntity)this) || e instanceof Player && (((Player)e).isCreative() || ((Player)e).isSpectator()));
    private final ServerBossEvent bossEventSummer = (ServerBossEvent)new ServerBossEvent((Component)Component.translatable((String)"entity.mna.faerie_queen.summer"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
    private final ServerBossEvent bossEventWinter = (ServerBossEvent)new ServerBossEvent((Component)Component.translatable((String)"entity.mna.faerie_queen.winter"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
    protected static final EntityDataAccessor<Byte> SEASON = SynchedEntityData.defineId(FaerieQueen.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<String> SUMMONER_UUID = SynchedEntityData.defineId(FaerieQueen.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Boolean> IS_JOINING = SynchedEntityData.defineId(FaerieQueen.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private Player summonedBy;
    public int faerieDeathTime;
    private int globalSpellCooldown = 0;
    private static final int COOLDOWN_FLING_ID = 1025;
    private static final int COOLDOWN_HEAL_ID = 1026;
    private static final int COOLDOWN_DISJUNCTION_ID = 1027;
    private static final int COOLDOWN_MINDCONTROL_ID = 1028;
    private static final int COOLDOWN_ILLUSIONCREEPER_ID = 1029;
    private static final int COOLDOWN_WINTERICE_ID = 1030;
    private static final int COOLDOWN_CHILL_ID = 1031;
    private static final int COOLDOWN_ICESPIKE_ID = 1032;
    private static final int COOLDOWN_FRAILTY_ID = 1033;
    private static final int COOLDOWN_SUMMERFIRE_ID = 1034;
    private static final int COOLDOWN_HEATWAVE_ID = 1035;
    private static final int COOLDOWN_SUNDER_ID = 1036;
    private static final float LIFT_SPEED = 0.055f;

    public FaerieQueen(EntityType<? extends Monster> type, Level world) {
        super(type, world);
        this.setSeason(Math.random() < 0.5);
        this.setNoGravity(true);
        this.moveControl = new MoveHelperController(this);
        this.setCooldown(1029, (int)(100.0 + Math.random() * 300.0));
        SummonUtils.setBonusSummons((LivingEntity)this, 99);
        this.threat = new ThreatTable(e -> {
            if (e == this) {
                return false;
            }
            if (e instanceof Player && (((Player)e).isCreative() || ((Player)e).isSpectator())) {
                return false;
            }
            return !(e instanceof FaerieQueen);
        });
    }

    public FaerieQueen(Level world, Vec3 pos, boolean winter) {
        this((EntityType<? extends Monster>)((EntityType)EntityInit.FAERIE_QUEEN.get()), world);
        this.setSeason(winter);
        this.setPos(pos);
    }

    @Override
    public void tick() {
        if (this.globalSpellCooldown > 0) {
            --this.globalSpellCooldown;
        }
        if (this.getTarget() instanceof FaerieQueen) {
            this.threat.remove(this.getTarget());
            this.setTarget(null);
        }
        if (this.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()))) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.2, 0.0));
        }
        this.setNoGravity(true);
        super.tick();
        if (this.getTarget() instanceof FaerieQueen) {
            this.setTarget(null);
        }
        if (!this.isNaturalSpawn()) {
            if (this.getSummonedBy() == null) {
                this.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            boolean remove = false;
            this.lookAt(EntityAnchorArgument.Anchor.FEET, this.getSummonedBy().getEyePosition());
            if (this.getSummonedBy().isDeadOrDying() || this.getSummonedBy().distanceToSqr((Entity)this) > 256.0) {
                remove = true;
            } else {
                boolean bl = remove = this.tickCount > 700;
            }
            if (remove && !this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        } else {
            this.tickAuras();
        }
    }

    private void tickAuras() {
        if (this.level().isClientSide() || this.level().getGameTime() % 60L != 0L) {
            return;
        }
        this.level().getEntities((Entity)this, this.getBoundingBox().inflate(16.0), this.AURA_SELECTOR).stream().map(e -> (LivingEntity)e).forEach(e -> {
            if (this.isWinter()) {
                if (!e.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                } else {
                    e.setTicksFrozen(e.getTicksFrozen() + 20);
                }
                e.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.SNOWBLIND.get()), 100, 2));
            } else if (!e.isInWaterOrRain() && !e.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
                e.igniteForSeconds((float)(5));
            }
        });
    }

    private void lockTargetInPlace(LivingEntity target, boolean raise) {
        int duration = 200;
        target.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()));
        target.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()), duration, raise ? 1 : 0));
        if (raise && target instanceof Player) {
            EntityUtil.SetLiftSpeed((Player)target, 0.055f);
        }
    }

    protected void registerGoals() {
        double moveSpeed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(0, (Goal)new FaerieQueenFly());
        ItemStack flingSpell = new SpellRecipe(Shapes.PROJECTILE, Components.FLING).setShapeModifier(Attribute.SPEED, 3.0f).setComponentModifier(0, Attribute.SPEED, 3.0f).createAsSpell();
        ItemStack healSpell = new SpellRecipe(Shapes.SELF, Components.HEAL).setComponentModifier(0, Attribute.MAGNITUDE, 20.0f).createAsSpell();
        ItemStack disjunctionSpell = new SpellRecipe(Shapes.SELF, Components.DISJUNCTION).setComponentModifier(0, Attribute.MAGNITUDE, 20.0f).createAsSpell();
        ItemStack mindControlSpell = new SpellRecipe(Shapes.BOLT, Components.MIND_CONTROL).setShapeModifier(Attribute.RANGE, 32.0f).setComponentModifier(0, Attribute.DURATION, 15.0f).createAsSpell();
        ItemStack winterIceSpell = new SpellRecipe(Shapes.PROJECTILE, Components.FROST_DAMAGE).setShapeModifier(Attribute.RADIUS, 2.0f).setShapeModifier(Attribute.SPEED, 2.0f).setComponentModifier(0, Attribute.DAMAGE, 10.0f).createAsSpell();
        ItemStack chillSpell = new SpellRecipe(Shapes.BOLT, Components.CHILL).setShapeModifier(Attribute.RANGE, 32.0f).setComponentModifier(0, Attribute.MAGNITUDE, 2.0f).setComponentModifier(0, Attribute.DURATION, 20.0f).setComponentModifier(0, Attribute.DAMAGE, 20.0f).setComponentModifier(0, Attribute.RADIUS, 10.0f).createAsSpell();
        ItemStack iceSpikeSpell = new SpellRecipe(Shapes.PROJECTILE, Components.ICE_SPIKE).setShapeModifier(Attribute.RADIUS, 3.0f).setShapeModifier(Attribute.SPEED, 2.0f).setComponentModifier(0, Attribute.DAMAGE, 20.0f).setComponentModifier(0, Attribute.DURATION, 20.0f).createAsSpell();
        ItemStack frailtySpell = new SpellRecipe(Shapes.CHAIN, Components.FRAILTY).setShapeModifier(Attribute.RANGE, 32.0f).setShapeModifier(Attribute.MAGNITUDE, 100.0f).setShapeModifier(Attribute.RADIUS, 5.0f).setComponentModifier(0, Attribute.DURATION, 10.0f).setComponentModifier(0, Attribute.MAGNITUDE, 3.0f).createAsSpell();
        ItemStack summerFireSpell = new SpellRecipe(Shapes.PROJECTILE, Components.FIRE_DAMAGE).setShapeModifier(Attribute.RADIUS, 2.0f).setShapeModifier(Attribute.SPEED, 2.0f).setComponentModifier(0, Attribute.DAMAGE, 10.0f).setComponentModifier(0, Attribute.PRECISION, 1.0f).createAsSpell();
        ItemStack heatwaveSpell = new SpellRecipe(Shapes.BOLT, Components.HEATWAVE).setShapeModifier(Attribute.RANGE, 32.0f).setComponentModifier(0, Attribute.MAGNITUDE, 2.0f).setComponentModifier(0, Attribute.DURATION, 20.0f).setComponentModifier(0, Attribute.DAMAGE, 20.0f).setComponentModifier(0, Attribute.RADIUS, 10.0f).createAsSpell();
        ItemStack sunderSpell = new SpellRecipe(Shapes.CHAIN, Components.SUNDER).setShapeModifier(Attribute.RANGE, 32.0f).setShapeModifier(Attribute.MAGNITUDE, 100.0f).setShapeModifier(Attribute.RADIUS, 5.0f).setComponentModifier(0, Attribute.DURATION, 10.0f).setComponentModifier(0, Attribute.MAGNITUDE, 3.0f).createAsSpell();
        this.goalSelector.addGoal(1, new CastSpellOnSelfGoal<FaerieQueen>(this, healSpell, this::shouldHealSelf, this::snapSpellCastPrecast, me -> {
            this.spellCastReset();
            this.setCooldown(1026, 100);
        }, 21));
        this.goalSelector.addGoal(1, (Goal)new FaerieQueenSummonIllusions());
        this.goalSelector.addGoal(1, (Goal)new BossMonster.CastSpellAtTargetGoal(flingSpell, moveSpeed, 100, 10.0f, 0.0f, true, 1025, 8, 12, null, this::spellCastReset, this::simpleSpellCastPrecast).setUsePredicate(this::shouldFlingTarget));
        this.goalSelector.addGoal(2, (Goal)new BossMonster.CastSpellAtTargetGoal(disjunctionSpell, moveSpeed, 400, 28.0f, 0.0f, true, 1027, 20, 28, null, this::spellCastReset, this::snapSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
        this.goalSelector.addGoal(2, (Goal)new BossMonster.CastSpellAtTargetGoal(mindControlSpell, moveSpeed, 500, 28.0f, 0.0f, true, 1028, 20, 28, null, this::spellCastReset, this::snapSpellCastPrecast).setUsePredicate(this::shouldMindControlTarget));
        if (this.isWinter()) {
            this.goalSelector.addGoal(1, (Goal)new BossMonster.CastSpellAtTargetGoal(chillSpell, moveSpeed, 300, 28.0f, 0.0f, true, 1031, 5, 13, null, this::spellCastReset, this::spinSpellCastPrecast).setUsePredicate(this::shouldHighPriorityChillTarget));
            this.goalSelector.addGoal(1, (Goal)new BossMonster.CastSpellAtTargetGoal(frailtySpell, moveSpeed, 500, 28.0f, 0.0f, true, 1033, 20, 28, null, this::spellCastReset, this::snapSpellCastPrecast).setUsePredicate(this::shouldHighPriorityFrailtyTarget));
            this.goalSelector.addGoal(5, (Goal)new BossMonster.CastSpellAtTargetGoal(chillSpell, moveSpeed, 300, 28.0f, 0.0f, true, 1031, 5, 13, null, this::spellCastReset, this::spinSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
            this.goalSelector.addGoal(5, (Goal)new BossMonster.CastSpellAtTargetGoal(frailtySpell, moveSpeed, 500, 28.0f, 0.0f, true, 1033, 20, 28, null, this::spellCastReset, this::snapSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
            this.goalSelector.addGoal(5, (Goal)new BossMonster.CastSpellAtTargetGoal(iceSpikeSpell, moveSpeed, 300, 28.0f, 0.0f, true, 1032, 5, 13, null, this::spellCastReset, this::simpleSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
            this.goalSelector.addGoal(6, (Goal)new BossMonster.CastSpellAtTargetGoal(winterIceSpell, moveSpeed, 0, 28.0f, 0.0f, true, 1030, 5, 13, null, this::spellCastReset, this::simpleSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
        } else {
            this.goalSelector.addGoal(1, (Goal)new BossMonster.CastSpellAtTargetGoal(heatwaveSpell, moveSpeed, 300, 28.0f, 0.0f, true, 1035, 5, 13, null, this::spellCastReset, this::spinSpellCastPrecast).setUsePredicate(this::shouldHighPriorityHeatwaveTarget));
            this.goalSelector.addGoal(5, (Goal)new BossMonster.CastSpellAtTargetGoal(heatwaveSpell, moveSpeed, 300, 28.0f, 0.0f, true, 1035, 5, 13, null, this::spellCastReset, this::spinSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
            this.goalSelector.addGoal(5, (Goal)new BossMonster.CastSpellAtTargetGoal(sunderSpell, moveSpeed, 500, 28.0f, 0.0f, true, 1036, 20, 28, null, this::spellCastReset, this::snapSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
            this.goalSelector.addGoal(6, (Goal)new BossMonster.CastSpellAtTargetGoal(summerFireSpell, moveSpeed, 0, 28.0f, 0.0f, true, 1034, 5, 13, null, this::spellCastReset, this::simpleSpellCastPrecast).setUsePredicate(this::spellCastPredicate));
        }
        if (this.targetSelector.availableGoals.size() == 0) {
            this.targetSelector.addGoal(1, (Goal)new BossMonster.ThreatTableHurtByTargetGoal((PathfinderMob)this, FaerieQueen.class));
            this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, le -> true));
            this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SEASON, (byte)0);
        builder.define(SUMMONER_UUID, "");
        builder.define(IS_JOINING, false);
    }

    @Override
    public void die(DamageSource pCause) {
        if (pCause.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            super.die(pCause);
            return;
        }
        this.setAction(Action.DEATH);
        this.removeAllEffects();
        super.die(pCause);
    }

    protected void tickDeath() {
        ++this.faerieDeathTime;
        if (!this.level().isClientSide()) {
            if (this.faerieDeathTime == 30) {
                this.playSound(SFX.Event.Ritual.FAERIE_BLOW_KISS, 1.0f, 1.0f);
            } else if (this.faerieDeathTime == 61) {
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
        if (this.faerieDeathTime >= 60 && this.level().isClientSide()) {
            ParticleConfigurations.ArcaneParticleBurst(this.level(), this.getEyePosition());
        }
    }

    protected boolean shouldDropLoot() {
        return this.level().getEntitiesOfClass(FaerieQueen.class, this.getBoundingBox().inflate(32.0)).size() <= 1;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 675.0).add(Attributes.MOVEMENT_SPEED, (double)0.7f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 14.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (this.isNaturalSpawn()) {
            return InteractionResult.FAIL;
        }
        if (player != this.getSummonedBy()) {
            return InteractionResult.FAIL;
        }
        if (this.getAction() == Action.FJ_WAITING_ITEM && player.getItemInHand(hand).getItem() == ItemInit.MOTE_AIR.get()) {
            EntityUtil.SetLiftSpeed(player, 0.055f);
            if (!this.level().isClientSide()) {
                player.getUseItem().shrink(1);
                this.setAction(Action.FJ_IMBUING);
                this.level().playSound(null, this.blockPosition(), SFX.Event.Ritual.FAERIE_IMBUE, SoundSource.PLAYERS, 1.0f, 1.0f);
                this.lockTargetInPlace((LivingEntity)player, false);
                this.setTimer("raise", 10, () -> this.lockTargetInPlace((LivingEntity)player, true));
                this.setTimer("kiss", 52, () -> {
                    this.level().playSound(null, this.blockPosition(), SFX.Event.Ritual.FAERIE_BLOW_KISS, SoundSource.PLAYERS, 1.0f, 1.0f);
                    this.lockTargetInPlace((LivingEntity)player, false);
                });
                this.setTimer("leave_sound", 100, () -> this.level().playSound(null, this.blockPosition(), SFX.Event.Ritual.FAERIE_LEAVE, SoundSource.PLAYERS, 1.0f, 1.0f));
                this.setTimer("advance", 120, () -> {
                    IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
                    if (progression != null && progression.getTier() < 5) {
                        if (progression.getAlliedFaction() == null) {
                            progression.setAlliedFaction(Factions.FEY, player);
                            player.sendSystemMessage((Component)Component.translatable((String)"event.mna.faction_ally_fey"));
                        }
                        if (progression.getAlliedFaction() == Factions.FEY) {
                            progression.setTier(progression.getTier() + 1, player);
                            player.sendSystemMessage((Component)Component.translatable((String)"mna:progresscondition.advanced", (Object[])new Object[]{progression.getTier()}));
                        }
                    }
                    player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()));
                });
                this.setTimer("despawn", 165, () -> this.discard());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private UUID getSummonerGuid() {
        try {
            return UUID.fromString((String)this.entityData.get(SUMMONER_UUID));
        }
        catch (Throwable t) {
            return null;
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

    private Player getSummonedBy() {
        if (this.summonedBy == null && this.getSummonerGuid() != null) {
            this.summonedBy = this.level().getPlayerByUUID(this.getSummonerGuid());
        }
        return this.summonedBy;
    }

    public boolean isExiting() {
        return this.isNaturalSpawn() && this.getAction() == Action.DEATH;
    }

    public boolean isWinter() {
        return (Byte)this.entityData.get(SEASON) == 0;
    }

    public boolean isNaturalSpawn() {
        return (Boolean)this.entityData.get(IS_JOINING) == false;
    }

    public FaerieQueen setSeason(boolean winter) {
        if (this.isWinter() == winter) {
            return this;
        }
        this.entityData.set(SEASON, (winter ? (byte)0 : 1));
        this.goalSelector.removeAllGoals(g -> true);
        this.registerGoals();
        return this;
    }

    public void setSummoner(Player player) {
        if (player == null || player.getGameProfile() == null) {
            return;
        }
        this.entityData.set(SUMMONER_UUID, player.getGameProfile().getId().toString());
        this.entityData.set(IS_JOINING, true);
        this.hideBossBar();
        this.summonedBy = player;
        this.setInvulnerable(true);
        this.goalSelector.availableGoals.clear();
        this.targetSelector.availableGoals.clear();
        this.setAction(Action.FJ_WAITING_ITEM);
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
        return this.isWinter() ? this.bossEventWinter : this.bossEventSummer;
    }

    @Override
    public void setupSpawn() {
    }

    @Override
    protected void spawnParticles() {
        if (this.getAction() == Action.DEATH) {
            return;
        }
        int numAuraParticles = 3;
        int numWingParticles = 2;
        Vec3 forward = this.getForward().normalize();
        Vec3 side = forward.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        forward = forward.scale(-0.5);
        if (this.isWinter()) {
            int i;
            for (i = 0; i < numAuraParticles; ++i) {
                Vec3 pos = new Vec3(this.getX() - 0.2 + Math.random() * 0.4, this.getY() + Math.random() * 0.5, this.getZ() - 0.2 + Math.random() * 0.4);
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setMover(new ParticleOrbitMover(pos.x, pos.y, pos.z, (double)0.01f + Math.random() * (double)0.01f, (double)0.01f + Math.random() * (double)0.01f, 1.0 + Math.random() * 3.0)), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
            }
            for (i = 0; i < numWingParticles; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())).setColor(148, 242, 244), this.getX() - side.x + forward.x + forward.x * Math.random() + Math.random() * side.x * 2.0, this.getY() + Math.random() * (double)this.getEyeHeight(), this.getZ() - side.z + forward.z + forward.z * Math.random() + Math.random() * side.z * 2.0, 0.0, 0.0, 0.0);
            }
        } else {
            int i;
            for (i = 0; i < numAuraParticles; ++i) {
                Vec3 pos = new Vec3(this.getX() - 0.2 + Math.random() * 0.4, this.getY() + Math.random() * 0.5, this.getZ() - 0.2 + Math.random() * 0.4);
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setMover(new ParticleOrbitMover(pos.x, pos.y, pos.z, (double)0.01f + Math.random() * (double)0.01f, (double)0.01f + Math.random() * (double)0.01f, 1.0 + Math.random() * 3.0)), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
            }
            for (i = 0; i < numWingParticles; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())).setColor(229, 220, 84), this.getX() - side.x + forward.x + forward.x * Math.random() + Math.random() * side.x * 2.0, this.getY() + Math.random() * (double)this.getEyeHeight(), this.getZ() - side.z + forward.z + forward.z * Math.random() + Math.random() * side.z * 2.0, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public ResourceLocation getArenaStructureID() {
        return RLoc.create("faction/boss/fey_tree");
    }

    @Override
    public int getArenaStructureSegment() {
        return 2;
    }

    @Override
    public HashMap<String, Integer> getDamageResists() {
        return new HashMap<String, Integer>();
    }

    @Override
    public IFaction getFaction() {
        return Factions.FEY;
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

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BossMonster<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        block0 : switch (this.getAction()) {
            case DEATH: {
                builder.thenPlay("animation.fey_queen.blow_kiss");
                break;
            }
            case FJ_WAITING_ITEM: {
                builder.thenLoop("animation.fey_queen.wait_gift");
                break;
            }
            case FJ_IMBUING: {
                builder.thenPlay("animation.fey_queen.infuse");
                break;
            }
            case ATTACKING: {
                switch (this.getAttackAction()) {
                    case CAST_2H_UP: {
                        builder.thenPlay("animation.fey_queen.cast_5");
                        break block0;
                    }
                    case CAST_SIMPLE_LEFT: {
                        builder.thenPlay("animation.fey_queen.cast_2");
                        break block0;
                    }
                    case CAST_SIMPLE_RIGHT: {
                        builder.thenPlay("animation.fey_queen.cast_1");
                        break block0;
                    }
                    case CAST_SNAP_LEFT: {
                        builder.thenPlay("animation.fey_queen.cast_4");
                        break block0;
                    }
                    case CAST_SNAP_RIGHT: {
                        builder.thenPlay("animation.fey_queen.cast_3");
                        break block0;
                    }
                    case CAST_SPIN_LEFT: {
                        builder.thenPlay("animation.fey_queen.cast_7");
                        break block0;
                    }
                    case CAST_SPIN_RIGHT: {
                        builder.thenPlay("animation.fey_queen.cast_6");
                        break block0;
                    }
                }
                break;
            }
            default: {
                double movementSpeed = this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length();
                if (movementSpeed > 0.1) {
                    builder.thenLoop("animation.fey_queen.moving");
                    break;
                }
                builder.thenLoop("animation.fey_queen.idle");
            }
        }
        return state.setAndContinue(builder);
    }

    private void spellCastReset() {
        this.setAttackAction(AttackAction.NONE);
        this.setAction(Action.IDLE);
        this.globalSpellCooldown = 10 + (int)(Math.random() * 20.0);
    }

    private boolean simpleSpellCastPrecast(BossMonster<?> me) {
        this.setAction(Action.ATTACKING);
        this.setAttackAction(Math.random() < 0.5 ? AttackAction.CAST_SIMPLE_LEFT : AttackAction.CAST_SIMPLE_RIGHT);
        return true;
    }

    private boolean spinSpellCastPrecast(BossMonster<?> me) {
        this.setAction(Action.ATTACKING);
        this.setAttackAction(Math.random() < 0.5 ? AttackAction.CAST_SPIN_LEFT : AttackAction.CAST_SPIN_RIGHT);
        return true;
    }

    private boolean snapSpellCastPrecast(BossMonster<?> me) {
        this.setAction(Action.ATTACKING);
        this.setAttackAction(Math.random() < 0.5 ? AttackAction.CAST_SNAP_LEFT : AttackAction.CAST_SNAP_RIGHT);
        return true;
    }

    private boolean shouldFlingTarget(Entity target) {
        return this.spellCastPredicate(target) && target.distanceTo((Entity)this) < 4.0f;
    }

    private boolean shouldHighPriorityFrailtyTarget(Entity target) {
        return this.spellCastPredicate(target) && target instanceof LivingEntity && ((LivingEntity)target).hasEffect(MobEffects.DAMAGE_RESISTANCE);
    }

    private boolean shouldHighPriorityHeatwaveTarget(Entity target) {
        return this.spellCastPredicate(target) && target instanceof LivingEntity && ((LivingEntity)target).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHILL.get()));
    }

    private boolean shouldHighPriorityChillTarget(Entity target) {
        return this.spellCastPredicate(target) && target instanceof LivingEntity && ((LivingEntity)target).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.HEATWAVE.get()));
    }

    private boolean shouldMindControlTarget(Entity target) {
        return this.spellCastPredicate(target) && SummonUtils.isSummon(target) && !SummonUtils.isTargetFriendly(target, (LivingEntity)this);
    }

    private boolean spellCastPredicate(Entity target) {
        return this.getAction() == Action.IDLE && this.globalSpellCooldown == 0 && this.getTarget() != null && this.getTarget().isAlive();
    }

    private boolean shouldHealSelf(FaerieQueen me) {
        return this.getAction() == Action.IDLE && this.globalSpellCooldown == 0 && this.getHealth() <= this.getMaxHealth() / 2.0f && !this.isOnCooldown(1026);
    }

    static class MoveHelperController
    extends MoveControl {
        private final FaerieQueen parentEntity;
        private int courseChangeCooldown;

        public MoveHelperController(FaerieQueen queen) {
            super((Mob)queen);
            this.parentEntity = queen;
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                float minMoveSpeed = 0.1f;
                float maxMoveSpeed = 0.6f;
                Vec3 v = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
                double distSqr = v.distanceToSqr(this.parentEntity.position());
                if (distSqr < 16.0) {
                    double pct = distSqr / 16.0;
                    this.speedModifier = MathUtils.lerpf(minMoveSpeed, maxMoveSpeed, (float)pct);
                    this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().scale(pct));
                    if (this.parentEntity.getTarget() != null) {
                        this.parentEntity.lookAt(EntityAnchorArgument.Anchor.FEET, this.parentEntity.getTarget().getEyePosition());
                    }
                } else {
                    this.speedModifier = maxMoveSpeed;
                }
                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
                    Vec3 vector3d = new Vec3(this.wantedX - this.parentEntity.getX(), this.wantedY - this.parentEntity.getY(), this.wantedZ - this.parentEntity.getZ());
                    double d0 = vector3d.length();
                    if (this.isCollided(vector3d = vector3d.normalize(), Mth.ceil((double)d0))) {
                        this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add(vector3d.scale(0.1)));
                    } else {
                        this.operation = MoveControl.Operation.WAIT;
                    }
                }
            }
        }

        private boolean isCollided(Vec3 p_220673_1_, int p_220673_2_) {
            AABB axisalignedbb = this.parentEntity.getBoundingBox();
            for (int i = 1; i < p_220673_2_; ++i) {
                axisalignedbb = axisalignedbb.move(p_220673_1_);
                if (this.parentEntity.level().noCollision((Entity)this.parentEntity, axisalignedbb)) continue;
                return false;
            }
            return true;
        }
    }

    class FaerieQueenFly
    extends Goal {
        public FaerieQueenFly() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            MoveControl movementcontroller = FaerieQueen.this.getMoveControl();
            if (!movementcontroller.hasWanted()) {
                return true;
            }
            if (FaerieQueen.this.getTarget() == null) {
                double d2;
                double d1;
                double d0 = movementcontroller.getWantedX() - FaerieQueen.this.getX();
                double d3 = d0 * d0 + (d1 = movementcontroller.getWantedY() - FaerieQueen.this.getY()) * d1 + (d2 = movementcontroller.getWantedZ() - FaerieQueen.this.getZ()) * d2;
                return d3 < 4.0 || d3 > 3600.0;
            }
            double dist = FaerieQueen.this.getTarget().distanceToSqr((Entity)FaerieQueen.this);
            return !FaerieQueen.this.getSensing().hasLineOfSight((Entity)FaerieQueen.this.getTarget()) || dist > 400.0 || FaerieQueen.this.level().getGameTime() % 100L == 0L;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            if (FaerieQueen.this.getTarget() != null) {
                Vec3 direction = FaerieQueen.this.position().subtract(FaerieQueen.this.getTarget().position()).normalize();
                direction = direction.add(0.0, -direction.y - 1.0 + Math.random() * 2.0, 0.0);
                Vec3 offset = direction.scale(8.0 + Math.random() * 4.0);
                offset = offset.add(-2.0 + Math.random() * 4.0, 0.0, -2.0 + Math.random() * 4.0);
                Vec3 target = FaerieQueen.this.getTarget().position().add(offset);
                if (FaerieQueen.this.getTarget().onGround()) {
                    target = target.add(0.0, 1.5, 0.0);
                }
                BlockPos pos = BlockPos.containing((double)target.x, (double)target.y, (double)target.z);
                int count = 0;
                while (FaerieQueen.this.level().isEmptyBlock(pos) && count < 16) {
                    ++count;
                    pos = pos.below();
                }
                if (count >= 16) {
                    target = target.add(0.0, -10.0, 0.0);
                }
                FaerieQueen.this.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.0);
            }
        }
    }

    class FaerieQueenSummonIllusions
    extends Goal {
        private static final int DURATION = 42;
        private static final int SUMMON_AT = 30;
        private int tickCount = 0;

        FaerieQueenSummonIllusions() {
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public boolean canUse() {
            boolean hasTarget = FaerieQueen.this.getTarget() != null;
            boolean noSummons = FaerieQueen.this.level().getEntitiesOfClass(IllusionCreeper.class, FaerieQueen.this.getBoundingBox().inflate(32.0)).size() == 0;
            boolean offCooldown = !FaerieQueen.this.isOnCooldown(1029);
            boolean isIdle = FaerieQueen.this.getAction() == Action.IDLE || FaerieQueen.this.getAction() == Action.ATTACKING && FaerieQueen.this.getAttackAction() == AttackAction.CAST_2H_UP;
            return hasTarget && noSummons && offCooldown && isIdle;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canContinueToUse() {
            return FaerieQueen.this.getAction() == Action.ATTACKING && FaerieQueen.this.getAttackAction() == AttackAction.CAST_2H_UP && this.tickCount < 42;
        }

        public void start() {
            FaerieQueen.this.setAction(Action.ATTACKING);
            FaerieQueen.this.setAttackAction(AttackAction.CAST_2H_UP);
            FaerieQueen.this.playSound(SFX.Entity.WitherLich.SUMMON, 1.0f, 1.0f);
        }

        public void stop() {
            FaerieQueen.this.setCooldown(1029, 600);
            FaerieQueen.this.setAttackAction(AttackAction.NONE);
            FaerieQueen.this.setAction(Action.IDLE);
            this.tickCount = 0;
        }

        public void tick() {
            ++this.tickCount;
            if (this.tickCount == 30) {
                for (int i = 0; i < 10; ++i) {
                    this.summonCreeper(true);
                }
                this.summonCreeper(false);
            }
            if (this.tickCount >= 42) {
                this.stop();
            }
        }

        private void summonCreeper(boolean illusion) {
            Vec3 pos = FaerieQueen.this.position();
            pos = pos.add(-5.0 + Math.random() * 10.0, 0.0, -5.0 + Math.random() * 10.0);
            IllusionCreeper c = illusion ? new IllusionCreeper(FaerieQueen.this.level()) : new Creeper(EntityType.CREEPER, FaerieQueen.this.level());
            c.setPos(pos);
            c.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60));
            SummonUtils.setSummon((Mob)c, (LivingEntity)FaerieQueen.this, 400);
            FaerieQueen.this.level().addFreshEntity((Entity)c);
        }
    }

    public static enum Action {
        IDLE,
        FJ_WAITING_ITEM,
        FJ_IMBUING,
        ATTACKING,
        ENRAGING,
        DEATH;

    }

    public static enum AttackAction {
        NONE(0, 0),
        CAST_SIMPLE_RIGHT(13, 17),
        CAST_SIMPLE_LEFT(13, 17),
        CAST_SNAP_RIGHT(28, 32),
        CAST_SNAP_LEFT(28, 32),
        CAST_2H_UP(28, 32),
        CAST_SPIN_RIGHT(13, 17),
        CAST_SPIN_LEFT(13, 17);

        public final int animLength;
        public final int resetTime;

        private AttackAction(int animLength, int resetTime) {
            this.animLength = animLength;
            this.resetTime = resetTime;
        }
    }
}
