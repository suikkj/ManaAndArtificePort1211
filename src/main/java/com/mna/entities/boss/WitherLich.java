 
package com.mna.entities.boss;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.google.common.collect.Maps;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.DamageHelper;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.tools.RLoc;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.summon.InsectSwarm;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.EntityUtil;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class WitherLich
extends BossMonster<WitherLich>
implements IFactionEnemy<WitherLich> {
    private final SpellRecipe meleeSpell = new SpellRecipe();
    private final ItemStack melee;
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
    private final ServerBossEvent powerWordDeathBar = (ServerBossEvent)new ServerBossEvent((Component)Component.translatable((String)"entity.mna.wither_lich.power_word_death"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
    protected static final EntityDataAccessor<Byte> SHIELD_COUNT = SynchedEntityData.defineId(WitherLich.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> TEXTURE_VARIANT = SynchedEntityData.defineId(WitherLich.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<UUID>> FACTION_JOIN_PLAYER = SynchedEntityData.defineId(WitherLich.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Boolean> IS_JOINING = SynchedEntityData.defineId(WitherLich.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> REACHING = SynchedEntityData.defineId(WitherLich.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private Vector3 start;
    private Vector3 end;
    private int lerpTicks;
    private int lerpCount = 0;
    private int finishTicks = 55;
    private Player cachedPlayer;
    private int powerWordDeathTicks = 0;
    private int numSummons = 0;
    private int lichDeathTime;
    ArrayList<EntityType<? extends Mob>> summonQueue = new ArrayList();
    private static final int COOLDOWN_SUMMON_ID = 1024;
    private static final int COOLDOWN_FROSTBOLT_ID = 1025;
    private static final int COOLDOWN_WITHERBOLT_ID = 1026;
    private static final int COOLDOWN_FOSSILIZE_BOLT_ID = 1027;
    private static final int COOLDOWN_INSECT_SWARM_BREATH_ID = 1028;
    private static final int COOLDOWN_FROST_BREATH_ID = 1029;
    private boolean shouldShieldedStrike = false;
    private int globalSpellCooldown = 0;

    public WitherLich(EntityType<? extends WitherLich> type, Level worldIn) {
        super(type, worldIn);
        this.melee = new ItemStack((ItemLike)ItemInit.SPELL.get());
        this.meleeSpell.setOverrideAffinity(Affinity.ICE);
        this.meleeSpell.setShape(Shapes.TOUCH);
        ((ModifiedSpellPart)this.meleeSpell.getShape()).setValue(Attribute.RANGE, 8.0f);
        this.meleeSpell.addComponent(Components.LACERATE);
        this.meleeSpell.addComponent(Components.IMPALE);
        this.meleeSpell.addComponent(Components.LIFE_TAP);
        ((ModifiedSpellPart)this.meleeSpell.getComponent(0)).setValue(Attribute.DURATION, 5.0f);
        ((ModifiedSpellPart)this.meleeSpell.getComponent(0)).setValue(Attribute.MAGNITUDE, 3.0f);
        ((ModifiedSpellPart)this.meleeSpell.getComponent(1)).setValue(Attribute.DAMAGE, 15.0f);
        ((ModifiedSpellPart)this.meleeSpell.getComponent(2)).setValue(Attribute.DURATION, 15.0f);
        ((ModifiedSpellPart)this.meleeSpell.getComponent(2)).setValue(Attribute.LESSER_MAGNITUDE, 4.0f);
        this.meleeSpell.writeToNBT(this.melee.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        SummonUtils.setBonusSummons((LivingEntity)this, 99);
        this.setNoGravity(true);
        this.moveControl = new MoveHelperController(this);
        this.getPersistentData().putFloat("life_tap_pct", 0.25f);
        this.entityData.set(SHIELD_COUNT, 5);
        this.setCooldown(1024, 0);
        this.powerWordDeathTicks = 1;
    }

    public WitherLich(Level worldIn) {
        this((EntityType<? extends WitherLich>)((EntityType)EntityInit.LICH.get()), worldIn);
    }

    public WitherLich(Level level, Vec3 position) {
        this(level);
        this.setPos(position);
    }

    private Player getFactionJoinPlayer() {
        if (this.cachedPlayer == null && ((Optional)this.entityData.get(FACTION_JOIN_PLAYER)).isPresent()) {
            this.cachedPlayer = this.level().getPlayerByUUID((UUID)((Optional)this.entityData.get(FACTION_JOIN_PLAYER)).get());
        }
        return this.cachedPlayer;
    }

    private int getDesiredSummonPower() {
        int baseline = 2;
        byte shields = (Byte)this.entityData.get(SHIELD_COUNT);
        baseline *= Math.max(5 - shields, 1);
        return baseline *= Math.min(Math.max(this.threat.size(), 1), 10);
    }

    private void buildSummonList() {
        SummonCandidate next;
        this.summonQueue.clear();
        int desiredSummonPower = this.getDesiredSummonPower();
        ArrayList<SummonCandidate> summonPool = new ArrayList<SummonCandidate>();
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)EntityType.ZOMBIE, 1));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)EntityType.SKELETON, 1));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)EntityType.ZOMBIE_VILLAGER, 2));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)EntityType.DROWNED, 2));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)EntityType.STRAY, 2));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)EntityType.WITHER_SKELETON, 3));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)EntityType.ZOMBIFIED_PIGLIN, 2));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)((EntityType)EntityInit.SKELETON_ASSASSIN.get()), 5));
        summonPool.add(new SummonCandidate((EntityType<? extends Mob>)((EntityType)EntityInit.HULKING_ZOMBIE.get()), 10));
        for (int summonPower = 0; summonPower < desiredSummonPower; summonPower += next.weight) {
            int delta = desiredSummonPower - summonPower;
            List filtered = summonPool.stream().filter(s -> s.weight <= delta).sorted((a, b) -> a.weight - b.weight).collect(Collectors.toList());
            if (filtered.size() == 0) break;
            next = (SummonCandidate)filtered.get(filtered.size() - 1);
            this.summonQueue.add(next.type);
        }
    }

    private void createSummon() {
        if (this.summonQueue.size() == 0) {
            return;
        }
        BlockPos center = this.blockPosition();
        boolean found = false;
        int yCount = 0;
        while (!found) {
            while (yCount < 10 && this.level().isEmptyBlock(center)) {
                center = center.below();
                ++yCount;
            }
            center = center.above();
            if (this.level().isEmptyBlock(center)) {
                found = true;
                continue;
            }
            center = this.blockPosition().offset(-5 + (int)(Math.random() * 10.0), 0, -5 + (int)(Math.random() * 10.0));
        }
        if (!found) {
            center = this.blockPosition();
        }
        EntityType<? extends Mob> type = this.summonQueue.get(0);
        this.summonQueue.remove(0);
        Mob summon = (Mob)type.create(this.level());
        SummonUtils.setSummon(summon, (LivingEntity)this, Integer.MAX_VALUE);
        byte phase = (Byte)this.entityData.get(SHIELD_COUNT);
        float pctHealthBoost = 1.0f + Math.abs(0.2f * (float)(5 - phase));
        float armorBoost = 2 + 2 * (5 - phase);
        summon.getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(ResourceLocation.parse("wither_lich_summon_armorboost"), (double)armorBoost, AttributeModifier.Operation.ADD_VALUE));
        summon.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(ResourceLocation.parse("wither_lich_summon_healthboost"), (double)pctHealthBoost, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        summon.setHealth(summon.getMaxHealth());
        switch (phase) {
            case 4: {
                summon.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE));
                break;
            }
            case 3: {
                summon.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE));
                summon.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE));
                break;
            }
            case 2: {
                summon.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
                summon.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE));
                break;
            }
            case 0: 
            case 1: {
                summon.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
                summon.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
                summon.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE));
            }
        }
        summon.setPos(Vec3.atBottomCenterOf((Vec3i)center));
        this.level().addFreshEntity((Entity)summon);
        SummonUtils.addTrackedEntity((LivingEntity)this, (Entity)summon);
        ++this.numSummons;
    }

    private void decrementShield() {
        this.shouldShieldedStrike = false;
        this.entityData.set(SHIELD_COUNT, ((byte)MathUtils.clamp((Byte)this.entityData.get(SHIELD_COUNT) - 1, 0, 5)));
        this.setCooldown(1024, 0);
    }

    public boolean damageEntity(Entity entityIn) {
        SpellCastingResult success = SpellCaster.Affect(this.melee, this.meleeSpell, this.level(), new SpellSource((LivingEntity)this, InteractionHand.MAIN_HAND), new SpellTarget(entityIn));
        if (success.getCode().isConsideredSuccess()) {
            if (this.random.nextFloat() < 0.4f && entityIn instanceof Mob) {
                ((Mob)entityIn).setTarget((LivingEntity)this);
            }
            return true;
        }
        return false;
    }

    private void spellCastReset() {
        this.setAttackAction(AttackAction.NONE);
        this.setAction(Action.IDLE);
        this.globalSpellCooldown = 10 + (int)(Math.random() * 30.0);
    }

    private boolean spellCastPrecast(BossMonster<?> me) {
        this.setAction(Action.ATTACKING);
        this.setAttackAction(Math.random() < 0.5 ? AttackAction.CAST_1_LEFT : AttackAction.CAST_1_RIGHT);
        this.playSound(SFX.Entity.WitherLich.CAST, 1.0f, 0.8f + (float)(Math.random() * 0.4));
        return true;
    }

    private boolean spellCastPredicate(Entity target) {
        return this.getAction() == Action.IDLE && this.globalSpellCooldown == 0 && this.getTarget() != null && this.getTarget().isAlive();
    }

    public void decrementSummons() {
        --this.numSummons;
        if (this.numSummons <= 0) {
            this.shouldShieldedStrike = true;
            this.numSummons = 0;
        }
    }

    public Vec3 getLookVector() {
        return Vec3.directionFromRotation((float)this.getXRot(), (float)this.getYHeadRot()).normalize();
    }

    private int getPowerWordDeathTime() {
        return 1200 * Math.max(((Byte)this.entityData.get(SHIELD_COUNT)).byteValue(), 1);
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

    public int getTextureVariant() {
        return ((Byte)this.entityData.get(TEXTURE_VARIANT)).byteValue();
    }

    public WitherLich setFactionJoin(Player player, Vec3 start, Vec3 end, int lerpTicks) {
        this.cachedPlayer = player;
        this.entityData.set(FACTION_JOIN_PLAYER, Optional.of(player.getGameProfile().getId()));
        this.entityData.set(IS_JOINING, true);
        this.hideBossBar();
        this.setAction(Action.FJ_CONVERTING);
        this.start = new Vector3(start.x, start.y, start.z);
        this.end = new Vector3(end.x, end.y, end.z);
        this.lerpTicks = lerpTicks;
        this.powerWordDeathTicks = 0;
        return this;
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
    public void tick() {
        if (!this.isAlive()) {
            this.setAction(Action.DEATH);
        }
        if (this.powerWordDeathTicks > 0 && this.powerWordDeathTicks < this.getPowerWordDeathTime()) {
            ++this.powerWordDeathTicks;
        }
        if (this.globalSpellCooldown > 0) {
            --this.globalSpellCooldown;
        }
        this.setNoGravity(true);
        super.tick();
        if (!this.level().isClientSide() && this.getAction() == Action.FJ_CONVERTING) {
            if (this.getFactionJoinPlayer() == null) {
                this.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            ++this.lerpCount;
            if (this.lerpCount <= this.lerpTicks) {
                float pct = (float)this.lerpCount / (float)this.lerpTicks;
                Vector3 pos = Vector3.lerp(this.start, this.end, pct);
                this.setPos(pos.x, pos.y, pos.z);
                if (this.lerpCount < 20) {
                    this.lookAt(EntityAnchorArgument.Anchor.FEET, this.end.toVec3D());
                }
            } else {
                this.entityData.set(REACHING, true);
                if (this.lerpCount - this.lerpTicks > this.finishTicks) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            return;
        }
        float pctHeal = 0.2f * (float)((Byte)this.entityData.get(SHIELD_COUNT)).byteValue();
        if (pctHeal > 0.0f && this.getHealth() < this.getMaxHealth() * pctHeal) {
            this.heal(2.5f);
        }
        if (!this.level().isClientSide() && this.level().getGameTime() % 100L == 0L && this.getAction() != Action.SUMMON) {
            int summons = (int)SummonUtils.getSummons((LivingEntity)this).stream().filter(e -> !(e instanceof InsectSwarm)).count();
            while (this.numSummons > summons) {
                this.decrementSummons();
            }
            this.numSummons = summons;
            if (this.numSummons == 0) {
                this.setCooldown(1024, 0);
            }
        }
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }
    }

    protected void registerGoals() {
        double moveSpeed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(0, (Goal)new WitherLichFly());
        this.goalSelector.addGoal(1, (Goal)new ShieldedStrikeGoal());
        this.goalSelector.addGoal(2, (Goal)new SummonMinionsGoal());
        this.goalSelector.addGoal(2, (Goal)new PowerWordDeathGoal());
        ItemStack frostBolt = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe frostBoltSpell = new SpellRecipe();
        frostBoltSpell.setShape(Shapes.PROJECTILE);
        frostBoltSpell.addComponent(Components.FROST_DAMAGE);
        ((ModifiedSpellPart)frostBoltSpell.getComponent(0)).setValue(Attribute.DAMAGE, 20.0f);
        frostBoltSpell.addComponent(Components.SLOW);
        ((ModifiedSpellPart)frostBoltSpell.getComponent(1)).setValue(Attribute.DURATION, 10.0f);
        ((ModifiedSpellPart)frostBoltSpell.getComponent(1)).setValue(Attribute.MAGNITUDE, 2.0f);
        frostBoltSpell.writeToNBT(frostBolt.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack witherBolt = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe witherBoltSpell = new SpellRecipe();
        witherBoltSpell.setShape(Shapes.BOLT);
        ((ModifiedSpellPart)witherBoltSpell.getShape()).setValue(Attribute.RANGE, 24.0f);
        witherBoltSpell.addComponent(Components.WITHER);
        ((ModifiedSpellPart)witherBoltSpell.getComponent(0)).setValue(Attribute.DURATION, 10.0f);
        ((ModifiedSpellPart)witherBoltSpell.getComponent(0)).setValue(Attribute.MAGNITUDE, 3.0f);
        witherBoltSpell.writeToNBT(witherBolt.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack fossilizeBolt = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe fossilizeSpell = new SpellRecipe();
        fossilizeSpell.setShape(Shapes.CHAIN);
        ((ModifiedSpellPart)fossilizeSpell.getShape()).setValue(Attribute.RANGE, 32.0f);
        ((ModifiedSpellPart)fossilizeSpell.getShape()).setValue(Attribute.RADIUS, 10.0f);
        ((ModifiedSpellPart)fossilizeSpell.getShape()).setValue(Attribute.MAGNITUDE, 100.0f);
        fossilizeSpell.addComponent(Components.FOSSILIZE);
        ((ModifiedSpellPart)fossilizeSpell.getComponent(0)).setValue(Attribute.DURATION, 10.0f);
        fossilizeSpell.writeToNBT(fossilizeBolt.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemStack insectSwarm = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe insectSwarmSpell = new SpellRecipe();
        insectSwarmSpell.setShape(Shapes.SELF);
        insectSwarmSpell.addComponent(Components.INSECT_SWARM);
        insectSwarmSpell.writeToNBT(insectSwarm.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        this.goalSelector.addGoal(4, (Goal)new CastSpellAtTargetGoal(witherBolt, moveSpeed, 400, 20.0f, 0.0f, true, 1026, 8, 12, null, this::spellCastReset, this::spellCastPrecast).setUsePredicate(this::spellCastPredicate));
        this.goalSelector.addGoal(4, (Goal)new CastSpellAtTargetGoal(fossilizeBolt, moveSpeed, 400, 26.0f, 0.0f, true, 1027, 8, 12, null, this::spellCastReset, this::spellCastPrecast).setUsePredicate(this::spellCastPredicate));
        this.goalSelector.addGoal(5, (Goal)new CastSpellAtTargetGoal(frostBolt, moveSpeed, 60, 64.0f, 0.0f, true, 1025, 8, 12, null, this::spellCastReset, this::spellCastPrecast).setUsePredicate(this::spellCastPredicate));
        this.goalSelector.addGoal(3, (Goal)new CastSpellAtTargetGoal(insectSwarm, moveSpeed, 800, 20.0f, 0.0f, false, 1028, 20, 55, null, this::spellCastReset, me -> {
            this.setAction(Action.ATTACKING);
            this.setAttackAction(AttackAction.BREATH_INSECTS);
            this.playSound(SFX.Entity.WitherLich.BREATH_ATTACK, 1.0f, 0.8f + (float)(Math.random() * 0.4));
            return true;
        }).setUninterruptable().setUsePredicate(this::spellCastPredicate));
        this.goalSelector.addGoal(3, (Goal)new CastSpellAtTargetGoal(frostBolt, moveSpeed, 600, 20.0f, 0.0f, false, 1029, 20, 55, null, this::spellCastReset, me -> {
            this.setAction(Action.ATTACKING);
            this.setAttackAction(AttackAction.BREATH_FROST);
            this.playSound(SFX.Entity.WitherLich.BREATH_ATTACK, 1.0f, 0.8f + (float)(Math.random() * 0.4));
            int initialDelay = 10;
            int interval = 20;
            for (int i = 0; i < 3; ++i) {
                int delay = initialDelay + i * interval;
                this.setTimer("frost_breath", delay, () -> EntityUtil.getEntitiesWithinCone(this.level(), this.position(), this.getLookVector(), 20.0f, -60.0f, 60.0f, e -> e != this && (!SummonUtils.isSummon((Entity)e) || SummonUtils.getSummoner(e) != this)).forEach(e -> e.hurt(DamageHelper.createSourcedType(DamageHelper.FROST, this.level().registryAccess(), (Entity)this), 15.0f)));
            }
            return false;
        }).setUninterruptable().setUsePredicate(this::spellCastPredicate));
        this.goalSelector.addGoal(7, (Goal)new MeleeAttackGoal((PathfinderMob)this, moveSpeed, true));
        this.goalSelector.addGoal(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.targetSelector.addGoal(1, (Goal)new BossMonster.ThreatTableHurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal(this, Player.class, 10, true, false, le -> true));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    @Override
    public boolean hurt(DamageSource type, float amount) {
        float healthTarget;
        if (type.is(DamageTypeTags.IS_FREEZING) || type.is(DamageTypeTags.IS_FALL) || type.is(DamageTypes.STARVE) || type.is(DamageTypes.WITHER) || type.is(DamageTypes.IN_WALL) || type.is(DamageTypes.FLY_INTO_WALL)) {
            return false;
        }
        if (!type.is(DamageTypes.FELL_OUT_OF_WORLD) && (healthTarget = 0.2f * (float)((Byte)this.entityData.get(SHIELD_COUNT) - 1)) > 0.0f && this.getHealth() - amount < this.getMaxHealth() * healthTarget) {
            if (this.numSummons == 0) {
                this.setCooldown(1024, 0);
            } else {
                amount = this.getHealth() - healthTarget;
            }
        }
        return super.hurt(type, amount);
    }

    @Override
    public void die(DamageSource pCause) {
        if (!pCause.is(DamageTypes.FELL_OUT_OF_WORLD) && (Byte)this.entityData.get(SHIELD_COUNT) > 0) {
            this.setHealth(1.0f);
            return;
        }
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.setAction(Action.DEATH);
        this.setAttackAction(AttackAction.NONE);
        this.removeAllEffects();
        super.die(pCause);
    }

    protected void tickDeath() {
        ++this.lichDeathTime;
        if (this.lichDeathTime == 107) {
            if (!this.level().isClientSide()) {
                this.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get()), 200));
            }
            this.noPhysics = true;
        } else if (this.lichDeathTime > 120 && this.lichDeathTime <= 150) {
            this.setDeltaMovement(0.0, 0.5, 0.0);
        } else if (this.lichDeathTime > 150) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHIELD_COUNT, (byte)5);
        builder.define(TEXTURE_VARIANT, ((byte)(Math.random() * 4.0)));
        builder.define(FACTION_JOIN_PLAYER, Optional.empty());
        builder.define(REACHING, false);
        builder.define(IS_JOINING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("shieldCount", ((Byte)this.entityData.get(SHIELD_COUNT)).byteValue());
        nbt.putByte("textureVariant", ((Byte)this.entityData.get(TEXTURE_VARIANT)).byteValue());
        nbt.putInt("power_word_death", this.powerWordDeathTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("shieldCount")) {
            this.entityData.set(SHIELD_COUNT, nbt.getByte("shieldCount"));
        }
        if (nbt.contains("textureVariant")) {
            this.entityData.set(TEXTURE_VARIANT, nbt.getByte("textureVariant"));
        }
        if (nbt.contains("power_word_death")) {
            this.powerWordDeathTicks = nbt.getInt("power_word_death");
        }
        this.setAction(Action.IDLE);
        this.setAttackAction(AttackAction.NONE);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.powerWordDeathTicks > 0) {
            this.powerWordDeathBar.setProgress((float)this.powerWordDeathTicks / (float)this.getPowerWordDeathTime());
        } else {
            this.powerWordDeathBar.setVisible(false);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.shouldShowBossBar()) {
            this.powerWordDeathBar.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (this.shouldShowBossBar()) {
            this.powerWordDeathBar.removePlayer(player);
        }
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (this.isOnCooldown(1) || this.getAction() != Action.IDLE) {
            return true;
        }
        ArrayList<AttackAction> actionPool = new ArrayList<AttackAction>();
        actionPool.add(AttackAction.SWING_LEFT);
        actionPool.add(AttackAction.SWING_RIGHT);
        AttackAction selected = (AttackAction)(actionPool.get((int)((double)actionPool.size() * Math.random())));
        this.setAction(Action.ATTACKING);
        this.setAttackAction(selected);
        this.playSound(SFX.Entity.Odin.ATTACK, 1.0f, (float)(0.6 + Math.random() * 0.8));
        this.playSound(SFX.Entity.WitherLich.ATTACK, 1.0f, (float)(0.6 + Math.random() * 0.8));
        this.setTimer("", selected.animLength, () -> this.damageEntity(entityIn));
        this.setTimer("resetattack", selected.resetTime, () -> {
            this.setAttackAction(AttackAction.NONE);
            this.setAction(Action.IDLE);
            this.setCooldown(1, 20);
        });
        return true;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SFX.Entity.WitherLich.HIT;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.WitherLich.DEATH;
    }

    protected SoundEvent getAmbientSound() {
        return SFX.Entity.WitherLich.IDLE;
    }

    
    @Override
    protected PlayState handleAnimState(AnimationState<? extends BossMonster<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.getAction() == Action.FJ_CONVERTING) {
            if (!((Boolean)this.entityData.get(REACHING)).booleanValue()) {
                builder.thenLoop("animation.WitherLich.move");
            } else {
                builder.thenPlay("animation.WitherLich.reach");
            }
            return state.setAndContinue(builder);
        }
        if (this.getAction() == Action.DEATH) {
            builder.thenPlay("animation.WitherLich.death");
        } else if (this.getAction() == Action.SHIELDED_STRIKE) {
            builder.thenPlay("animation.WitherLich.shielded_strike");
        } else if (this.getAction() == Action.POWER_WORD_DEATH) {
            builder.thenPlay("animation.WitherLich.point");
        } else if (this.getAction() == Action.SUMMON) {
            builder.thenPlay("animation.WitherLich.cast_2h_raise");
        } else if (this.getAction() == Action.ATTACKING) {
            switch (this.getAttackAction()) {
                case BREATH_FROST: 
                case BREATH_INSECTS: {
                    builder.thenLoop("animation.WitherLich.breath_attack");
                    break;
                }
                case CAST_1_LEFT: {
                    builder.thenPlay("animation.WitherLich.cast_1_left");
                    break;
                }
                case CAST_1_RIGHT: {
                    builder.thenPlay("animation.WitherLich.cast_1_right");
                    break;
                }
                case SWING_LEFT: {
                    builder.thenPlay("animation.WitherLich.cast_2_left");
                    break;
                }
                case SWING_RIGHT: {
                    builder.thenPlay("animation.WitherLich.cast_2_right");
                    break;
                }
                default: {
                    builder.thenLoop("animation.WitherLich.idle");
                    break;
                }
            }
        } else {
            double movementSpeed = this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length();
            if (movementSpeed > 0.2) {
                builder.thenLoop("animation.WitherLich.move");
            } else {
                builder.thenLoop("animation.WitherLich.idle");
            }
        }
        return state.setAndContinue(builder);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 850.0).add(Attributes.MOVEMENT_SPEED, (double)0.7f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 14.0).add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    @Override
    public void setupSpawn() {
        this.setInvulnerableTicks(80);
        this.setHealth(this.getMaxHealth() / 3.0f);
    }

    @Override
    protected void spawnParticles() {
        if (this.getAttackAction() == AttackAction.BREATH_FROST) {
            this.spawnConeParticles(new MAParticleType(((MAParticleType)(ParticleInit.FROST.get())).setScale(0.2f)), 25);
        } else if (this.getAttackAction() == AttackAction.BREATH_INSECTS) {
            this.spawnConeParticles(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setColor(69, 66, 30), 10);
        }
    }

    private void spawnConeParticles(MAParticleType particle, int count) {
        Vec3 forward = Vec3.directionFromRotation((float)this.getXRot(), (float)this.getYHeadRot()).scale(0.25);
        Vec3 origin = this.position().add(forward.x, forward.y + 1.5, forward.z);
        for (int i = 0; i < count; ++i) {
            Vector3f direction = new Vector3f((float)forward.x, (float)forward.y, (float)forward.z);
            direction.rotate((Quaternionfc)Axis.YP.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
            direction.rotate((Quaternionfc)Axis.ZN.rotationDegrees((float)(-15 + (int)(Math.random() * 30.0))));
            direction.normalize();
            direction.mul((float)(0.5 + Math.random()));
            this.level().addParticle((ParticleOptions)particle, origin.x + (double)direction.x(), origin.y + (double)direction.y(), origin.z + (double)direction.z(), (double)direction.x(), (double)direction.y(), (double)direction.z());
        }
    }

    @Override
    public ResourceLocation getArenaStructureID() {
        return RLoc.create("faction/boss/cathedral");
    }

    @Override
    public int getArenaStructureSegment() {
        return 2;
    }

    @Override
    public HashMap<String, Integer> getDamageResists() {
        return Maps.newHashMap();
    }

    @Override
    public IFaction getFaction() {
        return Factions.UNDEAD;
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

    static class MoveHelperController
    extends MoveControl {
        private final WitherLich parentEntity;
        private int courseChangeCooldown;

        public MoveHelperController(WitherLich lich) {
            super((Mob)lich);
            this.parentEntity = lich;
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

    class SummonCandidate {
        public final EntityType<? extends Mob> type;
        public final int weight;

        public SummonCandidate(EntityType<? extends Mob> type, int weight) {
            this.type = type;
            this.weight = weight;
        }
    }

    public static enum AttackAction {
        NONE(0, 0),
        CAST_1_LEFT(10, 14),
        CAST_1_RIGHT(10, 14),
        SWING_LEFT(4, 8),
        SWING_RIGHT(4, 8),
        BREATH_FROST(48, 54),
        BREATH_INSECTS(48, 54);

        public final int animLength;
        public final int resetTime;

        private AttackAction(int animLength, int resetTime) {
            this.animLength = animLength;
            this.resetTime = resetTime;
        }
    }

    public static enum Action {
        IDLE,
        FJ_CONVERTING,
        ATTACKING,
        POWER_WORD_DEATH,
        SUMMON,
        SHIELDED_STRIKE,
        DEATH;

    }

    class WitherLichFly
    extends Goal {
        public WitherLichFly() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            MoveControl movementcontroller = WitherLich.this.getMoveControl();
            if (!movementcontroller.hasWanted()) {
                return true;
            }
            if (WitherLich.this.getTarget() == null) {
                double d2;
                double d1;
                double d0 = movementcontroller.getWantedX() - WitherLich.this.getX();
                double d3 = d0 * d0 + (d1 = movementcontroller.getWantedY() - WitherLich.this.getY()) * d1 + (d2 = movementcontroller.getWantedZ() - WitherLich.this.getZ()) * d2;
                return d3 < 4.0 || d3 > 3600.0;
            }
            double dist = WitherLich.this.getTarget().distanceToSqr((Entity)WitherLich.this);
            return !WitherLich.this.getSensing().hasLineOfSight((Entity)WitherLich.this.getTarget()) || dist > 400.0;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            if (WitherLich.this.getTarget() != null) {
                Vec3 direction = WitherLich.this.position().subtract(WitherLich.this.getTarget().position()).normalize();
                direction = direction.add(0.0, -direction.y, 0.0);
                Vec3 offset = direction.scale(10.0);
                Vec3 target = WitherLich.this.getTarget().position().add(offset);
                if (WitherLich.this.getTarget().onGround()) {
                    target = target.add(0.0, 1.5, 0.0);
                }
                WitherLich.this.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.0);
            }
        }
    }

    class ShieldedStrikeGoal
    extends Goal {
        private int tickCount = 0;

        ShieldedStrikeGoal() {
        }

        public boolean canUse() {
            return WitherLich.this.shouldShieldedStrike || WitherLich.this.getAction() == Action.SHIELDED_STRIKE;
        }

        public boolean canContinueToUse() {
            return this.canUse() && this.tickCount < 33;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void start() {
            this.tickCount = 0;
            WitherLich.this.setAction(Action.SHIELDED_STRIKE);
        }

        public void stop() {
            WitherLich.this.setAttackAction(AttackAction.NONE);
            WitherLich.this.setAction(Action.IDLE);
            WitherLich.this.powerWordDeathTicks = 1;
        }

        public void tick() {
            ++this.tickCount;
            if (this.tickCount == 5) {
                WitherLich.this.decrementShield();
            }
        }
    }

    class SummonMinionsGoal
    extends Goal {
        private static final int DURATION = 62;
        private int tickCount = 0;
        private int summonInterval = 1;

        SummonMinionsGoal() {
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public boolean canUse() {
            boolean noSummons = WitherLich.this.numSummons == 0;
            boolean offCooldown = !WitherLich.this.isOnCooldown(1024);
            boolean isIdle = WitherLich.this.getAction() == Action.IDLE || WitherLich.this.getAction() == Action.SUMMON;
            return noSummons && offCooldown && isIdle;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canContinueToUse() {
            return WitherLich.this.getAction() == Action.SUMMON || this.tickCount < 62;
        }

        public void start() {
            WitherLich.this.setAction(Action.SUMMON);
            WitherLich.this.buildSummonList();
            this.summonInterval = Math.max(62 / WitherLich.this.summonQueue.size(), 1);
            WitherLich.this.playSound(SFX.Entity.WitherLich.SUMMON, 1.0f, 1.0f);
        }

        public void stop() {
            WitherLich.this.setCooldown(1024, Integer.MAX_VALUE);
            WitherLich.this.setAttackAction(AttackAction.NONE);
            WitherLich.this.setAction(Action.IDLE);
            this.tickCount = 0;
        }

        public void tick() {
            ++this.tickCount;
            if (this.tickCount % this.summonInterval == 0) {
                WitherLich.this.createSummon();
            }
            if (this.tickCount > 62) {
                this.stop();
            }
        }
    }

    class PowerWordDeathGoal
    extends Goal {
        private int tickCount = 0;
        private LivingEntity target = null;

        public PowerWordDeathGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canUse() {
            return WitherLich.this.powerWordDeathTicks >= WitherLich.this.getPowerWordDeathTime() && WitherLich.this.getTarget() != null;
        }

        public boolean canContinueToUse() {
            boolean hasTarget = this.target != null;
            boolean timer = this.tickCount < 80;
            return hasTarget && timer;
        }

        public void start() {
            WitherLich.this.getNavigation().stop();
            WitherLich.this.setAction(Action.POWER_WORD_DEATH);
            this.target = WitherLich.this.getTarget();
            WitherLich.this.playSound(SFX.Entity.WitherLich.PW_DEATH, 1.0f, 1.0f);
        }

        public void stop() {
            this.tickCount = 1;
            WitherLich.this.powerWordDeathTicks = 1;
            WitherLich.this.setAction(Action.IDLE);
            WitherLich.this.setAttackAction(AttackAction.NONE);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            ++this.tickCount;
            WitherLich.this.setAction(Action.POWER_WORD_DEATH);
            WitherLich.this.lookAt(EntityAnchorArgument.Anchor.FEET, this.target.getEyePosition());
            if (this.tickCount == 44) {
                this.target.hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.FELL_OUT_OF_WORLD, WitherLich.this.level().registryAccess(), (Entity)WitherLich.this), this.target.getMaxHealth() * 0.9f);
            }
        }
    }
}
