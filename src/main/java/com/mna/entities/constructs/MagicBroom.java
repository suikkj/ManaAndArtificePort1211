 
package com.mna.entities.constructs;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.EntityInit;
import com.mna.items.ItemInit;
import com.mna.tools.InventoryUtilities;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.IItemHandler;
import com.mna.api.tools.MNACaps;

public class MagicBroom
extends AbstractGolem {
    private static final float detection_radius = 16.0f;
    private static final float detection_height = 4.0f;
    private static final float max_distance_from_container = 48.0f;
    private static final float movement_speed = 0.4f;
    public static final int BROOM_TYPE_NORMAL = 0;
    public static final int BROOM_TYPE_VORTEX = 1;
    private static final Predicate<ItemEntity> ITEM_SELECTOR = e -> {
        if (e.getPersistentData().contains("PreventRemoteMovement")) {
            return false;
        }
        return !e.hasPickUpDelay() && e.isAlive();
    };
    private int deposit_counter = 0;
    private boolean depositing = false;
    private static final EntityDataAccessor<BlockPos> CONTAINER_POSITION = SynchedEntityData.defineId(MagicBroom.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> BROOM_TYPE = SynchedEntityData.defineId(MagicBroom.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final String KEY_TARGETPOS = "magic_broom_targetPosition";
    private static final String KEY_BROOM_TYPE = "magic_broom_type";
    private float moveCounter = 0.0f;
    private float moveRotation = 0.0f;

    public MagicBroom(EntityType<? extends AbstractGolem> type, Level worldIn) {
        super(type, worldIn);
        this.setCanPickUpLoot(true);
    }

    public float getStepHeight() {
        return 1.8f;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(NeoForgeMod.SWIM_SPEED, 2.0);
    }

    private void updateRotations() {
        if (Math.abs(this.getDeltaMovement().length()) > (double)0.1f) {
            this.moveCounter += 0.3f;
            this.moveRotation = (float)Math.sin(this.moveCounter) - (float)Math.sin(this.moveCounter - 1.0f);
            if ((int)this.moveCounter % 3 == 0 && this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.GRASS_STEP, SoundSource.NEUTRAL, 0.25f, 1.0f, false);
            }
        } else {
            this.moveCounter = 1.57f;
            if (this.moveRotation > 0.0f) {
                this.moveRotation -= 0.1f;
                if (this.moveRotation < 0.0f) {
                    this.moveRotation = 0.0f;
                }
            } else if (this.moveRotation < 0.0f) {
                this.moveRotation += 0.1f;
                if (this.moveRotation > 0.0f) {
                    this.moveRotation = 0.0f;
                }
            }
        }
    }

    public float getRotation() {
        return this.moveRotation;
    }

    public MagicBroom(Level worldIn) {
        super((EntityType)EntityInit.MAGIC_BROOM.get(), worldIn);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new FindItemsGoal());
        this.goalSelector.addGoal(2, (Goal)new DepositItemsGoal());
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CONTAINER_POSITION, BlockPos.ZERO);
        builder.define(BROOM_TYPE, 0);
    }

    public int getBroomType() {
        return (Integer)this.entityData.get(BROOM_TYPE);
    }

    public void setBroomType(int type) {
        this.entityData.set(BROOM_TYPE, type);
    }

    public void tick() {
        super.tick();
        if (this.getBroomType() == 1) {
            if (this.level().isClientSide()) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.2f).setColor(10, 10, 10), this.getX(), this.getY(), this.getZ(), (double)0.3f, (double)0.05f, 0.5);
            }
            this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(16.0)).forEach(e -> {
                if (e != null && e.isAlive() && this.getSensing().hasLineOfSight((Entity)e)) {
                    Vec3 tPos = e.position();
                    Vec3 cPos = this.position();
                    Vec3 delta = cPos.subtract(tPos).normalize();
                    e.setDeltaMovement(delta.scale((double)0.15f));
                }
            });
        }
    }

    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (player.isShiftKeyDown() && !this.level().isClientSide() && this.isAlive()) {
            this.setCanPickUpLoot(false);
            this.remove(Entity.RemovalReason.DISCARDED);
            ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(this.getBroomType() == 0 ? (ItemLike)ItemInit.MAGIC_BROOM.get() : (ItemLike)ItemInit.VORTEX_BROOM.get()));
            entity.setPickUpDelay(20);
            this.level().addFreshEntity((Entity)entity);
            this.dropAllDeathLoot(this.getLastDamageSource());
            return InteractionResult.SUCCESS;
        }
        return super.interactAt(player, vec, hand);
    }

    public BlockPos getContainerPosition() {
        return (BlockPos)this.entityData.get(CONTAINER_POSITION);
    }

    public void setContainerPosition(BlockPos pos) {
        this.entityData.set(CONTAINER_POSITION, pos);
    }

    public boolean canBreatheUnderwater() {
        return true;
    }

    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
    }

    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    public boolean isCustomNameVisible() {
        return false;
    }

    public boolean canHoldItem(ItemStack stack) {
        ItemStack curStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (curStack.isEmpty()) {
            return true;
        }
        return curStack.getItem() == stack.getItem() && curStack.getCount() <= curStack.getMaxStackSize() - stack.getCount();
    }

    public void aiStep() {
        super.aiStep();
        this.updateRotations();
        if (this.level().isClientSide() && Math.abs(this.getDeltaMovement().length()) > (double)0.1f) {
            float particle_spread = 0.05f;
            Vec3 velocity = new Vec3((double)-0.05f + Math.random() * (double)0.1f, (double)0.025f, (double)-0.05f + Math.random() * (double)0.1f);
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), this.getX() + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.getY() + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), this.getZ() + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
        if (this.isAlive()) {
            for (ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(3.0, 0.25, 3.0))) {
                if (!itementity.isAlive() || itementity.getItem().isEmpty() || itementity.hasPickUpDelay()) continue;
                this.pickUpItem(itementity);
            }
        }
    }

    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        if (itemEntity.hasPickUpDelay() || itemEntity.level().isClientSide()) {
            return;
        }
        ItemStack mhStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack groundStack = itemEntity.getItem();
        if (mhStack.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, groundStack);
            this.take((Entity)itemEntity, mhStack.getCount());
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
        } else if (ItemStack.matches((ItemStack)groundStack, (ItemStack)mhStack)) {
            int space = mhStack.getMaxStackSize() - mhStack.getCount();
            int qtyCollected = Math.min(space, groundStack.getCount());
            ItemStack pickupStack = groundStack.split(qtyCollected);
            mhStack.setCount(mhStack.getCount() + pickupStack.getCount());
            this.setItemSlot(EquipmentSlot.MAINHAND, mhStack);
            if (groundStack.getCount() <= 0) {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                itemEntity.setItem(groundStack);
            }
        }
    }

    protected void dropAllDeathLoot(DamageSource damageSourceIn) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemstack.isEmpty()) {
            this.spawnAtLocation(itemstack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        BlockPos target = this.getContainerPosition();
        CompoundTag targetPos = new CompoundTag();
        targetPos.putInt("x", target.getX());
        targetPos.putInt("y", target.getY());
        targetPos.putInt("z", target.getZ());
        compound.put(KEY_TARGETPOS, (Tag)targetPos);
        compound.putInt(KEY_BROOM_TYPE, ((Integer)this.entityData.get(BROOM_TYPE)).intValue());
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(KEY_TARGETPOS)) {
            CompoundTag targetPos = compound.getCompound(KEY_TARGETPOS);
            this.setContainerPosition(new BlockPos(targetPos.getInt("x"), targetPos.getInt("y"), targetPos.getInt("z")));
        }
        if (compound.contains(KEY_BROOM_TYPE)) {
            this.entityData.set(BROOM_TYPE, compound.getInt(KEY_BROOM_TYPE));
        }
    }

    class FindItemsGoal
    extends Goal {
        private BlockPos originPos;
        private boolean abort = false;
        private ArrayList<Integer> ignoredEntityIDs;
        private int stuck_count;
        private Vec3 last_pos;

        public FindItemsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
            this.ignoredEntityIDs = new ArrayList();
        }

        public boolean canUse() {
            if (MagicBroom.this.depositing) {
                return false;
            }
            if (this.abort) {
                this.abort = false;
                return false;
            }
            List list = MagicBroom.this.level().getEntitiesOfClass(ItemEntity.class, MagicBroom.this.getBoundingBox().inflate(16.0, 4.0, 16.0), e -> {
                if (MagicBroom.this.getSensing().hasLineOfSight((Entity)e)) {
                    return ITEM_SELECTOR.test((ItemEntity)e);
                }
                return false;
            });
            if (!list.isEmpty()) {
                ItemStack stack = MagicBroom.this.getItemBySlot(EquipmentSlot.MAINHAND);
                if (stack.getCount() == stack.getMaxStackSize()) {
                    return false;
                }
                if (stack.isEmpty()) {
                    return true;
                }
                for (ItemEntity item : list) {
                    if (item.getItem().getItem() != MagicBroom.this.getItemBySlot(EquipmentSlot.MAINHAND).getItem()) continue;
                    return true;
                }
            }
            return false;
        }

        public void tick() {
            if (this.originPos == null || !MagicBroom.this.level().isLoaded(this.originPos)) {
                this.abort = true;
                return;
            }
            this.stuck_count = this.last_pos != null && MagicBroom.this.position().distanceTo(this.last_pos) < 0.25 ? ++this.stuck_count : 0;
            ItemStack itemstack = MagicBroom.this.getItemBySlot(EquipmentSlot.MAINHAND);
            ItemEntity pickupTarget = null;
            if (itemstack.isEmpty()) {
                List list = MagicBroom.this.level().getEntitiesOfClass(ItemEntity.class, new AABB(this.originPos).inflate(16.0, 4.0, 16.0), e -> !this.ignoredEntityIDs.contains(e.getId()) && ITEM_SELECTOR.test((ItemEntity)e));
                if (!list.isEmpty()) {
                    pickupTarget = (ItemEntity)list.get(0);
                }
            } else {
                List list = MagicBroom.this.level().getEntitiesOfClass(ItemEntity.class, new AABB(this.originPos).inflate(16.0, 4.0, 16.0), item -> !this.ignoredEntityIDs.contains(item.getId()) && !item.hasPickUpDelay() && item.isAlive() && item.getItem().getItem() == itemstack.getItem());
                if (!list.isEmpty() && itemstack.getCount() < itemstack.getMaxStackSize()) {
                    pickupTarget = (ItemEntity)list.get(0);
                }
            }
            if (!(pickupTarget == null || this.stuck_count != 20 && MagicBroom.this.getNavigation().moveTo((Entity)pickupTarget, (double)0.4f))) {
                this.ignoredEntityIDs.add(pickupTarget.getId());
                this.stuck_count = 0;
            }
        }

        public void start() {
            this.ignoredEntityIDs.clear();
            this.stuck_count = 0;
            this.last_pos = null;
            this.originPos = MagicBroom.this.getContainerPosition();
            this.abort = false;
            List list = MagicBroom.this.level().getEntitiesOfClass(ItemEntity.class, MagicBroom.this.getBoundingBox().inflate(16.0, 1.0, 16.0), e -> {
                if (MagicBroom.this.getSensing().hasLineOfSight((Entity)e)) {
                    return ITEM_SELECTOR.test((ItemEntity)e);
                }
                return false;
            });
            if (!list.isEmpty()) {
                MagicBroom.this.getNavigation().moveTo((Entity)list.get(0), (double)0.4f);
            }
        }
    }

    class DepositItemsGoal
    extends Goal {
        private BlockEntity targetTE;
        private boolean abort = false;

        public DepositItemsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            if (this.abort) {
                this.abort = false;
                return false;
            }
            if (MagicBroom.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            }
            return MagicBroom.this.blockPosition().closerThan((Vec3i)MagicBroom.this.getContainerPosition(), 48.0);
        }

        public void stop() {
            super.stop();
            MagicBroom.this.depositing = false;
            MagicBroom.this.deposit_counter = 10;
        }

        public void start() {
            BlockPos target = MagicBroom.this.getContainerPosition();
            MagicBroom.this.getNavigation().moveTo((double)target.getX(), (double)target.getY(), (double)target.getZ(), (double)0.4f);
            this.targetTE = MagicBroom.this.getCommandSenderWorld().getBlockEntity(MagicBroom.this.getContainerPosition());
            this.abort = false;
        }

        public void tick() {
            if (this.targetTE == null) {
                this.abort = true;
                return;
            }
            java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(this.targetTE, Direction.UP);
            FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)MagicBroom.this.level()));
            if (!handler.isPresent()) {
                this.abort = true;
                return;
            }
            ItemStack activeStack = MagicBroom.this.getItemBySlot(EquipmentSlot.MAINHAND);
            BlockPos target = MagicBroom.this.getContainerPosition();
            double dist = MagicBroom.this.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (dist < 4.0) {
                MagicBroom.this.navigation.stop();
                if (!MagicBroom.this.depositing) {
                    MagicBroom.this.depositing = true;
                    if (this.targetTE instanceof Container) {
                        ((Container)this.targetTE).startOpen((Player)player);
                    }
                }
                if (MagicBroom.this.deposit_counter <= 0) {
                    MagicBroom.this.deposit_counter = 10;
                    if (InventoryUtilities.mergeIntoInventory(handler.get(), activeStack, 10)) {
                        MagicBroom.this.depositing = false;
                    }
                } else {
                    --MagicBroom.this.deposit_counter;
                }
                if (!MagicBroom.this.depositing && this.targetTE instanceof Container) {
                    ((Container)this.targetTE).stopOpen((Player)player);
                }
            } else {
                MagicBroom.this.getNavigation().moveTo((double)target.getX(), (double)target.getY(), (double)target.getZ(), (double)0.4f);
            }
        }
    }
}


