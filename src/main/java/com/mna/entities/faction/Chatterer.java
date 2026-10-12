 
package com.mna.entities.faction;

import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ai.TargetEnemyFactionGoal;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.targeting.SpellTargetHelper;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.faction.HulkingZombie;
import com.mna.entities.faction.SkeletonAssassin;
import com.mna.entities.faction.base.BaseFlyingFactionMob;
import com.mna.factions.Factions;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import com.mna.api.tools.MNACap;

public class Chatterer
extends BaseFlyingFactionMob<Chatterer>
implements AnimationController.SoundKeyframeHandler<Chatterer> {
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(Chatterer.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final int CHATTER_ID = 1000;
    private static final int PARALYZE_DIST = 10;

    public Chatterer(EntityType<Chatterer> type, Level world) {
        super(type, world);
        this.randomFlyCooldown = 220;
    }

    public Chatterer(Level world) {
        this((EntityType<Chatterer>)((EntityType)EntityInit.CHATTERER.get()), world);
    }

    private void checkForParalysis() {
        if (this.level().isClientSide()) {
            return;
        }
        ArrayList players = new ArrayList();
        this.level().players().forEach(p -> {
            double dist = p.distanceToSqr((Entity)this);
            boolean isBlind = p.hasEffect(MobEffects.BLINDNESS);
            if (isBlind) {
                return;
            }
            if (dist < 100.0 && p.isAlive()) {
                if (this.getRaidTarget() == p) {
                    players.add(p);
                } else if (!p.isCreative() && !p.isSpectator()) {
                    MNACap.get(p, PlayerProgressionProvider.PROGRESSION).ifPresent(pr -> {
                        if (pr.getAlliedFaction() != Factions.UNDEAD) {
                            players.add(p);
                        }
                    });
                }
            }
        });
        for (Player p2 : players) {
            EntityHitResult hitResult = SpellTargetHelper.rayTraceEntities(this.level(), (Entity)p2, p2.getEyePosition(), p2.getEyePosition().add(p2.getForward().scale(10.0)), p2.getBoundingBox().inflate(10.0), e -> e == this);
            if (!p2.hasEffect(MobEffects.GLOWING) && (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY)) continue;
            this.setAction(Action.PARALYZED);
            this.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()), 60));
            this.setTimer("paralyze", 60, () -> {
                this.setAction(Action.IDLE);
                this.checkForParalysis();
            }, false);
            return;
        }
        if (this.getAction() == Action.PARALYZED) {
            this.setAction(Action.IDLE);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().getGameTime() % 10L == 0L) {
            this.checkForParalysis();
        }
        if (this.level().isClientSide() && this.getAction() != Action.PARALYZED) {
            RandomSource random = this.level().getRandom();
            Vec3 pos = this.position().add(this.getForward().scale(-0.25));
            for (int i = 0; i < 30; ++i) {
                int color = random.nextInt(50);
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get())).setColor(color, color, color).setMaxAge(10 + random.nextInt(10)).setScale(0.05f + (float)(random.nextGaussian() * 0.05)), pos.x - 0.1 + random.nextGaussian() * 0.2 + this.getDeltaMovement().x, pos.y - 0.35 + random.nextGaussian() * 0.2 + this.getDeltaMovement().y, pos.z - 0.1 + random.nextGaussian() * 0.2 + this.getDeltaMovement().z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, (Goal)new SummonReinforcements());
        this.goalSelector.addGoal(3, (Goal)new Chatter());
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_ID, -1);
    }

    
    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, (double)0.55f).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 0.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, -1.0);
    }

    public static boolean canSpawnPredicate(EntityType<Chatterer> type, ServerLevelAccessor worldIn, MobSpawnType reason, BlockPos pos, RandomSource randomIn) {
        return IFactionEnemy.canMnASpawn((LevelAccessor)worldIn, pos) && worldIn.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn((ServerLevelAccessor)worldIn, (BlockPos)pos, (RandomSource)randomIn) && pos.getY() < worldIn.getMinBuildHeight() + 64 && Chatterer.checkMobSpawnRules(type, (LevelAccessor)worldIn, (MobSpawnType)reason, (BlockPos)pos, (RandomSource)randomIn);
    }

    @Override
    public IFaction getFaction() {
        return Factions.UNDEAD;
    }

    @Override
    public CompoundTag getPacketData() {
        return new CompoundTag();
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
    }

    private Action getAction() {
        int ordinal = (this.getStateFlag() & 0x7F8) >> 3 & 0xFF;
        if (ordinal >= Action.values().length) {
            return Action.IDLE;
        }
        return Action.values()[ordinal];
    }

    private void setAction(Action action) {
        byte ordinal = (byte)((action.ordinal() & 0xFF) << 3);
        int flag = this.getStateFlag();
        flag &= 0xFFFFF807;
        this.setState(flag |= ordinal);
    }

    @Override
    protected float keepDistanceFromAttackTarget() {
        return 6.0f;
    }

    @Override
    protected void addControllerListeners(AnimationController<Chatterer> controller) {
        controller.transitionLength(2);
        controller.setSoundKeyframeHandler((AnimationController.SoundKeyframeHandler)this);
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends BaseFlyingFactionMob<?>> state) {
        RawAnimation builder = RawAnimation.begin();
        if (this.getAction() != Action.IDLE) {
            switch (this.getAction()) {
                case CHATTERING: {
                    builder.thenPlay("animation.chatterer.chatter");
                    break;
                }
                case DESTRUCTING: {
                    builder.thenPlayAndHold("animation.chatterer.destruct");
                    break;
                }
                case PARALYZED: {
                    builder.thenPlay("animation.chatterer.paralyzed");
                    break;
                }
            }
        }
        if (builder.getAnimationStages().size() == 0) {
            if (this.getDeltaMovement().length() > (double)0.1f) {
                builder.thenLoop("animation.chatterer.move");
            } else if (this.getTarget() == null || !this.getTarget().isAlive()) {
                builder.thenLoop("animation.chatterer.idle");
            } else {
                builder.thenLoop("animation.chatterer.idle_hostile");
            }
        }
        return state.setAndContinue(builder);
    }

    public void handle(SoundKeyframeEvent<Chatterer> event) {
        if (event.getKeyframeData().getSound().contains("click")) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Entity.Chatterer.CLACK, this.getSoundSource(), 1.0f, (float)(0.95 + Math.random() * 0.1), false);
        }
    }

    public static enum Action {
        IDLE,
        CHATTERING,
        PARALYZED,
        DESTRUCTING;

    }

    private class SummonReinforcements
    extends Goal {
        private SummonReinforcements() {
        }

        public boolean canUse() {
            if (Chatterer.this.getTarget() == null || Chatterer.this.getAction() != Action.IDLE) {
                return false;
            }
            LivingEntity target = Chatterer.this.getTarget();
            if (Chatterer.this.distanceTo((Entity)target) > 16.0f) {
                return false;
            }
            if (!target.hasEffect(MobEffects.BLINDNESS)) {
                return false;
            }
            if (Chatterer.this.getTier() > 0 && !target.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.FORTIFICATION.get()))) {
                return false;
            }
            if (Chatterer.this.getTier() == 1 && !target.hasEffect(MobEffects.POISON)) {
                return false;
            }
            return Chatterer.this.getTier() != 2 || target.hasEffect(MobEffects.WITHER);
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            Chatterer.this.setAction(Action.DESTRUCTING);
            if (Chatterer.this.level().isClientSide()) {
                return;
            }
            Chatterer.this.setTimer("destruct", 75, () -> {
                int i;
                int tier = Chatterer.this.getTier();
                int numSkeletons = 0;
                int numZombies = 0;
                ArrayList spawns = new ArrayList();
                spawns.add(new SkeletonAssassin(Chatterer.this.level()));
                if (tier == 0) {
                    numZombies = 1 + (int)(Math.random() * 2.0);
                } else if (tier == 1) {
                    numZombies = 1 + (int)(Math.random() * 2.0);
                    numSkeletons = 1 + (int)(Math.random() * 1.0);
                } else {
                    numZombies = 1 + (int)(Math.random() * 3.0);
                    numSkeletons = 1 + (int)(Math.random() * 2.0);
                    if (Math.random() < 0.5) {
                        spawns.add(new HulkingZombie(Chatterer.this.level()));
                    }
                }
                for (i = 0; i < numZombies; ++i) {
                    spawns.add(new Zombie(Chatterer.this.level()));
                }
                for (i = 0; i < numSkeletons; ++i) {
                    Skeleton skeleton = new Skeleton(EntityType.SKELETON, Chatterer.this.level());
                    skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)Items.BOW));
                    spawns.add(skeleton);
                }
                spawns.forEach(z -> {
                    z.setPos(Chatterer.this.position().add(-1.0 + Math.random() * 2.0, 0.0, -1.0 + Math.random() * 2.0));
                    z.setTarget(Chatterer.this.getTarget());
                    z.targetSelector.removeAllGoals(g -> true);
                    z.targetSelector.addGoal(0, (Goal)new TargetEnemyFactionGoal((Mob)z, LivingEntity.class, true, true, Factions.UNDEAD));
                    Chatterer.this.level().addFreshEntity((Entity)z);
                });
                Chatterer.this.discard();
            });
        }
    }

    private class Chatter
    extends Goal {
        private Chatter() {
        }

        public boolean canUse() {
            if (Chatterer.this.isOnCooldown(1000) || Chatterer.this.getTarget() == null || Chatterer.this.getAction() != Action.IDLE) {
                return false;
            }
            LivingEntity target = Chatterer.this.getTarget();
            if (Chatterer.this.distanceTo((Entity)target) > 8.0f) {
                return false;
            }
            if (!target.hasEffect(MobEffects.BLINDNESS)) {
                return true;
            }
            if (Chatterer.this.getTier() > 0 && !target.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.FORTIFICATION.get()))) {
                return true;
            }
            if (Chatterer.this.getTier() == 1 && !target.hasEffect(MobEffects.POISON)) {
                return true;
            }
            return Chatterer.this.getTier() == 2 && !target.hasEffect(MobEffects.WITHER);
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            Chatterer.this.setCooldown(1000, 60);
            Chatterer.this.setAction(Action.CHATTERING);
            LivingEntity target = Chatterer.this.getTarget();
            Chatterer.this.setTimer("chatter", 36, () -> {
                if (Chatterer.this.getAction() != Action.CHATTERING) {
                    return;
                }
                Chatterer.this.setAction(Action.IDLE);
                if (Chatterer.this.distanceTo((Entity)target) > 8.0f) {
                    return;
                }
                if (!target.hasEffect(MobEffects.BLINDNESS)) {
                    Chatterer.this.setCooldown(1000, 80);
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0));
                    return;
                }
                if (Chatterer.this.getTier() > 0 && !target.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.FORTIFICATION.get()))) {
                    Chatterer.this.setCooldown(1000, 100);
                    target.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.FORTIFICATION.get()), 200, 0));
                    return;
                }
                if (Chatterer.this.getTier() == 1 && !target.hasEffect(MobEffects.POISON)) {
                    Chatterer.this.setCooldown(1000, 100);
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                    return;
                }
                if (Chatterer.this.getTier() == 2 && !target.hasEffect(MobEffects.WITHER)) {
                    Chatterer.this.setCooldown(1000, 100);
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                    return;
                }
            });
        }
    }
}

