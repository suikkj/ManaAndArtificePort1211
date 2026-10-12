 
package com.mna.entities.utility;

import com.mna.entities.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;

public class StationaryBlock
extends FallingBlockEntity
{
    private static final EntityDataAccessor<Integer> MAXAGE = SynchedEntityData.defineId(StationaryBlock.class, (EntityDataSerializer)EntityDataSerializers.INT);

    public StationaryBlock(Level worldIn, double x, double y, double z, BlockState fallingBlockState) {
        this((EntityType<? extends FallingBlockEntity>)((EntityType)EntityInit.STATIONARY_BLOCK_ENTITY.get()), worldIn);
        this.blocksBuilding = true;
        this.setPos(x, y + (double)((1.0f - this.getBbHeight()) / 2.0f), z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setStartPos(this.blockPosition());
        this.blockState = fallingBlockState;
        this.setNoGravity(true);
        this.setDeltaMovement(0.0, 0.0, 0.0);
    }

    public StationaryBlock(EntityType<? extends FallingBlockEntity> type, Level world) {
        super(type, world);
    }

    public void tick() {
        if (this.blockState.isAir()) {
            this.remove(Entity.RemovalReason.DISCARDED);
        } else {
            Block block = this.blockState.getBlock();
            if (this.time++ == 0) {
                BlockPos blockpos = this.blockPosition();
                if (this.level().getBlockState(blockpos).is(block)) {
                    this.level().removeBlock(blockpos, false);
                } else if (!this.level().isClientSide()) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                    return;
                }
            }
            if (this.time > (Integer)this.entityData.get(MAXAGE)) {
                BlockPos myPosition = this.blockPosition();
                this.restoreBlock(myPosition, block);
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private void restoreBlock(BlockPos myPosition, Block block) {
        BlockState blockstate = this.level().getBlockState(myPosition);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, 0.5, 0.7));
        if (!blockstate.is(Blocks.MOVING_PISTON)) {
            this.remove(Entity.RemovalReason.DISCARDED);
            boolean flag2 = blockstate.canBeReplaced((BlockPlaceContext)new DirectionalPlaceContext(this.level(), myPosition, Direction.UP, ItemStack.EMPTY, Direction.UP));
            boolean flag4 = this.blockState.canSurvive((LevelReader)this.level(), myPosition);
            if (flag2 && flag4) {
                if (this.blockState.hasProperty((Property)BlockStateProperties.WATERLOGGED) && this.level().getFluidState(myPosition).getType() == Fluids.WATER) {
                    this.blockState = (BlockState)this.blockState.setValue((Property)BlockStateProperties.WATERLOGGED, (Comparable)Boolean.valueOf(true));
                }
                if (this.level().setBlock(myPosition, this.blockState, 3)) {
                    BlockEntity tileentity;
                    if (block instanceof FallingBlock) {
                        ((FallingBlock)block).onLand(this.level(), myPosition, this.blockState, blockstate, (FallingBlockEntity)this);
                    }
                    if (this.blockData != null && this.blockState.hasBlockEntity() && (tileentity = this.level().getBlockEntity(myPosition)) != null) {
                        CompoundTag compoundnbt = tileentity.saveWithFullMetadata(com.mna.api.tools.MNAItemData.provider());
                        for (String s : this.blockData.getAllKeys()) {
                            Tag inbt = this.blockData.get(s);
                            if ("x".equals(s) || "y".equals(s) || "z".equals(s)) continue;
                            compoundnbt.put(s, inbt.copy());
                        }
                        tileentity.loadAdditional(compoundnbt, com.mna.api.tools.MNAItemData.provider());
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
        super.defineSynchedData(builder);
        builder.define(MAXAGE, 50);
    }

    public void setMaxAge(int age) {
        this.entityData.set(MAXAGE, age);
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(Block.getId((BlockState)this.getBlockState()));
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        int stateId = additionalData.readInt();
        this.blockState = Block.stateById((int)stateId);
        if (this.blockState == null) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }
    }
}


