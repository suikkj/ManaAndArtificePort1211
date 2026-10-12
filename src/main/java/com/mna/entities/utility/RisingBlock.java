 
package com.mna.entities.utility;

import com.mna.entities.EntityInit;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import com.mna.api.tools.MNAItemData;

public class RisingBlock
extends Entity
{
    private static final EntityDataAccessor<Integer> MAXAGE = SynchedEntityData.defineId(RisingBlock.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(RisingBlock.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(RisingBlock.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    public BlockState blockState = Blocks.SAND.defaultBlockState();
    public int time;
    public boolean dropItem = true;
    private long removeAtMillis;
    @Nullable
    public CompoundTag blockData;

    public RisingBlock(Level worldIn, double x, double y, double z, BlockState fallingBlockState) {
        super((EntityType)EntityInit.RISING_BLOCK_ENTITY.get(), worldIn);
        this.blocksBuilding = true;
        this.setPos(x, y + (double)((1.0f - this.getBbHeight()) / 2.0f), z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setStartPos(this.blockPosition());
        this.blockState = fallingBlockState;
        this.setNoGravity(true);
        this.setDeltaMovement(0.0, 0.5, 0.0);
        this.hasImpulse = true;
    }

    public RisingBlock(EntityType<? extends RisingBlock> type, Level world) {
        super(type, world);
    }

    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
        } else if (this.level().isClientSide() && this.removeAtMillis > 0L) {
            if (System.currentTimeMillis() >= this.removeAtMillis) {
                super.setRemoved(Entity.RemovalReason.DISCARDED);
            }
        } else {
            Block block = this.blockState.getBlock();
            if (this.time++ == 0) {
                BlockPos blockpos = this.blockPosition();
                if (this.level().getBlockState(blockpos).is(block)) {
                    if (block instanceof DoorBlock) {
                        BlockState below = this.level().getBlockState(blockpos.below());
                        if (below.getBlock() == block) {
                            this.level().setBlock(blockpos.below(), Blocks.AIR.defaultBlockState(), 16);
                        }
                        this.level().setBlock(blockpos, Blocks.AIR.defaultBlockState(), 16);
                    } else {
                        this.level().destroyBlock(blockpos, false);
                    }
                } else if (!this.level().isClientSide()) {
                    this.discard();
                    return;
                }
            }
            List entitiesAbove = this.level().getEntities((Entity)this, this.getBoundingBox().expandTowards(0.0, 1.0, 0.0));
            entitiesAbove.forEach(p -> this.moveEntity((Entity)p));
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level().isClientSide()) {
                BlockPos myPosition = this.blockPosition();
                if (this.time > (Integer)this.entityData.get(MAXAGE)) {
                    this.verticalCollision = true;
                }
                if (!this.verticalCollision) {
                    if (!(this.level().isClientSide() || (this.time <= 100 || myPosition.getY() >= 1 && myPosition.getY() <= 256) && this.time <= 600)) {
                        if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation((ItemLike)block);
                        }
                        this.discard();
                    }
                } else {
                    this.restoreBlock(myPosition, block);
                }
            }
        }
    }

    private void moveEntity(Entity p) {
        p.setOnGround(true);
        if (p.getDeltaMovement().y < this.getDeltaMovement().y) {
            p.setDeltaMovement(p.getDeltaMovement().x, this.getDeltaMovement().y, p.getDeltaMovement().z);
        }
    }

    private void restoreBlock(BlockPos myPosition, Block block) {
        BlockState blockstate = this.level().getBlockState(myPosition);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, 0.5, 0.7));
        if (!blockstate.is(Blocks.MOVING_PISTON)) {
            this.discard();
            if (block instanceof DoorBlock && this.blockState.hasProperty((Property)DoorBlock.HALF)) {
                this.blockState = (BlockState)this.blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            }
            boolean flag2 = blockstate.canBeReplaced((BlockPlaceContext)new DirectionalPlaceContext(this.level(), myPosition, Direction.UP, ItemStack.EMPTY, Direction.UP));
            boolean flag4 = this.blockState.canSurvive((LevelReader)this.level(), myPosition);
            if (flag2 && flag4) {
                if (this.blockState.hasProperty((Property)BlockStateProperties.WATERLOGGED) && this.level().getFluidState(myPosition).getType() == Fluids.WATER) {
                    this.blockState = (BlockState)this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
                }
                if (this.level().setBlock(myPosition, this.blockState, 3)) {
                    BlockEntity tileentity;
                    if (this.blockData != null && this.blockState.getBlock() instanceof EntityBlock && (tileentity = this.level().getBlockEntity(myPosition)) != null) {
                        CompoundTag compoundnbt = tileentity.saveWithFullMetadata(com.mna.api.tools.MNAItemData.provider());
                        for (String s : this.blockData.getAllKeys()) {
                            Tag inbt = this.blockData.get(s);
                            if ("x".equals(s) || "y".equals(s) || "z".equals(s)) continue;
                            compoundnbt.put(s, inbt.copy());
                        }
                        com.mna.api.tools.MNATileAccess.loadAdditional(tileentity, compoundnbt, com.mna.api.tools.MNAItemData.provider());
                        tileentity.setChanged();
                    }
                } else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    this.spawnAtLocation((ItemLike)block);
                }
            } else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                this.spawnAtLocation((ItemLike)block);
            }
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(DATA_START_POS, BlockPos.ZERO);
        builder.define(MAXAGE, 50);
        builder.define(SPEED, Float.valueOf(0.05f));
    }

    public void setMaxAge(int age) {
        this.entityData.set(MAXAGE, age);
    }

    public void setSpeed(float speed) {
        this.entityData.set(SPEED, Float.valueOf(speed));
        this.setDeltaMovement(0.0, speed, 0.0);
    }

    public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(Block.getId((BlockState)this.getBlockState()));
    }

    public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf additionalData) {
        int stateId = additionalData.readInt();
        this.blockState = Block.stateById((int)stateId);
        if (this.blockState == null) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }
    }

    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos p_31960_) {
        this.entityData.set(DATA_START_POS, p_31960_);
    }

    public BlockPos getStartPos() {
        return (BlockPos)this.entityData.get(DATA_START_POS);
    }

    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    protected void addAdditionalSaveData(CompoundTag p_31973_) {
        p_31973_.put("BlockState", (Tag)NbtUtils.writeBlockState((BlockState)this.blockState));
        p_31973_.putInt("Time", this.time);
        p_31973_.putBoolean("DropItem", this.dropItem);
        if (this.blockData != null) {
            p_31973_.put("TileEntityData", (Tag)this.blockData);
        }
    }

    protected void readAdditionalSaveData(CompoundTag p_31964_) {
        this.blockState = NbtUtils.readBlockState((HolderGetter)this.level().holderLookup(Registries.BLOCK), (CompoundTag)p_31964_.getCompound("BlockState"));
        this.time = p_31964_.getInt("Time");
        if (p_31964_.contains("DropItem", 99)) {
            this.dropItem = p_31964_.getBoolean("DropItem");
        }
        if (p_31964_.contains("TileEntityData", 10)) {
            this.blockData = p_31964_.getCompound("TileEntityData");
        }
        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }
    }

    public boolean displayFireAnimation() {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory p_31962_) {
        super.fillCrashReportCategory(p_31962_);
        p_31962_.setDetail("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_149654_) {
        super.recreateFromPacket(p_149654_);
        this.blockState = Block.stateById((int)p_149654_.getData());
        this.blocksBuilding = true;
        double d0 = p_149654_.getX();
        double d1 = p_149654_.getY();
        double d2 = p_149654_.getZ();
        this.setPos(d0, d1 + (double)((1.0f - this.getBbHeight()) / 2.0f), d2);
        this.setStartPos(this.blockPosition());
    }
}


