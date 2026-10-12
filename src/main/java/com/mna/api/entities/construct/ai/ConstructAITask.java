 
package com.mna.api.entities.construct.ai;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.entities.construct.ai.IMutexManager;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class ConstructAITask<T extends ConstructAITask<?>>
extends Goal {
    protected static final int MOVE_COOLDOWN = 20;
    public static final int SWING_BUFFER_TIME = 5;
    public static final int MELEE_ATTACK_TIME = 8;
    public static final int MELEE_ATTACK_DIST = 2;
    protected IConstruct<?> construct;
    protected IMutexManager mutexManager;
    protected Player owner;
    protected int exitCode = -1;
    protected boolean hasStarted = false;
    protected boolean isOneOff = false;
    protected UUID oneOffFollowTarget = null;
    protected int moveCooldown;
    protected int fleeCooldown;
    protected int teleportCooldown = 0;
    private BlockPos moveBlockTarget = null;
    private Entity moveEntityTarget = null;
    protected int attackCooldown = 0;
    private List<ConstructAITaskParameter> _parameters;
    private String id = null;
    protected final ResourceLocation guiIcon;
    private boolean isStart = false;
    private final HashMap<Integer, String> connections;

    public ConstructAITask(IConstruct<?> construct, ResourceLocation guiIcon) {
        this.construct = construct;
        this._parameters = this.instantiateParameters();
        this.connections = new HashMap();
        this.guiIcon = guiIcon;
        List<String> ids = this._parameters.stream().map(p -> p.getId()).toList();
        List<String> badIds = ids.stream().filter(i -> Collections.frequency(ids, i) > 1).distinct().toList();
        if (badIds.size() > 0) {
            throw new IllegalArgumentException("Duplicate Lodestar Parameter ID found in construct task \"" + this.getType().toString() + "\". All parameter IDs must be unique within a single task.  Offending IDs: " + String.join((CharSequence)", ", badIds));
        }
    }

    public IConstruct<?> getConstruct() {
        return this.construct;
    }

    @Nullable
    public IMutexManager getMutexManager() {
        return this.mutexManager;
    }

    public final String getId() {
        return this.id;
    }

    public final boolean isStart() {
        return this.isStart;
    }

    public abstract ResourceLocation getType();

    public final boolean isOneOff() {
        return this.isOneOff;
    }

    public final UUID getOneOffFollowTarget() {
        return this.oneOffFollowTarget;
    }

    public final AbstractGolem getConstructAsEntity() {
        return this.getConstruct().asEntity();
    }

    public int getInteractTime(ConstructCapability capability, int baseline) {
        if (this.getConstruct() == null) {
            return 30;
        }
        ConstructMaterial material = this.getConstruct().getConstructData().getLowestMaterialCooldownMultiplierForCapability(capability);
        if (material == null) {
            return baseline;
        }
        return (int)Math.max(material.getCooldownMultiplierFor(capability) * (float)baseline, 5.0f);
    }

    public int getInteractTime(ConstructCapability capability) {
        return this.getInteractTime(capability, 50);
    }

    public ConstructCapability[] requiredCapabilities() {
        return new ConstructCapability[0];
    }

    public boolean allowSteeringMountedConstructsDuringTask() {
        return false;
    }

    public final String getConnectionForResultCode(int result) {
        return this.connections.getOrDefault(result, null);
    }

    protected Player createFakePlayer() {
        UUID ownerID = this.construct.getOwnerId();
        FakePlayer fakePlayer = null;
        if (ownerID == null) {
            fakePlayer = FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)this.construct.asEntity().level()));
        } else {
            fakePlayer = FakePlayerFactory.get((ServerLevel)((ServerLevel)this.construct.asEntity().level()), (GameProfile)new GameProfile(ownerID, "[Construct Fake Player]"));
            IPlayerProgression p = (IPlayerProgression)fakePlayer.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (p != null) {
                this.construct.getOwner();
                p.setTier(this.construct.getEquivalentTier(), null);
                p.setAlliedFaction(this.construct.getFaction(), null);
            }
        }
        fakePlayer.setPos(this.construct.asEntity().position());
        return fakePlayer;
    }

    public boolean hasStarted() {
        return this.hasStarted;
    }

    public boolean areCapabilitiesMet() {
        return this.construct.getConstructData().areCapabilitiesEnabled(this.requiredCapabilities());
    }

    protected Optional<ConstructAITaskParameter> getParameter(String id) {
        return this.getParameters().stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<String> getConnectedIDs() {
        return this.connections.values().stream().toList();
    }

    public int getRequiredIntelligence() {
        return 1;
    }

    public boolean hasMoveTarget() {
        return this.moveBlockTarget != null || this.moveEntityTarget != null;
    }

    public BlockPos getMoveBlockTarget() {
        return this.moveBlockTarget;
    }

    public Entity getMoveEntityTarget() {
        return this.moveEntityTarget;
    }

    public final void setIdAndIsStart(String id, boolean isStart) {
        if (this.id == null) {
            this.id = id;
            this.isStart = isStart;
        }
    }

    public void setConstruct(IConstruct<?> construct) {
        this.construct = construct;
    }

    public void setMutexManager(IMutexManager mutexManager) {
        this.mutexManager = mutexManager;
    }

    public void clearMoveTarget() {
        this.moveBlockTarget = null;
        this.moveEntityTarget = null;
    }

    public void setMoveTarget(BlockPos pos) {
        this.moveBlockTarget = pos;
        this.moveEntityTarget = null;
        if (pos != null) {
            this.construct.getDiagnostics().setMovePos(Vec3.atCenterOf((Vec3i)pos));
        } else {
            this.construct.getDiagnostics().setMovePos(null);
        }
    }

    public void setMoveTarget(Entity entity) {
        this.moveBlockTarget = null;
        this.moveEntityTarget = entity;
        if (entity != null) {
            this.construct.getDiagnostics().pushTaskUpdate(this.id, this.guiIcon, IConstructDiagnostics.Status.RUNNING, entity.getId());
        } else {
            this.construct.getDiagnostics().pushTaskUpdate(this.id, this.guiIcon, IConstructDiagnostics.Status.RUNNING, -1);
        }
    }

    public final void setOneOff(Player followWhenDone) {
        this.isOneOff = true;
        this.oneOffFollowTarget = followWhenDone != null && followWhenDone.getGameProfile() != null ? followWhenDone.getGameProfile().getId() : null;
    }

    public abstract void inflateParameters();

    public void onTaskSet() {
        this.exitCode = -1;
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public void tick() {
        if (this.moveCooldown > 0) {
            --this.moveCooldown;
        }
        if (this.attackCooldown > 0) {
            --this.attackCooldown;
        }
        if (this.teleportCooldown > 0) {
            --this.teleportCooldown;
        }
        if (this.fleeCooldown > 0) {
            --this.fleeCooldown;
        }
        if (this.owner != null && !this.owner.isAlive()) {
            this.owner = null;
        }
    }

    public boolean canUse() {
        if (this.construct == null) {
            return false;
        }
        if (!this.areCapabilitiesMet()) {
            this.confuseConstructCapsMissing();
            this.forceFail();
            return false;
        }
        return true;
    }

    public boolean canContinueToUse() {
        return !this.isFinished();
    }

    public void start() {
        if (this.construct != null) {
            this.owner = this.construct.getOwner();
        }
        this.hasStarted = true;
        this.construct.getDiagnostics().pushTaskUpdate(this.id, this.guiIcon, IConstructDiagnostics.Status.RUNNING, -1);
    }

    public boolean isFinished() {
        return this.exitCode >= 0;
    }

    public boolean isSuccess() {
        return this.exitCode == 0;
    }

    public void stop() {
        super.stop();
        this.hasStarted = false;
        this.releaseMutexes();
        if (this.exitCode == 1) {
            this.construct.getDiagnostics().pushTaskUpdate(this.id, this.guiIcon, IConstructDiagnostics.Status.FAILURE, -1);
        } else {
            this.construct.getDiagnostics().pushTaskUpdate(this.id, this.guiIcon, IConstructDiagnostics.Status.SUCCESS, -1);
        }
    }

    protected boolean claimBlockMutex(BlockPos pos) {
        if (this.getMutexManager() != null) {
            return this.getMutexManager().claimMutex(pos, this.construct, this);
        }
        return true;
    }

    protected void releaseBlockMutex(BlockPos pos) {
        if (this.getMutexManager() != null) {
            this.getMutexManager().releaseMutex(pos, this.construct, this);
        }
    }

    protected boolean claimEntityMutex(Entity entity) {
        if (this.getMutexManager() != null) {
            return this.getMutexManager().claimMutex(entity, this.construct, this);
        }
        return true;
    }

    protected void releaseEntityMutex(Entity entity) {
        if (this.getMutexManager() != null) {
            this.getMutexManager().releaseMutex(entity, this.construct, this);
        }
    }

    public void releaseMutexes() {
        if (this.getMutexManager() != null) {
            this.getMutexManager().releaseAllMutexes(this.construct, this);
        }
    }

    protected void pushDiagnosticMessage(String message, boolean allowDuplicates) {
        this.construct.pushDiagnosticMessage(message, this.guiIcon, allowDuplicates);
    }

    protected void pushDiagnosticMessage(Component message, boolean allowDuplicates) {
        this.pushDiagnosticMessage(message.getContents().toString(), allowDuplicates);
    }

    public void pushDiagnosticMessage(Component message) {
        this.pushDiagnosticMessage(message, false);
    }

    public void pushDiagnosticMessage(String message) {
        this.pushDiagnosticMessage(message, false);
    }

    public final void confuseConstructCapsMissing() {
        String cap = Arrays.asList(this.requiredCapabilities()).stream().map(c -> c.toString()).collect(Collectors.joining(", "));
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.capability_missing", cap));
        this.construct.setConfused(100);
    }

    public final void confuseConstructLowIntelligence() {
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.low_intelligence", new Object[0]));
        this.construct.setConfused(100);
    }

    public void insertOrDiscardItem(ItemStack stack) {
        int i;
        AbstractGolem c = this.getConstructAsEntity();
        int limit = this.getConstruct().getCarrySize();
        InteractionHand[] hands = this.getConstruct().getCarryingHands();
        for (i = 0; i < hands.length; ++i) {
            ItemStack carried = c.getItemInHand(hands[i]);
            if (!ItemStack.matches((ItemStack)stack, (ItemStack)carried)) continue;
            if (carried.getCount() + stack.getCount() <= limit) {
                carried.setCount(carried.getCount() + stack.getCount());
                return;
            }
            if (carried.getCount() >= limit) continue;
            int remaining = limit - carried.getCount();
            carried.setCount(limit);
            stack.setCount(remaining);
        }
        hands = this.getConstruct().getEmptyHands();
        for (i = 0; i < hands.length; ++i) {
            if (stack.getCount() <= limit) {
                c.setItemInHand(hands[i], stack);
                return;
            }
            ItemStack copy = stack.copy();
            copy.setCount(limit);
            stack.setCount(stack.getCount() - limit);
            c.setItemInHand(hands[i], copy);
        }
        if (!stack.isEmpty() && this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.ITEM_STORAGE)) {
            ManaAndArtificeMod.getInventoryHelper().mergeIntoInventory((IItemHandler)this.construct, stack);
        }
    }

    public void insertOrDropItem(ItemStack stack) {
        int i;
        AbstractGolem c = this.getConstructAsEntity();
        int limit = this.getConstruct().getCarrySize();
        InteractionHand[] hands = this.getConstruct().getCarryingHands();
        for (i = 0; i < hands.length; ++i) {
            ItemStack carried = c.getItemInHand(hands[i]);
            if (!ItemStack.matches((ItemStack)stack, (ItemStack)carried)) continue;
            if (carried.getCount() + stack.getCount() <= limit) {
                carried.setCount(carried.getCount() + stack.getCount());
                return;
            }
            if (carried.getCount() >= limit) continue;
            int remaining = limit - carried.getCount();
            carried.setCount(limit);
            stack.setCount(remaining);
        }
        hands = this.getConstruct().getEmptyHands();
        for (i = 0; i < hands.length; ++i) {
            if (stack.getCount() <= limit) {
                c.setItemInHand(hands[i], stack);
                return;
            }
            ItemStack copy = stack.copy();
            copy.setCount(limit);
            stack.setCount(stack.getCount() - limit);
            c.setItemInHand(hands[i], copy);
        }
        if (!(stack.isEmpty() || this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.ITEM_STORAGE) && ManaAndArtificeMod.getInventoryHelper().mergeIntoInventory((IItemHandler)this.construct, stack))) {
            ItemEntity drop = new ItemEntity(c.level(), c.getX(), c.getY(), c.getZ(), stack);
            drop.setDeltaMovement(0.0, 0.25, 0.0);
            drop.setDefaultPickUpDelay();
            c.level().addFreshEntity((Entity)drop);
        }
    }

    public List<ItemStack> getHeldItems() {
        AbstractGolem c = this.getConstructAsEntity();
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        if (!c.getMainHandItem().isEmpty()) {
            stacks.add(c.getMainHandItem());
        }
        if (!c.getOffhandItem().isEmpty()) {
            stacks.add(c.getOffhandItem());
        }
        return stacks;
    }

    protected List<ItemStack> getLootRoll(ResourceLocation lootTable, ItemStack tool, LootContextParamSet paramSet) {
        AbstractGolem c = this.construct.asEntity();
        LootParams lootparams = new LootParams.Builder((ServerLevel)c.level()).withParameter(LootContextParams.THIS_ENTITY, this.construct.asEntity()).withParameter(LootContextParams.ORIGIN, this.construct.asEntity().position()).withParameter(LootContextParams.TOOL, tool).create(paramSet);
        LootTable loottable = c.level().getServer().reloadableRegistries().getLootTable(lootTable);
        return loottable.getRandomItems(lootparams);
    }

    protected void swingHandWithCapability(ConstructCapability cap) {
        this.construct.getHandWithCapability(ConstructCapability.SHEAR).ifPresent(h -> this.construct.asEntity().swing(h));
    }

    public boolean doMove() {
        return this.doMove(2.5f);
    }

    public boolean doMove(float closeEnough) {
        if (this.moveEntityTarget != null && !this.moveEntityTarget.isAlive()) {
            this.moveEntityTarget = null;
        }
        if (this.moveBlockTarget == null && this.moveEntityTarget == null) {
            return false;
        }
        Vec3 invPos = this.moveEntityTarget != null ? this.moveEntityTarget.position().add(0.0, 0.25, 0.0) : new Vec3((double)this.moveBlockTarget.getX() + 0.5, (double)this.moveBlockTarget.getY(), (double)this.moveBlockTarget.getZ() + 0.5);
        AbstractGolem c = this.getConstructAsEntity();
        this.construct.getDiagnostics().setMovePos(invPos);
        double dist = c.distanceToSqr(invPos);
        boolean movecomplete = false;
        if (dist > (double)(closeEnough * closeEnough)) {
            if (this.moveCooldown == 0 || c.getNavigation().isDone()) {
                movecomplete = this.moveEntityTarget != null ? this.setPathToEntity(this.moveEntityTarget, closeEnough) : this.setPathToXYZ(invPos, closeEnough);
                this.moveCooldown = 20;
            }
        } else {
            movecomplete = true;
        }
        if (movecomplete) {
            c.getNavigation().stop();
            this.construct.getDiagnostics().setMovePos(null);
            return true;
        }
        return false;
    }

    protected final String translate(String key, Object ... args) {
        return Component.translatable((String)key, (Object[])args).getString();
    }

    protected final String translate(BlockEntity te) {
        if (te == null) {
            return "Null Tile Entity";
        }
        return this.translate(te.getBlockState());
    }

    protected final String translate(ItemStack stack) {
        return this.translate(stack.getDescriptionId(), new Object[0]);
    }

    protected final String translate(FluidStack stack) {
        return this.translate(stack.getTranslationKey(), new Object[0]);
    }

    protected final String translate(BlockState state) {
        if (state == null) {
            return "Null Block state";
        }
        return this.translate(state.getBlock().getDescriptionId(), new Object[0]);
    }

    protected final String translate(Entity e) {
        if (e == null) {
            return "Null Entity";
        }
        return this.translate(e.getType().getDescriptionId(), new Object[0]);
    }

    protected boolean setPathToEntity(Entity entity, float closeEnough) {
        if (this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.TELEPORT) && this.teleportCooldown <= 0) {
            return this.setPathToXYZ(entity.position(), closeEnough);
        }
        double dist = entity.position().distanceToSqr(this.construct.asEntity().position());
        if (dist < (double)(closeEnough * closeEnough)) {
            return true;
        }
        AbstractGolem c = this.getConstructAsEntity();
        c.setSpeed(Math.min((float)c.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.55f));
        if (!c.getNavigation().moveTo(entity, (double)c.getSpeed())) {
            if (c.getNavigation().isDone()) {
                return true;
            }
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.move_entity_fail", this.translate(entity)), false);
        }
        return false;
    }

    protected boolean setPathToXYZ(Vec3 pos, float closeEnough) {
        AbstractGolem c = this.getConstructAsEntity();
        double dist = pos.distanceToSqr(c.position());
        if (dist < (double)(closeEnough * closeEnough)) {
            return true;
        }
        if (this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.TELEPORT) && this.teleportCooldown <= 0) {
            Vec3 targetPos = pos.add(this.construct.asEntity().position().subtract(pos).normalize().scale((double)(closeEnough * 0.95f)));
            if ((dist > 36.0 || this.construct.asEntity().horizontalCollision || c.level().clip(new ClipContext(c.getEyePosition(), targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)c)).getType() != HitResult.Type.MISS) && this.construct.getTeleportCooldown() <= 0) {
                BlockHitResult bhr = c.level().clip(new ClipContext(targetPos.add(0.0, 2.0, 0.0), targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)c));
                Vec3 teleportPos = targetPos;
                boolean canTeleport = true;
                if (bhr != null && bhr.getType() != HitResult.Type.MISS) {
                    BlockPos bp = BlockPos.containing((Position)bhr.getLocation());
                    if (bhr.isInside() || !this.isTeleportDestinationValid(c, bp)) {
                        canTeleport = false;
                        for (int xShift = 0; xShift <= 2 && !canTeleport; ++xShift) {
                            block1: for (int yShift = -1; yShift <= 1; ++yShift) {
                                for (int zShift = 0; zShift <= 2; ++zShift) {
                                    if (xShift == 0 && zShift == 0 || (bhr = c.level().clip(new ClipContext(targetPos.add((double)xShift, (double)(2 + yShift), (double)zShift), targetPos.add((double)xShift, (double)yShift, (double)zShift), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)c))) == null || !this.isTeleportDestinationValid(c, bp = BlockPos.containing((Position)bhr.getLocation()))) continue;
                                    teleportPos = bhr.getLocation();
                                    canTeleport = true;
                                    continue block1;
                                }
                            }
                        }
                    } else {
                        teleportPos = bhr.getLocation();
                    }
                }
                if (canTeleport) {
                    this.construct.setTeleportCooldown(this.getInteractTime(ConstructCapability.TELEPORT));
                    c.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                    c.level().playSound((Player)null, c.xo, c.yo, c.zo, SoundEvents.ENDERMAN_TELEPORT, c.getSoundSource(), 0.01f, 1.0f);
                    c.level().broadcastEntityEvent((Entity)c, (byte)46);
                    c.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.1f, 1.0f);
                    c.lookAt(EntityAnchorArgument.Anchor.FEET, pos);
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.move_block_success", Math.floor(pos.x * 100.0) / 100.0, Math.floor(pos.y * 100.0) / 100.0, Math.floor(pos.z * 100.0) / 100.0), false);
                    return true;
                }
            }
        }
        c.setSpeed(Math.min((float)c.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.55f));
        if (!c.getNavigation().moveTo(pos.x, pos.y, pos.z, (double)c.getSpeed())) {
            BlockPos blockPos = BlockPos.containing((Position)c.position());
            double[] dists = new double[]{blockPos.distToCenterSqr((Position)pos), blockPos.offset(1, 0, 0).distToCenterSqr((Position)pos), blockPos.offset(0, 0, 1).distToCenterSqr((Position)pos), blockPos.offset(1, 0, 1).distToCenterSqr((Position)pos)};
            double ceSqr = closeEnough * closeEnough;
            for (double bpDist : dists) {
                if (!(bpDist <= ceSqr)) continue;
                return true;
            }
            c.lookAt(EntityAnchorArgument.Anchor.FEET, pos);
            c.move(MoverType.SELF, pos.subtract(c.position()).normalize().scale((double)((float)c.getAttributeValue(Attributes.MOVEMENT_SPEED))));
        }
        return false;
    }

    protected final boolean isTeleportDestinationValid(AbstractGolem c, BlockPos pos) {
        return c.level().getBlockStates(new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(pos), net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.above()))).allMatch(state -> state.isAir());
    }

    protected boolean doAttack() {
        this.moveEntityTarget = this.construct.asEntity().getTarget();
        int ranged_result = this.doRangedAttack();
        if (ranged_result != -1) {
            return ranged_result != 0;
        }
        return this.doMeleeAttack();
    }

    private boolean doMeleeAttack() {
        if (!this.construct.getConstructData().areCapabilitiesEnabled(ConstructCapability.MELEE_ATTACK)) {
            return true;
        }
        if (this.moveEntityTarget instanceof LivingEntity && this.construct.validateFriendlyTarget((LivingEntity)this.moveEntityTarget)) {
            this.construct.asEntity().setTarget(null);
            return true;
        }
        if (this.doMove(2.0f)) {
            this.construct.asEntity().lookAt((Entity)this.construct.asEntity().getTarget(), 10.0f, 10.0f);
            if (this.attackCooldown <= 0 && this.construct.getAttackDelay() <= 0) {
                this.construct.getHandWithCapability(ConstructCapability.MELEE_ATTACK).ifPresent(h -> {
                    if (this.construct.canDualSweep() && Math.random() < 0.25) {
                        this.construct.setDualSweeping();
                        DelayedEventQueue.pushEvent(this.construct.asEntity().level(), new TimedDelayedEvent<Entity>("attack", 8, this.moveEntityTarget, this::meleeSweepCallback));
                    } else {
                        this.construct.asEntity().swing(h);
                        DelayedEventQueue.pushEvent(this.construct.asEntity().level(), new TimedDelayedEvent<Entity>("attack", 8, this.moveEntityTarget, this::meleeAttackCallback));
                    }
                });
                return true;
            }
        }
        return false;
    }

    private void meleeAttackCallback(String identifier, Entity target) {
        if (target == null) {
            return;
        }
        this.attackCooldown = 10;
        this.construct.asEntity().doHurtTarget(target);
    }

    private void meleeSweepCallback(String identifier, Entity target) {
        if (target == null) {
            return;
        }
        this.attackCooldown = 10;
        AbstractGolem c = this.construct.asEntity();
        for (LivingEntity livingentity : c.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.0, 0.25, 2.0))) {
            if (livingentity == c || c.isAlliedTo((Entity)livingentity) || !(c.distanceToSqr((Entity)livingentity) < 9.0)) continue;
            livingentity.knockback((double)0.4f, (double)Mth.sin((float)(c.getYRot() * ((float)Math.PI / 180))), (double)(-Mth.cos((float)(c.getYRot() * ((float)Math.PI / 180)))));
            c.doHurtTarget((Entity)livingentity);
        }
        c.level().playSound((Player)null, c.getX(), c.getY(), c.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, c.getSoundSource(), 1.0f, 1.0f);
        if (c.level() instanceof ServerLevel) {
            double d0 = -Mth.sin((float)(c.getYRot() * ((float)Math.PI / 180)));
            double d1 = Mth.cos((float)(c.getYRot() * ((float)Math.PI / 180)));
            ((ServerLevel)c.level()).sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, c.getX() + d0, c.getY(0.5), c.getZ() + d1, 0, d0, 0.0, d1, 0.0);
        }
    }

    private int doRangedAttack() {
        if (!(this.construct.isRangedAttacking() || this.construct.canManaCannonAttack() || this.construct.canFluidSpray() || this.construct.canSpellCast())) {
            return -1;
        }
        AbstractGolem constructEntity = this.construct.asEntity();
        LivingEntity target = constructEntity.getTarget();
        double dist = constructEntity.distanceTo((Entity)target);
        if (target != this.construct.getOwner() && dist < 2.0 && this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.MELEE_ATTACK)) {
            return -1;
        }
        int closeEnoughDist = 16;
        if (this.construct.canFluidSpray()) {
            closeEnoughDist = 6;
        }
        if ((this.fleeCooldown == 0 || constructEntity.getNavigation().isDone()) && this.doMove(closeEnoughDist)) {
            this.construct.asEntity().getLookControl().setLookAt((Entity)this.construct.asEntity().getTarget());
            if (this.construct.getAttackDelay() <= 0 && this.construct.asEntity().getLookControl().isLookingAtTarget() && this.construct.performRangedAttack(this.construct.asEntity().getTarget())) {
                return 1;
            }
        }
        return 0;
    }

    public abstract boolean isFullyConfigured();

    protected void faceBlockPos(BlockPos pos) {
        AbstractGolem c = this.getConstructAsEntity();
        double d0 = (double)pos.getX() + 0.5 - c.getX();
        double d2 = (double)pos.getZ() + 0.5 - c.getZ();
        double d1 = (double)pos.getY() + 0.5;
        double d3 = Mth.sqrt((float)((float)(d0 * d0 + d2 * d2)));
        float f = (float)(Mth.atan2((double)d2, (double)d0) * 57.2957763671875) - 90.0f;
        float f1 = (float)(-(Mth.atan2((double)d1, (double)d3) * 57.2957763671875));
        c.xRotO = c.getXRot();
        c.setXRot(this.updateRotation(c.getXRot(), f1, 30.0f));
        c.yRotO = c.getYRot();
        c.setYRot(this.updateRotation(c.getYRot(), f, 30.0f));
        c.yBodyRotO = c.getYHeadRot();
        c.setYHeadRot(this.updateRotation(c.getYRot(), f, 30.0f));
    }

    private float updateRotation(float start, float end, float step) {
        float f = Mth.wrapDegrees((float)(end - start));
        if (f > step) {
            f = step;
        }
        if (f < -step) {
            f = -step;
        }
        return start + f;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public abstract T copyFrom(ConstructAITask<?> var1);

    public final void copyConnections(ConstructAITask<?> other) {
        this.connections.clear();
        other.connections.forEach((k, v) -> this.connections.put((Integer)k, (String)v));
    }

    public abstract void readNBT(CompoundTag var1);

    public final CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", this.getType().toString());
        this.writeInternal(nbt);
        return nbt;
    }

    protected abstract CompoundTag writeInternal(CompoundTag var1);

    public abstract T duplicate();

    public final void loadParameterData(ListTag list) {
        if (!list.isEmpty() && list.getElementType() != 10) {
            ManaAndArtificeMod.LOGGER.warn("M&A >> construct parameters were not a list of NBT tags; skipping.  This task will be unconfigured.");
            return;
        }
        for (int i = 0; i < list.size(); ++i) {
            String id;
            if (i >= this._parameters.size()) {
                return;
            }
            CompoundTag tag = list.getCompound(i);
            if (tag == null || (id = tag.getString("parameter_id")) == null) continue;
            Optional<ConstructAITaskParameter> param = this._parameters.stream().filter(p -> p.getId().equals(id)).findFirst();
            if (param.isPresent()) {
                param.get().loadData(tag);
                continue;
            }
            ManaAndArtificeMod.LOGGER.warn("M&A >> construct task parameter " + id + " not found.  Skipping.");
        }
    }

    public final ListTag saveParameterData() {
        ListTag list = new ListTag();
        this._parameters.forEach(p -> list.add(p.saveData()));
        return list;
    }

    public final void loadConnections(ListTag list) {
        list.forEach(t -> {
            CompoundTag connection;
            if (t instanceof CompoundTag && (connection = (CompoundTag)t).contains("index") && connection.contains("target")) {
                String id = connection.getString("target");
                int index = connection.getInt("index");
                this.connections.put(index, id);
            }
        });
    }

    protected List<ConstructAITaskParameter> instantiateParameters() {
        return new ArrayList<ConstructAITaskParameter>();
    }

    public List<ConstructAITaskParameter> getParameters() {
        return this._parameters;
    }

    public static final ConstructTask getTypeFromNBT(CompoundTag nbt) {
        if (nbt.contains("type", 8)) {
            ResourceLocation id = ResourceLocation.parse(nbt.getString("type"));
            ConstructTask task = (ConstructTask)ManaAndArtificeMod.getConstructTaskRegistry().getValue(id);
            if (task != null) {
                return task;
            }
        }
        return (ConstructTask)ManaAndArtificeMod.getConstructTaskRegistry().getValue(ResourceLocation.fromNamespaceAndPath("mna", "stay"));
    }

    public String getNextTask() {
        return this.connections.getOrDefault(this.exitCode, null);
    }

    public void forceFail() {
        this.exitCode = 1;
    }

    public void setSuccessCode() {
        this.exitCode = 0;
    }
}


