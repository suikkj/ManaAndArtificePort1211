 
package com.mna.entities.utility;

import com.mna.ManaAndArtifice;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.tools.math.Vector3;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class EldrinFlight
extends Entity
{
    private static final EntityDataAccessor<BlockPos> END_POS = SynchedEntityData.defineId(EldrinFlight.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    private Vector3 startPosition;
    private Vector3 endPosition;
    private Vector3 controlPointA;
    private Vector3 controlPointB;
    private float travelTime;
    private Player player;
    private int age = 0;
    private boolean heightAdjusted = false;
    private static final int MIN_Y = 4;

    public EldrinFlight(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public EldrinFlight(Level world, Player player, Vec3 start, Vec3 end) {
        this(world, player, start, end, 50);
    }

    public EldrinFlight(Level world, Player player, Vec3 start, Vec3 end, int blocksPerSecond) {
        super((EntityType)EntityInit.ELDRIN_FLIGHT.get(), world);
        this.player = player;
        this.startPosition = new Vector3(start);
        if (!world.isClientSide) {
            if (end.y == -1.0) {
                ChunkAccess chunk = world.getChunk(BlockPos.containing((Position)end));
                int yPos = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, (int)end.x(), (int)end.z());
                this.endPosition = new Vector3(end.x, yPos + 1, end.z);
            } else {
                this.endPosition = new Vector3(end);
            }
        } else {
            this.endPosition = new Vector3(end);
        }
        Vector3 delta = this.endPosition.sub(this.startPosition);
        this.controlPointA = this.startPosition.add(delta.scale(0.33f)).add(new Vector3(0.0, 100.0, 0.0));
        this.controlPointB = this.startPosition.add(delta.scale(0.66f)).add(new Vector3(0.0, 100.0, 0.0));
        this.noPhysics = true;
        this.travelTime = delta.length() / (float)blocksPerSecond * 20.0f;
        this.setPos(start.x(), start.y(), start.z());
    }

    public void tick() {
        ++this.age;
        if (!(this.level().isClientSide() || this.startPosition != null && this.endPosition != null && this.controlPointA != null && this.controlPointB != null)) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        if (this.level().isClientSide()) {
            if (this.player != null) {
                this.player.getPersistentData().putBoolean("eldrin_flight", true);
            }
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), this.getX(), this.getY(), this.getZ(), (Math.random() - 0.5) * 0.2, 0.0, (Math.random() - 0.5) * 0.2);
            if (this.age < 60 && this.age % 10 == 0) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.TRAIL.get())), this.getX(), this.getY(), this.getZ(), (double)this.getId(), this.level().getRandom().nextDouble(), (double)(this.travelTime + 70.0f - (float)this.age));
            }
        } else if (this.age == 1 && this.player != null) {
            this.player.startRiding((Entity)this, true);
        }
        if (!this.heightAdjusted) {
            if (this.getEndPos().getX() == 0 && this.getEndPos().getY() == 0 && this.getEndPos().getZ() == 0) {
                this.setEndPos(BlockPos.containing((Position)this.endPosition.toVec3D()));
            }
            if (this.level().isLoaded(this.getEndPos())) {
                BlockPos test = this.getEndPos();
                if (test.getY() >= this.level().dimensionType().logicalHeight()) {
                    int delta = this.level().dimensionType().logicalHeight() - 3 - test.getY();
                    test = test.offset(0, delta, 0);
                }
                while (test.getY() > 4 && !this.level().isEmptyBlock(test) && !this.level().isEmptyBlock(test.above())) {
                    test = test.below();
                }
                while (test.getY() > 4 && this.level().isEmptyBlock(test.below())) {
                    test = test.below();
                }
                while (test.getY() < this.level().getHeight() - 3 && !this.level().isEmptyBlock(test) && !this.level().isEmptyBlock(test.above())) {
                    test = test.above();
                }
                if (!this.level().isEmptyBlock(test) && !this.level().isEmptyBlock(test.above())) {
                    test.offset(0, this.level().getHeight() - test.getY(), 0);
                }
                this.setEndPos(test);
                this.endPosition = new Vector3(test);
                this.heightAdjusted = true;
            }
        }
        if (!this.level().isClientSide() && this.age > 80 && this.getPassengers().size() == 0) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        if ((float)this.age < this.travelTime) {
            Vector3 tickPos = Vector3.bezier(this.startPosition, this.endPosition, this.controlPointA, this.controlPointB, (float)this.age / this.travelTime);
            Vector3 nextTickPos = Vector3.bezier(this.startPosition, this.endPosition, this.controlPointA, this.controlPointB, (float)this.age + 1.0f / this.travelTime);
            this.setPos(tickPos.x, tickPos.y, tickPos.z);
            this.lookAt(EntityAnchorArgument.Anchor.FEET, nextTickPos.toVec3D());
        } else if ((float)this.age >= this.travelTime && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            if (this.player != null) {
                this.player.setPos(this.getX(), this.getY(), this.getZ());
            }
        }
    }

    public void onRemovedFromLevel() {
        if (this.player != null) {
            if (this.level().isClientSide()) {
                for (int i = 0; i < 200; ++i) {
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), this.player.getX(), this.player.getY(), this.player.getZ(), (Math.random() - 0.5) * 0.2, Math.random() * 0.2, (Math.random() - 0.5) * 0.2);
                }
                ManaAndArtifice.instance.proxy.resetRenderViewEntity();
            }
            this.player.getPersistentData().remove("eldrin_flight");
            this.player.getPersistentData().remove("eldrin_flight_entity_id");
            this.player.fallDistance = 0.0f;
            this.player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_FLIGHT.get()));
        }
        super.onRemovedFromLevel();
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(END_POS, BlockPos.ZERO);
    }

    private void setEndPos(BlockPos pos) {
        if (!this.level().isClientSide()) {
            this.entityData.set(END_POS, pos);
        }
    }

    private BlockPos getEndPos() {
        return (BlockPos)this.entityData.get(END_POS);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_184206_1_) {
        super.onSyncedDataUpdated(p_184206_1_);
        if (p_184206_1_ == END_POS && this.level().isClientSide()) {
            this.heightAdjusted = true;
            this.endPosition = new Vector3(this.getEndPos());
        }
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("start")) {
            this.startPosition = Vector3.readFromNBT(compound.getCompound("start"));
        }
        if (compound.contains("end")) {
            this.endPosition = Vector3.readFromNBT(compound.getCompound("end"));
        }
        if (compound.contains("control_a")) {
            this.controlPointA = Vector3.readFromNBT(compound.getCompound("control_a"));
        }
        if (compound.contains("control_b")) {
            this.controlPointB = Vector3.readFromNBT(compound.getCompound("control_b"));
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        CompoundTag start = new CompoundTag();
        CompoundTag end = new CompoundTag();
        CompoundTag control_a = new CompoundTag();
        CompoundTag control_b = new CompoundTag();
        this.startPosition.writeToNBT(start);
        this.endPosition.writeToNBT(end);
        this.controlPointA.writeToNBT(control_a);
        this.controlPointB.writeToNBT(control_b);
        compound.put("start", (Tag)start);
        compound.put("end", (Tag)end);
        compound.put("control_a", (Tag)control_a);
        compound.put("control_b", (Tag)control_b);
    }

    public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        if (this.player != null) {
            buffer.writeInt(this.player.getId());
        } else {
            buffer.writeInt(-1);
        }
        buffer.writeFloat(this.travelTime);
        if (this.startPosition != null) {
            buffer.writeDouble((double)this.startPosition.x);
            buffer.writeDouble((double)this.startPosition.y);
            buffer.writeDouble((double)this.startPosition.z);
        }
        if (this.endPosition != null) {
            buffer.writeDouble((double)this.endPosition.x);
            buffer.writeDouble((double)this.endPosition.y);
            buffer.writeDouble((double)this.endPosition.z);
        }
        if (this.controlPointA != null) {
            buffer.writeDouble((double)this.controlPointA.x);
            buffer.writeDouble((double)this.controlPointA.y);
            buffer.writeDouble((double)this.controlPointA.z);
        }
        if (this.controlPointB != null) {
            buffer.writeDouble((double)this.controlPointB.x);
            buffer.writeDouble((double)this.controlPointB.y);
            buffer.writeDouble((double)this.controlPointB.z);
        }
    }

    public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf additionalData) {
        Entity e = this.level().getEntity(additionalData.readInt());
        if (e instanceof Player) {
            this.player = (Player)e;
        }
        this.travelTime = additionalData.readFloat();
        this.startPosition = new Vector3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        this.endPosition = new Vector3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        this.controlPointA = new Vector3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        this.controlPointB = new Vector3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
    }
}


