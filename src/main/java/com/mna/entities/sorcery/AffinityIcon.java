 
package com.mna.entities.sorcery;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.IAffinityEntity;
import com.mna.blocks.ritual.ChimeriteCrystalBlock;
import com.mna.entities.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class AffinityIcon
extends Entity
implements IAffinityEntity {
    private static final String KEY_NBT_AFFINITY = "affinity";
    private static final EntityDataAccessor<String> AFFINITY = SynchedEntityData.defineId(AffinityIcon.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private boolean invalid = false;

    public AffinityIcon(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public AffinityIcon(Level worldIn) {
        super((EntityType)EntityInit.AFFINITY_ICON.get(), worldIn);
    }

    @Override
    public Affinity getAffinity() {
        try {
            return Affinity.valueOf((String)this.entityData.get(AFFINITY));
        }
        catch (Exception ex) {
            if (!this.level().isClientSide()) {
                this.invalid = true;
                ManaAndArtifice.LOGGER.error("Failed to load affinity for Affinity Icon Entry.  Marked as invalid and will remove.");
            }
            return Affinity.UNKNOWN;
        }
    }

    public void setAffinity(Affinity affinity) {
        this.entityData.set(AFFINITY, affinity.toString());
    }

    public void tick() {
        if (!this.level().isClientSide()) {
            if (this.level().getGameTime() % 400L == 0L) {
                this.invalid = true;
                BlockPos checkPos = this.blockPosition();
                for (int i = -1; i <= 1 && this.invalid; ++i) {
                    for (int j = -1; j <= 1 && this.invalid; ++j) {
                        for (int k = -1; k <= 1 && this.invalid; ++k) {
                            if (!(this.level().getBlockState(checkPos.offset(i, j, k)).getBlock() instanceof ChimeriteCrystalBlock)) continue;
                            this.invalid = false;
                        }
                    }
                }
            }
            if (this.invalid) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(AFFINITY, Affinity.UNKNOWN.toString());
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains(KEY_NBT_AFFINITY)) {
            this.entityData.set(AFFINITY, compound.getString(KEY_NBT_AFFINITY));
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString(KEY_NBT_AFFINITY, (String)this.entityData.get(AFFINITY));
    }
}


