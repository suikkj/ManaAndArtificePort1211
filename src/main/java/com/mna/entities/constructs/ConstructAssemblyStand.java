 
package com.mna.entities.constructs;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.entities.ISpellInteractibleEntity;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.entities.EntityInit;
import com.mna.entities.constructs.ai.ConstructCommandFollowAndGuard;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.animated.Construct;
import com.mna.events.EventDispatcher;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.AnimusDust;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNAItemData;

public class ConstructAssemblyStand
extends LivingEntity
implements ISpellInteractibleEntity<ConstructAssemblyStand> {
    public static final int WOBBLE_TIME = 5;
    private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0f, 0.0f, -10.0f);
    private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0f, 0.0f, 10.0f);
    private static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0f, 0.0f, -1.0f);
    private static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0f, 0.0f, 1.0f);
    public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(ConstructAssemblyStand.class, (EntityDataSerializer)EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(ConstructAssemblyStand.class, (EntityDataSerializer)EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(ConstructAssemblyStand.class, (EntityDataSerializer)EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(ConstructAssemblyStand.class, (EntityDataSerializer)EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_LEG_POSE = SynchedEntityData.defineId(ConstructAssemblyStand.class, (EntityDataSerializer)EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_LEG_POSE = SynchedEntityData.defineId(ConstructAssemblyStand.class, (EntityDataSerializer)EntityDataSerializers.ROTATIONS);
    private Rotations headPose = DEFAULT_HEAD_POSE;
    private Rotations bodyPose = DEFAULT_BODY_POSE;
    private Rotations leftArmPose = DEFAULT_LEFT_ARM_POSE;
    private Rotations rightArmPose = DEFAULT_RIGHT_ARM_POSE;
    private Rotations leftLegPose = DEFAULT_LEFT_LEG_POSE;
    private Rotations rightLegPose = DEFAULT_RIGHT_LEG_POSE;
    private final NonNullList<ItemStack> bodyItems = NonNullList.withSize((int)4, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armItems = NonNullList.withSize((int)2, ItemStack.EMPTY);
    public long lastHit;
    private static final Predicate<Entity> RIDABLE_MINECARTS = p_31582_ -> p_31582_ instanceof AbstractMinecart && ((AbstractMinecart)p_31582_).canBeRidden();

    public ConstructAssemblyStand(EntityType<? extends ConstructAssemblyStand> type, Level world) {
        super(type, world);
    }

    public ConstructAssemblyStand(Level world, double x, double y, double z) {
        this((EntityType<? extends ConstructAssemblyStand>)((EntityType)EntityInit.CONSTRUCT_ASSEMBLY_STAND.get()), world);
        this.setPos(x, y, z);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
        builder.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
        builder.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
        builder.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
        builder.define(DATA_LEFT_LEG_POSE, DEFAULT_LEFT_LEG_POSE);
        builder.define(DATA_RIGHT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
    }

    public float getStepHeight() {
        return 0.0f;
    }

    public ConstructMaterial[] getComposition() {
        ArrayList materials = new ArrayList();
        this.bodyItems.forEach(is -> {
            if (is.getItem() instanceof ItemConstructPart) {
                materials.add(((ItemConstructPart)is.getItem()).getMaterial());
            }
        });
        this.armItems.forEach(is -> {
            if (is.getItem() instanceof ItemConstructPart) {
                materials.add(((ItemConstructPart)is.getItem()).getMaterial());
            }
        });
        return materials.toArray(new ConstructMaterial[0]);
    }

    public ItemConstructPart[] getPartsForMaterial(ConstructMaterial matl) {
        ArrayList parts = new ArrayList();
        this.bodyItems.forEach(is -> {
            if (is.getItem() instanceof ItemConstructPart && ((ItemConstructPart)is.getItem()).getMaterial() == matl) {
                parts.add((ItemConstructPart)is.getItem());
            }
        });
        this.armItems.forEach(is -> {
            if (is.getItem() instanceof ItemConstructPart && ((ItemConstructPart)is.getItem()).getMaterial() == matl) {
                parts.add((ItemConstructPart)is.getItem());
            }
        });
        return parts.toArray(new ItemConstructPart[0]);
    }

    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    private boolean hasPhysics() {
        return !this.isNoGravity();
    }

    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && this.hasPhysics();
    }

    public Iterable<ItemStack> getHandSlots() {
        return this.armItems;
    }

    public Iterable<ItemStack> getArmorSlots() {
        return this.bodyItems;
    }

    public ItemStack getItemBySlot(EquipmentSlot slot) {
        switch (slot) {
            case LEGS: 
            case CHEST: 
            case HEAD: {
                return (ItemStack)this.bodyItems.get(slot.getIndex());
            }
            case MAINHAND: 
            case OFFHAND: {
                return (ItemStack)this.armItems.get(slot.getIndex());
            }
        }
        return ItemStack.EMPTY;
    }

    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        this.verifyEquippedItem(stack);
        if (!stack.isEmpty()) {
            this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
        }
        if (slot.getType() == EquipmentSlot.Type.HAND) {
            this.armItems.set(slot.getIndex(), stack);
        } else {
            this.bodyItems.set(slot.getIndex(), stack);
        }
    }

    public boolean canTakeItem(ItemStack p_31638_) {
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem((ItemStack)p_31638_);
        return this.getItemBySlot(equipmentslot).isEmpty();
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag listtag = new ListTag();
        for (ItemStack itemstack : this.bodyItems) {
            CompoundTag compoundtag = new CompoundTag();
            if (!itemstack.isEmpty()) {
                MNAItemData.save(itemstack, compoundtag);
            }
            listtag.add(compoundtag);
        }
        tag.put("ArmorItems", (Tag)listtag);
        ListTag listtag1 = new ListTag();
        for (ItemStack itemstack1 : this.armItems) {
            CompoundTag compoundtag1 = new CompoundTag();
            if (!itemstack1.isEmpty()) {
                MNAItemData.save(itemstack1, compoundtag1);
            }
            listtag1.add(compoundtag1);
        }
        tag.put("HandItems", (Tag)listtag1);
        tag.put("Pose", (Tag)this.writePose());
    }

    public void readAdditionalSaveData(CompoundTag p_31600_) {
        super.readAdditionalSaveData(p_31600_);
        if (p_31600_.contains("ArmorItems", 9)) {
            ListTag listtag = p_31600_.getList("ArmorItems", 10);
            for (int i = 0; i < this.bodyItems.size(); ++i) {
                this.bodyItems.set(i, ItemStack.parseOptional(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(), (CompoundTag)listtag.getCompound(i)));
            }
        }
        if (p_31600_.contains("HandItems", 9)) {
            ListTag listtag1 = p_31600_.getList("HandItems", 10);
            for (int j = 0; j < this.armItems.size(); ++j) {
                this.armItems.set(j, ItemStack.parseOptional(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(), (CompoundTag)listtag1.getCompound(j)));
            }
        }
        this.noPhysics = !this.hasPhysics();
        CompoundTag compoundtag = p_31600_.getCompound("Pose");
        this.readPose(compoundtag);
    }

    private void readPose(CompoundTag p_31658_) {
        ListTag listtag = p_31658_.getList("Head", 5);
        this.setHeadPose(listtag.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(listtag));
        ListTag listtag1 = p_31658_.getList("Body", 5);
        this.setBodyPose(listtag1.isEmpty() ? DEFAULT_BODY_POSE : new Rotations(listtag1));
        ListTag listtag2 = p_31658_.getList("LeftArm", 5);
        this.setLeftArmPose(listtag2.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(listtag2));
        ListTag listtag3 = p_31658_.getList("RightArm", 5);
        this.setRightArmPose(listtag3.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(listtag3));
        ListTag listtag4 = p_31658_.getList("LeftLeg", 5);
        this.setLeftLegPose(listtag4.isEmpty() ? DEFAULT_LEFT_LEG_POSE : new Rotations(listtag4));
        ListTag listtag5 = p_31658_.getList("RightLeg", 5);
        this.setRightLegPose(listtag5.isEmpty() ? DEFAULT_RIGHT_LEG_POSE : new Rotations(listtag5));
    }

    private CompoundTag writePose() {
        CompoundTag compoundtag = new CompoundTag();
        if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
            compoundtag.put("Head", (Tag)this.headPose.save());
        }
        if (!DEFAULT_BODY_POSE.equals(this.bodyPose)) {
            compoundtag.put("Body", (Tag)this.bodyPose.save());
        }
        if (!DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
            compoundtag.put("LeftArm", (Tag)this.leftArmPose.save());
        }
        if (!DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
            compoundtag.put("RightArm", (Tag)this.rightArmPose.save());
        }
        if (!DEFAULT_LEFT_LEG_POSE.equals(this.leftLegPose)) {
            compoundtag.put("LeftLeg", (Tag)this.leftLegPose.save());
        }
        if (!DEFAULT_RIGHT_LEG_POSE.equals(this.rightLegPose)) {
            compoundtag.put("RightLeg", (Tag)this.rightLegPose.save());
        }
        return compoundtag;
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean isPushable() {
        return false;
    }

    protected void doPush(Entity p_31564_) {
    }

    protected void pushEntities() {
        List list = this.level().getEntities((Entity)this, this.getBoundingBox(), RIDABLE_MINECARTS);
        for (int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            if (!(this.distanceToSqr(entity) <= 0.2)) continue;
            entity.push((Entity)this);
        }
    }

    public void tick() {
        Rotations rotations5;
        Rotations rotations4;
        Rotations rotations3;
        Rotations rotations2;
        Rotations rotations1;
        super.tick();
        Rotations rotations = (Rotations)this.entityData.get(DATA_HEAD_POSE);
        if (!this.headPose.equals(rotations)) {
            this.setHeadPose(rotations);
        }
        if (!this.bodyPose.equals((rotations1 = (Rotations)this.entityData.get(DATA_BODY_POSE)))) {
            this.setBodyPose(rotations1);
        }
        if (!this.leftArmPose.equals((rotations2 = (Rotations)this.entityData.get(DATA_LEFT_ARM_POSE)))) {
            this.setLeftArmPose(rotations2);
        }
        if (!this.rightArmPose.equals((rotations3 = (Rotations)this.entityData.get(DATA_RIGHT_ARM_POSE)))) {
            this.setRightArmPose(rotations3);
        }
        if (!this.leftLegPose.equals((rotations4 = (Rotations)this.entityData.get(DATA_LEFT_LEG_POSE)))) {
            this.setLeftLegPose(rotations4);
        }
        if (!this.rightLegPose.equals((rotations5 = (Rotations)this.entityData.get(DATA_RIGHT_LEG_POSE)))) {
            this.setRightLegPose(rotations5);
        }
    }

    public InteractionResult interactAt(Player p_31594_, Vec3 p_31595_, InteractionHand p_31596_) {
        ItemStack itemstack = p_31594_.getItemInHand(p_31596_);
        if (p_31594_.isSpectator()) {
            return InteractionResult.SUCCESS;
        }
        if (p_31594_.level().isClientSide()) {
            return InteractionResult.CONSUME;
        }
        if (itemstack.getItem() instanceof ItemConstructPart) {
            EquipmentSlot equipmentslot1;
            EquipmentSlot slot = EquipmentSlot.FEET;
            switch (((ItemConstructPart)itemstack.getItem()).getSlot()) {
                case HEAD: {
                    slot = EquipmentSlot.HEAD;
                    break;
                }
                case LEFT_ARM: {
                    slot = EquipmentSlot.MAINHAND;
                    break;
                }
                case LEGS: {
                    slot = EquipmentSlot.LEGS;
                    break;
                }
                case RIGHT_ARM: {
                    slot = EquipmentSlot.OFFHAND;
                    break;
                }
                case TORSO: {
                    slot = EquipmentSlot.CHEST;
                }
            }
            if (itemstack.isEmpty() ? this.hasItemInSlot(equipmentslot1 = this.getClickedSlot(p_31595_)) && this.swapItem(p_31594_, equipmentslot1, itemstack, p_31596_) : this.swapItem(p_31594_, slot, itemstack, p_31596_)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (itemstack.getItem() == ItemInit.ANIMUS_DUST.get()) {
            this.summonConstruct(p_31594_);
            itemstack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean isConstructComplete() {
        ArrayList<ItemStack> parts = new ArrayList<ItemStack>();
        parts.add(this.getItemBySlot(EquipmentSlot.HEAD));
        parts.add(this.getItemBySlot(EquipmentSlot.CHEST));
        parts.add(this.getItemBySlot(EquipmentSlot.MAINHAND));
        parts.add(this.getItemBySlot(EquipmentSlot.OFFHAND));
        parts.add(this.getItemBySlot(EquipmentSlot.LEGS));
        return parts.stream().allMatch(is -> !is.isEmpty() && is.getItem() instanceof ItemConstructPart);
    }

    private void summonConstruct(Player player) {
        if (!this.isConstructComplete()) {
            return;
        }
        if (this.level().isClientSide() || player.getGameProfile() == null || player.getGameProfile().getId() == null) {
            AnimusDust.spawnParticles(this.level(), this.blockPosition());
            return;
        }
        Construct construct = new Construct(this.level());
        construct.setConstructParts(this.getItemBySlot(EquipmentSlot.HEAD), this.getItemBySlot(EquipmentSlot.CHEST), this.getItemBySlot(EquipmentSlot.MAINHAND), this.getItemBySlot(EquipmentSlot.OFFHAND), this.getItemBySlot(EquipmentSlot.LEGS));
        Vec3 fwd = this.getForward().normalize();
        construct.moveTo(this.getX() + fwd.x, this.getY() + fwd.y, this.getZ() + fwd.z, 0.0f, 0.0f);
        construct.lookAt(EntityAnchorArgument.Anchor.FEET, this.position().add(fwd.scale(10.0)));
        construct.setOwner(player.getGameProfile().getId());
        this.level().addFreshEntity((Entity)construct);
        ConstructCommandFollowAndGuard task = (ConstructCommandFollowAndGuard)ConstructTasks.FOLLOW_DEFEND.instantiateTask(construct);
        task.setFollowTarget((LivingEntity)player);
        task.setShouldGuard(false);
        EventDispatcher.DispatchConstructCrafted(construct, player);
        if (player instanceof ServerPlayer) {
            CustomAdvancementTriggers.SUMMON_CONSTRUCT.trigger((ServerPlayer)player, construct);
        }
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        this.brokenByPlayer(this.damageSources().generic());
        this.kill();
    }

    private EquipmentSlot getClickedSlot(Vec3 p_31660_) {
        EquipmentSlot equipmentslot = EquipmentSlot.MAINHAND;
        double d0 = p_31660_.y;
        EquipmentSlot equipmentslot1 = EquipmentSlot.LEGS;
        if (d0 >= 0.1 && d0 < 0.55 && this.hasItemInSlot(equipmentslot1)) {
            equipmentslot = EquipmentSlot.LEGS;
        } else if (d0 >= 0.9 && d0 < 1.6 && this.hasItemInSlot(EquipmentSlot.CHEST)) {
            equipmentslot = EquipmentSlot.CHEST;
        } else if (d0 >= 0.4 && d0 < 1.2 && this.hasItemInSlot(EquipmentSlot.LEGS)) {
            equipmentslot = EquipmentSlot.LEGS;
        } else if (d0 >= 1.6 && this.hasItemInSlot(EquipmentSlot.HEAD)) {
            equipmentslot = EquipmentSlot.HEAD;
        } else if (!this.hasItemInSlot(EquipmentSlot.MAINHAND) && this.hasItemInSlot(EquipmentSlot.OFFHAND)) {
            equipmentslot = EquipmentSlot.OFFHAND;
        }
        return equipmentslot;
    }

    private boolean swapItem(Player player, EquipmentSlot slot, ItemStack insert, InteractionHand hand) {
        ItemStack itemstack = this.getItemBySlot(slot);
        if (player.getAbilities().instabuild && itemstack.isEmpty() && !insert.isEmpty()) {
            ItemStack itemstack2 = insert.copy();
            itemstack2.setCount(1);
            this.setItemSlot(slot, itemstack2);
            return true;
        }
        if (!insert.isEmpty() && insert.getCount() > 1) {
            if (!itemstack.isEmpty()) {
                return false;
            }
            ItemStack itemstack1 = insert.copy();
            itemstack1.setCount(1);
            this.setItemSlot(slot, itemstack1);
            insert.shrink(1);
            return true;
        }
        this.setItemSlot(slot, insert);
        player.setItemInHand(hand, itemstack);
        return true;
    }

    public boolean hurt(DamageSource p_31579_, float p_31580_) {
        if (!this.level().isClientSide() && !this.isRemoved()) {
            if (p_31579_.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                this.kill();
                return false;
            }
            if (!this.isInvulnerableTo(p_31579_)) {
                if (p_31579_.is(DamageTypeTags.IS_EXPLOSION)) {
                    this.brokenByAnything(p_31579_);
                    this.kill();
                    return false;
                }
                if (p_31579_.is(DamageTypes.IN_FIRE)) {
                    if (this.isOnFire()) {
                        this.causeDamage(p_31579_, 0.15f);
                    } else {
                        this.igniteForSeconds((float)(5));
                    }
                    return false;
                }
                if (p_31579_.is(DamageTypes.ON_FIRE) && this.getHealth() > 0.5f) {
                    this.causeDamage(p_31579_, 4.0f);
                    return false;
                }
                boolean flag = p_31579_.getDirectEntity() instanceof AbstractArrow;
                boolean flag1 = flag && ((AbstractArrow)p_31579_.getDirectEntity()).getPierceLevel() > 0;
                boolean flag2 = "player".equals(p_31579_.getMsgId());
                if (!flag2 && !flag) {
                    return false;
                }
                if (p_31579_.getEntity() instanceof Player && !((Player)p_31579_.getEntity()).getAbilities().mayBuild) {
                    return false;
                }
                if (p_31579_.isCreativePlayer()) {
                    this.playBrokenSound();
                    this.showBreakingParticles();
                    this.kill();
                    return flag1;
                }
                long i = this.level().getGameTime();
                if (i - this.lastHit > 5L && !flag) {
                    this.level().broadcastEntityEvent((Entity)this, (byte)32);
                    this.gameEvent(GameEvent.ENTITY_DAMAGE, p_31579_.getEntity());
                    this.lastHit = i;
                } else {
                    this.brokenByPlayer(p_31579_);
                    this.showBreakingParticles();
                    this.kill();
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public void handleEntityEvent(byte p_31568_) {
        if (p_31568_ == 32) {
            if (this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3f, 1.0f, false);
                this.lastHit = this.level().getGameTime();
            }
        } else {
            super.handleEntityEvent(p_31568_);
        }
    }

    public boolean shouldRenderAtSqrDistance(double p_31574_) {
        double d0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d0) || d0 == 0.0) {
            d0 = 4.0;
        }
        return p_31574_ < (d0 *= 64.0) * d0;
    }

    private void showBreakingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.6666666666666666), this.getZ(), 10, (double)(this.getBbWidth() / 4.0f), (double)(this.getBbHeight() / 4.0f), (double)(this.getBbWidth() / 4.0f), 0.05);
        }
    }

    private void causeDamage(DamageSource p_31649_, float p_31650_) {
        float f = this.getHealth();
        if ((f -= p_31650_) <= 0.5f) {
            this.brokenByAnything(p_31649_);
            this.kill();
        } else {
            this.setHealth(f);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, p_31649_.getEntity());
        }
    }

    private void brokenByPlayer(DamageSource p_31647_) {
        Block.popResource((Level)this.level(), (BlockPos)this.blockPosition(), (ItemStack)new ItemStack((ItemLike)Items.ARMOR_STAND));
        this.brokenByAnything(p_31647_);
    }

    private void brokenByAnything(DamageSource p_31654_) {
        this.playBrokenSound();
        this.dropAllDeathLoot(p_31654_);
        for (int i = 0; i < this.armItems.size(); ++i) {
            ItemStack itemstack = (ItemStack)this.armItems.get(i);
            if (itemstack.isEmpty()) continue;
            Block.popResource((Level)this.level(), (BlockPos)this.blockPosition().above(), (ItemStack)itemstack);
            this.armItems.set(i, ItemStack.EMPTY);
        }
        for (int j = 0; j < this.bodyItems.size(); ++j) {
            ItemStack itemstack1 = (ItemStack)this.bodyItems.get(j);
            if (itemstack1.isEmpty()) continue;
            Block.popResource((Level)this.level(), (BlockPos)this.blockPosition().above(), (ItemStack)itemstack1);
            this.bodyItems.set(j, ItemStack.EMPTY);
        }
    }

    private void playBrokenSound() {
        this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0f, 1.0f);
    }

    protected float getStandingEyeHeight(Pose p_31614_, EntityDimensions p_31615_) {
        return p_31615_.height * (this.isBaby() ? 0.5f : 0.9f);
    }

    public double getMyRidingOffset() {
        return 0.1;
    }

    public void travel(Vec3 p_31656_) {
        if (this.hasPhysics()) {
            super.travel(p_31656_);
        }
    }

    public void setYBodyRot(float p_31670_) {
        this.yBodyRotO = this.yRotO = p_31670_;
        this.yHeadRotO = this.yHeadRot = p_31670_;
    }

    public void setYHeadRot(float p_31668_) {
        this.yBodyRotO = this.yRotO = p_31668_;
        this.yHeadRotO = this.yHeadRot = p_31668_;
    }

    public void setHeadPose(Rotations p_31598_) {
        this.headPose = p_31598_;
        this.entityData.set(DATA_HEAD_POSE, p_31598_);
    }

    public void setBodyPose(Rotations p_31617_) {
        this.bodyPose = p_31617_;
        this.entityData.set(DATA_BODY_POSE, p_31617_);
    }

    public void setLeftArmPose(Rotations p_31624_) {
        this.leftArmPose = p_31624_;
        this.entityData.set(DATA_LEFT_ARM_POSE, p_31624_);
    }

    public void setRightArmPose(Rotations p_31629_) {
        this.rightArmPose = p_31629_;
        this.entityData.set(DATA_RIGHT_ARM_POSE, p_31629_);
    }

    public void setLeftLegPose(Rotations p_31640_) {
        this.leftLegPose = p_31640_;
        this.entityData.set(DATA_LEFT_LEG_POSE, p_31640_);
    }

    public void setRightLegPose(Rotations p_31652_) {
        this.rightLegPose = p_31652_;
        this.entityData.set(DATA_RIGHT_LEG_POSE, p_31652_);
    }

    public Rotations getHeadPose() {
        return this.headPose;
    }

    public Rotations getBodyPose() {
        return this.bodyPose;
    }

    public Rotations getLeftArmPose() {
        return this.leftArmPose;
    }

    public Rotations getRightArmPose() {
        return this.rightArmPose;
    }

    public Rotations getLeftLegPose() {
        return this.leftLegPose;
    }

    public Rotations getRightLegPose() {
        return this.rightLegPose;
    }

    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
    }

    public boolean isPickable() {
        return true;
    }

    public boolean skipAttackInteraction(Entity p_31687_) {
        return p_31687_ instanceof Player && !this.level().mayInteract((Player)p_31687_, this.blockPosition());
    }

    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource p_31636_) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    public void thunderHit(ServerLevel p_31576_, LightningBolt p_31577_) {
    }

    public boolean isAffectedByPotions() {
        return false;
    }

    public boolean attackable() {
        return false;
    }

    public ItemStack getPickResult() {
        return new ItemStack((ItemLike)Items.ARMOR_STAND);
    }

    public static boolean checkAndConvert(ArmorStand stand, ItemStack clickedWith, Player player, InteractionHand hand) {
        if (stand.level().isClientSide() || !(clickedWith.getItem() instanceof ItemConstructPart)) {
            return false;
        }
        MutableBoolean allEmpty = new MutableBoolean(true);
        stand.getHandSlots().forEach(is -> {
            if (!is.isEmpty()) {
                allEmpty.setFalse();
            }
        });
        stand.getArmorSlots().forEach(is -> {
            if (!is.isEmpty()) {
                allEmpty.setFalse();
            }
        });
        if (!allEmpty.getValue().booleanValue()) {
            return false;
        }
        Vec3 pos = stand.position();
        ConstructAssemblyStand conStand = new ConstructAssemblyStand(stand.level(), pos.x, pos.y, pos.z);
        conStand.moveTo(stand.getX(), stand.getY(), stand.getZ(), stand.getYRot(), 0.0f);
        stand.level().addFreshEntity((Entity)conStand);
        stand.remove(Entity.RemovalReason.DISCARDED);
        EquipmentSlot slot = EquipmentSlot.FEET;
        switch (((ItemConstructPart)clickedWith.getItem()).getSlot()) {
            case HEAD: {
                slot = EquipmentSlot.HEAD;
                break;
            }
            case LEFT_ARM: {
                slot = EquipmentSlot.MAINHAND;
                break;
            }
            case LEGS: {
                slot = EquipmentSlot.LEGS;
                break;
            }
            case RIGHT_ARM: {
                slot = EquipmentSlot.OFFHAND;
                break;
            }
            case TORSO: {
                slot = EquipmentSlot.CHEST;
            }
        }
        conStand.swapItem(player, slot, clickedWith, hand);
        return true;
    }

    @Override
    public boolean onShapeTarget(ISpellDefinition spell, SpellSource source) {
        if (source.isPlayerCaster() && spell.getComponents().stream().anyMatch(c -> c.getPart() == Components.ANIMUS || c.getPart() == Components.GREATER_ANIMUS)) {
            this.summonConstruct(source.getPlayer());
            return false;
        }
        return true;
    }
}

