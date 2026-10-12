/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobType
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForgeMod
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.PlayState
 */
package com.mna.entities.faction;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ISpellInteractibleEntity;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.entities.EntityInit;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.ProjectileHelper;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class Barkling
extends BaseFactionMob<Barkling>
implements ISpellInteractibleEntity<Barkling> {
    private static final String INSTRUCTION_DAMAGE = "damage";
    private static final String ACTION_MELEE = "melee";
    private static final String ACTION_PARRY = "parry";
    private static final int PARRY_CD_T1 = 40;
    private static final int PARRY_CD_T2 = 20;
    private int parryCooldown = 0;
    private boolean isActing = false;
    private boolean isMeleeAttacking = false;
    private boolean isParrying = false;

    public Barkling(EntityType<Barkling> type, Level worldIn) {
        super(type, worldIn);
    }

    public Barkling(Level worldIn) {
        this((EntityType<Barkling>)((EntityType)EntityInit.BARKLING.get()), worldIn);
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.isActing) {
            if (this.isParrying) {
                builder.thenPlay("animation.barkling.parry");
            } else if (this.swingingArm == InteractionHand.MAIN_HAND) {
                builder.thenPlay("animation.barkling.attack_1");
            } else {
                builder.thenPlay("animation.barkling.attack_2");
            }
        }
        if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
            if (this.getTarget() != null) {
                builder.thenLoop("animation.barkling.walk_hasTarget");
            } else {
                builder.thenLoop("animation.barkling.walk_noTarget");
            }
        } else if (this.getTarget() != null) {
            builder.thenLoop("animation.barkling.idle_hasTarget");
        } else {
            builder.thenLoop("animation.barkling.idle_noTarget");
        }
        return state.setAndContinue(builder);
    }

    @Override
    public void tick() {
        super.tick();
        --this.parryCooldown;
    }

    @Override
    public IFaction getFaction() {
        return Factions.FEY;
    }

    public void setTarget(LivingEntity pLivingEntity) {
        if (this.getTarget() == pLivingEntity) {
            return;
        }
        super.setTarget(pLivingEntity);
        if (!this.level().isClientSide()) {
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    public void swing(InteractionHand p_226292_1_, boolean p_226292_2_) {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 20, ACTION_MELEE, this::handleDelayCallback));
        this.isActing = true;
        this.isMeleeAttacking = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.playSound(SFX.Entity.Generic.WOOSH, 1.0f, (float)(0.9 + Math.random() * 0.2));
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (!this.isMeleeAttacking) {
            return true;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>(INSTRUCTION_DAMAGE, 10, entityIn, this::handleDelayCallback));
        this.isMeleeAttacking = false;
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new FactionTierWrapperGoal(1, this, new ParryGoal()));
        this.goalSelector.addGoal(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5, false));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.35f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, this::factionTargetPlayerPredicate));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, this::factionTargetHelpPredicate));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public SoundEvent getAmbientSound() {
        return SoundEvents.CHORUS_FLOWER_DEATH;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.AXE_STRIP;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.WOOD_PLACE;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isParrying) {
            Entity e = source.getDirectEntity();
            if (source.is(DamageTypes.MOB_PROJECTILE) && e instanceof Projectile) {
                ProjectileHelper.ReflectProjectile((LivingEntity)this, (Projectile)e, this.getTier() >= 1, (float)e.getDeltaMovement().length());
                return false;
            }
        }
        if (source.is(DamageTypes.DROWN)) {
            return false;
        }
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
            amount *= 2.0f;
        }
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
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entityIn instanceof LivingEntity) {
            f += com.mna.api.enchantments.MNAEnchantmentHelper.getDamageBonus((ItemStack)this.getMainHandItem(), (LivingEntity)entityIn);
            f1 += (float)com.mna.api.enchantments.MNAEnchantmentHelper.getKnockbackBonus((LivingEntity)this);
        }
        if ((i = com.mna.api.enchantments.MNAEnchantmentHelper.getFireAspect((LivingEntity)this)) > 0) {
            entityIn.igniteForSeconds((float)(i * 4));
        }
        if (flag = entityIn.hurt(this.damageSources().mobAttack((LivingEntity)this), f)) {
            if (f1 > 0.0f && entityIn instanceof LivingEntity) {
                ((LivingEntity)entityIn).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            com.mna.tools.MATools.doEnchantDamageEffects(this, entityIn);
            this.setLastHurtMob(entityIn);
            if (this.random.nextFloat() < 0.2f && entityIn instanceof Mob) {
                ((Mob)entityIn).setTarget((LivingEntity)this);
            }
        }
        return flag;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, (double)0.45f).add(NeoForgeMod.SWIM_SPEED, (double)0.6f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    private void handleDelayCallback(String identifier, Entity entity) {
        if (!this.level().isClientSide() && this.isAlive()) {
            switch (identifier) {
                case "damage": {
                    this.damageEntity(entity);
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
                case "parry": {
                    this.parryCooldown = this.getTier() == 1 ? 40 : 20;
                    this.isParrying = false;
                }
            }
            this.isActing = this.isMeleeAttacking || this.isParrying;
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isActing);
        nbt.putBoolean("parrying", this.isParrying);
        nbt.putInt("target", this.getTarget() == null ? -1 : this.getTarget().getId());
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        Entity e;
        this.isActing = nbt.getBoolean("attacking");
        this.isParrying = nbt.getBoolean("parrying");
        int targetId = nbt.getInt("target");
        if (targetId == -1) {
            this.setTarget(null);
        }
        if ((e = this.level().getEntity(targetId)) instanceof LivingEntity) {
            this.setTarget((LivingEntity)e);
        }
    }

    @Override
    public boolean onShapeTarget(ISpellDefinition spell, SpellSource source) {
        if (!this.isParrying) {
            return false;
        }
        Vec3 fwd = Vec3.directionFromRotation((Vec2)this.getRotationVector());
        return ManaAndArtificeMod.getSpellHelper().reflectSpell(this.level(), (LivingEntity)this, spell, source, this.position(), fwd, true);
    }

    public class ParryGoal
    extends Goal {
        private Entity dodgeEntity;

        public ParryGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canUse() {
            if (Barkling.this.parryCooldown > 0) {
                return false;
            }
            Vec3 rt_startVec = Barkling.this.position();
            Vec3 thisFwd = Vec3.directionFromRotation((Vec2)Barkling.this.getRotationVector());
            Vec3 rt_endVec = rt_startVec.add(thisFwd.scale(10.0));
            List potentials = Barkling.this.level().getEntities((Entity)Barkling.this, Barkling.this.getBoundingBox().inflate(10.0)).stream().filter(e -> e instanceof Projectile && !e.onGround()).filter(e -> {
                AABB axisalignedbb = e.getBoundingBox().inflate(5.0);
                Optional optional = axisalignedbb.clip(rt_startVec, rt_endVec);
                return optional.isPresent();
            }).collect(Collectors.toList());
            if (potentials.size() == 0) {
                return false;
            }
            potentials.sort(new Comparator<Entity>(){

                @Override
                public int compare(Entity o1, Entity o2) {
                    Double d1 = o1.distanceToSqr((Entity)Barkling.this);
                    Double d2 = o2.distanceToSqr((Entity)Barkling.this);
                    return d1.compareTo(d2);
                }
            });
            this.dodgeEntity = (Entity)potentials.get(0);
            return true;
        }

        public void start() {
            Barkling.this.getNavigation().stop();
            Barkling.this.isParrying = true;
            Barkling.this.isActing = true;
            Barkling.this.getLookControl().setLookAt(this.dodgeEntity, 30.0f, 30.0f);
            DelayedEventQueue.pushEvent(Barkling.this.level(), new TimedDelayedEvent<String>("resetattack", 70, Barkling.ACTION_PARRY, Barkling.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(Barkling.this);
        }

        public void tick() {
            Barkling.this.getLookControl().setLookAt(this.dodgeEntity, 30.0f, 30.0f);
        }

        public boolean canContinueToUse() {
            return Barkling.this.isParrying;
        }

        protected EntityHitResult rayTraceEntity(Vec3 startVec, Vec3 endVec, float size, Entity searchEntity) {
            return ProjectileUtil.getEntityHitResult((Level)Barkling.this.level(), (Entity)Barkling.this, (Vec3)startVec, (Vec3)endVec, (AABB)Barkling.this.getBoundingBox().inflate((double)size), entity -> searchEntity == entity);
        }
    }
}


