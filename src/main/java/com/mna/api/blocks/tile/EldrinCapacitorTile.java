 
package com.mna.api.blocks.tile;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.PlayerOwnershipRecord;
import com.mna.api.blocks.tile.IEldrinCapacitorTile;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.events.EldrinPowerTransferredEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.gui.EldrinCapacitorPermissionsContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import com.mna.api.tools.MNACap;

public class EldrinCapacitorTile
extends BlockEntity
implements IEldrinCapacitorTile,
Consumer<FriendlyByteBuf> {
    protected List<Affinity> suppliedAffinities;
    protected HashMap<Affinity, Float> power;
    protected float capacity;
    protected List<EldrinCapacitorPermissionsContainer> openContainers;
    protected boolean isPublic;
    protected boolean shareWithTeam;
    protected boolean shareWithFaction;
    protected PlayerOwnershipRecord placedBy;
    protected boolean redstoneDisablesSupply = true;

    public EldrinCapacitorTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, float capacity, Affinity ... suppliedAffinities) {
        super(pType, pPos, pBlockState);
        this.suppliedAffinities = Arrays.asList(suppliedAffinities);
        this.power = new HashMap();
        this.capacity = capacity;
        this.openContainers = new ArrayList<EldrinCapacitorPermissionsContainer>();
        this.placedBy = PlayerOwnershipRecord.of((CompoundTag)null);
        this.suppliedAffinities.forEach(a -> this.power.put((Affinity)(a), Float.valueOf(0.0f)));
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, EldrinCapacitorTile tile) {
        if (!level.isClientSide()) {
            tile.ambientCharge();
        }
    }

    protected void ambientCharge() {
        if (this.getPlacedBy() == null) {
            this.level.destroyBlock(this.getBlockPos(), true);
            return;
        }
        MNACap.get(this.level, ManaAndArtificeMod.getWorldMagicCapability()).ifPresent(worldMagic -> {
            IWellspringNodeRegistry wellsprings = worldMagic.getWellspringRegistry();
            this.suppliedAffinities.forEach(affinity -> {
                float chargeRate;
                float current = this.power.get(affinity).floatValue();
                float amountToRequest = Math.min(this.capacity - current, chargeRate = this.getChargeRate());
                if (amountToRequest > 0.0f) {
                    float amount = wellsprings.consumePower(this.placedBy.getPlayerProfileID(), this.level, (Affinity)((affinity)), amountToRequest);
                    this.power.put((Affinity)((affinity)), Float.valueOf(current + amount));
                }
            });
        });
    }

    protected Vec3 getVFXOriginOffset() {
        return Vec3.atCenterOf((Vec3i)this.getBlockPos());
    }

    @Override
    public boolean isPublic() {
        return this.isPublic;
    }

    @Override
    public boolean shareWithTeam() {
        return this.shareWithTeam;
    }

    @Override
    public boolean shareWithFaction() {
        return this.shareWithFaction;
    }

    @Override
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public void setTeamShare(boolean teamShare) {
        this.shareWithTeam = teamShare;
    }

    @Override
    public void setFactionShare(boolean factionShare) {
        this.shareWithFaction = factionShare;
    }

    @Override
    public List<Affinity> getAffinities() {
        return this.suppliedAffinities;
    }

    @Override
    public float getCapacity(Affinity affinity) {
        if (!this.supplies(affinity)) {
            return 0.0f;
        }
        return this.capacity;
    }

    @Override
    public float getCharge(Affinity affinity) {
        if (!this.supplies(affinity)) {
            return 0.0f;
        }
        return this.power.get(affinity).floatValue();
    }

    @Override
    public void setCharge(Affinity affinity, float amount) {
        if (this.supplies(affinity)) {
            this.power.put(affinity, Float.valueOf(amount));
        }
    }

    @Override
    public float charge(Affinity affinity, float amount) {
        if (!this.supplies(affinity)) {
            return 0.0f;
        }
        float current = this.power.get(affinity).floatValue();
        if (current + amount > this.capacity) {
            amount = this.capacity - current;
        }
        this.power.put(affinity, Float.valueOf(current + amount));
        return amount;
    }

    @Override
    public float consume(Affinity affinity, float amount) {
        if (!this.supplies(affinity)) {
            return 0.0f;
        }
        float current = this.power.get(affinity).floatValue();
        if (current < amount) {
            amount = current;
        }
        this.power.put(affinity, Float.valueOf(current - amount));
        return amount;
    }

    @Override
    public float supply(Player requester, Vec3 destPos, Affinity affinity, float amount) {
        if (this.getPlacedBy() == null) {
            return 0.0f;
        }
        if (this.redstoneDisablesSupply && this.getLevel().getBestNeighborSignal(this.getBlockPos()) > 0) {
            return 0.0f;
        }
        float actualAmount = IEldrinCapacitorTile.super.supply(requester, destPos, affinity, amount, true);
        EldrinPowerTransferredEvent event = new EldrinPowerTransferredEvent(affinity, actualAmount, this.getVFXOriginOffset(), destPos);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return 0.0f;
        }
        return IEldrinCapacitorTile.super.supply(requester, destPos, affinity, amount, false);
    }

    @Override
    public void setPlacedBy(Player player) {
        this.placedBy = PlayerOwnershipRecord.of(player);
    }

    @Override
    public UUID getPlacedBy() {
        return this.placedBy.getPlayerProfileID();
    }

    @Override
    public String getPlacedByTeam() {
        return this.placedBy.getTeam();
    }

    @Override
    public String getPlacedByPlayerName() {
        return this.placedBy.getDisplayName();
    }

    @Override
    public IFaction getPlacedByFaction() {
        return this.placedBy.getFaction();
    }

    public void addContainer(EldrinCapacitorPermissionsContainer container) {
        this.openContainers.add(container);
    }

    public void removeContainer(EldrinCapacitorPermissionsContainer container) {
        this.openContainers.remove(container);
    }

    @Override
    public void accept(FriendlyByteBuf data) {
        data.writeBlockPos(this.getBlockPos());
        data.writeNbt(this.placedBy.save(this.getLevel()));
        data.writeInt(this.power.size());
        for (Map.Entry<Affinity, Float> entry : this.power.entrySet()) {
            data.writeInt(entry.getKey().ordinal());
            data.writeFloat(entry.getValue().floatValue());
        }
    }

    public EldrinCapacitorTile readFrom(FriendlyByteBuf data) {
        this.placedBy = PlayerOwnershipRecord.of(data.readNbt());
        this.power.clear();
        int count = data.readInt();
        for (int i = 0; i < count; ++i) {
            Affinity aff = Affinity.values()[data.readInt() % Affinity.values().length];
            this.power.put(aff, Float.valueOf(data.readFloat()));
        }
        return this;
    }

    protected void saveAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.putBoolean("isPublic", this.isPublic);
        pTag.putBoolean("teamShare", this.shareWithTeam);
        pTag.putBoolean("factionShare", this.shareWithFaction);
        pTag.put("owner", (Tag)this.placedBy.save(this.getLevel()));
        this.suppliedAffinities.forEach(affinity -> pTag.putFloat(affinity.name(), this.power.get(affinity).floatValue()));
        pTag.putFloat("capacity", this.capacity);
    }

    public void loadAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        if (pTag.contains("isPublic")) {
            this.isPublic = pTag.getBoolean("isPublic");
        }
        if (pTag.contains("teamShare")) {
            this.shareWithTeam = pTag.getBoolean("teamShare");
        }
        if (pTag.contains("factionShare")) {
            this.shareWithFaction = pTag.getBoolean("factionShare");
        }
        if (pTag.contains("owner")) {
            this.placedBy = PlayerOwnershipRecord.of(pTag.getCompound("owner"));
        }
        this.power.clear();
        for (Affinity affinity : Affinity.values()) {
            if (!pTag.contains(affinity.name())) continue;
            this.power.put(affinity, Float.valueOf(pTag.getFloat(affinity.name())));
        }
        this.suppliedAffinities = new ArrayList<Affinity>(this.power.keySet());
        if (pTag.contains("capacity")) {
            this.capacity = pTag.getFloat("capacity");
        }
    }
}


