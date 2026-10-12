/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.util.Mth
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobType
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForgeMod
 *  software.bernie.geckolib.animatable.GeoEntity
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.mna.entities.summon;

import com.mna.tools.MATools;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.entities.EntityInit;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BloodGolem
extends Monster
implements GeoEntity {
    protected AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private int nonRotatedDeathTime = 0;

    public BloodGolem(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public BloodGolem(Level pLevel) {
        this((EntityType<? extends Monster>)((EntityType)EntityInit.BLOOD_GOLEM.get()), pLevel);
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && SummonUtils.getSummoner((LivingEntity)this) == null) {
            this.discard();
            return;
        }
        if (this.level().isClientSide() && !this.isDeadOrDying()) {
            Vec3 fwd = this.getForward().normalize().scale((double)0.4f);
            Vec3 side = this.getForward().normalize().cross(new Vec3(0.0, 1.0, 0.0)).scale((double)0.9f);
            int particleCount = this.swinging ? 5 : 1;
            for (int i = 0; i < particleCount; ++i) {
                Vec3 particleVelocity = this.swinging ? this.getForward().add(0.0, 0.25, 0.0) : Vec3.ZERO;
                Vec3 particlePos = this.position().add((MathUtils.RandomBetween(-fwd.x, fwd.x) + MathUtils.RandomBetween(-side.x, side.x)) * this.getBoundingBox().getXsize(), (double)MathUtils.RandomBetween(0.25f, 1.25f) * this.getBoundingBox().getYsize(), (MathUtils.RandomBetween(-fwd.z, fwd.z) + MathUtils.RandomBetween(-side.z, side.z)) * this.getBoundingBox().getZsize());
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setGravity(0.05f).setPhysics(true), particlePos.x, particlePos.y, particlePos.z, particleVelocity.x, particleVelocity.y, particleVelocity.z);
            }
        }
    }

    protected void tickDeath() {
        ++this.nonRotatedDeathTime;
        if (this.nonRotatedDeathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent((Entity)this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean doHurtTarget(Entity pEntity) {
        if (pEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)pEntity;
            float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float knockback = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
            float preAttackHealth = living.getHealth();
            boolean didHurtTarget = living.hurt(this.damageSources().mobAttack((LivingEntity)this), damage);
            if (didHurtTarget) {
                if (knockback > 0.0f) {
                    living.knockback((double)(knockback * 0.5f), (double)Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)))));
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                }
                if (pEntity instanceof Player) {
                    Player player = (Player)pEntity;
                    this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
                }
                this.setLastHurtMob(pEntity);
                if ((!(MATools.isUndead(living))) && living.canChangeDimensions()) {
                    float postAttackHealth = living.getHealth();
                    float actualDamage = preAttackHealth - postAttackHealth;
                    LivingEntity summoner = SummonUtils.getSummoner((LivingEntity)this);
                    if (summoner != null) {
                        summoner.heal(actualDamage / 4.0f);
                    }
                    this.heal(actualDamage);
                }
            }
            return didHurtTarget;
        }
        return super.doHurtTarget(pEntity);
    }

    private void maybeDisableShield(Player pPlayer, ItemStack pMobItemStack, ItemStack pPlayerItemStack) {
        if (!pMobItemStack.isEmpty() && !pPlayerItemStack.isEmpty() && pMobItemStack.getItem() instanceof AxeItem && pPlayerItemStack.is(Items.SHIELD)) {
            float f = 0.25f + (float)EnchantmentHelper.getBlockEfficiency((LivingEntity)this) * 0.05f;
            if (this.random.nextFloat() < f) {
                pPlayer.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level().broadcastEntityEvent((Entity)pPlayer, (byte)30);
            }
        }
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, (Goal)new RandomLookAroundGoal((Mob)this));
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, (double)0.3f).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, (double)0.8f).add((Attribute)NeoForgeMod.STEP_HEIGHT_ADDITION.get(), 1.0);
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> this.handleAnimState((AnimationState<BloodGolem>)state));
        registrar.add(new AnimationController[]{controller});
    }

    protected PlayState handleAnimState(AnimationState<BloodGolem> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.isDeadOrDying()) {
            builder.thenPlayAndHold("animation.blood_golem.die");
        } else if (this.tickCount < 26) {
            builder.thenPlayAndHold("animation.blood_golem.summon");
        } else {
            if (this.swinging) {
                switch (this.swingingArm) {
                    case MAIN_HAND: {
                        builder.thenPlay("animation.blood_golem.swing_right");
                        break;
                    }
                    case OFF_HAND: {
                        builder.thenPlay("animation.blood_golem.swing_left");
                    }
                }
            }
            if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                builder.thenLoop("animation.blood_golem.walk");
            } else {
                builder.thenLoop("animation.blood_golem.idle");
            }
        }
        return state.setAndContinue(builder);
    }

    protected SoundEvent getAmbientSound() {
        return SFX.Entity.BloodGolem.IDLE;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.BloodGolem.DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SFX.Entity.BloodGolem.HIT;
    }
}


