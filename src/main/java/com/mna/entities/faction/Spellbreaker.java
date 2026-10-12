 
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
import com.mna.effects.EffectInit;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class Spellbreaker
extends BaseFactionMob<Spellbreaker>
implements ISpellInteractibleEntity<Spellbreaker> {
    private static final String INSTRUCTION_DAMAGE = "damage";
    private static final String INSTRUCTION_SILENCE = "silence";
    private static final String INSTRUCTION_RALLY = "rally";
    private static final int SHIELD_BASH_CD = 300;
    private static final int RALLY_CD = 3600;
    private int shieldBashCooldown = 0;
    private int rallyCooldown = 0;
    private boolean isActing = false;
    private boolean isMeleeAttacking = false;
    private boolean isShieldBashing = false;
    private boolean isRallying = false;

    public Spellbreaker(EntityType<Spellbreaker> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public IFaction getFaction() {
        return Factions.COUNCIL;
    }

    public boolean isBlocking() {
        return !this.isActing;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> state) {
        if (!this.isActing) {
            if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.combat_walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.combat_idle"));
        }
        if (this.isShieldBashing) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.combat_shield_bash"));
        }
        if (this.isRallying) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.combat_rally"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.combat_stab"));
    }

    @Override
    public void tick() {
        super.tick();
        --this.shieldBashCooldown;
        --this.rallyCooldown;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new FactionTierWrapperGoal(2, this, new RallyGoal()));
        this.goalSelector.addGoal(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, this.getAttributeValue(Attributes.MOVEMENT_SPEED), false));
        this.goalSelector.addGoal(5, (Goal)new RandomStrollGoal((PathfinderMob)this, (double)0.35f));
        this.goalSelector.addGoal(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, this::factionTargetPlayerPredicate));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, this::factionTargetHelpPredicate));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public void swing(InteractionHand p_226292_1_, boolean p_226292_2_) {
        if (this.isActing) {
            return;
        }
        DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<String>("resetattack", 40, "", this::handleDelayCallback));
        this.isActing = true;
        this.isMeleeAttacking = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (!this.isMeleeAttacking) {
            return true;
        }
        if (this.tier >= 2 && this.shieldBashCooldown <= 0) {
            this.isShieldBashing = true;
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>(INSTRUCTION_SILENCE, 20, entityIn, this::handleDelayCallback));
        } else {
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Entity>(INSTRUCTION_DAMAGE, 20, entityIn, this::handleDelayCallback));
        }
        this.isMeleeAttacking = false;
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
            if (f1 > 0.0f && entityIn instanceof LivingEntity) {
                ((LivingEntity)entityIn).knockback((double)(f1 * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if (entityIn instanceof Player) {
                Player playerentity = (Player)entityIn;
                this.maybeDisableShield(playerentity, this.getMainHandItem(), playerentity.isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY);
            }
            this.doEnchantDamageEffects((LivingEntity)this, entityIn);
            this.setLastHurtMob(entityIn);
            if (this.random.nextFloat() < 0.4f && entityIn instanceof Mob) {
                ((Mob)entityIn).setTarget((LivingEntity)this);
            }
        }
        return flag;
    }

    private void maybeDisableShield(Player p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_) {
        if (!p_233655_2_.isEmpty() && !p_233655_3_.isEmpty() && p_233655_2_.getItem() instanceof AxeItem && p_233655_3_.getItem() == Items.SHIELD) {
            float f = 0.25f + (float)EnchantmentHelper.getBlockEfficiency((LivingEntity)this) * 0.05f;
            if (this.random.nextFloat() < f) {
                p_233655_1_.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level().broadcastEntityEvent((Entity)p_233655_1_, (byte)30);
            }
        }
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, (double)0.55f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    private void handleDelayCallback(String identifier, Entity entity) {
        if (!this.level().isClientSide() && this.isAlive()) {
            switch (identifier) {
                case "damage": {
                    this.damageEntity(entity);
                    this.playSound(SFX.Entity.Generic.WOOSH, 1.0f, (float)(0.9 + Math.random() * (double)0.2f));
                    break;
                }
                case "silence": {
                    this.damageEntity(entity);
                    if (entity instanceof Player) {
                        ((Player)entity).addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.SILENCE.get()), 100));
                    }
                    this.playSound(SFX.Entity.Spellbreaker.SHIELD_BASH, 1.0f, (float)(0.9 + Math.random() * (double)0.2f));
                    break;
                }
                case "rally": {
                    this.playSound(SFX.Entity.Spellbreaker.RALLY, 1.0f, 1.0f);
                    List<Monster> allies = this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(10.0), e -> e.isAlive() && e instanceof Spellbreaker);
                    for (Monster m : allies) {
                        m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, true));
                        m.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false));
                        m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2, false, true));
                    }
                    break;
                }
            }
        }
    }

    private void handleDelayCallback(String identifier, String data) {
        if (!this.level().isClientSide()) {
            this.isActing = false;
            if (this.isShieldBashing) {
                this.shieldBashCooldown = 300;
                this.isShieldBashing = false;
            } else if (this.isRallying) {
                this.rallyCooldown = 3600;
                this.isRallying = false;
            }
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("attacking", this.isActing);
        nbt.putBoolean("shield_bashing", this.isShieldBashing);
        nbt.putBoolean("rallying", this.isRallying);
        return nbt;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.isActing = nbt.getBoolean("attacking");
        this.isShieldBashing = nbt.getBoolean("shield_bashing");
        this.isRallying = nbt.getBoolean("rallying");
    }

    @Override
    public boolean onShapeTarget(ISpellDefinition spell, SpellSource source) {
        if (!(source.getCaster() instanceof BossMonster) || Math.random() < 0.25) {
            Vec3 fwd = Vec3.directionFromRotation((Vec2)this.getRotationVector());
            return ManaAndArtificeMod.getSpellHelper().reflectSpell(this.level(), (LivingEntity)this, spell, source, this.position(), fwd, true);
        }
        return true;
    }

    public class RallyGoal
    extends Goal {
        public RallyGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return Spellbreaker.this.rallyCooldown <= 0 && Spellbreaker.this.getTarget() != null && Spellbreaker.this.getHealth() < Spellbreaker.this.getMaxHealth() * 0.75f && !Spellbreaker.this.hasEffect(MobEffects.REGENERATION) && !Spellbreaker.this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !Spellbreaker.this.hasEffect(MobEffects.DAMAGE_BOOST) && !Spellbreaker.this.isActing;
        }

        public void start() {
            Spellbreaker.this.getNavigation().stop();
            Spellbreaker.this.isRallying = true;
            Spellbreaker.this.isActing = true;
            DelayedEventQueue.pushEvent(Spellbreaker.this.level(), new TimedDelayedEvent<LivingEntity>(Spellbreaker.INSTRUCTION_RALLY, 20, (LivingEntity)Spellbreaker.this, Spellbreaker.this::handleDelayCallback));
            DelayedEventQueue.pushEvent(Spellbreaker.this.level(), new TimedDelayedEvent<String>("resetattack", 40, "", Spellbreaker.this::handleDelayCallback));
            ServerMessageDispatcher.sendEntityStateMessage(Spellbreaker.this);
        }

        public boolean canContinueToUse() {
            return Spellbreaker.this.isRallying;
        }
    }
}

