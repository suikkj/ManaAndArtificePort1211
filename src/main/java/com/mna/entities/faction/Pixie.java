 
package com.mna.entities.faction;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.DamageHelper;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ai.CastSpellOnSelfGoal;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.effects.EffectInit;
import com.mna.entities.ai.FactionTierWrapperGoal;
import com.mna.entities.faction.base.BaseFlyingFactionMob;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.network.ServerMessageDispatcher;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class Pixie
extends BaseFlyingFactionMob<Pixie> {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Pixie.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TELEPORTING = SynchedEntityData.defineId(Pixie.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AFFINITY = SynchedEntityData.defineId(Pixie.class, (EntityDataSerializer)EntityDataSerializers.INT);

    public Pixie(EntityType<Pixie> type, Level worldIn) {
        super(type, worldIn);
    }

    public static boolean canSpawnPredicate(EntityType<Pixie> type, ServerLevelAccessor worldIn, MobSpawnType reason, BlockPos pos, RandomSource randomIn) {
        if (IFactionEnemy.canMnASpawn((LevelAccessor)worldIn, pos)) {
            return worldIn.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn((ServerLevelAccessor)worldIn, (BlockPos)pos, (RandomSource)randomIn) && Pixie.checkMobSpawnRules(type, (LevelAccessor)worldIn, (MobSpawnType)reason, (BlockPos)pos, (RandomSource)randomIn);
        }
        return false;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.spawnParticles();
        }
        super.tick();
    }

    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide()) {
            this.pickRandomAffinity();
        }
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SFX.Entity.Pixie.HURT;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.Pixie.DEATH;
    }

    @Override
    public IFaction getFaction() {
        return Factions.FEY;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getAffinity() == Affinity.FIRE && source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        if (this.getAffinity() == Affinity.WATER && source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        if (this.getAffinity() == Affinity.EARTH && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            amount /= 2.0f;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(TELEPORTING, false);
        builder.define(AFFINITY, Affinity.WIND.ordinal());
    }

    public boolean isActing() {
        return this.isAttacking() || this.isTeleporting();
    }

    public boolean isAttacking() {
        return (Boolean)this.entityData.get(ATTACKING);
    }

    public boolean isTeleporting() {
        return (Boolean)this.entityData.get(TELEPORTING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public void setTeleporting(boolean teleporting) {
        this.entityData.set(TELEPORTING, teleporting);
    }

    public Affinity getAffinity() {
        return Affinity.values()[(Integer)this.entityData.get(AFFINITY)];
    }

    private void pickRandomAffinity() {
        Affinity[] validAffinities = new Affinity[]{Affinity.FIRE, Affinity.EARTH, Affinity.WATER, Affinity.WIND};
        this.entityData.set(AFFINITY, validAffinities[(int)(Math.random() * (double)validAffinities.length)].ordinal());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        CastSpellOnSelfGoal<Pixie> teleportGoal = new CastSpellOnSelfGoal<Pixie>(this, this.createBlinkSpell(), pix -> pix.getTarget() != null && pix.getTarget().distanceToSqr((Entity)pix) < 16.0, pix -> {
            pix.setTeleporting(true);
            pix.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()));
        }, pix -> pix.setTeleporting(false), 10);
        this.goalSelector.addGoal(6, (Goal)new FactionTierWrapperGoal(2, this, teleportGoal));
        this.goalSelector.addGoal(7, (Goal)new BoltAttackGoal(this));
    }

    private ItemStack createBlinkSpell() {
        ItemStack invisSpell = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe invis = new SpellRecipe();
        invis.setShape(Shapes.SELF);
        invis.addComponent(Components.BLINK);
        invis.changeComponentAttributeValue(0, Attribute.RANGE, 12.0f);
        invis.writeToNBT(invisSpell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        return invisSpell;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return FlyingMob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, (double)0.45f).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void addControllerListeners(AnimationController<Pixie> controller) {
        controller.transitionLength(2);
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFlyingFactionMob<?>> state) {
        if (!this.isActing()) {
            if (this.getDeltaMovement().length() > (double)0.1f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.pixie.move"));
            }
        } else {
            if (this.isAttacking()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.pixie.cast").thenLoop("animation.pixie.idle_hostile"));
            }
            if (this.isTeleporting()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.pixie.teleport").thenLoop("animation.pixie.idle_hostile"));
            }
        }
        if (this.getTarget() == null || !this.getTarget().isAlive()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.pixie.idle"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.pixie.idle_hostile"));
    }

    private void spawnParticles() {
        int numParticles = 2;
        for (int i = 0; i < numParticles; ++i) {
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())).setColor(229, 220, 84), this.getX() - 0.2 + Math.random() * 0.4, this.getY() + Math.random() * 0.5, this.getZ() - 0.2 + Math.random() * 0.4, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public CompoundTag getPacketData() {
        return new CompoundTag();
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
    }

    static class BoltAttackGoal
    extends Goal {
        private final Pixie parentEntity;
        public int attackTimer;

        public BoltAttackGoal(Pixie pixie) {
            this.parentEntity = pixie;
        }

        public boolean canUse() {
            return this.parentEntity.getTarget() != null;
        }

        public void start() {
            this.attackTimer = 0;
        }

        public void stop() {
            this.parentEntity.setAttacking(false);
        }

        public boolean canContinueToUse() {
            return this.parentEntity.getTarget() != null && this.parentEntity.getTarget().isAlive();
        }

        public void tick() {
            LivingEntity livingentity = this.parentEntity.getTarget();
            if (livingentity == null) {
                return;
            }
            if (livingentity.distanceToSqr((Entity)this.parentEntity) < 144.0 && this.parentEntity.getSensing().hasLineOfSight((Entity)livingentity)) {
                ++this.attackTimer;
                if (this.attackTimer == 40) {
                    this.parentEntity.setAttacking(true);
                }
                if (this.attackTimer == 42) {
                    if (!this.parentEntity.isSilent()) {
                        this.parentEntity.playSound(SFX.Entity.Pixie.ATTACK, 1.0f, (float)((double)0.9f + Math.random() * (double)0.2f));
                    }
                    boolean isMiss = Math.random() > (double)(0.3f + (float)this.parentEntity.getTier() * 0.5f);
                    Vec3 offset = new Vec3(0.0, 0.0, 0.0);
                    if (isMiss) {
                        offset = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() * 0.5);
                    }
                    int color = 0;
                    switch (this.parentEntity.getAffinity()) {
                        case EARTH: {
                            color = -14126821;
                            break;
                        }
                        case WIND: {
                            color = -3815995;
                            break;
                        }
                        case FIRE: {
                            color = -3648221;
                            break;
                        }
                        default: {
                            color = 0;
                        }
                    }
                    ServerMessageDispatcher.sendParticleSpawn(this.parentEntity.getX(), this.parentEntity.getY(), this.parentEntity.getZ(), this.parentEntity.getTarget().getX() + offset.x(), this.parentEntity.getTarget().getY() + (double)this.parentEntity.getTarget().getEyeHeight() + offset.y(), this.parentEntity.getTarget().getZ() + offset.z(), color, 64.0f, (ResourceKey<Level>)this.parentEntity.level().dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
                    if (!isMiss) {
                        switch (this.parentEntity.getAffinity()) {
                            case EARTH: {
                                this.applyEarthEffect();
                                break;
                            }
                            case WATER: {
                                this.applyWaterEffect();
                                break;
                            }
                            case FIRE: {
                                this.applyFireEffect();
                                break;
                            }
                            default: {
                                this.applyAirEffect();
                            }
                        }
                    }
                } else if (this.attackTimer >= 62) {
                    this.parentEntity.setAttacking(false);
                    this.attackTimer = 0;
                }
            } else if (this.attackTimer > 0) {
                --this.attackTimer;
            }
        }

        private void applyAirEffect() {
            LivingEntity tgt = this.parentEntity.getTarget();
            if (tgt.onGround()) {
                tgt.push((double)-0.2f + Math.random() * (double)0.4f, 1.0, (double)-0.2f + Math.random() * (double)0.4f);
                if (tgt instanceof Player) {
                    ((ServerPlayer)tgt).connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)tgt));
                }
            }
        }

        private void applyWaterEffect() {
            if (this.parentEntity.getTarget().isInWater()) {
                this.parentEntity.getTarget().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.WATERY_GRAVE.get()), 10));
            } else {
                this.parentEntity.getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2));
            }
            this.parentEntity.getTarget().hurt(DamageHelper.createSourcedType(DamageHelper.FROST, this.parentEntity.level().registryAccess(), (Entity)this.parentEntity), (float)(2 * this.parentEntity.getTier()));
        }

        private void applyEarthEffect() {
            if (!this.parentEntity.getTarget().onGround() && this.parentEntity.getTarget().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get())) == null) {
                this.parentEntity.getTarget().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()), 100));
            }
            this.parentEntity.getTarget().hurt(this.parentEntity.damageSources().mobAttack((LivingEntity)this.parentEntity), 4.0f);
        }

        private void applyFireEffect() {
            this.parentEntity.getTarget().addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100));
            if (this.parentEntity.getTarget() instanceof Player) {
                Player player = (Player)this.parentEntity.getTarget();
                player.getArmorSlots().forEach(stack -> stack.hurtAndBreak(5, (net.minecraft.server.level.ServerLevel)player.level(), player, item -> {}));
            }
        }
    }
}

