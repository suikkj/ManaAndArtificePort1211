/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.commands.arguments.EntityAnchorArgument$Anchor
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  software.bernie.geckolib.animatable.GeoBlockEntity
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.blocks.artifice.ConstructWorkbenchBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.entities.constructs.ai.ConstructCommandFollowAndGuard;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.constructs.animated.ConstructConstruction;
import com.mna.entities.sorcery.AffinityIcon;
import com.mna.events.EventDispatcher;
import com.mna.items.ItemInit;
import com.mna.particles.types.movers.ParticleBezierMover;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleSphereOrbitMover;
import com.mna.particles.types.movers.ParticleVelocityMover;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.tools.MNAItemData;

public class ConstructWorkbenchTile
extends BlockEntity
implements GeoBlockEntity {
    private static final String NBT_CRAFTING = "crafting";
    private static final String NBT_CRAFT_TICKS = "craft_ticks";
    private static final String NBT_CRAFTER = "crafter";
    private static final String NBT_NAME = "custom_name";
    private static final String NBT_KNOWN_POS = "known_pos";
    IConstructConstruction construct;
    private boolean crafting = false;
    private boolean entityInside = false;
    private int craftTicks = 0;
    private UUID crafter;
    ArrayList<PosAffinityData> knownPositions;
    private String constructName = null;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public ConstructWorkbenchTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.CONSTRUCT_WORKBENCH.get(), pos, state);
        this.construct = new ConstructConstruction();
        this.knownPositions = new ArrayList();
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ConstructWorkbenchTile tile) {
        if (tile.crafting) {
            if (tile.getLevel().isClientSide()) {
                Direction facing = (Direction)state.getValue((Property)ConstructWorkbenchBlock.FACING);
                Vec3 me = null;
                switch (facing) {
                    case NORTH: {
                        me = new Vec3((double)((float)pos.getX() + 0.5f), (double)((float)pos.getY() + 1.65f), (double)pos.getZ());
                        break;
                    }
                    case SOUTH: {
                        me = new Vec3((double)((float)pos.getX() + 0.5f), (double)((float)pos.getY() + 1.65f), (double)((float)pos.getZ() + 1.25f));
                        break;
                    }
                    case EAST: {
                        me = new Vec3((double)((float)pos.getX() + 1.0f), (double)((float)pos.getY() + 1.65f), (double)((float)pos.getZ() + 0.5f));
                        break;
                    }
                    case WEST: {
                        me = new Vec3((double)pos.getX(), (double)((float)pos.getY() + 1.65f), (double)((float)pos.getZ() + 0.5f));
                        break;
                    }
                    default: {
                        return;
                    }
                }
                if (tile.knownPositions.size() == 0) {
                    tile.findNearbyPoints();
                }
                for (PosAffinityData pad : tile.knownPositions) {
                    Vec3 start = new Vec3((double)((float)pad.getPos().getX() + 0.5f), (double)((float)pad.getPos().getY() + 0.5f), (double)((float)pad.getPos().getZ() + 0.5f));
                    tile.getForAffinity(pad.aff, start, me);
                }
            }
            ++tile.craftTicks;
            if (tile.craftTicks >= 200 && !tile.getLevel().isClientSide()) {
                tile.spawnConstruct(tile.crafter);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private MAParticleType getForAffinity(Affinity aff, Vec3 start, Vec3 end) {
        MAParticleType particle = null;
        switch (aff) {
            case ARCANE: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get())).setMaxAge(60).setMover(new ParticleLerpMover(start.x, start.y, start.z, end.x, end.y, end.z));
                break;
            }
            case EARTH: {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ITEM.get())).setStack(new ItemStack((ItemLike)ItemInit.GREATER_MOTE_EARTH.get())).setScale(0.1f).setMover(new ParticleVelocityMover(0.0, 0.2f, 0.0, true)).setGravity(0.05f), end.x - 1.0 + 2.0 * Math.random(), end.y - 1.4, end.z - 1.0 + 2.0 * Math.random(), 0.0, 0.0, 0.0);
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST_LERP.get())).setMover(new ParticleVelocityMover(0.0, 0.15f, 0.0, true)).setMaxAge(60).setScale(0.05f);
                break;
            }
            case ENDER: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())).setScale(0.15f);
                break;
            }
            case FIRE: 
            case HELLFIRE: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_LERP.get())).setScale(0.15f).setMover(new ParticleBezierMover(start, end));
                break;
            }
            case LIGHTNING: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get()));
                break;
            }
            case WATER: {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())).setColor(14, 28, 35).setScale(0.1f), start.x, start.y, start.z, 0.0, 0.0, 0.0);
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER_LERP.get())).setScale(0.05f).setColor(0, 128, 255).setMaxAge(60).setMover(new ParticleSphereOrbitMover(end.x, end.y, end.z, 0.05, 57.29577951308232 * (double)(ManaAndArtifice.instance.proxy.getGameTicks() % 360L), 1.25));
                break;
            }
            case ICE: {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setColor(14, 28, 35).setScale(0.1f), start.x, start.y, start.z, 0.0, 0.0, 0.0);
                Vec3 newEnd = new Vec3(end.x - 1.0 + Math.random() * 2.0, end.y - 2.0, end.z - 1.0 + Math.random() * 2.0);
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST_LERP.get())).setScale(0.1f).setColor(14, 28, 35).setMover(new ParticleLerpMover(newEnd.x, newEnd.y, newEnd.z, newEnd.x, newEnd.y + 3.0, newEnd.z));
                break;
            }
            case WIND: {
                Vec3 controlA = start.add(end.subtract(start).scale((double)0.33f)).add(0.0, 0.25 + Math.random(), 0.0);
                Vec3 controlB = start.add(end.subtract(start).scale((double)0.66f)).add(0.0, 0.25 + Math.random(), 0.0);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_LERP.get())).setScale(0.2f).setColor(10, 10, 10).setMover(new ParticleBezierMover(start, end, controlA, controlB)), start.x, start.y, start.z, 0.0, 0.0, 0.0);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.2f).setColor(10, 10, 10), end.x, end.y - 1.0, end.z, (double)0.1f, (double)0.1f, 1.25);
                break;
            }
            default: {
                particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_LERP_POINT.get()));
            }
        }
        if (particle != null) {
            this.getLevel().addParticle((ParticleOptions)particle, start.x, start.y, start.z, end.x, end.y, end.z);
        }
        return particle;
    }

    public int getCraftTicks() {
        return this.craftTicks;
    }

    public ArrayList<PosAffinityData> getKnownPositions() {
        return this.knownPositions;
    }

    public boolean getIsCrafting() {
        return this.crafting;
    }

    public ItemStack placePart(ItemStack stack) {
        if (this.crafting || stack.getCount() != 1 || !(stack.getItem() instanceof ItemConstructPart)) {
            return stack;
        }
        ItemStack output = this.construct.setPart(stack);
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), (BlockState)this.getBlockState().setValue(ConstructWorkbenchBlock.HAS_PART, Boolean.valueOf(!this.construct.isEmpty())), 3);
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(ConstructWorkbenchBlock.HAS_PART, Boolean.valueOf(!this.construct.isEmpty())), 3);
        }
        return output;
    }

    public ItemStack popPart() {
        ConstructSlot slot;
        ConstructSlot[] slots;
        ItemStack output = ItemStack.EMPTY;
        if (this.crafting) {
            return output;
        }
        ConstructSlot[] constructSlotArray = slots = new ConstructSlot[]{ConstructSlot.LEFT_ARM, ConstructSlot.RIGHT_ARM, ConstructSlot.TORSO, ConstructSlot.HEAD, ConstructSlot.LEGS};
        int n = constructSlotArray.length;
        for (int i = 0; i < n && (output = this.construct.removePart(slot = constructSlotArray[i])).isEmpty(); ++i) {
        }
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), (BlockState)this.getBlockState().setValue(ConstructWorkbenchBlock.HAS_PART, Boolean.valueOf(!this.construct.isEmpty())), 3);
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(ConstructWorkbenchBlock.HAS_PART, Boolean.valueOf(!this.construct.isEmpty())), 3);
        }
        return output;
    }

    public void setConstructData(IConstructConstruction data, String name) {
        if (!this.getLevel().isClientSide()) {
            this.construct = data.copy();
            this.constructName = name;
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), (BlockState)this.getBlockState().setValue(ConstructWorkbenchBlock.HAS_PART, Boolean.valueOf(!this.construct.isEmpty())), 3);
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(ConstructWorkbenchBlock.HAS_PART, Boolean.valueOf(!this.construct.isEmpty())), 3);
        }
    }

    private void spawnConstruct(UUID owner) {
        if (this.construct.isComplete() && !this.getLevel().isClientSide()) {
            this.crafting = false;
            this.craftTicks = 0;
            this.crafter = null;
            Construct eac = new Construct(this.level);
            BlockPos pos = this.getBlockPos();
            Direction dir = (Direction)this.getBlockState().getValue((Property)ConstructWorkbenchBlock.FACING);
            Vec3 fwdDir = new Vec3((double)dir.getStepX(), (double)dir.getStepY(), (double)dir.getStepZ());
            Vec3 vPos = new Vec3((double)((float)pos.getX() + 0.5f) + fwdDir.x, (double)pos.getY() + 0.5, (double)((float)pos.getZ() + 0.5f) + fwdDir.z);
            eac.moveTo(vPos.x, vPos.y, vPos.z, 0.0f, 0.0f);
            eac.lookAt(EntityAnchorArgument.Anchor.FEET, vPos.add(fwdDir.scale(10.0)));
            if (this.constructName != null) {
                eac.setCustomName((Component)Component.literal((String)this.constructName));
                this.constructName = null;
            }
            eac.setOwner(owner);
            HashMap affinities = new HashMap();
            this.getNearbyEntities().forEach(e -> {
                if (!affinities.containsKey(e.getAffinity())) {
                    affinities.put(e.getAffinity(), 1);
                } else {
                    affinities.put(e.getAffinity(), (Integer)affinities.get(e.getAffinity()) + 1);
                }
                e.remove(Entity.RemovalReason.DISCARDED);
            });
            for (Affinity aff : Affinity.values()) {
                this.construct.setAffinityScore(aff, 0);
            }
            for (Affinity aff : (java.util.Set<Affinity>)(java.util.Set)affinities.keySet()) {
                this.construct.setAffinityScore(aff, (Integer)affinities.get(aff));
            }
            eac.setConstructParts(this.construct);
            this.getLevel().addFreshEntity((Entity)eac);
            this.setChanged();
            this.construct = new ConstructConstruction();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            Player player = this.level.getPlayerByUUID(owner);
            if (player != null) {
                EventDispatcher.DispatchConstructCrafted(eac, player);
                if (player instanceof ServerPlayer) {
                    CustomAdvancementTriggers.SUMMON_CONSTRUCT.trigger((ServerPlayer)player, eac);
                }
            }
            ConstructCommandFollowAndGuard task = (ConstructCommandFollowAndGuard)ConstructTasks.FOLLOW_DEFEND.instantiateTask(eac);
            task.setFollowTarget((LivingEntity)player);
            task.setShouldGuard(false);
            eac.setCurrentCommand(player, task, true);
        }
    }

    private List<AffinityIcon> getNearbyEntities() {
        int search_radius = 5;
        final Vec3 myVec = new Vec3((double)((float)this.getBlockPos().getX() + 0.5f), (double)((float)this.getBlockPos().getY() + 0.5f), (double)((float)this.getBlockPos().getZ() + 0.5f));
        AABB bb = new AABB(this.getBlockPos()).inflate((double)search_radius);
        List<AffinityIcon> entities = this.getLevel().getEntitiesOfClass(AffinityIcon.class, bb).stream().map(e -> e).sorted(new Comparator<AffinityIcon>(){

            @Override
            public int compare(AffinityIcon o1, AffinityIcon o2) {
                Double d1 = o1.position().distanceToSqr(myVec);
                Double d2 = o2.position().distanceToSqr(myVec);
                return d1.compareTo(d2);
            }
        }).limit(8L).collect(Collectors.toList());
        return entities;
    }

    private boolean findNearbyPoints() {
        List<AffinityIcon> entities = this.getNearbyEntities();
        if (entities.size() < 8) {
            return false;
        }
        this.knownPositions.clear();
        entities.stream().forEach(e -> this.knownPositions.add(new PosAffinityData(BlockPos.containing((Position)e.position()), e.getAffinity())));
        return true;
    }

    public boolean isEmpty() {
        return this.construct.isEmpty();
    }

    public boolean startCrafting(@Nonnull Player player) {
        if (!this.crafting && this.construct.isComplete() && !this.getLevel().isClientSide() && this.findNearbyPoints()) {
            this.crafting = true;
            this.craftTicks = 0;
            this.crafter = player.getUUID();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            return true;
        }
        return false;
    }

    public boolean summonConstructImmediately(@Nonnull Player player) {
        if (!this.crafting && this.construct.isComplete() && !this.getLevel().isClientSide()) {
            this.spawnConstruct(player.getUUID());
            return true;
        }
        return false;
    }

    private void writeKnownPositions(CompoundTag nbt) {
        CompoundTag known = new CompoundTag();
        known.putInt("count", this.knownPositions.size());
        for (int i = 0; i < this.knownPositions.size(); ++i) {
            known.put("pos" + i, (Tag)NbtUtils.writeBlockPos((BlockPos)this.knownPositions.get(i).getPos()));
            known.putString("aff" + i, this.knownPositions.get(i).getAffinity().toString());
        }
        nbt.put(NBT_KNOWN_POS, (Tag)known);
    }

    private void readKnownPositions(CompoundTag nbt) {
        if (nbt.contains(NBT_KNOWN_POS)) {
            CompoundTag known = nbt.getCompound(NBT_KNOWN_POS);
            int count = known.getInt("count");
            this.knownPositions.clear();
            for (int i = 0; i < count; ++i) {
                try {
                    BlockPos pos = NbtUtils.readBlockPos(known, "pos" + i).orElse(null);
                    Affinity aff = Affinity.valueOf(known.getString("aff" + i));
                    this.knownPositions.add(new PosAffinityData(pos, aff));
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public void saveAdditional(CompoundTag base, net.minecraft.core.HolderLookup.Provider registries) {
        this.construct.WriteNBT(base);
        base.putBoolean(NBT_CRAFTING, this.crafting);
        base.putInt(NBT_CRAFT_TICKS, this.craftTicks);
        base.putString(NBT_CRAFTER, this.crafter != null ? this.crafter.toString() : "");
        if (this.constructName != null) {
            base.putString(NBT_NAME, this.constructName);
        }
        this.writeKnownPositions(base);
    }

    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        String crafterUUIDString;
        super.loadAdditional(nbt, registries);
        this.construct.ReadNBT(nbt);
        if (nbt.contains(NBT_CRAFTING)) {
            this.crafting = nbt.getBoolean(NBT_CRAFTING);
        }
        if (nbt.contains(NBT_CRAFT_TICKS)) {
            this.craftTicks = nbt.getInt(NBT_CRAFT_TICKS);
        }
        if (nbt.contains(NBT_CRAFTER) && !(crafterUUIDString = nbt.getString(NBT_CRAFTER)).isEmpty()) {
            this.crafter = UUID.fromString(crafterUUIDString);
        }
        if (nbt.contains(NBT_NAME)) {
            this.constructName = nbt.getString(NBT_NAME);
        }
        this.readKnownPositions(nbt);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag nbt = pkt.getTag();
        boolean wasCrafting = this.crafting;
        this.construct.ReadNBT(nbt);
        if (nbt.contains(NBT_CRAFTING)) {
            this.crafting = nbt.getBoolean(NBT_CRAFTING);
        }
        if (nbt.contains(NBT_CRAFT_TICKS)) {
            this.craftTicks = nbt.getInt(NBT_CRAFT_TICKS);
        }
        if (this.crafting && !wasCrafting) {
            BlockPos soundPos = this.getBlockPos();
            this.getLevel().playSound(ManaAndArtifice.instance.proxy.getClientPlayer(), (double)soundPos.getX(), (double)soundPos.getY(), (double)soundPos.getZ(), SFX.Event.Block.GANTRY_SUMMON, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        this.readKnownPositions(nbt);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag payload = super.getUpdateTag(registries);
        this.construct.WriteNBT(payload);
        payload.putBoolean(NBT_CRAFTING, this.crafting);
        payload.putInt(NBT_CRAFT_TICKS, this.craftTicks);
        this.writeKnownPositions(payload);
        return payload;
    }

    public void handleUpdateTag(CompoundTag tag) {
        this.loadAdditional(tag, com.mna.api.tools.MNAItemData.provider());
    }

    public IConstructConstruction getConstruct() {
        return this.construct;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> {
            if (this.crafting) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.construct_arch.assemble"));
            }
            if (this.entityInside) {
                if (state.isCurrentAnimationStage("animation.construct_arch.idle")) {
                    state.resetCurrentAnimation();
                }
                this.entityInside = false;
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.construct_arch.assemble_end").thenLoop("animation.construct_arch.idle"));
        });
        registrar.add(new AnimationController[]{controller});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(5.0);
    }

    public void swing() {
        this.entityInside = true;
    }

    class PosAffinityData {
        BlockPos pos;
        Affinity aff;

        public PosAffinityData(BlockPos pos, Affinity affinity) {
            this.aff = affinity;
            this.pos = pos;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public Affinity getAffinity() {
            return this.aff;
        }
    }
}


