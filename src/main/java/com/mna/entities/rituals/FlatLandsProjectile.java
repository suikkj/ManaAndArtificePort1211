 
package com.mna.entities.rituals;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.EntityInit;
import com.mna.tools.BlockUtils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlatLandsProjectile
extends Entity {
    private static final EntityDataAccessor<BlockPos> END = SynchedEntityData.defineId(FlatLandsProjectile.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    int age = 0;
    float interpPct = 0.0f;
    Player caster_reference;
    UUID casterID;
    int shotTime = 200;

    public FlatLandsProjectile(EntityType<? extends FlatLandsProjectile> type, Level worldIn) {
        super(type, worldIn);
    }

    public FlatLandsProjectile(Level worldIn) {
        this((EntityType<? extends FlatLandsProjectile>)((EntityType)EntityInit.FLAT_LANDS_PROJECTILE.get()), worldIn);
    }

    public void tick() {
        ++this.age;
        if (this.age > 200 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        Player caster = this.getCaster();
        if (caster == null) {
            return;
        }
        if (!this.level().isClientSide() && this.age > this.shotTime) {
            BlockPos end = (BlockPos)this.entityData.get(END);
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        BlockPos targetPos = end.offset(i, j, k);
                        if (this.level().getBlockEntity(targetPos) != null || !BlockUtils.destroyBlock((LivingEntity)caster, this.level(), targetPos, true, (Tier)Tiers.IRON)) continue;
                        BlockUtils.updateBlockState(this.level(), targetPos);
                    }
                }
            }
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
    }

    private Player getCaster() {
        if (this.caster_reference == null && this.casterID != null) {
            this.caster_reference = this.level().getPlayerByUUID(this.casterID);
        }
        return this.caster_reference;
    }

    public Vec3 getCurrentEndPoint() {
        return Vec3.atCenterOf((Vec3i)((Vec3i)this.entityData.get(END)));
    }

    public void setPoints(Vec3 start, BlockPos end) {
        this.setPos(start);
        this.entityData.set(END, end);
        this.shotTime = (int)start.distanceTo(Vec3.atCenterOf((Vec3i)end));
    }

    public void setCaster(Player caster) {
        this.caster_reference = caster;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(END, this.blockPosition());
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (END.equals(pKey)) {
            Vec3 start = this.position();
            Vec3 end = Vec3.atCenterOf((Vec3i)((Vec3i)this.entityData.get(END)));
            this.shotTime = (int)start.distanceTo(end);
            Vec3 delta = end.subtract(start).normalize();
            start = start.add(delta);
            int[] color = Affinity.EARTH.getColor();
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())).setMaxAge(this.shotTime).setColor(color[0], color[1], color[2]), start.x(), start.y(), start.z(), end.x(), end.y(), end.z());
        }
        super.onSyncedDataUpdated(pKey);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("end_x") && compound.contains("end_y") && compound.contains("end_z")) {
            this.entityData.set(END, new BlockPos(compound.getInt("end_x"), compound.getInt("end_y"), compound.getInt("end_z")));
        }
        if (compound.contains("caster_uuid")) {
            try {
                this.casterID = UUID.fromString(compound.getString("caster_uuid"));
            }
            catch (Exception e) {
                ManaAndArtifice.LOGGER.error("Error loading caster UUID for flat lands projectile!");
            }
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("end_x", ((BlockPos)this.entityData.get(END)).getX());
        compound.putInt("end_y", ((BlockPos)this.entityData.get(END)).getY());
        compound.putInt("end_z", ((BlockPos)this.entityData.get(END)).getZ());
        compound.putString("caster_uuid", this.caster_reference != null ? this.caster_reference.getUUID().toString() : "");
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isPickable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    protected boolean canRide(Entity entityIn) {
        return false;
    }
}


