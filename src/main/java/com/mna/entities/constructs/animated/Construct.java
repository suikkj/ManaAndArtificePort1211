 
package com.mna.entities.constructs.animated;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.construct.Animations;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.FluidParameterRegistry;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructFleeTarget;
import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.events.construct.ConstructSprayEffectEvent;
import com.mna.api.events.construct.ConstructSprayTargetingEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.items.DynamicItemFilter;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.IAnimPacketSync;
import com.mna.entities.ai.LerpLeap;
import com.mna.entities.attributes.AttributeInit;
import com.mna.entities.constructs.ai.ConstructCollectItems;
import com.mna.entities.constructs.ai.ConstructCommandFollowAndGuard;
import com.mna.entities.constructs.ai.ConstructCommandFollowLodestar;
import com.mna.entities.constructs.ai.ConstructCommandReturnToTable;
import com.mna.entities.constructs.ai.ConstructDuel;
import com.mna.entities.constructs.ai.ConstructMove;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.animated.ConstructConstruction;
import com.mna.entities.constructs.animated.ConstructDiagnostics;
import com.mna.entities.constructs.animated.ConstructMoodlets;
import com.mna.entities.constructs.movement.ConstructMoveControl;
import com.mna.entities.constructs.movement.ConstructPathNavigator;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.items.ItemInit;
import com.mna.items.constructs.parts.arms.ConstructPartAxeArmLeft;
import com.mna.items.constructs.parts.arms.ConstructPartAxeArmRight;
import com.mna.items.constructs.parts.arms.ConstructPartBladeArmLeft;
import com.mna.items.constructs.parts.arms.ConstructPartBladeArmRight;
import com.mna.items.constructs.parts.arms.ConstructPartFluidNozzleLeft;
import com.mna.items.constructs.parts.arms.ConstructPartFluidNozzleRight;
import com.mna.items.constructs.parts.arms.ConstructPartHammerArmLeft;
import com.mna.items.constructs.parts.arms.ConstructPartHammerArmRight;
import com.mna.items.constructs.parts.arms.ConstructPartManaCannonLeft;
import com.mna.items.constructs.parts.arms.ConstructPartManaCannonRight;
import com.mna.items.constructs.parts.head.ConstructPartHornHead;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_client.SpawnParticleEffectMessage;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.spells.SpellCaster;
import com.mna.spells.components.ComponentFling;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.EntityUtil;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNAItemData;

public class Construct
extends AbstractGolem
implements IAnimPacketSync<Construct>,
GeoEntity,
AnimationController.CustomKeyframeHandler<Construct>,
PlayerRideableJumping,
IFluidHandler,
IConstruct<Construct>,
IItemHandlerModifiable {
    private static final String NBT_OWNER = "owner";
    private static final float PART_DROP_CHANCE = 0.85f;
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> MANA = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_MANA = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BUOYANCY = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> STEERABLE = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> FLUIDTYPE = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FLUIDAMT = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLUID_CAP = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AWAY_TYPE = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> FISHING_POS = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> FORCED_ANIMATION = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FORCED_ANIMATION_IDLE_ONLY = SynchedEntityData.defineId(Construct.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final int COOLDOWN_HAMMER_LEAP = 0;
    private static final int COOLDOWN_AXE_LEAP = 0;
    private static final int COOLDOWN_DUAL_SHOT = 0;
    public static final int AWAY_TYPE_NONE = 0;
    public static final int AWAY_TYPE_MINING = 1;
    public static final int AWAY_TYPE_ADVENTURING = 2;
    public static final int AWAY_TYPE_HUNTING = 3;
    public static final float REQUIRED_INTELLIGENCE_FOR_PLAYER_LOOT = 36.0f;
    public static final float REQUIRED_PERCEPTION_FOR_PLAYER_LOOT = 28.0f;
    IConstructConstruction constructCapabilities;
    ConstructDiagnostics constructDiagnostics;
    private boolean needsSync = true;
    private boolean requestingDiagnostics = false;
    private Player cached_owner;
    protected AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    protected boolean constructJumping;
    private ConstructMoodlets moodlets;
    private NonNullList<ItemStack> inventory;
    private final Map<Integer, Integer> cooldowns = Maps.newHashMap();
    private String cached_team_name;
    private IFaction lastKnownOwnerFaction;
    private int lastKnownOwnerTier;
    private ConstructAITask<?> current;
    private Animations forcedAnimation;
    private boolean forcedAnimationIdleOnly;
    private static final float MANA_PER_RANGED_ATTACK = 50.0f;
    private static final int FLUID_PER_RANGED_ATTACK = 20;
    private static final float FLUID_SPRAY_RANGE = 10.0f;
    private int chargeCounter = 0;
    private int attackDelay = 0;
    protected float jumpPower;
    private int hornDelay = 0;
    private int teleportCooldown = 0;
    private double lastYd;
    private Fluid __cachedFluid = Fluids.EMPTY;
    private boolean clearAnimCache;
    private float defeatedHealCount = 0.0f;
    private boolean isRegenerateHealing = false;
    private boolean forceEndDefeated = false;
    public final float yaw_rnd;
    public final float pitch_rnd;
    public final float roll_rnd;
    private boolean dancing = false;
    private int danceTicks = 0;
    private int danceIndex = 0;
    private int defeatedCounter = 0;
    private ISpellDefinition currentSpell;
    private boolean ANIM_PACKET = false;
    private int actionFlags = 0;
    private boolean adventure_leave_pfx = false;
    private boolean adventure_return_pfx = false;
    private boolean mine_leave_pfx = false;
    private boolean mine_return_pfx = false;
    private int pfxCounter = 0;
    private boolean redirectingDamage = false;

    public Construct(EntityType<? extends AbstractGolem> type, Level worldIn) {
        super(type, worldIn);
        this.constructCapabilities = new ConstructConstruction();
        this.constructDiagnostics = new ConstructDiagnostics();
        this.moodlets = new ConstructMoodlets();
        this.inventory = NonNullList.createWithCapacity((int)0);
        this.moveControl = new ConstructMoveControl(this, 85, 10, 1.0f, 1.0f, true);
        this.yaw_rnd = worldIn.random.nextFloat() * 360.0f;
        this.pitch_rnd = -2.0f + worldIn.random.nextFloat() * 4.0f;
        this.roll_rnd = -2.0f + worldIn.random.nextFloat() * 4.0f;
    }

    public Construct(Level worldIn) {
        this((EntityType<? extends AbstractGolem>)((EntityType)EntityInit.ANIMATED_CONSTRUCT.get()), worldIn);
    }

    private void recalculateAll() {
        if (this.constructCapabilities != null) {
            int i;
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.constructCapabilities.calculateMaxHealth());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)this.constructCapabilities.calculateSpeed());
            this.getAttribute(Attributes.FLYING_SPEED).setBaseValue(this.canFly() ? 1.0 : (double)this.constructCapabilities.calculateSpeed());
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)this.constructCapabilities.calculateDamage());
            this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue((double)this.constructCapabilities.calculateAttackRate());
            this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue((double)this.constructCapabilities.calculateKnockback());
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue((double)this.constructCapabilities.calculateKnockbackResistance());
            this.getAttribute(Attributes.ARMOR).setBaseValue((double)this.constructCapabilities.calculateArmor());
            this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue((double)this.constructCapabilities.calculateToughness());
            this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(32.0);
            this.getAttribute(AttributeInit.PERCEPTION_DISTANCE).setBaseValue((double)this.constructCapabilities.calculatePerception());
            this.getAttribute(AttributeInit.INTELLIGENCE).setBaseValue((double)this.constructCapabilities.calculateIntelligence());
            this.getAttribute(AttributeInit.RANGED_DAMAGE).setBaseValue((double)this.constructCapabilities.calculateRangedDamage());
            this.entityData.set(MAX_MANA, Float.valueOf(this.constructCapabilities.calculateMana()));
            this.entityData.set(BUOYANCY, Float.valueOf(this.constructCapabilities.calculateBuoyancy()));
            this.entityData.set(FLUID_CAP, this.constructCapabilities.calculateFluidCapacity());
            this.navigation.setCanFloat(this.canSwim());
            if (this.navigation instanceof ConstructPathNavigator) {
                ((ConstructPathNavigator)this.navigation).setEvaluatorFlags(this.canFly(), this.canSwim());
                if (!this.canSwim()) {
                    this.setPathfindingMalus(PathType.WATER, -1.0f);
                    this.setPathfindingMalus(PathType.WATER_BORDER, -1.0f);
                }
                this.setPathfindingMalus(PathType.LEAVES, 0.0f);
            }
            int inventorySize = this.constructCapabilities.calculateInventorySize();
            NonNullList newInv = NonNullList.createWithCapacity((int)inventorySize);
            for (i = 0; i < inventorySize; ++i) {
                newInv.add(i, ItemStack.EMPTY);
            }
            for (i = 0; i < Math.min(this.inventory.size(), newInv.size()); ++i) {
                newInv.set(i, ((ItemStack)this.inventory.get(i)).copy());
            }
            this.inventory = newInv;
            if (this.canSwim()) {
                this.getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue((double)this.constructCapabilities.calculateSpeed());
            }
            this.registerHammerLeap();
            this.registerAxeLeap();
        }
    }

    public void setConstructParts(@Nonnull ItemStack head, @Nonnull ItemStack torso, @Nonnull ItemStack left_arm, @Nonnull ItemStack right_arm, @Nonnull ItemStack legs) {
        this.constructCapabilities.setPart(head, false);
        this.constructCapabilities.setPart(torso, false);
        this.constructCapabilities.setPart(left_arm, false);
        this.constructCapabilities.setPart(right_arm, false);
        this.constructCapabilities.setPart(legs, true);
        this.recalculateAll();
    }

    public void setConstructParts(IConstructConstruction construction) {
        this.constructCapabilities = construction.copy();
        this.recalculateAll();
    }

    protected PathNavigation createNavigation(Level world) {
        return new ConstructPathNavigator(this, world);
    }

    public void tick() {
        if (this.noPhysics) {
            this.setDeltaMovement(0.0, 0.0, 0.0);
        }
        this.tickCooldowns();
        this.isRegenerateHealing = true;
        super.tick();
        this.isRegenerateHealing = false;
        this.updateSwingTime();
        this.tickDefeated();
        this.tickDanceMix();
        this.tickBonusRegeneration();
        this.tickMoodlets();
        this.tickClientParticles();
        this.tickSyncUpdates();
    }

    private void tickDefeated() {
        if (this.isDefeated()) {
            --this.defeatedCounter;
            if (this.level().getGameTime() % 5L == 0L) {
                this.regenerate(1.0f);
                if (!this.forceEndDefeated) {
                    LivingEntity owner = this.getOwner() == null ? this : this.getOwner();
                    List<Mob> nearbyHostiles = this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16.0), arg_0 -> this.lambda$tickDefeated$0((LivingEntity)owner, arg_0));
                    if (nearbyHostiles.size() > 0) {
                        this.defeatedCounter += 5;
                    }
                }
            }
            if (this.defeatedCounter == 15) {
                this.actionFlags = ActionFlags.DEFEATED.clear(this.actionFlags);
                this.actionFlags = ActionFlags.DEFEATED_END.set(this.actionFlags);
                if (!this.level().isClientSide()) {
                    this.ANIM_PACKET = true;
                    ServerMessageDispatcher.sendEntityStateMessage(this);
                    this.ANIM_PACKET = false;
                }
            } else if (this.defeatedCounter == 1) {
                this.getMoodlets().clearMoodlet(4);
                this.setNoAi(false);
                this.defeatedCounter = 0;
                this.forceEndDefeated = false;
                this.resetActions();
                return;
            }
        }
    }

    private void tickDanceMix() {
        if (!ManaAndArtifice.instance.proxy.checkConstructDanceMixPlaying()) {
            if (this.dancing) {
                this.dancing = false;
                this.danceTicks = 0;
                this.getMoodlets().clearMoodlet(8);
            }
        } else {
            this.dancing = true;
            this.setHappy(10000);
            ++this.danceTicks;
            if (this.danceTicks % 200 == 0) {
                this.danceIndex = (int)(Math.random() * 3.0);
            }
        }
    }

    private void tickBonusRegeneration() {
        if (this.tickCount % 40 == 0) {
            int numWickerParts;
            int totalParts;
            int numWoodParts = this.constructCapabilities.getPartsForMaterial(ConstructMaterial.WOOD).size();
            int numBoneParts = this.constructCapabilities.getPartsForMaterial(ConstructMaterial.BONE).size();
            this.regenerate(1.0f + 0.5f * (float)(numWoodParts + numBoneParts));
            if (!this.level().isClientSide() && this.level().dimensionType().ultraWarm() && (totalParts = numWoodParts + (numWickerParts = this.constructCapabilities.getPartsForMaterial(ConstructMaterial.WICKERWOOD).size())) > 1) {
                this.igniteForSeconds((float)(totalParts * 5));
            }
        }
    }

    private void tickClientParticles() {
        if (this.level().isClientSide()) {
            if (this.mine_leave_pfx) {
                this.spawnMineLeaveParticles();
            } else if (this.mine_return_pfx) {
                this.spawnMineReturnParticles();
            } else if (this.adventure_leave_pfx) {
                this.spawnAdventureLeaveParticles();
            } else if (this.adventure_return_pfx) {
                this.spawnAdventureReturnParticles();
            }
            this.spawnSmoothParticles(ManaAndArtifice.instance.proxy.getPartialTick());
            if (!ActionFlags.MINE_ENTER.isFlagSet(this.actionFlags)) {
                this.mine_leave_pfx = false;
            }
            if (!ActionFlags.MINE_LEAVE.isFlagSet(this.actionFlags)) {
                this.mine_return_pfx = false;
            }
            if (!ActionFlags.ADVENTURE_ENTER.isFlagSet(this.actionFlags)) {
                this.adventure_leave_pfx = false;
            }
            if (!ActionFlags.ADVENTURE_LEAVE.isFlagSet(this.actionFlags)) {
                this.adventure_return_pfx = false;
            }
            if (ActionFlags.EAT.isFlagSet(this.actionFlags) && ManaAndArtifice.instance.proxy.getGameTicks() % 5L == 0L) {
                this.level().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, this.getSoundSource(), 1.0f, 1.0f);
            }
            if (this.canFly() && !this.onGround() && !this.isAway() && !ActionFlags.ADVENTURE_LEAVE.isFlagSet(this.actionFlags)) {
                this.spawnRocketParticles();
            }
        }
    }

    private void tickMoodlets() {
        this.moodlets.tick();
        Player owner = this.getOwner();
        if (this.getHealth() < this.getMaxHealth() * 0.25f || owner != null && this.getSensing().hasLineOfSight((Entity)owner) && owner.getHealth() < owner.getMaxHealth() * 0.25f) {
            this.setConcerned(250);
        } else {
            this.getMoodlets().clearMoodlet(2);
        }
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }
    }

    private void tickSyncUpdates() {
        if (this.needsSync) {
            this.needsSync = false;
            if (!this.level().isClientSide()) {
                ServerMessageDispatcher.sendEntityStateMessage(this);
            } else {
                ClientMessageDispatcher.sendAnimatedConstructSyncRequestMessage(this, false);
            }
        }
        if (this.level().isClientSide() && this.requestingDiagnostics && (this.tickCount % 20 == 0 || this.getDiagnostics().needsUpdate())) {
            ClientMessageDispatcher.sendAnimatedConstructSyncRequestMessage(this, true);
        }
    }

    private void tickCooldowns() {
        this.cooldowns.keySet().forEach(c -> {
            int cd = this.cooldowns.get(c);
            if (cd > 0) {
                this.cooldowns.put((Integer)c, cd - 1);
            }
        });
        if (this.hornDelay > 0) {
            --this.hornDelay;
        }
        if (this.attackDelay > 0) {
            --this.attackDelay;
        }
        if (this.teleportCooldown > 0) {
            --this.teleportCooldown;
        }
    }

    protected void customServerAiStep() {
        if (!this.level().isClientSide() && this.getCurrentCommand() != null && this.getCurrentCommand().isFinished() && this.getCurrentCommand().isOneOff()) {
            ConstructCommandFollowAndGuard cmd = (ConstructCommandFollowAndGuard)ConstructTasks.FOLLOW_DEFEND.instantiateTask(this);
            UUID follow = this.getCurrentCommand().getOneOffFollowTarget();
            Player followTarget = follow != null ? this.level().getPlayerByUUID(follow) : this.getOwner();
            cmd.setFollowTarget((LivingEntity)followTarget);
            this.setCurrentCommand(followTarget, cmd, true);
        }
        super.customServerAiStep();
    }

    public void setRecordPlayingNearby(BlockPos position, boolean playing) {
        if (playing && !this.dancing) {
            this.dancing = true;
            this.danceTicks = 0;
            this.setHappy(10000);
        } else if (!playing) {
            this.dancing = false;
            this.getMoodlets().clearMoodlet(8);
        }
    }

    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.canRiderInteract()) {
                if (this.isInWater() && (double)this.getWaterLevelAbove() - this.getY() > (double)0.2f) {
                    if (this.isUnderWater()) {
                        if (this.getDeltaMovement().y() < 0.0) {
                            this.setDeltaMovement(this.getDeltaMovement().add(0.0, (double)(0.01f * this.getBuoyancy()), 0.0));
                        }
                    } else {
                        double depth = this.getFluidTypeHeight((NeoForgeMod.WATER_TYPE).value());
                        float yCap = depth > 0.25 || this.horizontalCollision ? 0.05f : 0.001f;
                        Vec3 newDelta = this.getDeltaMovement().add(0.0, 0.01 * (double)this.getBuoyancy(), 0.0);
                        if (newDelta.y > (double)yCap) {
                            newDelta = new Vec3(newDelta.x, (double)yCap, newDelta.z);
                        }
                        this.setDeltaMovement(newDelta);
                    }
                    this.lastYd = 0.0;
                }
                LivingEntity livingentity = this.getControllingPassenger();
                this.setYRot(livingentity.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(livingentity.getXRot() * 0.5f);
                this.setRot(this.getYRot(), this.getXRot());
                this.yHeadRot = this.yBodyRot = this.getYRot();
                float f = livingentity.xxa * 0.5f;
                float f1 = livingentity.zza;
                if (f1 <= 0.0f) {
                    f1 *= 0.25f;
                }
                if (this.jumpPower > 0.0f && !this.isJumping() && this.onGround()) {
                    double d0 = this.getJumpStrength() * (double)this.jumpPower * (double)this.getBlockJumpFactor();
                    double d1 = this.hasEffect(MobEffects.JUMP) ? d0 + (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1f) : d0;
                    float horizontalScale = 1.8f;
                    Vec3 vector3d = this.getDeltaMovement();
                    this.setDeltaMovement(vector3d.x * (double)horizontalScale, d1, vector3d.z * (double)horizontalScale);
                    this.setJumping(true);
                    this.hasImpulse = true;
                    CommonHooks.onLivingJump((LivingEntity)this);
                    if (f1 > 0.0f) {
                        float f2 = Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180)));
                        float f3 = Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180)));
                        this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4f * f2 * this.jumpPower), 0.0, (double)(0.4f * f3 * this.jumpPower)));
                    }
                    this.jumpPower = 0.0f;
                }
                if (this.isControlledByLocalInstance()) {
                    if (!this.onGround()) {
                        this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.05f);
                    } else {
                        this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.35f);
                    }
                    super.travel(new Vec3((double)f, travelVector.y, (double)f1));
                } else if (livingentity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }
                if (this.onGround()) {
                    this.jumpPower = 0.0f;
                    this.setJumping(false);
                }
                this.calculateEntityAnimation(false);
            } else {
                this.jumpPower = 0.02f;
                super.travel(travelVector);
            }
        }
    }

    protected float getFlyingSpeed() {
        return this.getControllingPassenger() instanceof Player ? this.getSpeed() * 0.1f : 0.04f;
    }

    protected void positionRider(Entity pPassenger, Entity.MoveFunction pCallback) {
        this.setRiderPosition(pPassenger, pCallback);
    }

    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        this.lastYd = this.getDeltaMovement().y;
        super.checkFallDamage(y, onGroundIn, state, pos);
    }

    public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 deltaMovement, float pFriction) {
        float frictionInfluencedSpeed = this.onGround() ? this.getSpeed() * (0.21600002f / (pFriction * pFriction * pFriction)) : this.getFlyingSpeed();
        this.moveRelative(frictionInfluencedSpeed, deltaMovement);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 vec3 = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.getBlockStateOn().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow((Entity)this))) {
            vec3 = new Vec3(vec3.x, 0.2, vec3.z);
        }
        return vec3;
    }

    private Vec3 handleOnClimbable(Vec3 p_21298_) {
        if (this.onClimbable()) {
            this.resetFallDistance();
            double d0 = Mth.clamp((double)p_21298_.x, (double)-0.15f, (double)0.15f);
            double d1 = Mth.clamp((double)p_21298_.z, (double)-0.15f, (double)0.15f);
            double d2 = Math.max(p_21298_.y, (double)-0.15f);
            p_21298_ = new Vec3(d0, d2, d1);
        }
        return p_21298_;
    }

    public boolean isColliding(BlockPos pPos, BlockState pState) {
        if (pState.is(BlockTags.LEAVES)) {
            return false;
        }
        return super.isColliding(pPos, pState);
    }

    public float getStepHeight() {
        return 1.5f;
    }

    public boolean isAway() {
        return ActionFlags.AWAY.isFlagSet(this.actionFlags);
    }

    public int getAwayType() {
        return (Integer)this.entityData.get(AWAY_TYPE);
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public boolean canCollideWith(Entity p_20303_) {
        if (this.isAway()) {
            return false;
        }
        if (p_20303_ instanceof Player) {
            if (this.playerCanCommand((Player)p_20303_)) {
                return false;
            }
            return this.current instanceof ConstructCommandFollowAndGuard;
        }
        return false;
    }

    public void push(Entity pEntity) {
        if (this.isAway()) {
            return;
        }
        super.push(pEntity);
    }

    public void push(double pX, double pY, double pZ) {
        if (this.isAway()) {
            return;
        }
        super.push(pX, pY, pZ);
    }

    public float getWaterSlowDown() {
        return this.canSwim() ? 0.98f : super.getWaterSlowDown();
    }

    public boolean isAffectedByFluids() {
        if (((Float)this.entityData.get(BUOYANCY)).floatValue() < 0.0f) {
            return false;
        }
        return super.isAffectedByFluids();
    }

    public void jumpInFluid(FluidType type) {
        if (this.canFly() && this.isInWater()) {
            this.jumpFromGround();
        }
        super.jumpInFluid(type);
    }

    protected float getJumpPower() {
        if (this.canFly() && this.isInWater()) {
            return 0.12f * this.getBlockJumpFactor();
        }
        return 0.42f * this.getBlockJumpFactor();
    }

    protected boolean canAddPassenger(Entity entityIn) {
        if (this.isAway() || this.isDefeated() || ActionFlags.MINE_ENTER.isFlagSet(this.actionFlags) || ActionFlags.MINE_LEAVE.isFlagSet(this.actionFlags) || ActionFlags.ADVENTURE_ENTER.isFlagSet(this.actionFlags) || ActionFlags.ADVENTURE_LEAVE.isFlagSet(this.actionFlags)) {
            return false;
        }
        if (this.getPassengers().size() != 0) {
            return false;
        }
        if (entityIn instanceof Player) {
            return this.getConstructData().isCapabilityEnabled(ConstructCapability.CARRY_PLAYER) && this.playerCanCommand((Player)entityIn);
        }
        return false;
    }

    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 p_30562_, LivingEntity p_30563_) {
        double d0 = this.getX() + p_30562_.x;
        double d1 = this.getBoundingBox().minY;
        double d2 = this.getZ() + p_30562_.z;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        block0: for (Pose pose : p_30563_.getDismountPoses()) {
            blockpos$mutableblockpos.set(d0, d1, d2);
            double d3 = this.getBoundingBox().maxY + 0.75;
            do {
                double d4 = this.level().getBlockFloorHeight((BlockPos)blockpos$mutableblockpos);
                if ((double)blockpos$mutableblockpos.getY() + d4 > d3) continue block0;
                if (DismountHelper.isBlockFloorValid((double)d4)) {
                    AABB aabb = p_30563_.getLocalBoundsForPose(pose);
                    Vec3 vec3 = new Vec3(d0, (double)blockpos$mutableblockpos.getY() + d4, d2);
                    if (DismountHelper.canDismountTo((CollisionGetter)this.level(), (LivingEntity)p_30563_, (AABB)aabb.move(vec3))) {
                        p_30563_.setPose(pose);
                        return vec3;
                    }
                }
                blockpos$mutableblockpos.move(Direction.UP);
            } while ((double)blockpos$mutableblockpos.getY() < d3);
        }
        return null;
    }

    public Vec3 getDismountLocationForPassenger(LivingEntity p_30576_) {
        Vec3 vec3 = Construct.getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)p_30576_.getBbWidth(), (float)(this.getYRot() + (p_30576_.getMainArm() == HumanoidArm.RIGHT ? 90.0f : -90.0f)));
        Vec3 vec31 = this.getDismountLocationInDirection(vec3, p_30576_);
        if (vec31 != null) {
            return vec31;
        }
        Vec3 vec32 = Construct.getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)p_30576_.getBbWidth(), (float)(this.getYRot() + (p_30576_.getMainArm() == HumanoidArm.LEFT ? 90.0f : -90.0f)));
        Vec3 vec33 = this.getDismountLocationInDirection(vec32, p_30576_);
        return vec33 != null ? vec33 : this.position();
    }

    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
        return ((Float)this.entityData.get(BUOYANCY)).floatValue() >= 1.0f;
    }

    public boolean canRiderInteract() {
        return (Boolean)this.entityData.get(STEERABLE);
    }

    public boolean isPushable() {
        if (this.isAway()) {
            return false;
        }
        return !this.isVehicle();
    }

    public boolean isPushedByFluid() {
        if (this.isAway()) {
            return false;
        }
        return this.getConstructData().getAffinityScore(Affinity.WATER) < 4;
    }

    protected boolean isImmobile() {
        if (this.isAway()) {
            return false;
        }
        return super.isImmobile() && this.isVehicle();
    }

    public boolean canJump() {
        return true;
    }

    public double getPassengersRidingOffset() {
        float actualScale;
        double output = 0.75;
        if (this.canSwim()) {
            BlockPos bp = this.blockPosition();
            FluidState flState = this.level().getFluidState(bp);
            FluidState flStateBelow = this.level().getFluidState(bp.below());
            if ((double)flState.getOwnHeight() > 0.8 || (double)flStateBelow.getOwnHeight() > 0.8) {
                return -0.5;
            }
        }
        if ((actualScale = this.getPersistentData().getFloat("mna_entity_scale")) < 1.0f) {
            return output - (double)(0.21f / actualScale);
        }
        return output + (double)(actualScale - 1.0f);
    }

    public boolean isJumping() {
        return this.constructJumping;
    }

    public double getJumpStrength() {
        return 1.25;
    }

    @Override
    @Nullable
    public Player getOwner() {
        IPlayerProgression progression;
        if (this.entityData.get(OWNER_UUID) == null || !((Optional)this.entityData.get(OWNER_UUID)).isPresent()) {
            return null;
        }
        if (this.cached_owner == null || !this.cached_owner.isAlive()) {
            this.cached_owner = this.level().getPlayerByUUID((UUID)((Optional)this.entityData.get(OWNER_UUID)).get());
        }
        if (this.cached_owner != null && (progression = (IPlayerProgression)this.cached_owner.getData(PlayerProgressionProvider.PROGRESSION.get())) != null) {
            this.lastKnownOwnerTier = progression.getTier();
            this.lastKnownOwnerFaction = progression.getAlliedFaction() != null ? progression.getAlliedFaction() : null;
        }
        return this.cached_owner;
    }

    @Override
    @Nullable
    public UUID getOwnerId() {
        if (this.entityData.get(OWNER_UUID) == null || !((Optional)this.entityData.get(OWNER_UUID)).isPresent()) {
            return null;
        }
        return (UUID)((Optional)this.entityData.get(OWNER_UUID)).get();
    }

    public String getScoreboardName() {
        Player owner;
        if (this.cached_team_name == null) {
            this.cached_team_name = super.getScoreboardName();
        }
        if ((owner = this.getOwner()) != null) {
            this.cached_team_name = owner.getScoreboardName();
        }
        return this.cached_team_name;
    }

    @Override
    public ConstructAITask<?> getCurrentCommand() {
        return this.current;
    }

    @Override
    public int getCarrySize() {
        return Math.max(1, 2 * this.constructCapabilities.getAffinityScore(Affinity.ENDER));
    }

    @Override
    public int getIntelligence() {
        return (int)this.getAttribute(AttributeInit.INTELLIGENCE).getValue();
    }

    @Override
    public InteractionHand[] getEmptyHands() {
        ArrayList emptyHands = new ArrayList();
        this.constructCapabilities.getPart(ConstructSlot.LEFT_ARM).ifPresent(p -> {
            if (p.getModelTypeMutex() == 1 && this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                emptyHands.add(InteractionHand.MAIN_HAND);
            }
        });
        this.constructCapabilities.getPart(ConstructSlot.RIGHT_ARM).ifPresent(p -> {
            if (p.getModelTypeMutex() == 1 && this.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                emptyHands.add(InteractionHand.OFF_HAND);
            }
        });
        return (InteractionHand[])((emptyHands).toArray(new InteractionHand[0]));
    }

    @Override
    public InteractionHand[] getCarryingHands() {
        return this.getCarryingHands((Predicate<ItemStack>)null);
    }

    @Override
    public InteractionHand[] getCarryingHands(Predicate<ItemStack> filter) {
        ArrayList carryingHands = new ArrayList();
        this.constructCapabilities.getPart(ConstructSlot.LEFT_ARM).ifPresent(p -> {
            ItemStack left;
            if (p.getModelTypeMutex() == 1 && !(left = this.getItemInHand(InteractionHand.MAIN_HAND)).isEmpty() && (filter == null || filter.test(left))) {
                carryingHands.add(InteractionHand.MAIN_HAND);
            }
        });
        this.constructCapabilities.getPart(ConstructSlot.RIGHT_ARM).ifPresent(p -> {
            ItemStack right;
            if (p.getModelTypeMutex() == 1 && !(right = this.getItemInHand(InteractionHand.OFF_HAND)).isEmpty() && (filter == null || filter.test(right))) {
                carryingHands.add(InteractionHand.OFF_HAND);
            }
        });
        return (InteractionHand[])((carryingHands).toArray(new InteractionHand[0]));
    }

    @Override
    public InteractionHand[] getCarryingHands(DynamicItemFilter filter) {
        return this.getCarryingHands((ItemStack is) -> filter.matches((ItemStack)is));
    }

    @Override
    public boolean hasItem(DynamicItemFilter _filter) {
        InteractionHand[] hands = this.getCarryingHands((ItemStack is) -> _filter.matches((ItemStack)is));
        if (hands.length > 0) {
            return true;
        }
        return InventoryUtilities.hasStackInInventory(_filter, (IItemHandler)this);
    }

    @Override
    public boolean hasItem(ItemStack stack, boolean checkDurability, boolean compareNBT) {
        InteractionHand[] hands = this.getCarryingHands((ItemStack is) -> {
            if (checkDurability && is.getDamageValue() != stack.getDamageValue()) {
                return false;
            }
            if (compareNBT && !ManaAndArtificeMod.getItemHelper().AreTagsEqual((ItemStack)is, stack)) {
                return false;
            }
            return is.getItem() == stack.getItem();
        });
        if (hands.length > 0) {
            return true;
        }
        return InventoryUtilities.hasStackInInventory(stack, checkDurability, compareNBT, (IItemHandler)this);
    }

    @Override
    public boolean hasItem(ResourceLocation tag, int quantity) {
        List<Item> items = MATags.getItemTagContents(tag);
        if (items.size() == 0) {
            return false;
        }
        int count = 0;
        ItemStack stack = this.getItemInHand(InteractionHand.MAIN_HAND);
        if (items.contains(stack.getItem())) {
            count += stack.getCount();
        }
        if (items.contains((stack = this.getItemInHand(InteractionHand.OFF_HAND)).getItem())) {
            count += stack.getCount();
        }
        if (count >= quantity) {
            return true;
        }
        if (!this.constructCapabilities.areCapabilitiesEnabled(ConstructCapability.ITEM_STORAGE)) {
            return false;
        }
        for (int i = 0; i < this.getSlots(); ++i) {
            stack = this.getStackInSlot(i);
            if (!items.contains(stack.getItem()) || (count += stack.getCount()) < quantity) continue;
            return true;
        }
        return false;
    }

    public boolean mnaCanBreatheUnderwater() {
        return true;
    }

    public boolean isBlocking() {
        return this.getConstructData().isCapabilityEnabled(ConstructCapability.BLOCK) && !this.swinging;
    }

    public void onShieldBlock() {
        this.attackDelay -= 20;
    }

    @Override
    public IConstructConstruction getConstructData() {
        return this.constructCapabilities;
    }

    public boolean canSwim() {
        return ((Float)this.entityData.get(BUOYANCY)).floatValue() >= 1.0f;
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag nbt = new CompoundTag();
        if (this.ANIM_PACKET) {
            nbt.putByte("anim", (byte)1);
            if (this.swingingArm != null) {
                nbt.putByte("hand", (byte)this.swingingArm.ordinal());
            }
            nbt.putInt("actionFlags", this.actionFlags);
            nbt.putInt("defeatedCounter", this.defeatedCounter);
        } else if (!this.requestingDiagnostics) {
            this.constructCapabilities.WriteNBT(nbt);
        } else {
            if (this.constructDiagnostics.needsUpdate()) {
                nbt.put("diag", (Tag)this.constructDiagnostics.writeToNBT());
            }
            nbt.put("inv", (Tag)MNAItemData.saveItems((CompoundTag)new CompoundTag(), this.inventory));
            nbt.put("hat", (Tag)MNAItemData.save(this.constructCapabilities.getHat(), new CompoundTag()));
            nbt.put("banner", (Tag)MNAItemData.save(this.constructCapabilities.getBanner(), new CompoundTag()));
        }
        this.moodlets.writeToNBT(nbt);
        return nbt;
    }

    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @Override
    public ConstructDiagnostics getDiagnostics() {
        return this.constructDiagnostics;
    }

    @Override
    public Optional<InteractionHand> getHandWithCapability(ConstructCapability capability) {
        ArrayList hands = new ArrayList();
        this.constructCapabilities.getPart(ConstructSlot.LEFT_ARM).ifPresent(p -> {
            if (Arrays.asList(p.getEnabledCapabilities()).contains(capability)) {
                hands.add(InteractionHand.MAIN_HAND);
            }
        });
        this.constructCapabilities.getPart(ConstructSlot.RIGHT_ARM).ifPresent(p -> {
            if (Arrays.asList(p.getEnabledCapabilities()).contains(capability)) {
                hands.add(InteractionHand.OFF_HAND);
            }
        });
        if (hands.size() > 0) {
            Random r = new Random();
            InteractionHand h = (InteractionHand)hands.get(r.nextInt(hands.size()));
            return Optional.of(h);
        }
        return Optional.empty();
    }

    public boolean handHasCapability(InteractionHand hand, ConstructCapability capability) {
        Optional<ItemConstructPart> part = Optional.empty();
        part = hand == InteractionHand.MAIN_HAND ? this.constructCapabilities.getPart(ConstructSlot.LEFT_ARM) : this.constructCapabilities.getPart(ConstructSlot.RIGHT_ARM);
        if (!part.isPresent()) {
            return false;
        }
        return Arrays.asList(((ItemConstructPart)part.get()).getEnabledCapabilities()).contains(capability);
    }

    @Override
    public boolean canManaCannonAttack() {
        return this.getMana() >= 50.0f && this.getConstructData().areCapabilitiesEnabled(ConstructCapability.RANGED_ATTACK, ConstructCapability.STORE_MANA) && this.getTarget() != null;
    }

    @Override
    public boolean canFluidSpray() {
        boolean baseline;
        boolean bl = baseline = this.getStoredFluidAmount() >= 20 && this.getConstructData().areCapabilitiesEnabled(ConstructCapability.FLUID_STORE, ConstructCapability.FLUID_DISPENSE);
        return baseline && FluidParameterRegistry.INSTANCE.forFluid(this.__cachedFluid).checkPredicate((Entity)this.getTarget(), SummonUtils.isTargetFriendly((Entity)this.getTarget(), (LivingEntity)this.getOwner()), this);
    }

    @Override
    public boolean canDualSweep() {
        if (this.getIntelligence() < 16 && !this.getConstructData().isCapabilityEnabled(ConstructCapability.TAUNT)) {
            return false;
        }
        Optional<ItemConstructPart> leftArm = this.getConstructData().getPart(ConstructSlot.LEFT_ARM);
        Optional<ItemConstructPart> rightArm = this.getConstructData().getPart(ConstructSlot.RIGHT_ARM);
        return leftArm.isPresent() && rightArm.isPresent() && leftArm.get() instanceof ConstructPartBladeArmLeft && rightArm.get() instanceof ConstructPartBladeArmRight;
    }

    @Override
    public boolean expandFluidRange() {
        if (this.getIntelligence() < 16 && !this.getConstructData().isCapabilityEnabled(ConstructCapability.TAUNT)) {
            return false;
        }
        Optional<ItemConstructPart> leftArm = this.getConstructData().getPart(ConstructSlot.LEFT_ARM);
        Optional<ItemConstructPart> rightArm = this.getConstructData().getPart(ConstructSlot.RIGHT_ARM);
        return leftArm.isPresent() && rightArm.isPresent() && leftArm.get() instanceof ConstructPartFluidNozzleLeft && rightArm.get() instanceof ConstructPartFluidNozzleRight;
    }

    @Override
    public Optional<LivingEntity> getDualCannonTarget() {
        boolean canDuelCannon;
        boolean bl = canDuelCannon = this.getIntelligence() >= 16 || this.getConstructData().isCapabilityEnabled(ConstructCapability.TAUNT);
        if (this.getTarget() == null || !canDuelCannon || this.isOnCooldown(0)) {
            return Optional.empty();
        }
        if (this.getMana() < 100.0f) {
            return Optional.empty();
        }
        Optional<ItemConstructPart> leftArm = this.getConstructData().getPart(ConstructSlot.LEFT_ARM);
        Optional<ItemConstructPart> rightArm = this.getConstructData().getPart(ConstructSlot.RIGHT_ARM);
        if (leftArm.isPresent() && rightArm.isPresent() && leftArm.get() instanceof ConstructPartManaCannonLeft && rightArm.get() instanceof ConstructPartManaCannonRight) {
            return this.level().getEntities((Entity)this, this.getTarget().getBoundingBox().inflate(6.0), e -> e != this.getTarget() && e.isAlive() && e instanceof LivingEntity && this.getSensing().hasLineOfSight(e) && !this.isAlliedTo((Entity)e)).stream().map(e -> (LivingEntity)e).findFirst();
        }
        return Optional.empty();
    }

    @Override
    public boolean canSpellCast() {
        ISpellDefinition[] spells = this.getCastableSpells();
        if (spells.length == 0) {
            return false;
        }
        float minManaCost = Float.MAX_VALUE;
        for (ISpellDefinition spell : spells) {
            if (!(spell.getManaCost() < minManaCost)) continue;
            minManaCost = spell.getManaCost();
        }
        return this.getMana() >= minManaCost && this.getConstructData().areCapabilitiesEnabled(ConstructCapability.STORE_MANA, ConstructCapability.CAST_SPELL);
    }

    @Override
    public ISpellDefinition[] getCastableSpells() {
        return this.getConstructData().getCastableSpells();
    }

    @Override
    @Nullable
    public ISpellDefinition getCastableSpell(ConstructSlot slot) {
        return this.getConstructData().getCastableSpell(slot);
    }

    @Override
    public float getMaxMana() {
        return ((Float)this.entityData.get(MAX_MANA)).floatValue();
    }

    @Override
    public boolean isFishing() {
        return ActionFlags.FISH.isFlagSet(this.actionFlags);
    }

    @Override
    public boolean isRangedAttacking() {
        return ActionFlags.RANGED_ATTACK.isFlagSet(this.actionFlags);
    }

    @Override
    public float getMana() {
        return ((Float)this.entityData.get(MANA)).floatValue();
    }

    @Override
    public float getManaPct() {
        if (this.getMaxMana() == 0.0f) {
            return 0.0f;
        }
        return MathUtils.clamp01(this.getMana() / Math.max(this.getMaxMana(), 1.0f));
    }

    public float getBuoyancy() {
        return ((Float)this.entityData.get(BUOYANCY)).floatValue();
    }

    public float getWaterLevelAbove() {
        AABB axisalignedbb = this.getBoundingBox();
        int i = Mth.floor((double)axisalignedbb.minX);
        int j = Mth.ceil((double)axisalignedbb.maxX);
        int k = Mth.floor((double)axisalignedbb.maxY);
        int l = Mth.ceil((double)(axisalignedbb.maxY - this.lastYd));
        int i1 = Mth.floor((double)axisalignedbb.minZ);
        int j1 = Mth.ceil((double)axisalignedbb.maxZ);
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        block0: for (int k1 = k; k1 < l; ++k1) {
            float f = 0.0f;
            for (int l1 = i; l1 < j; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.set(l1, k1, i2);
                    FluidState fluidstate = this.level().getFluidState((BlockPos)blockpos$mutable);
                    if (fluidstate.is(FluidTags.WATER)) {
                        f = Math.max(f, fluidstate.getHeight((BlockGetter)this.level(), (BlockPos)blockpos$mutable));
                    }
                    if (f >= 1.0f) continue block0;
                }
            }
            if (!(f < 1.0f)) continue;
            return (float)blockpos$mutable.getY() + f;
        }
        return l + 1;
    }

    public ConstructMoodlets getMoodlets() {
        return this.moodlets;
    }

    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return false;
    }

    public boolean canBeAffected(MobEffectInstance inst) {
        if (inst.getEffect() == MobEffects.WITHER || inst.getEffect() == MobEffects.BAD_OMEN || inst.getEffect() == MobEffects.POISON || inst.getEffect() == MobEffects.WITHER || inst.getEffect() == MobEffects.HUNGER || inst.getEffect() == EffectInit.ASPHYXIATE.get() || inst.getEffect() == EffectInit.CHOOSING_WELLSPRING.get() || inst.getEffect() == EffectInit.CHRONO_ANCHOR.get() || inst.getEffect() == EffectInit.COLD_DARK.get() || inst.getEffect() == EffectInit.ELDRIN_FLIGHT.get() || inst.getEffect() == EffectInit.LIFT.get() || inst.getEffect() == EffectInit.MIST_FORM.get() || inst.getEffect() == EffectInit.WELLSPRING_SIGHT.get() || inst.getEffect() == EffectInit.EARTHQUAKE.get() || inst.getEffect() == EffectInit.LACERATE.get() || inst.getEffect() == EffectInit.SOAR.get() || inst.getEffect() == EffectInit.SPIDER_CLIMBING.get()) {
            return false;
        }
        return super.canBeAffected(inst);
    }

    public boolean canBeSeenAsEnemy() {
        if (this.isDefeated()) {
            return false;
        }
        return super.canBeSeenAsEnemy();
    }

    @Nullable
    public BlockPos getFishingTarget() {
        return (BlockPos)this.entityData.get(FISHING_POS);
    }

    public boolean canFly() {
        if (this.constructCapabilities == null) {
            return false;
        }
        return this.constructCapabilities.isCapabilityEnabled(ConstructCapability.FLY);
    }

    public boolean canTakeItem(ItemStack stack) {
        if (!this.canPickUpLoot()) {
            return false;
        }
        return InventoryUtilities.hasRoomFor((IItemHandler)this, stack);
    }

    public boolean isRenderDisabled() {
        return this.isAway();
    }

    public boolean playerCanCommand(Player player) {
        if (player == null) {
            return false;
        }
        if (((Optional)this.entityData.get(OWNER_UUID)).isPresent() && !((UUID)((Optional)this.entityData.get(OWNER_UUID)).get()).equals(player.getUUID())) {
            return this.isAlliedTo((Entity)player);
        }
        return true;
    }

    @Override
    public boolean isDefeated() {
        return this.defeatedCounter > 0;
    }

    @Override
    public boolean isDueling() {
        return com.mna.tools.MNAGoalSelectorAccess.getRunningGoals(this.goalSelector).anyMatch(g -> {
            Optional<ConstructAITask<?>> cur;
            return g.getGoal() instanceof ConstructCommandFollowLodestar && (cur = ((ConstructCommandFollowLodestar)g.getGoal()).currentCommand()).isPresent() && cur.get() instanceof ConstructDuel;
        });
    }

    @Override
    public int getAttackDelay() {
        return this.attackDelay;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.GENERIC_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    protected boolean isOnCooldown(int id) {
        return this.cooldowns.getOrDefault(id, 0) > 0;
    }

    public boolean isInWall() {
        if (this.noPhysics) {
            return false;
        }
        float f = this.getDimensions(this.getPose()).width() * 0.8f;
        AABB aabb = AABB.ofSize((Vec3)this.getEyePosition(), (double)f, (double)1.0E-6, (double)f);
        return BlockPos.betweenClosedStream((AABB)aabb).anyMatch(p_201942_ -> {
            BlockState blockstate = this.level().getBlockState(p_201942_);
            if (blockstate.is(BlockTags.LEAVES)) {
                return false;
            }
            return !blockstate.isAir() && blockstate.isSuffocating((BlockGetter)this.level(), p_201942_) && Shapes.joinIsNotEmpty((VoxelShape)blockstate.getCollisionShape((BlockGetter)this.level(), p_201942_).move((double)p_201942_.getX(), (double)p_201942_.getY(), (double)p_201942_.getZ()), (VoxelShape)Shapes.create((AABB)aabb), (BooleanOp)BooleanOp.AND);
        });
    }

    public boolean fireImmune() {
        List<ConstructMaterial> comp = this.getConstructData().getComposition();
        return comp.stream().filter(c -> c == ConstructMaterial.OBSIDIAN).count() > 2L;
    }

    @Override
    public int getTeleportCooldown() {
        return this.teleportCooldown;
    }

    @Override
    public int getEquivalentTier() {
        return Math.min(this.lastKnownOwnerTier, this.constructCapabilities.getMaximumTier());
    }

    @Override
    public IFaction getFaction() {
        return this.lastKnownOwnerFaction;
    }

    protected void setCooldown(int id, int ticks) {
        this.cooldowns.put(id, ticks);
    }

    @Override
    public void setHat(ItemStack stack) {
        ItemStack curHat = this.constructCapabilities.getHat();
        if (!curHat.isEmpty() && !this.level().isClientSide()) {
            ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), curHat);
            this.level().addFreshEntity((Entity)item);
        }
        this.constructCapabilities.setHat(stack);
        this.setHappy(200);
        this.needsSync = true;
    }

    @Override
    public void setBanner(ItemStack stack) {
        this.constructCapabilities.setBanner(stack);
        this.needsSync = true;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void onPlayerJump(int jumpPowerIn) {
        if (jumpPowerIn < 0) {
            jumpPowerIn = 0;
        }
        this.jumpPower = jumpPowerIn >= 90 ? 1.0f : 0.4f + 0.4f * (float)jumpPowerIn / 90.0f;
    }

    public void setOwner(UUID playerID) {
        this.entityData.set(OWNER_UUID, Optional.of(playerID));
    }

    @Override
    public boolean setCurrentCommand(Player player, ConstructAITask<?> cmd) {
        return this.setCurrentCommand(player, cmd, false);
    }

    public boolean setCurrentCommand(Player player, ConstructAITask<?> cmd, boolean skipOwnershipCheck) {
        if (!skipOwnershipCheck && !this.playerCanCommand(player)) {
            this.pushDiagnosticMessage(Component.translatable((String)"mna.constructs.feedback.notowner", (Object[])new Object[]{player != null ? player.getDisplayName().getString() : "UNKNOWN"}).getString(), null);
            return false;
        }
        if (cmd == null) {
            return false;
        }
        if (this.current != null) {
            this.current.stop();
            this.goalSelector.removeGoal(this.current);
        }
        this.resetActions();
        ConstructAITask<?> myCommand = cmd.duplicate();
        if (cmd.isOneOff()) {
            ((ConstructAITask)(myCommand)).setOneOff(player);
        }
        ((ConstructAITask)(myCommand)).setConstruct(this);
        ((ConstructAITask)(myCommand)).copyConnections(cmd);
        ((ConstructAITask)(myCommand)).onTaskSet();
        this.current = myCommand;
        this.goalSelector.addGoal(4, myCommand);
        if (this.current != null && this.current.allowSteeringMountedConstructsDuringTask()) {
            this.entityData.set(STEERABLE, true);
        } else {
            this.entityData.set(STEERABLE, false);
        }
        return true;
    }

    public void setRequestingDiagnostics(boolean requesting) {
        this.requestingDiagnostics = requesting;
    }

    public void setJumping(boolean jumping) {
        this.constructJumping = jumping;
        this.jumping = jumping;
    }

    public void adjustMana(float amount) {
        float mana = this.getMana();
        if ((mana += amount) > this.getMaxMana()) {
            mana = this.getMaxMana();
        }
        this.entityData.set(MANA, Float.valueOf(mana));
    }

    @Override
    public void setMining(boolean mining) {
        if (mining) {
            this.ejectPassengers();
            this.actionFlags = ActionFlags.MINE_ENTER.set(this.actionFlags);
            this.actionFlags = ActionFlags.MINE_LEAVE.clear(this.actionFlags);
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Boolean>("away", 120, true, this::setAwayCallback));
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Boolean>("flag", 60, true, (s, b) -> this.entityData.set(AWAY_TYPE, 1)));
        } else {
            this.actionFlags = ActionFlags.MINE_ENTER.clear(this.actionFlags);
            this.actionFlags = ActionFlags.MINE_LEAVE.set(this.actionFlags);
            this.setAwayCallback("away", false);
        }
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    @Override
    public void setAdventuring(boolean adventuring) {
        if (adventuring) {
            this.ejectPassengers();
            this.actionFlags = ActionFlags.ADVENTURE_ENTER.set(this.actionFlags);
            this.actionFlags = ActionFlags.ADVENTURE_LEAVE.clear(this.actionFlags);
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Boolean>("away", 140, true, this::setAwayCallback));
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Boolean>("flag", 100, true, (s, b) -> this.entityData.set(AWAY_TYPE, 2)));
        } else {
            this.actionFlags = ActionFlags.ADVENTURE_ENTER.clear(this.actionFlags);
            this.actionFlags = ActionFlags.ADVENTURE_LEAVE.set(this.actionFlags);
            this.setAwayCallback("away", false);
        }
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    @Override
    public void setHunting(boolean hunting) {
        if (hunting) {
            this.ejectPassengers();
            this.actionFlags = ActionFlags.ADVENTURE_ENTER.set(this.actionFlags);
            this.actionFlags = ActionFlags.ADVENTURE_LEAVE.clear(this.actionFlags);
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Boolean>("away", 140, true, this::setAwayCallback));
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Boolean>("flag", 100, true, (s, b) -> this.entityData.set(AWAY_TYPE, 3)));
        } else {
            this.actionFlags = ActionFlags.ADVENTURE_ENTER.clear(this.actionFlags);
            this.actionFlags = ActionFlags.ADVENTURE_LEAVE.set(this.actionFlags);
            this.setAwayCallback("away", false);
        }
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    public void setAwayCallback(String identifier, boolean away) {
        if (away) {
            this.ejectPassengers();
            this.actionFlags = ActionFlags.AWAY.set(this.actionFlags);
            this.ANIM_PACKET = true;
            ServerMessageDispatcher.sendEntityStateMessage(this);
            this.ANIM_PACKET = false;
        } else {
            this.actionFlags = ActionFlags.AWAY.clear(this.actionFlags);
            this.entityData.set(AWAY_TYPE, 0);
        }
    }

    protected void pushEntities() {
        if (this.isAway()) {
            return;
        }
        super.pushEntities();
    }

    @Override
    public void setWatering() {
        if (!ActionFlags.WATER.isFlagSet(this.actionFlags)) {
            this.actionFlags = ActionFlags.WATER.set(this.actionFlags);
            this.ANIM_PACKET = true;
            ServerMessageDispatcher.sendEntityStateMessage(this);
            this.ANIM_PACKET = false;
        }
    }

    @Override
    public void resetActions() {
        this.actionFlags = 0;
        this.entityData.set(AWAY_TYPE, 0);
        if (!this.level().isClientSide()) {
            this.ANIM_PACKET = true;
            ServerMessageDispatcher.sendEntityStateMessage(this);
            this.ANIM_PACKET = false;
        }
    }

    public void setTarget(LivingEntity entitylivingbaseIn) {
        if (entitylivingbaseIn == null && !this.level().isClientSide()) {
            this.resetAttackState();
        }
        if (entitylivingbaseIn == this) {
            return;
        }
        if (entitylivingbaseIn != null && entitylivingbaseIn == this.getOwner() && !FluidParameterRegistry.INSTANCE.forFluid(this.__cachedFluid).checkPredicate((Entity)entitylivingbaseIn, true, this)) {
            return;
        }
        if (entitylivingbaseIn == null) {
            this.getMoodlets().clearMoodlet(1);
            this.needsSync = true;
        } else if (SummonUtils.isTargetFriendly((Entity)entitylivingbaseIn, (LivingEntity)this.getOwner())) {
            this.setConcerned(9999);
        } else {
            this.setAngry(9999);
        }
        super.setTarget(entitylivingbaseIn);
    }

    @Override
    public void setHappy(int strength) {
        this.moodlets.addMoodlet(8, strength);
        this.needsSync = true;
    }

    @Override
    public void setAngry(int strength) {
        this.moodlets.addMoodlet(1, strength);
        this.needsSync = true;
    }

    @Override
    public void setConcerned(int strength) {
        this.moodlets.addMoodlet(2, strength);
        this.needsSync = true;
    }

    @Override
    public void setConfused(int strength) {
        this.moodlets.addMoodlet(4, strength);
        this.needsSync = true;
    }

    @Override
    public void setUnimpressed(int strength) {
        this.moodlets.addMoodlet(32, strength);
        this.needsSync = true;
    }

    @Override
    public void setFishing(BlockPos pos) {
        this.entityData.set(FISHING_POS, pos);
        this.actionFlags = ActionFlags.FISH.set(this.actionFlags);
        Optional<InteractionHand> fishHand = this.getHandWithCapability(ConstructCapability.FISH);
        if (fishHand.isPresent()) {
            this.swingingArm = fishHand.get();
        }
        if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.FISHING_BOBBER_THROW, 1.0f, 1.0f);
        }
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    public void playSound(SoundEvent pSound, float pVolume, float pPitch) {
        if (this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), pSound, this.getSoundSource(), pVolume, pPitch, false);
        } else {
            this.level().playSound(null, this.blockPosition(), pSound, this.getSoundSource(), pVolume, pPitch);
        }
    }

    @Override
    public void setEating(InteractionHand hand) {
        this.actionFlags = ActionFlags.EAT.set(this.actionFlags);
        this.swingingArm = hand;
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    @Override
    public void stopFishing() {
        if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f);
        }
        this.actionFlags = ActionFlags.FISH.clear(this.actionFlags);
        this.actionFlags = ActionFlags.FISH_END.set(this.actionFlags);
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    @Override
    public void setDefeated(int duration) {
        this.defeatedHealCount = 0.0f;
        com.mna.tools.MNAGoalSelectorAccess.getRunningGoals(this.goalSelector).forEach(g -> {
            if (g.getGoal() instanceof ConstructDuel) {
                ((ConstructDuel)g.getGoal()).forceFail();
            }
        });
        this.setNoAi(true);
        this.setTarget(null);
        this.defeatedCounter = duration;
        this.actionFlags = ActionFlags.DEFEATED.set(this.actionFlags);
        this.moodlets.addMoodlet(4, duration);
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    @Override
    public void setDualSweeping() {
        if (this.level().isClientSide()) {
            return;
        }
        this.actionFlags = ActionFlags.DUAL_SWEEP.set(this.actionFlags);
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    @Override
    public void forceAnimation(Animations animation, boolean onlyWhenIdle) {
        this.forcedAnimation = animation;
        this.forcedAnimationIdleOnly = onlyWhenIdle;
        this.entityData.set(FORCED_ANIMATION, animation.ordinal());
        this.entityData.set(FORCED_ANIMATION_IDLE_ONLY, onlyWhenIdle);
    }

    @Override
    public void clearForcedAnimation() {
        this.entityData.set(FORCED_ANIMATION, -1);
    }

    @Override
    public void setTeleportCooldown(int cooldown) {
        this.teleportCooldown = cooldown;
    }

    protected void checkInsideBlocks() {
        AABB aabb = this.getBoundingBox();
        BlockPos blockpos = BlockPos.containing((double)(aabb.minX + 1.0E-7), (double)(aabb.minY + 1.0E-7), (double)(aabb.minZ + 1.0E-7));
        BlockPos blockpos1 = BlockPos.containing((double)(aabb.maxX - 1.0E-7), (double)(aabb.maxY - 1.0E-7), (double)(aabb.maxZ - 1.0E-7));
        if (this.level().hasChunksAt(blockpos, blockpos1)) {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            for (int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
                for (int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
                    for (int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                        blockpos$mutableblockpos.set(i, j, k);
                        BlockState blockstate = this.level().getBlockState((BlockPos)blockpos$mutableblockpos);
                        if (blockstate.is(BlockTags.LEAVES)) continue;
                        try {
                            blockstate.entityInside(this.level(), (BlockPos)blockpos$mutableblockpos, (Entity)this);
                            this.onInsideBlock(blockstate);
                            continue;
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.forThrowable((Throwable)throwable, (String)"Colliding entity with block");
                            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being collided with");
                            CrashReportCategory.populateBlockDetails((CrashReportCategory)crashreportcategory, (LevelHeightAccessor)this.level(), (BlockPos)blockpos$mutableblockpos, (BlockState)blockstate);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }
    }

    private void regenerate(float amount) {
        this.isRegenerateHealing = true;
        this.heal(amount);
        this.isRegenerateHealing = false;
    }

    public void heal(float pHealAmount) {
        if (this.isDefeated() && !this.isRegenerateHealing) {
            this.defeatedHealCount += pHealAmount;
            if (this.defeatedHealCount >= 20.0f && this.defeatedCounter > 16) {
                this.defeatedCounter = 16;
                this.forceEndDefeated = true;
            }
        }
        super.heal(pHealAmount);
    }

    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player) {
            return (Player)entity;
        }
        return null;
    }

    public void spawnRocketParticles() {
        Vec3 jitter;
        int i;
        Vec3 particleVelocity;
        double speed = this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length();
        Vec3 particlePosition = this.position().add(0.0, (double)0.025f, 0.0);
        Vec3 forward = this.getForward();
        Vec3 offset = forward.cross(new Vec3(0.0, 1.0, 0.0)).normalize().scale((double)0.3f);
        if (speed > (double)0.1f && this.getDeltaMovement().y < speed / 4.0) {
            particleVelocity = this.getDeltaMovement().scale(-1.0);
            particlePosition = particlePosition.add(-forward.x * (double)0.7f, 0.55, -forward.z * (double)0.7f);
        } else {
            particleVelocity = new Vec3(0.0, (double)-0.05f, 0.0);
        }
        MAParticleType particleType = (MAParticleType)(ParticleInit.FLAME.get());
        float particleScale = 0.15f;
        if (this.isInWater()) {
            particleType = (MAParticleType)(ParticleInit.WATER.get());
            particleScale = 0.05f;
        }
        Vec3i color = new Vec3i(10, 30, 65);
        if (this.constructCapabilities.getLowestMaterialCooldownMultiplierForCapability(ConstructCapability.FLY) == ConstructMaterial.OBSIDIAN) {
            color = new Vec3i(170, 60, 10);
        }
        int numParticles = 2;
        for (i = 0; i < numParticles; ++i) {
            jitter = new Vec3(this.level().getRandom().nextGaussian(), 0.0, this.level().getRandom().nextGaussian()).normalize().scale((double)0.0125f);
            this.level().addParticle((ParticleOptions)new MAParticleType(particleType).setColor(color.getX(), color.getY(), color.getZ()).setScale(particleScale), particlePosition.x + offset.x + jitter.x * 3.0, particlePosition.y, particlePosition.z + offset.z + jitter.z * 3.0, particleVelocity.x + jitter.x, particleVelocity.y, particleVelocity.z + jitter.z);
        }
        for (i = 0; i < numParticles; ++i) {
            jitter = new Vec3(this.level().getRandom().nextGaussian(), 0.0, this.level().getRandom().nextGaussian()).normalize().scale((double)0.0125f);
            this.level().addParticle((ParticleOptions)new MAParticleType(particleType).setColor(color.getX(), color.getY(), color.getZ()).setScale(particleScale), particlePosition.x - offset.x + jitter.x * 3.0, particlePosition.y, particlePosition.z - offset.z + jitter.z * 3.0, particleVelocity.x + jitter.x, particleVelocity.y, particleVelocity.z + jitter.z);
        }
    }

    public void spawnMineReturnParticles() {
        BlockState bp = this.level().getBlockState(this.blockPosition().below());
        if (bp.isAir()) {
            return;
        }
        Vec3 feetPos = this.position();
        for (int i = 0; i < 20; ++i) {
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ITEM.get())).setStack(new ItemStack((ItemLike)bp.getBlock())).setMover(new ParticleVelocityMover(-0.2 + Math.random() * 0.4, 0.2f, -0.2 + Math.random() * 0.4, true)).setGravity(0.05f), feetPos.x - 0.5 + Math.random(), feetPos.y, feetPos.z - 0.5 + Math.random(), 0.0, 0.0, 0.0);
        }
    }

    public void spawnMineLeaveParticles() {
        BlockState bp = this.level().getBlockState(this.blockPosition().below());
        if (bp.isAir()) {
            return;
        }
        Vec3 feetPos = this.position();
        for (int i = 0; i < 10; ++i) {
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ITEM.get())).setStack(new ItemStack((ItemLike)bp.getBlock())).setMover(new ParticleVelocityMover(-0.2 + Math.random() * 0.4, 0.2f, -0.2 + Math.random() * 0.4, true)).setGravity(0.05f), feetPos.x - 0.5 + Math.random(), feetPos.y, feetPos.z - 0.5 + Math.random(), 0.0, 0.0, 0.0);
        }
    }

    public void spawnAdventureLeaveParticles() {
        ++this.pfxCounter;
        Vec3 fwd = this.getForward();
        Vec3 feetPos = this.position().add(fwd.scale((double)this.pfxCounter));
        if (this.pfxCounter > 25) {
            return;
        }
        for (int i = 0; i < 10; ++i) {
            Vec3 particleOffset = fwd.scale(Math.random());
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.COZY_SMOKE.get())), feetPos.x + particleOffset.x, feetPos.y + particleOffset.y, feetPos.z + particleOffset.z, 0.0, 0.02, 0.0);
        }
    }

    public void spawnAdventureReturnParticles() {
        ++this.pfxCounter;
        Vec3 fwd = this.getForward();
        Vec3 feetPos = this.position().add(fwd.scale((double)Math.min(-25 + this.pfxCounter, 0)));
        if (this.pfxCounter > 25) {
            return;
        }
        for (int i = 0; i < 10; ++i) {
            Vec3 particleOffset = fwd.scale(Math.random());
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.COZY_SMOKE.get())), feetPos.x + particleOffset.x, feetPos.y + particleOffset.y, feetPos.z + particleOffset.z, 0.0, 0.02, 0.0);
        }
    }

    public void handleStartJump(int jumpPower) {
    }

    public void handleStopJump() {
    }

    private void setRiderPosition(Entity entity, Entity.MoveFunction callback) {
        if (this.hasPassenger(entity)) {
            Vec3 fwd = Vec3.directionFromRotation((Vec2)this.getRotationVector()).normalize().scale(-0.25);
            double d0 = this.getY() + this.getPassengersRidingOffset();
            callback.accept(entity, this.getX() + fwd.x, d0, this.getZ() + fwd.z);
        }
    }

    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() == ItemInit.BELL_OF_BIDDING.get() || mainHand.getItem() == ItemInit.BELL_OF_BIDDING.get() || offHand.getItem() == ItemInit.__DEBUG.get() || mainHand.getItem() == ItemInit.__DEBUG.get() || offHand.getItem() == ItemInit.CONSTRUCT_REPAIR_KIT.get() || mainHand.getItem() == ItemInit.CONSTRUCT_REPAIR_KIT.get()) {
            return InteractionResult.PASS;
        }
        if (mainHand.getItem() instanceof BannerItem) {
            this.getConstructData().setBanner(mainHand);
            return InteractionResult.sidedSuccess((!player.level().isClientSide() ? 1 : 0) != 0);
        }
        if (offHand.getItem() instanceof BannerItem) {
            this.getConstructData().setBanner(offHand);
            return InteractionResult.sidedSuccess((!player.level().isClientSide() ? 1 : 0) != 0);
        }
        if (this.getOwner() == player && FluidUtil.interactWithFluidHandler((Player)player, (InteractionHand)hand, (IFluidHandler)this)) {
            return InteractionResult.sidedSuccess((!player.level().isClientSide() ? 1 : 0) != 0);
        }
        if (this.getOwner() == player && player.getItemInHand(hand).getItem() == Items.BUCKET && !this.getFluidInTank(0).isEmpty()) {
            if (player.isCrouching()) {
                this.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                return InteractionResult.sidedSuccess((!player.level().isClientSide() ? 1 : 0) != 0);
            }
            if (player.level().isClientSide()) {
                player.sendSystemMessage((Component)Component.translatable((String)"mna.constructs.feedback.bucket_void"));
            }
        }
        if (this.canAddPassenger((Entity)player)) {
            this.mountTo(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected void mountTo(Player player) {
        if (!this.level().isClientSide()) {
            player.setXRot(this.getXRot());
            player.setYRot(this.getYRot());
            player.startRiding((Entity)this);
            if (this.current != null && this.current.allowSteeringMountedConstructsDuringTask()) {
                this.entityData.set(STEERABLE, true);
            } else {
                this.entityData.set(STEERABLE, false);
            }
        }
    }

    protected void doPush(Entity entityIn) {
        if (entityIn instanceof Construct || this.isAway()) {
            return;
        }
        super.doPush(entityIn);
    }

    public void soundHorn() {
        if (this.hornDelay > 0) {
            return;
        }
        if (this.level().isClientSide()) {
            ClientMessageDispatcher.dispatchConstructHorn(this.getId());
        } else {
            Optional<ItemConstructPart> head = this.getConstructData().getPart(ConstructSlot.HEAD);
            if (head.isPresent() && head.get() instanceof ConstructPartHornHead) {
                this.playSound(SFX.Entity.Construct.HORN, 1.0f, (float)(0.9 + Math.random() * 0.2));
                this.hornDelay = 100;
            }
        }
    }

    protected void dropAllDeathLoot(DamageSource damageSourceIn) {
        super.dropAllDeathLoot((net.minecraft.server.level.ServerLevel)this.level(), damageSourceIn);
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
            for (ConstructSlot slot : ConstructSlot.values()) {
                this.constructCapabilities.getPart(slot).ifPresent(p -> {
                    if (!this.level().isClientSide()) {
                        if (Math.random() > (double)0.85f) {
                            drops.add(new ItemStack((ItemLike)p));
                        } else {
                            drops.addAll(p.getMaterial().getDeathLootMaterialDrops(this, damageSourceIn));
                        }
                    }
                });
            }
            drops.forEach(drop -> {
                ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), drop);
                if (damageSourceIn.is(DamageTypeTags.IS_EXPLOSION)) {
                    itementity.setDeltaMovement(-0.5 + Math.random(), Math.random() * 0.5, -0.5 + Math.random());
                }
                itementity.setDefaultPickUpDelay();
                this.level().addFreshEntity((Entity)itementity);
            });
            this.dropAllItems();
        }
    }

    protected void updateSwingTime() {
        int i = 16;
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= i) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }
        this.attackAnim = (float)this.swingTime / (float)i;
    }

    public void resetAttackState() {
        this.chargeCounter = 0;
        this.actionFlags = 0;
        this.stopUsingItem();
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
    }

    public boolean isFollowing(Player player) {
        ConstructCommandFollowAndGuard cmd;
        return this.current instanceof ConstructCommandFollowAndGuard && (cmd = (ConstructCommandFollowAndGuard)this.current).getFollowTarget() != null && cmd.getFollowTarget().getId() == player.getId();
    }

    public void moveTo(Vec3 position, Player player) {
        if (!this.playerCanCommand(player)) {
            this.pushDiagnosticMessage(Component.translatable((String)"mna.constructs.feedback.notowner", (Object[])new Object[]{player.getDisplayName().getString()}).getString(), null);
            return;
        }
        ConstructAITask<?> inst = ConstructTasks.MOVE.instantiateTask(this);
        ((ConstructMove)inst).setDesiredLocation(BlockPos.containing((Position)position));
        this.setCurrentCommand(player, inst);
    }

    public void eatItem(Player player) {
        if (!this.playerCanCommand(player)) {
            this.pushDiagnosticMessage(Component.translatable((String)"mna.constructs.feedback.notowner", (Object[])new Object[]{player.getDisplayName().getString()}).getString(), null);
            return;
        }
        ConstructAITask<?> inst = ConstructTasks.EAT_ITEM.instantiateTask(this);
        inst.setOneOff(player);
        this.setCurrentCommand(player, inst);
    }

    public void pickupItem(BlockPos target, Player player) {
        if (!this.playerCanCommand(player)) {
            this.pushDiagnosticMessage(Component.translatable((String)"mna.constructs.feedback.notowner", (Object[])new Object[]{player.getDisplayName().getString()}).getString(), null);
            return;
        }
        ConstructAITask<?> inst = ConstructTasks.GATHER_ITEMS.instantiateTask(this);
        inst.setOneOff(player);
        ((ConstructCollectItems)inst).setArea(new AABB(target));
        this.setCurrentCommand(player, inst);
    }

    @Override
    public void lookTowards(Vec3 pos) {
        this.lookTowards(pos, 15.0f);
    }

    @Override
    public void lookTowards(Vec3 pos, float maxAngle) {
        Vec3 myPos = this.position();
        Vec3 desiredHeading = pos.subtract(myPos).normalize();
        Vec3 offset = MathUtils.rotateTowards(this.getDeltaMovement(), desiredHeading, maxAngle).normalize();
        this.lookAt(EntityAnchorArgument.Anchor.FEET, this.position().add(offset));
    }

    public Vec3 collide(Vec3 pVec) {
        AABB aabb = this.getBoundingBox();
        List list = this.level().getEntityCollisions((Entity)this, aabb.expandTowards(pVec));
        Vec3 vec3 = pVec.lengthSqr() == 0.0 ? pVec : Construct.collideBoundingBox((Entity)this, pVec, aabb, this.level(), list);
        boolean xChanged = pVec.x != vec3.x;
        boolean yChanged = pVec.y != vec3.y;
        boolean zChanged = pVec.z != vec3.z;
        boolean falling = this.onGround() || yChanged && pVec.y < 0.0;
        float stepHeight = this.maxUpStep();
        if (stepHeight > 0.0f && falling && (xChanged || zChanged)) {
            Vec3 vec33;
            Vec3 vec31 = Construct.collideBoundingBox((Entity)this, new Vec3(pVec.x, (double)stepHeight, pVec.z), aabb, this.level(), list);
            Vec3 vec32 = Construct.collideBoundingBox((Entity)this, new Vec3(0.0, (double)stepHeight, 0.0), aabb.expandTowards(pVec.x, 0.0, pVec.z), this.level(), list);
            if (vec32.y < (double)stepHeight && (vec33 = Construct.collideBoundingBox((Entity)this, new Vec3(pVec.x, 0.0, pVec.z), aabb.move(vec32), this.level(), list).add(vec32)).horizontalDistanceSqr() > vec31.horizontalDistanceSqr()) {
                vec31 = vec33;
            }
            if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr()) {
                return vec31.add(Construct.collideBoundingBox((Entity)this, new Vec3(0.0, -vec31.y + pVec.y, 0.0), aabb.move(vec31), this.level(), list));
            }
        }
        return vec3;
    }

    public static Vec3 collideBoundingBox(@Nullable Entity pEntity, Vec3 pVec, AABB pCollisionBox, Level pLevel, List<VoxelShape> pPotentialHits) {
        boolean flag;
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)(pPotentialHits.size() + 1));
        if (!pPotentialHits.isEmpty()) {
            builder.addAll(pPotentialHits);
        }
        WorldBorder worldborder = pLevel.getWorldBorder();
        boolean bl = flag = pEntity != null && worldborder.isInsideCloseToBorder(pEntity, pCollisionBox.expandTowards(pVec));
        if (flag) {
            builder.add(worldborder.getCollisionShape());
        }
        Iterable<VoxelShape> blockVoxels = pLevel.getBlockCollisions(pEntity, pCollisionBox.expandTowards(pVec));
        List<VoxelShape> filtered = StreamSupport.stream(blockVoxels.spliterator(), false).filter(shape -> {
            if (pEntity != null) {
                BlockPos blockPos = new BlockPos((int)shape.min(Direction.Axis.X), (int)shape.min(Direction.Axis.Y), (int)shape.min(Direction.Axis.Z));
                BlockState state = pEntity.level().getBlockState(blockPos);
                if (state.is(BlockTags.LEAVES)) {
                    return false;
                }
            }
            return true;
        }).toList();
        builder.addAll(filtered);
        return Construct.collideWithShapes(pVec, pCollisionBox, (List<VoxelShape>)builder.build());
    }

    private static Vec3 collideWithShapes(Vec3 pDeltaMovement, AABB pEntityBB, List<VoxelShape> pShapes) {
        boolean flag;
        if (pShapes.isEmpty()) {
            return pDeltaMovement;
        }
        double dx = pDeltaMovement.x;
        double dy = pDeltaMovement.y;
        double dz = pDeltaMovement.z;
        if (dy != 0.0 && (dy = Shapes.collide((Direction.Axis)Direction.Axis.Y, (AABB)pEntityBB, pShapes, (double)dy)) != 0.0) {
            pEntityBB = pEntityBB.move(0.0, dy, 0.0);
        }
        boolean bl = flag = Math.abs(dx) < Math.abs(dz);
        if (flag && dz != 0.0 && (dz = Shapes.collide((Direction.Axis)Direction.Axis.Z, (AABB)pEntityBB, pShapes, (double)dz)) != 0.0) {
            pEntityBB = pEntityBB.move(0.0, 0.0, dz);
        }
        if (dx != 0.0) {
            dx = Shapes.collide((Direction.Axis)Direction.Axis.X, (AABB)pEntityBB, pShapes, (double)dx);
            if (!flag && dx != 0.0) {
                pEntityBB = pEntityBB.move(dx, 0.0, 0.0);
            }
        }
        if (!flag && dz != 0.0) {
            dz = Shapes.collide((Direction.Axis)Direction.Axis.Z, (AABB)pEntityBB, pShapes, (double)dz);
        }
        return new Vec3(dx, dy, dz);
    }

    @Override
    public List<LivingEntity> getValidAttackTargets(@Nullable LivingEntity defending) {
        double targetRange = this.getAttribute(AttributeInit.PERCEPTION_DISTANCE).getValue();
        return this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(targetRange, targetRange, targetRange)).stream().filter(e -> {
            LivingEntity guardTarget;
            Player owner = this.getOwner();
            if (e == this || !e.isAlive()) {
                return false;
            }
            if (!this.getSensing().hasLineOfSight((Entity)e)) {
                return false;
            }
            if (owner != null && e == owner || this.isAlliedTo(defending, (Entity)e)) {
                return FluidParameterRegistry.INSTANCE.forFluid(this.getStoredFluid()).checkPredicate((Entity)e, true, this);
            }
            if (e instanceof Creeper && !this.getFluidInTank(0).getFluid().is(FluidTags.WATER)) {
                return false;
            }
            if (e instanceof IFactionEnemy && owner != null) {
                IPlayerProgression progression = (IPlayerProgression)owner.getData(PlayerProgressionProvider.PROGRESSION.get());
                if (progression != null && progression.getAlliedFaction() != null && progression.getAlliedFaction() == ((IFactionEnemy)e).getFaction()) {
                    return false;
                }
                return progression.getAlliedFaction() != null;
            }
            if (e instanceof Player && !this.isAlliedTo(defending, (Entity)e)) {
                return true;
            }
            Object object = guardTarget = defending != null ? defending : owner;
            if (e instanceof Mob && guardTarget != null) {
                Mob mob = (Mob)e;
                LivingEntity target = mob.getTarget();
                if (!(target == null || target != guardTarget && target.isAlliedTo((Entity)guardTarget))) {
                    return true;
                }
                LivingEntity lastHurtBy = mob.getLastHurtByMob();
                if (!(lastHurtBy == null || lastHurtBy != guardTarget && lastHurtBy.isAlliedTo((Entity)guardTarget))) {
                    return true;
                }
            }
            if (e instanceof Enemy && !(e instanceof IFactionEnemy)) {
                return true;
            }
            return FluidParameterRegistry.INSTANCE.forFluid(this.getStoredFluid()).checkPredicate((Entity)e, true, this);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean validateFriendlyTarget(LivingEntity target) {
        if (this.__cachedFluid != null) {
            return FluidParameterRegistry.INSTANCE.forFluid(this.__cachedFluid).checkPredicate((Entity)target, true, this);
        }
        if (this.currentSpell != null) {
            return !this.currentSpell.isHarmful();
        }
        return false;
    }

    public void consumeManaForRangedAttack() {
        this.adjustMana(-50.0f);
    }

    public void consumeFluidForSpray() {
        this.drain(20, IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public boolean performRangedAttack(LivingEntity attackTarget) {
        if (!this.getSensing().hasLineOfSight((Entity)attackTarget)) {
            this.getNavigation().moveTo((Entity)attackTarget, this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            return false;
        }
        this.entityData.set(STEERABLE, false);
        this.getLookControl().setLookAt((Entity)attackTarget, 30.0f, 30.0f);
        ++this.chargeCounter;
        this.getNavigation().stop();
        boolean isFriendly = SummonUtils.isTargetFriendly((Entity)attackTarget, (LivingEntity)this.getOwner());
        if (this.chargeCounter == 1) {
            this.actionFlags = ActionFlags.RANGED_ATTACK.set(this.actionFlags);
            if (this.canSpellCast()) {
                this.setupSpellCast(Math.random() < 0.5 ? ConstructSlot.LEFT_ARM : ConstructSlot.RIGHT_ARM);
            } else if (this.canManaCannonAttack()) {
                if (this.getDualCannonTarget().isPresent()) {
                    this.actionFlags = ActionFlags.DUAL_SHOOT.set(this.actionFlags);
                } else {
                    this.getHandWithCapability(ConstructCapability.RANGED_ATTACK).ifPresent(h -> {
                        this.swingingArm = h;
                    });
                }
            } else if (this.canFluidSpray() && FluidParameterRegistry.INSTANCE.forFluid(this.__cachedFluid).checkPredicate((Entity)attackTarget, isFriendly, this)) {
                this.actionFlags = ActionFlags.SPRAY.set(this.actionFlags);
                if (this.expandFluidRange()) {
                    this.actionFlags = ActionFlags.DUAL_SPRAY.set(this.actionFlags);
                } else {
                    this.getHandWithCapability(ConstructCapability.FLUID_DISPENSE).ifPresent(h -> {
                        this.swingingArm = h;
                    });
                }
            }
            this.ANIM_PACKET = true;
            ServerMessageDispatcher.sendEntityStateMessage(this);
            this.ANIM_PACKET = false;
            return false;
        }
        if (isFriendly && !this.validateFriendlyTarget(attackTarget)) {
            this.setTarget(null);
            return true;
        }
        if (ActionFlags.SPRAY.isFlagSet(this.actionFlags)) {
            return this.performFluidAttack(attackTarget);
        }
        if (ActionFlags.CAST_SPELL_SWING.isFlagSet(this.actionFlags) || ActionFlags.CAST_SPELL_CHANNEL.isFlagSet(this.actionFlags)) {
            --this.chargeCounter;
            return this.performSpellAttack(attackTarget);
        }
        return this.performManaCannonAttack(attackTarget);
    }

    @Override
    public boolean setupSpellCast(ConstructSlot slot) {
        this.actionFlags = ActionFlags.RANGED_ATTACK.set(this.actionFlags);
        if (!this.canSpellCast()) {
            this.resetSpellCast();
            return false;
        }
        ISpellDefinition selection = this.getCastableSpell(slot);
        if (selection == null && (selection = slot == ConstructSlot.LEFT_ARM ? this.getCastableSpell(ConstructSlot.RIGHT_ARM) : this.getCastableSpell(ConstructSlot.LEFT_ARM)) == null) {
            this.resetSpellCast();
            return false;
        }
        if (this.getMana() < selection.getManaCost()) {
            this.resetSpellCast();
            return false;
        }
        List<SpellReagent> reagents = selection.getReagents(null, this.swingingArm, null);
        HashMap<Item, Boolean> missing = new HashMap<Item, Boolean>();
        for (SpellReagent reagent : reagents) {
            if (this.hasItem(reagent.getReagentStack(), reagent.getIgnoreDurability(), reagent.getCompareNBT())) continue;
            missing.put(reagent.getReagentStack().getItem(), reagent.getOptional());
        }
        List missingRequired = missing.entrySet().stream().filter(e -> (Boolean)e.getValue() == false).collect(Collectors.toList());
        if (missingRequired.size() > 0) {
            this.pushDiagnosticMessage(Component.translatable((String)"mna.constructs.feedback.cast.missing_reagent").getString(), ConstructTasks.CAST_SPELL_AT_TARGET.getIconTexture());
            this.resetSpellCast();
            return false;
        }
        CompoundTag tmp = new CompoundTag();
        selection.writeToNBT(tmp);
        this.currentSpell = SpellRecipe.fromNBT(tmp);
        this.actionFlags = this.currentSpell.isChanneled() ? ActionFlags.CAST_SPELL_CHANNEL.set(this.actionFlags) : ActionFlags.CAST_SPELL_SWING.set(this.actionFlags);
        this.getHandWithCapability(ConstructCapability.CAST_SPELL).ifPresent(h -> {
            this.swingingArm = h;
        });
        this.ANIM_PACKET = true;
        ServerMessageDispatcher.sendEntityStateMessage(this);
        this.ANIM_PACKET = false;
        return true;
    }

    @Override
    public boolean setupSpellCast(boolean mainHand) {
        return this.setupSpellCast(mainHand ? ConstructSlot.LEFT_ARM : ConstructSlot.RIGHT_ARM);
    }

    @Override
    public boolean startSpellCast(@Nullable SpellTarget targetHint) {
        SpellSource source = new SpellSource((LivingEntity)this, this.swingingArm);
        ItemStack spellStack = new ItemStack((ItemLike)ItemInit.SPELL.get());
        this.currentSpell.writeToNBT(spellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        SpellCaster.applyAdjusters(spellStack, (LivingEntity)this, this.swingingArm, false, this.currentSpell, SpellCastStage.CALCULATING_MANA_COST);
        List<SpellReagent> reagents = this.currentSpell.getReagents(null, this.swingingArm, null);
        HashMap<Item, Boolean> missing = new HashMap<Item, Boolean>();
        boolean allReagentsConsumed = true;
        ArrayList<Pair<IItemHandler, Direction>> myInv = new ArrayList<Pair<IItemHandler, Direction>>();
        myInv.add(new Pair(this, Direction.UP));
        for (SpellReagent reagent : reagents) {
            if (!this.hasItem(reagent.getReagentStack(), !reagent.getIgnoreDurability(), reagent.getCompareNBT())) {
                missing.put(reagent.getReagentStack().getItem(), reagent.getOptional());
                continue;
            }
            if (!reagent.getConsume()) continue;
            boolean found = false;
            InteractionHand[] hands = this.getCarryingHands((ItemStack is) -> {
                if (!reagent.getIgnoreDurability() && is.getDamageValue() != reagent.getReagentStack().getDamageValue()) {
                    return false;
                }
                if (reagent.getCompareNBT() && !ManaAndArtificeMod.getItemHelper().AreTagsEqual((ItemStack)is, reagent.getReagentStack())) {
                    return false;
                }
                return is.getItem() == reagent.getReagentStack().getItem();
            });
            if (hands.length > 0) {
                ItemStack handStack = this.getItemInHand(hands[0]);
                handStack.shrink(1);
                if (handStack.isEmpty()) {
                    this.setItemInHand(hands[0], ItemStack.EMPTY);
                } else {
                    this.setItemInHand(hands[0], handStack);
                }
                found = true;
            } else {
                found = InventoryUtilities.consumeAcrossInventories(reagent.getReagentStack(), reagent.getIgnoreDurability(), reagent.getCompareNBT(), false, myInv);
            }
            allReagentsConsumed &= found;
        }
        if (!allReagentsConsumed) {
            this.resetSpellCast();
            return false;
        }
        float discountFactor = 1.0f - 0.1f * (float)this.getConstructData().getAffinityScore(Affinity.ARCANE);
        this.adjustMana(-this.currentSpell.getManaCost() * discountFactor);
        if (this.currentSpell.isChanneled()) {
            this.setItemInHand(this.swingingArm, spellStack);
            this.startUsingItem(this.swingingArm);
        }
        SpellCaster.Affect(spellStack, this.currentSpell, this.level(), source, targetHint);
        return true;
    }

     
    @Override
    public boolean tickSpellCast() {
        ++this.chargeCounter;
        if (!this.currentSpell.isChanneled()) {
            if (this.chargeCounter < 20) return true;
            this.resetSpellCast();
            return false;
        }
        int maxChannelTime = (int)this.currentSpell.getShape().getValue(Attribute.DURATION) * 20;
        if (this.chargeCounter >= maxChannelTime) {
            this.resetSpellCast();
            return false;
        }
        if (this.chargeCounter < 5) return true;
        float discountFactor = 1.0f - 0.1f * (float)this.getConstructData().getAffinityScore(Affinity.ARCANE);
        float manaCost = this.currentSpell.getManaCost() * discountFactor;
        if (this.getMana() >= manaCost) {
            this.adjustMana(-manaCost);
            return true;
        }
        this.resetSpellCast();
        this.pushDiagnosticMessage((Component)Component.translatable((String)"mna.constructs.feedback.cast.no_mana"), ConstructTasks.CAST_SPELL_AT_TARGET.getIconTexture());
        return false;
    }

    @Override
    public void resetSpellCast() {
        this.stopUsingItem();
        this.resetAttackState();
        this.entityData.set(STEERABLE, true);
        this.currentSpell = null;
    }

    private boolean performFluidAttack(LivingEntity attackTarget) {
        if (this.chargeCounter == 2) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), FluidParameterRegistry.INSTANCE.forFluid(this.__cachedFluid).spraySound(), SoundSource.NEUTRAL, 0.25f, (float)(0.9 + Math.random() * 0.1));
        } else {
            if (this.chargeCounter == 15) {
                this.consumeFluidForSpray();
                this.resetAttackState();
                this.entityData.set(STEERABLE, true);
                return true;
            }
            if (this.chargeCounter >= 5 && this.chargeCounter % 5 == 0) {
                float range = 10.0f;
                float angle = 45.0f;
                if (this.expandFluidRange()) {
                    range *= 1.5f;
                    angle = 60.0f;
                }
                Vec3 forward = this.getTarget().position().subtract(this.position()).normalize();
                EntityUtil.getEntitiesWithinCone(this.level(), this.position(), forward, range, -angle, angle, e -> {
                    if (!(e instanceof LivingEntity)) {
                        return false;
                    }
                    boolean isFriendly = this.isAlliedTo((Entity)e);
                    if (e == this.getOwner() && !FluidParameterRegistry.INSTANCE.forFluid(this.__cachedFluid).checkPredicate((Entity)e, isFriendly, this)) {
                        return false;
                    }
                    ConstructSprayTargetingEvent targetEvt = new ConstructSprayTargetingEvent(this, (Entity)e, this.__cachedFluid, e == this.getOwner(), isFriendly);
                    NeoForge.EVENT_BUS.post(targetEvt);
                    return !targetEvt.isCanceled();
                }).stream().map(e -> e).forEach(e -> {
                    ConstructSprayEffectEvent effectEvt = new ConstructSprayEffectEvent((LivingEntity)this, (LivingEntity)e, this.__cachedFluid, e == this.getOwner(), SummonUtils.isTargetFriendly((Entity)e, (LivingEntity)this.getOwner()));
                    NeoForge.EVENT_BUS.post(effectEvt);
                });
            }
        }
        return false;
    }

    private boolean performSpellAttack(LivingEntity attackTarget) {
        if (this.chargeCounter > 80) {
            this.resetAttackState();
            this.stopUsingItem();
            this.entityData.set(STEERABLE, true);
            return true;
        }
        if (this.chargeCounter == 2) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, attackTarget.getEyePosition());
            this.startSpellCast(new SpellTarget((Entity)attackTarget));
            return false;
        }
        this.tickSpellCast();
        return false;
    }

    private boolean performManaCannonAttack(LivingEntity attackTarget) {
        if (this.chargeCounter == 20) {
            this.getDualCannonTarget().ifPresent(eTgt -> {
                this.spawnManaCannonShot((LivingEntity)eTgt);
                this.setCooldown(0, (int)(500.0f * this.getConstructData().getLowestMaterialCooldownMultiplierForCapability(ConstructCapability.RANGED_ATTACK).getCooldownMultiplierFor(ConstructCapability.RANGED_ATTACK)));
            });
            this.spawnManaCannonShot(attackTarget);
        } else if (this.chargeCounter == 40) {
            this.resetAttackState();
            this.entityData.set(STEERABLE, true);
            return true;
        }
        return false;
    }

    private void spawnManaCannonShot(LivingEntity target) {
        Affinity aff = this.getConstructData().getRandomContainedAffinity();
        SpellProjectile esp = new SpellProjectile((LivingEntity)this, this.level());
        esp.setForcedDamageAffinityAndTarget(aff, (float)this.getAttribute(AttributeInit.RANGED_DAMAGE).getValue(), (Entity)target);
        esp.setHomingStrength(MathUtils.clamp01((float)(this.getIntelligence() - 16) / 24.0f));
        this.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        esp.shoot((Entity)this, this.getForward(), 1.0f, 0.0f);
        this.level().addFreshEntity((Entity)esp);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SFX.Spell.Cast.ForAffinity(aff), SoundSource.NEUTRAL, 0.25f, (float)(0.9 + Math.random() * 0.1));
        this.consumeManaForRangedAttack();
    }

    public boolean isAlliedTo(Entity pEntity) {
        if (pEntity instanceof Construct) {
            Optional theirOwnerID = (Optional)((Construct)pEntity).entityData.get(OWNER_UUID);
            Optional myOwnerID = (Optional)this.entityData.get(OWNER_UUID);
            if (theirOwnerID.isPresent() && myOwnerID.isPresent()) {
                Player myOwner = this.getOwner();
                Player theirOwner = ((Construct)pEntity).getOwner();
                if (myOwner != null && theirOwner != null) {
                    return myOwner.isAlliedTo((Entity)theirOwner);
                }
                if (((UUID)myOwnerID.get()).equals(theirOwnerID.get())) {
                    return true;
                }
            }
        }
        return SummonUtils.isTargetFriendly(pEntity, (LivingEntity)this.getOwner());
    }

    public boolean isAlliedTo(@Nullable LivingEntity defending, Entity pEntity) {
        if (defending == null) {
            return this.isAlliedTo(pEntity);
        }
        return SummonUtils.isTargetFriendly(pEntity, defending);
    }

    public void die(DamageSource cause) {
        boolean shouldDie;
        if (this.isDueling()) {
            Component opponentName;
            this.setHealth(1.0f);
            Component component = opponentName = cause.getEntity() != null ? cause.getEntity().getCustomName() : null;
            if (opponentName == null) {
                opponentName = Component.translatable((String)"mna.constructs.feedback.target.no_name");
            }
            this.pushDiagnosticMessage((Component)Component.translatable((String)"mna.constructs.feedback.duel.defeat", (Object[])new Object[]{opponentName}), ConstructTasks.DUEL.getIconTexture());
            this.setDefeated(600);
            return;
        }
        boolean isReturningToTable = this.current instanceof ConstructCommandReturnToTable;
        boolean bl = shouldDie = cause.is(DamageTypes.OUTSIDE_BORDER) || cause.is(DamageTypes.FELL_OUT_OF_WORLD) || !GeneralConfigValues.ImmortalConstructs || isReturningToTable;
        if (shouldDie) {
            super.die(cause);
            if (isReturningToTable) {
                this.constructDiagnostics.pushDiagnosticMessage(Component.translatable((String)"mna.constructs.feedback.return_to_table").getString(), null, false);
            } else {
                this.constructDiagnostics.pushDiagnosticMessage(Component.translatable((String)"mna.constructs.feedback.death").getString(), null, false);
            }
        } else {
            this.setHealth(1.0f);
            this.setDefeated(300);
            List entities = this.level().getEntities((Entity)this, this.getBoundingBox().inflate(64.0));
            for (Entity entity : (java.util.List<Entity>)entities) {
                Mob mob;
                if (!(entity instanceof Mob) || (mob = (Mob)entity).getTarget() != this) continue;
                if (mob.getLastHurtByMob() == this) {
                    mob.setLastHurtByMob(null);
                }
                if (mob.getLastHurtMob() == this) {
                    mob.setLastHurtMob(null);
                }
                mob.setTarget(null);
            }
        }
    }

    public boolean hurt(DamageSource source, float amount) {
        int numBoneParts;
        int numWickerwoodParts;
        if (this.isAway()) {
            return false;
        }
        if (source.is(MATags.DamageTypes.CONSTRUCT_IMMUNE)) {
            return false;
        }
        if (this.getControllingPassenger() != null && source.getEntity() == this.getControllingPassenger()) {
            return false;
        }
        if (source.getEntity() != null && this.getOwner() != null && (source.getEntity() == this.getOwner() || this.isAlliedTo(source.getEntity()))) {
            this.setUnimpressed(60);
            return false;
        }
        if (this.getControllingPassenger() != null && !this.redirectingDamage) {
            this.redirectingDamage = true;
            this.getControllingPassenger().hurt(source, amount);
            this.redirectingDamage = false;
            return false;
        }
        if (this.defeatedCounter > 0) {
            return false;
        }
        if (this.fireImmune() && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.LAVA))) {
            return false;
        }
        int numWoodParts = this.constructCapabilities.getPartsForMaterial(ConstructMaterial.WOOD).size();
        int burnableParts = numWoodParts + (numWickerwoodParts = this.constructCapabilities.getPartsForMaterial(ConstructMaterial.WICKERWOOD).size());
        if (burnableParts > 0 && source.is(DamageTypeTags.IS_FIRE)) {
            amount *= 1.0f + (float)burnableParts * 0.2f;
        }
        if ((numBoneParts = this.constructCapabilities.getPartsForMaterial(ConstructMaterial.BONE).size()) > 0 && source.is(DamageTypeTags.IS_FREEZING)) {
            amount *= 1.0f - (float)numBoneParts * 0.2f;
        }
        float resistance = 0.0f;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            resistance += this.constructCapabilities.calculateExplosionResistance();
        }
        amount -= amount * resistance;
        resistance = 0.0f;
        if (source.is(MATags.DamageTypes.CONSTRUCT_RESIST_EARTH)) {
            resistance += (float)this.getConstructData().getAffinityScore(Affinity.EARTH);
        }
        if (source.is(MATags.DamageTypes.CONSTRUCT_RESIST_WIND)) {
            resistance += (float)this.getConstructData().getAffinityScore(Affinity.WIND);
        }
        if (source.is(MATags.DamageTypes.CONSTRUCT_RESIST_FIRE)) {
            resistance += (float)this.getConstructData().getAffinityScore(Affinity.FIRE);
        }
        if (source.is(MATags.DamageTypes.CONSTRUCT_RESIST_WATER)) {
            resistance += (float)this.getConstructData().getAffinityScore(Affinity.WATER);
        }
        if (source.is(MATags.DamageTypes.CONSTRUCT_RESIST_ARCANE)) {
            resistance += (float)this.getConstructData().getAffinityScore(Affinity.ARCANE);
        }
        if (source.is(MATags.DamageTypes.CONSTRUCT_RESIST_ENDER)) {
            resistance += (float)this.getConstructData().getAffinityScore(Affinity.ENDER);
        }
        amount -= resistance;
        if (this.getConstructData().isCapabilityEnabled(ConstructCapability.BLOCK) && source.getEntity() == this.getTarget() && this.attackDelay < 30) {
            this.attackDelay += 20;
        }
        if (amount <= 0.0f) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (super.doHurtTarget(entityIn)) {
            Mob mob;
            LivingEntity living;
            int water;
            if (this.constructCapabilities.getAffinityScore(Affinity.FIRE) > 0) {
                entityIn.igniteForSeconds((float)(this.constructCapabilities.getAffinityScore(Affinity.FIRE)));
            }
            if ((water = this.constructCapabilities.getAffinityScore(Affinity.WATER)) > 0 && entityIn instanceof LivingEntity) {
                living = (LivingEntity)entityIn;
                int amp = (int)Math.ceil((float)water / 2.0f);
                int dur = water * 20;
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, amp));
            }
            if (this.constructCapabilities.isCapabilityEnabled(ConstructCapability.TAUNT) && entityIn instanceof Mob && (mob = (Mob)entityIn).getTarget() != this) {
                mob.setTarget((LivingEntity)this);
                this.playSound(SFX.Entity.Construct.TAUNT, 1.0f, (float)(0.9 + Math.random() * 0.2));
            }
            if (this.getConstructData().isCapabilityEnabled(ConstructCapability.BLOCK)) {
                this.attackDelay = 10 + (int)(20.0 * Math.random());
            }
            if (!this.level().isClientSide() && (float)this.constructCapabilities.calculateIntelligence() >= 36.0f && (float)this.constructCapabilities.calculatePerception() >= 28.0f && entityIn instanceof LivingEntity) {
                living = (LivingEntity)entityIn;
                living.setLastHurtByPlayer((Player)FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)this.level())));
            }
            return true;
        }
        return false;
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.constructCapabilities != null) {
            this.constructCapabilities.WriteNBT(compound);
        }
        compound.putString(NBT_OWNER, this.entityData.get(OWNER_UUID) != null && ((Optional)this.entityData.get(OWNER_UUID)).isPresent() ? ((UUID)((Optional)this.entityData.get(OWNER_UUID)).get()).toString() : "");
        if (this.current != null) {
            compound.put("currentGoal", (Tag)this.current.writeNBT());
        }
        compound.putFloat("mana", ((Float)this.entityData.get(MANA)).floatValue());
        compound.putString("fluidType", (String)this.entityData.get(FLUIDTYPE));
        compound.putInt("fluidAmt", ((Integer)this.entityData.get(FLUIDAMT)).intValue());
        if (this.cached_team_name != null) {
            compound.putString("alliedTeam", this.cached_team_name);
        }
        if (this.lastKnownOwnerFaction != null) {
            compound.putString("ownerFaction", Registries.Factions.getKey(this.lastKnownOwnerFaction).toString());
        }
        compound.putInt("ownerTier", this.lastKnownOwnerTier);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        CompoundTag goalData;
        ConstructTask action;
        ConstructAITask<?> inst;
        super.readAdditionalSaveData(compound);
        this.constructCapabilities.ReadNBT(compound);
        if (compound.contains(NBT_OWNER)) {
            try {
                this.entityData.set(OWNER_UUID, Optional.of(UUID.fromString(compound.getString(NBT_OWNER))));
            }
            catch (Exception ex) {
                ManaAndArtifice.LOGGER.error("Error loading construct owner UUID.  It is now unclaimed and can be claimed by any player.");
            }
        }
        this.recalculateAll();
        if (compound.contains("mana")) {
            this.entityData.set(MANA, Float.valueOf(compound.getFloat("mana")));
        }
        if (compound.contains("fluidType")) {
            String fluidType = compound.getString("fluidType");
            ResourceLocation fLoc = ResourceLocation.parse(fluidType);
            this.setStoredFluid((Fluid)BuiltInRegistries.FLUID.get(fLoc));
        }
        if (compound.contains("fluidAmt")) {
            this.setStoredFluidAmount(compound.getInt("fluidAmt"));
        }
        if (compound.contains("alliedTeam")) {
            this.cached_team_name = compound.getString("alliedTeam");
        }
        if (compound.contains("ownerFaction")) {
            try {
                ResourceLocation factionID = ResourceLocation.parse(compound.getString("ownerFaction"));
                this.lastKnownOwnerFaction = (IFaction)Registries.Factions.getValue(factionID);
            }
            catch (Throwable factionID) {
                 
            }
        }
        if (compound.contains("ownerTier")) {
            this.lastKnownOwnerTier = compound.getInt("ownerTier");
        }
        if (compound.contains("currentGoal") && (inst = (action = ConstructAITask.getTypeFromNBT(goalData = compound.getCompound("currentGoal"))).instantiateTask(this)) != null) {
            this.current = inst;
            this.current.setConstruct(this);
            this.current.readNBT(goalData);
            if (this.current != null && this.current.allowSteeringMountedConstructsDuringTask()) {
                this.entityData.set(STEERABLE, true);
            } else {
                this.entityData.set(STEERABLE, false);
            }
            this.goalSelector.addGoal(4, this.current);
        }
        this.setNoAi(false);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, (double)0.35f).add(Attributes.FLYING_SPEED, (double)0.35f).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.ATTACK_SPEED, 20.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0).add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeInit.PERCEPTION_DISTANCE.get()), 8.0).add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeInit.INTELLIGENCE.get()), 8.0).add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeInit.RANGED_DAMAGE.get()), 0.0);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(MANA, Float.valueOf(0.0f));
        builder.define(MAX_MANA, Float.valueOf(0.0f));
        builder.define(STEERABLE, false);
        builder.define(BUOYANCY, Float.valueOf(0.0f));
        builder.define(FLUID_CAP, 0);
        builder.define(FLUIDAMT, 0);
        builder.define(AWAY_TYPE, 0);
        builder.define(FLUIDTYPE, "");
        builder.define(FISHING_POS, BlockPos.ZERO);
        builder.define(FORCED_ANIMATION, -1);
        builder.define(FORCED_ANIMATION_IDLE_ONLY, true);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        String id = (String)this.entityData.get(FLUIDTYPE);
        if (id != null && !id.isEmpty()) {
            ResourceLocation rLoc = ResourceLocation.parse(id);
            this.__cachedFluid = (Fluid)BuiltInRegistries.FLUID.get(rLoc);
        } else {
            this.__cachedFluid = null;
        }
        this.forcedAnimationIdleOnly = (Boolean)this.entityData.get(FORCED_ANIMATION_IDLE_ONLY);
        int forcedAnimIdx = (Integer)this.entityData.get(FORCED_ANIMATION);
        this.forcedAnimation = forcedAnimIdx == -1 ? null : Animations.values()[forcedAnimIdx % Animations.values().length];
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new ConstructSwimGoal());
        this.goalSelector.addGoal(0, (Goal)new OpenDoorGoal((Mob)this, true));
        this.goalSelector.addGoal(0, (Goal)new ConstructFleeTarget(this, 12.0f, 0.5, 0.5));
    }

    private void registerHammerLeap() {
        Optional<ItemConstructPart> leftArm = this.getConstructData().getPart(ConstructSlot.LEFT_ARM);
        if (!leftArm.isPresent() || !(leftArm.get() instanceof ConstructPartHammerArmLeft)) {
            return;
        }
        Optional<ItemConstructPart> rightArm = this.getConstructData().getPart(ConstructSlot.RIGHT_ARM);
        if (!rightArm.isPresent() || !(rightArm.get() instanceof ConstructPartHammerArmRight)) {
            return;
        }
        if (this.getIntelligence() < 16 && !this.getConstructData().isCapabilityEnabled(ConstructCapability.TAUNT)) {
            return;
        }
        float speed = this.getConstructData().getLowestMaterialCooldownMultiplierForCapability(ConstructCapability.SMITH).getCooldownMultiplierFor(ConstructCapability.SMITH) * 0.65f;
        float damage = this.getConstructData().calculateDamage() / 2.0f;
        this.goalSelector.addGoal(1, new LerpLeap<Construct>(this, 10, 5, e -> {
            if (this.getTarget() == null || this.isAlliedTo((Entity)this.getTarget()) || !this.getTarget().onGround()) {
                return false;
            }
            if (this.isOnCooldown(0)) {
                return false;
            }
            int yCount = 0;
            BlockPos pos = this.blockPosition().above();
            while (this.level().isEmptyBlock(pos) && yCount < 5) {
                ++yCount;
                pos = pos.above();
            }
            if (yCount < 5) {
                return false;
            }
            return this.getTarget() != null && this.getTarget().isAlive() && this.getTarget().distanceTo((Entity)this) < 4.0f && this.getSensing().hasLineOfSight((Entity)this.getTarget());
        }, evt -> {
            boolean sendFlags = false;
            switch (evt) {
                case DAMAGE: {
                    this.level().getEntities((Entity)this, this.getBoundingBox().inflate(4.0), tgt -> tgt instanceof LivingEntity && tgt.isAlive() && tgt != this && !this.isAlliedTo((Entity)tgt)).stream().map(e -> (LivingEntity)e).forEach(target -> {
                        float mX = (float)(this.getX() - target.getX());
                        float mZ = (float)(this.getZ() - target.getZ());
                        float speed2 = this.getConstructData().getLowestMaterialCooldownMultiplierForCapability(ConstructCapability.SMITH).getCooldownMultiplierFor(ConstructCapability.SMITH) * 0.65f;
                        ComponentFling.flingTarget(target, new Vec3((double)mX, (double)speed, (double)mZ), speed2, 3.0f);
                        target.hurt(this.damageSources().mobAttack((LivingEntity)this), damage);
                    });
                    if (this.level().isClientSide()) break;
                    ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.level().dimension(), 32.0f, this.getX(), this.getY(), this.getZ(), SpawnParticleEffectMessage.ParticleTypes.CONSTRUCT_HAMMER_SMASH);
                    break;
                }
                case LAND: {
                    this.actionFlags = ActionFlags.LEAP_LAND.set(this.actionFlags);
                    sendFlags = true;
                    break;
                }
                case LEAP: {
                    this.actionFlags = ActionFlags.LEAP_AIRBORNE.set(this.actionFlags);
                    sendFlags = true;
                    break;
                }
                case START: {
                    this.actionFlags = ActionFlags.LEAP_START.set(this.actionFlags);
                    sendFlags = true;
                    break;
                }
                default: {
                    this.actionFlags = 0;
                    sendFlags = true;
                    this.setCooldown(0, (int)(500.0f * this.getConstructData().getLowestMaterialCooldownMultiplierForCapability(ConstructCapability.SMITH).getCooldownMultiplierFor(ConstructCapability.SMITH)));
                }
            }
            if (sendFlags && !this.level().isClientSide()) {
                this.ANIM_PACKET = true;
                ServerMessageDispatcher.sendEntityStateMessage(this);
                this.ANIM_PACKET = false;
            }
        }));
    }

    private void registerAxeLeap() {
        Optional<ItemConstructPart> leftArm = this.getConstructData().getPart(ConstructSlot.LEFT_ARM);
        if (!leftArm.isPresent() || !(leftArm.get() instanceof ConstructPartAxeArmLeft)) {
            return;
        }
        Optional<ItemConstructPart> rightArm = this.getConstructData().getPart(ConstructSlot.RIGHT_ARM);
        if (!rightArm.isPresent() || !(rightArm.get() instanceof ConstructPartAxeArmRight)) {
            return;
        }
        if (this.getIntelligence() < 16 && !this.getConstructData().isCapabilityEnabled(ConstructCapability.TAUNT)) {
            return;
        }
        float damage = this.getConstructData().calculateDamage() * 3.0f;
        this.goalSelector.addGoal(1, new LerpLeap<Construct>(this, 10, 5, e -> {
            if (this.getTarget() == null || this.isAlliedTo((Entity)this.getTarget())) {
                return false;
            }
            if (this.isOnCooldown(0)) {
                return false;
            }
            int yCount = 0;
            BlockPos pos = this.blockPosition();
            while (this.level().isEmptyBlock(pos) && yCount < 5) {
                ++yCount;
                pos = pos.above();
            }
            if (yCount < 3) {
                return false;
            }
            double dist = this.getTarget().distanceTo((Entity)this);
            return this.getTarget() != null && this.getTarget().isAlive() && dist >= 4.0 && dist <= 16.0 && this.getSensing().hasLineOfSight((Entity)this.getTarget());
        }, evt -> {
            boolean sendFlags = false;
            switch (evt) {
                case DAMAGE: {
                    if (this.getTarget() == null) break;
                    this.getTarget().hurt(this.damageSources().mobAttack((LivingEntity)this), damage);
                    CompoundTag meta = new CompoundTag();
                    meta.putInt("entityID", this.getTarget().getId());
                    ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.level().dimension(), 32.0f, this.getX(), this.getY(), this.getZ(), SpawnParticleEffectMessage.ParticleTypes.CONSTRUCT_AXE_SMASH, meta);
                    break;
                }
                case LAND: {
                    this.actionFlags = ActionFlags.LEAP_LAND.set(this.actionFlags);
                    sendFlags = true;
                    break;
                }
                case LEAP: {
                    this.actionFlags = ActionFlags.LEAP_AIRBORNE.set(this.actionFlags);
                    sendFlags = true;
                    break;
                }
                case START: {
                    this.actionFlags = ActionFlags.LEAP_START.set(this.actionFlags);
                    sendFlags = true;
                    break;
                }
                default: {
                    this.actionFlags = 0;
                    sendFlags = true;
                    this.setCooldown(0, (int)(500.0f * this.getConstructData().getLowestMaterialCooldownMultiplierForCapability(ConstructCapability.CHOP_WOOD).getCooldownMultiplierFor(ConstructCapability.CHOP_WOOD)));
                }
            }
            if (sendFlags && !this.level().isClientSide()) {
                this.ANIM_PACKET = true;
                ServerMessageDispatcher.sendEntityStateMessage(this);
                this.ANIM_PACKET = false;
            }
        }));
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        if (nbt.contains("anim")) {
            if (nbt.contains("hand")) {
                this.swingingArm = InteractionHand.values()[nbt.getByte("hand")];
            }
            this.clearAnimCache = ActionFlags.ADVENTURE_ENTER.isFlagSet(this.actionFlags);
            this.actionFlags = nbt.getInt("actionFlags");
            this.defeatedCounter = nbt.getInt("defeatedCounter");
        } else {
            this.constructCapabilities.ReadNBT(nbt);
            this.recalculateAll();
        }
        if (nbt.contains("diag")) {
            this.constructDiagnostics.readFromNBT(nbt.getCompound("diag"));
        }
        if (nbt.contains("inv")) {
            for (int i = 0; i < 9; ++i) {
                this.setStackInSlot(i, ItemStack.EMPTY);
            }
            MNAItemData.loadItems((CompoundTag)nbt.getCompound("inv"), this.inventory);
        }
        if (nbt.contains("hat")) {
            this.constructCapabilities.setHat(ItemStack.parseOptional(this.registryAccess(), nbt.getCompound("hat")));
        }
        if (nbt.contains("banner")) {
            this.constructCapabilities.setBanner(ItemStack.parseOptional(this.registryAccess(), nbt.getCompound("banner")));
        }
        this.moodlets.readFromNBT(nbt);
        if (ManaAndArtifice.instance.proxy.checkConstructDanceMixPlaying()) {
            this.setHappy(10000);
        }
        if (this.isAway()) {
            this.mine_leave_pfx = false;
            this.mine_return_pfx = false;
            this.adventure_leave_pfx = false;
            this.adventure_return_pfx = false;
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (!this.level().isClientSide() && this.getCurrentCommand() instanceof ConstructCommandFollowLodestar) {
            ((ConstructCommandFollowLodestar)this.getCurrentCommand()).releaseMutexes();
        }
    }

    @Nullable
    public Fluid getStoredFluid() {
        return this.__cachedFluid;
    }

    public void setStoredFluid(Fluid fluid) {
        if (fluid == null) {
            fluid = Fluids.EMPTY;
        }
        if (this.__cachedFluid != fluid) {
            this.entityData.set(FLUIDTYPE, BuiltInRegistries.FLUID.getKey(fluid).toString());
            this.__cachedFluid = fluid;
        }
    }

    @Override
    public int getStoredFluidAmount() {
        return (Integer)this.entityData.get(FLUIDAMT);
    }

    @Override
    public float getFluidPct() {
        int capacity = this.getTankCapacity(1);
        int amount = this.getStoredFluidAmount();
        return (float)amount / (float)capacity;
    }

    public void setStoredFluidAmount(int amount) {
        amount = Math.max(0, Math.min(amount, this.getTankCapacity(1)));
        this.entityData.set(FLUIDAMT, amount);
    }

    public boolean hasStoredFluid() {
        return this.__cachedFluid.isSame(Fluids.EMPTY);
    }

    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        return this.drain(resource.getAmount(), action);
    }

    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (this.__cachedFluid == null || this.__cachedFluid.isSame(Fluids.EMPTY)) {
            return new FluidStack(Fluids.EMPTY, 0);
        }
        int amountRemoved = maxDrain;
        int fluidAmount = this.getStoredFluidAmount();
        Fluid fluid = this.__cachedFluid;
        if (fluidAmount < maxDrain) {
            amountRemoved = fluidAmount;
        }
        if ((fluidAmount -= amountRemoved) <= 0) {
            if (action == IFluidHandler.FluidAction.EXECUTE) {
                this.setStoredFluidAmount(0);
                this.setStoredFluid(Fluids.EMPTY);
                this.needsSync = true;
            }
        } else if (action == IFluidHandler.FluidAction.EXECUTE) {
            this.setStoredFluidAmount(fluidAmount);
            this.needsSync = true;
        }
        return new FluidStack(fluid, amountRemoved);
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (this.__cachedFluid != null && !this.__cachedFluid.isSame(Fluids.EMPTY) && !this.__cachedFluid.isSame(resource.getFluid())) {
            return 0;
        }
        int amountAdded = resource.getAmount();
        int resultingAmount = this.getStoredFluidAmount();
        if (resultingAmount + amountAdded > this.getTankCapacity(1)) {
            amountAdded = this.getTankCapacity(1) - resultingAmount;
        }
        resultingAmount += amountAdded;
        if (action == IFluidHandler.FluidAction.EXECUTE) {
            this.setStoredFluid(resource.getFluid());
            this.setStoredFluidAmount(resultingAmount);
        }
        return amountAdded;
    }

    public FluidStack getFluidInTank(int tank) {
        Fluid fluid = this.getStoredFluid();
        if (fluid != null) {
            return new FluidStack(fluid, this.getStoredFluidAmount());
        }
        return FluidStack.EMPTY;
    }

    public int getTankCapacity(int tank) {
        return (Integer)this.entityData.get(FLUID_CAP);
    }

    public int getTanks() {
        return 1;
    }

    public boolean isFluidValid(int tank, FluidStack stack) {
        return this.getFluidInTank(1).isEmpty() || this.getFluidInTank(1).getFluid().isSame(stack.getFluid());
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> this.handleAnimState((AnimationState<Construct>)state));
        controller.setCustomInstructionKeyframeHandler((AnimationController.CustomKeyframeHandler)this);
        registrar.add(new AnimationController[]{controller});
    }

    private PlayState handleAnimState(AnimationState<Construct> state) {
        block53: {
            block54: {
                if (!this.isAddedToLevel()) {
                    return PlayState.STOP;
                }
                if (this.forcedAnimation == null) break block53;
                if (!this.forcedAnimationIdleOnly) break block54;
                Vec3 vec3 = new Vec3(this.getX() - this.xOld, 0.0, this.getZ() - this.zOld);
                if (!(vec3.length() < 0.1)) break block53;
            }
            return state.setAndContinue(this.forcedAnimation.play());
        }
        boolean swim = false;
        if (this.canSwim()) {
            BlockPos bp = this.blockPosition();
            FluidState flState = this.level().getFluidState(bp);
            FluidState flStateBelow = this.level().getFluidState(bp.below());
            BlockState blState = this.level().getBlockState(bp);
            BlockState blStateBelow = this.level().getBlockState(bp.below());
            if (!blState.hasProperty((Property)BlockStateProperties.WATERLOGGED) && !blStateBelow.hasProperty((Property)BlockStateProperties.WATERLOGGED) && ((double)flState.getOwnHeight() > 0.8 || (double)flStateBelow.getOwnHeight() > 0.8)) {
                swim = true;
            }
        }
        if (ActionFlags.LEAP_START.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.leap").thenLoop("animation.construct.airborne_leaping"));
        }
        if (ActionFlags.LEAP_AIRBORNE.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.airborne_leaping"));
        }
        if (ActionFlags.LEAP_LAND.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.leap_land").thenLoop("animation.construct.idle"));
        }
        if (ActionFlags.DUAL_SWEEP.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.dual_sweep").thenLoop("animation.construct.idle"));
        }
        if (ActionFlags.DUAL_SHOOT.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.shoot_dual").thenLoop("animation.construct.idle"));
        }
        if (ActionFlags.DUAL_SPRAY.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.spray_both").thenLoop("animation.construct.idle"));
        }
        if (ActionFlags.DEFEATED.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.defeated_enter").thenLoop("animation.construct.defeated_loop"));
        }
        if (ActionFlags.DEFEATED_END.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.defeated_exit"));
        }
        if (ActionFlags.RANGED_ATTACK.isFlagSet(this.actionFlags)) {
            if (ActionFlags.SPRAY.isFlagSet(this.actionFlags)) {
                if (this.swingingArm == null || this.swingingArm == InteractionHand.MAIN_HAND) {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.spray_left"));
                }
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.spray_right"));
            }
            if (ActionFlags.CAST_SPELL_SWING.isFlagSet(this.actionFlags)) {
                if (this.swingingArm == null || this.swingingArm == InteractionHand.MAIN_HAND) {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.swing_left").thenLoop("animation.construct.idle"));
                }
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.swing_right").thenLoop("animation.construct.idle"));
            }
            if (ActionFlags.CAST_SPELL_CHANNEL.isFlagSet(this.actionFlags)) {
                if (this.swingingArm == null || this.swingingArm == InteractionHand.MAIN_HAND) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.channel_cast_left"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.channel_cast_right"));
            }
            if (this.swingingArm == null || this.swingingArm == InteractionHand.MAIN_HAND) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.shoot_left"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.shoot_right"));
        }
        if (ActionFlags.WATER.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.sprinkler"));
        }
        if (ActionFlags.EAT.isFlagSet(this.actionFlags)) {
            if (this.swingingArm == null || this.swingingArm == InteractionHand.MAIN_HAND) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.eat_left"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.eat_right"));
        }
        if (ActionFlags.FISH.isFlagSet(this.actionFlags)) {
            if (this.swingingArm == null || this.swingingArm == InteractionHand.MAIN_HAND) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.swing_left").thenLoop("animation.construct.fish_loop_left"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.swing_right").thenLoop("animation.construct.fish_loop_right"));
        }
        if (ActionFlags.FISH_END.isFlagSet(this.actionFlags)) {
            if (this.swingingArm == null || this.swingingArm == InteractionHand.MAIN_HAND) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.fish_end_left").thenLoop("animation.construct.idle"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.fish_end_right").thenLoop("animation.construct.idle"));
        }
        if (ActionFlags.MINE_ENTER.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.drill_enter").thenLoop("animation.construct.drill_loop"));
        }
        if (ActionFlags.MINE_LEAVE.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.drill_leave"));
        }
        if (ActionFlags.ADVENTURE_ENTER.isFlagSet(this.actionFlags)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.adventure_leave").thenLoop("animation.construct.idle"));
        }
        if (ActionFlags.ADVENTURE_LEAVE.isFlagSet(this.actionFlags)) {
            if (this.clearAnimCache) {
                state.getController().forceAnimationReset();
                this.clearAnimCache = false;
            }
            state.getController().setOverrideEasingType(EasingType.LINEAR);
            state.getController().transitionLength(1);
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.adventure_return"));
        }
        if (this.swinging) {
            if (this.swingingArm == InteractionHand.MAIN_HAND) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.swing_left"));
            }
            if (this.swingingArm == InteractionHand.OFF_HAND) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.swing_right"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct.dual_sweep"));
        }
        if (!(this.onGround() || this.canFly() || this.isInWaterOrBubble())) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.airborne"));
        }
        double speed = new Vec3(this.getX() - this.xOld, 0.0, this.getZ() - this.zOld).length();
        state.getController().transitionLength(5);
        if (this.canFly() && !this.onGround()) {
            if (speed > (double)0.1f && this.getDeltaMovement().y < speed / 4.0) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.fly_moving"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.fly_idle"));
        }
        if (speed > (double)0.1f) {
            if (swim) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.swim_loop"));
            }
            if (this.getMoodlets().getStrongestMoodlet() == 8) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.run_happy"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.run"));
        }
        if (speed > (double)0.02f) {
            if (swim) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.swim_loop"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.walk"));
        }
        if (this.dancing) {
            if (this.danceTicks < 148) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.dance_1"));
            }
            if (this.danceTicks < 432) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.dance_2"));
            }
            switch (this.danceIndex) {
                case 0: {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.dance_3"));
                }
                case 1: {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.dance_4"));
                }
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.dance_5"));
        }
        if (swim) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.swim_idle"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.construct.idle"));
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void handle(CustomInstructionKeyframeEvent<Construct> event) {
        String instructions = event.getKeyframeData().getInstructions();
        if (instructions.equals("hide")) {
            this.clearAnimCache = true;
            this.mine_leave_pfx = false;
            this.mine_return_pfx = false;
            this.adventure_leave_pfx = false;
            this.adventure_return_pfx = false;
        } else if (instructions.equals("show")) {
            this.clearAnimCache = false;
        } else if (instructions.equals("mine_enter_pfx_start")) {
            this.mine_leave_pfx = true;
            this.level().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), this.position().x, this.position().y, this.position().z, SFX.Spell.Impact.AoE.EARTH, this.getSoundSource(), 1.0f, 1.0f);
        } else if (instructions.equals("drill_leave_sound")) {
            this.level().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), this.position().x, this.position().y, this.position().z, SFX.Spell.Impact.AoE.EARTH, this.getSoundSource(), 1.0f, 1.0f);
        } else if (instructions.equals("mine_leave_pfx_start")) {
            this.mine_return_pfx = true;
        } else if (instructions.equals("mine_leave_pfx_stop")) {
            this.mine_return_pfx = false;
        } else if (instructions.equals("adventure_leave")) {
            this.adventure_leave_pfx = true;
            this.level().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), this.position().x, this.position().y, this.position().z, SFX.Spell.Impact.Single.WIND, this.getSoundSource(), 1.0f, 1.0f);
            this.pfxCounter = 0;
        } else if (instructions.equals("adventure_return")) {
            this.adventure_return_pfx = true;
            this.level().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), this.position().x, this.position().y, this.position().z, SFX.Spell.Impact.Single.WIND, this.getSoundSource(), 1.0f, 1.0f);
            this.pfxCounter = 0;
        } else if (instructions.equals("adventure_return_stop")) {
            this.level().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), this.position().x, this.position().y, this.position().z, SoundEvents.ARROW_HIT, this.getSoundSource(), 1.0f, 1.0f);
            this.adventure_return_pfx = false;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void spawnSmoothParticles(float partialTick) {
        block5: {
            block4: {
                Vec3 vel;
                Vec3 particlePos;
                Vec3 look;
                int i;
                if (!ActionFlags.SPRAY.isFlagSet(this.actionFlags) || !this.level().isClientSide() || this.getStoredFluid() == Fluids.EMPTY) break block4;
                FluidParameterRegistry.FluidParameter param = FluidParameterRegistry.INSTANCE.forFluid(this.getStoredFluid());
                float angle = (float)(Math.toRadians(60.0) * Math.sin((double)partialTick * Math.PI));
                for (i = 0; i < param.getParticleQuantity(); ++i) {
                    look = this.calculateViewVector(this.getXRot(), this.getYHeadRot());
                    look = look.yRot(angle - (float)Math.toRadians(40.0));
                    particlePos = this.position().add(0.0, 1.0, 0.0).add(look);
                    vel = param.adjustVelocity(look);
                    this.level().addParticle(param.particle(), particlePos.x(), particlePos.y(), particlePos.z(), vel.x, vel.y, vel.z);
                }
                if (!this.expandFluidRange()) break block5;
                angle = (float)(Math.toRadians(60.0) * -Math.sin((double)partialTick * Math.PI));
                for (i = 0; i < param.getParticleQuantity(); ++i) {
                    look = this.calculateViewVector(this.getXRot(), this.getYHeadRot());
                    look = look.yRot(angle + (float)Math.toRadians(40.0));
                    particlePos = this.position().add(0.0, 1.0, 0.0).add(look);
                    vel = param.adjustVelocity(look);
                    this.level().addParticle(param.particle(), particlePos.x(), particlePos.y(), particlePos.z(), vel.x, vel.y, vel.z);
                }
                break block5;
            }
            if (ActionFlags.WATER.isFlagSet(this.actionFlags)) {
                for (int i = 0; i < 20; ++i) {
                    this.level().addParticle((ParticleOptions)ParticleTypes.SPLASH, this.position().x, this.position().y + (double)0.9f + Math.random(), this.position().z, -0.5 + Math.random(), 0.0, -0.5 + Math.random());
                }
            }
        }
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.getStackInSlot(slot);
        if (stack.isEmpty() || stack.getCount() < amount) {
            return ItemStack.EMPTY;
        }
        if (!simulate) {
            ItemStack output = stack.split(amount);
            return output;
        }
        ItemStack output = stack.copy();
        output.setCount(amount);
        return output;
    }

    public int getSlotLimit(int slot) {
        if (this.constructCapabilities != null) {
            return this.constructCapabilities.calculateInventoryStackLimit();
        }
        return 1;
    }

    public int getSlots() {
        if (this.constructCapabilities != null) {
            return this.constructCapabilities.calculateInventorySize();
        }
        return 0;
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }
        return (ItemStack)this.inventory.get(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = this.getStackInSlot(slot);
        if (existing.isEmpty()) {
            if (!simulate) {
                this.inventory.set(slot, stack);
            }
            return ItemStack.EMPTY;
        }
        if (existing.isDamaged()) {
            return stack;
        }
        if (existing.has(DataComponents.CUSTOM_DATA) && !ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, existing)) {
            return stack;
        }
        int countToInsert = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
        ItemStack clone = stack.copy();
        clone.shrink(countToInsert);
        if (!simulate) {
            existing.setCount(existing.getCount() + countToInsert);
        }
        return clone;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return slot >= 0 && slot < this.inventory.size();
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.inventory.size()) {
            return;
        }
        this.inventory.set(slot, stack);
    }

    @Override
    public void dropAllItems() {
        if (!this.getMainHandItem().isEmpty()) {
            this.spawnAtLocation(this.getMainHandItem());
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (!this.getOffhandItem().isEmpty()) {
            this.spawnAtLocation(this.getOffhandItem());
            this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
        if (!this.getConstructData().getHat().isEmpty()) {
            this.setHat(ItemStack.EMPTY);
        }
        for (int i = 0; i < this.getSlots(); ++i) {
            ItemStack invStack = this.getStackInSlot(i);
            if (invStack.isEmpty()) continue;
            this.spawnAtLocation(invStack);
            this.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static void SpawnSmashParticles(Level world, Vec3 position) {
        double offsetZ;
        double offsetX;
        int i;
        for (i = 0; i < 10; ++i) {
            offsetX = -0.5 + Math.random();
            offsetZ = -0.5 + Math.random();
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setGravity(0.02f).setScale(0.1f), position.x + offsetX, position.y, position.z + offsetZ, -offsetX, 0.2 + 0.4 * Math.random(), -offsetZ);
        }
        for (i = 0; i < 10; ++i) {
            offsetX = -0.5 + Math.random();
            offsetZ = -0.5 + Math.random();
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setGravity(0.02f).setScale(0.1f), position.x + offsetX, position.y, position.z + offsetZ, -offsetX / 2.0, 0.2 + 0.4 * Math.random(), -offsetZ / 2.0);
        }
        for (i = 0; i < 10; ++i) {
            offsetX = -0.5 + Math.random();
            offsetZ = -0.5 + Math.random();
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setGravity(0.02f).setScale(0.2f), position.x + offsetX, position.y, position.z + offsetZ, 0.0, 0.2 + 0.4 * Math.random(), 0.0);
        }
        world.playLocalSound(position.x, position.y, position.z, SFX.Spell.Impact.AoE.EARTH, SoundSource.PLAYERS, 1.0f, 0.9f + (float)Math.random() * 0.2f, false);
    }

    @OnlyIn(value=Dist.CLIENT)
    public static void SpawnCritParticles(Level world, Vec3 position, CompoundTag tag) {
        if (tag == null) {
            return;
        }
        int entityID = tag.getInt("entityID");
        Entity e = world.getEntity(entityID);
        if (e != null) {
            Minecraft mc = Minecraft.getInstance();
            mc.particleEngine.createTrackingEmitter(e, (ParticleOptions)ParticleTypes.CRIT);
        }
    }

    public UUID getOwnerUUID() {
        return this.getOwnerId();
    }

    private   boolean lambda$tickDefeated$0(LivingEntity owner, Mob e) {
        return !SummonUtils.isTargetFriendly((Entity)e, owner) && this.getSensing().hasLineOfSight((Entity)e);
    }

    static enum ActionFlags {
        RANGED_ATTACK(1),
        MINE_ENTER(2),
        MINE_LEAVE(4),
        CRUSH(8),
        SPRAY(16),
        WATER(32),
        FISH(64),
        FISH_END(128),
        EAT(256),
        ADVENTURE_ENTER(512),
        ADVENTURE_LEAVE(1024),
        AWAY(2048),
        CAST_SPELL_SWING(4096),
        CAST_SPELL_CHANNEL(8192),
        DEFEATED(16384),
        DEFEATED_END(32768),
        LEAP_START(65536),
        LEAP_AIRBORNE(131072),
        LEAP_LAND(262144),
        DUAL_SWEEP(524288),
        DUAL_SHOOT(0x100000),
        DUAL_SPRAY(0x200000);

        private final int flag;

        private ActionFlags(int flag) {
            this.flag = flag;
        }

        public boolean isFlagSet(int mutex) {
            return (mutex & this.flag) != 0;
        }

        int set(int actionFlags) {
            return actionFlags | this.flag;
        }

        int clear(int actionFlags) {
            return actionFlags & ~this.flag;
        }
    }

    class ConstructSwimGoal
    extends Goal {
        public ConstructSwimGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP));
            Construct.this.getNavigation().setCanFloat(true);
        }

        public boolean canUse() {
            if (!Construct.this.canSwim() || Construct.this.hasControllingPassenger()) {
                return false;
            }
            return Construct.this.isInWater() && Construct.this.getFluidTypeHeight((NeoForgeMod.WATER_TYPE).value()) > Construct.this.getFluidJumpThreshold() || Construct.this.isInLava();
        }

        public void tick() {
            if (Construct.this.getRandom().nextFloat() < 0.8f) {
                Construct.this.jumpControl.jump();
            }
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }
}


