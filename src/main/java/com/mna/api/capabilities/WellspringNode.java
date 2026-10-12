 
package com.mna.api.capabilities;

import com.mna.api.affinity.Affinity;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WellspringNode {
    public static final float MIN_STRENGTH = 5.0f;
    public static final float MAX_STRENGTH = 25.0f;
    private final float strength;
    private Affinity affinity;
    private UUID claimedBy;
    private ResourceLocation claimedByFaction;
    private int yLevel = -1;

    public WellspringNode(Affinity affinity, float strength) {
        this.affinity = affinity;
        this.strength = Mth.clamp((float)strength, (float)5.0f, (float)25.0f);
        this.claimedByFaction = null;
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("affinity", this.affinity.ordinal());
        nbt.putFloat("strength", this.strength);
        if (this.claimedBy != null) {
            nbt.putString("claimedBy", this.claimedBy.toString());
        }
        if (this.claimedByFaction != null) {
            nbt.putString("claimedByFaction", this.claimedByFaction.toString());
        }
        if (this.yLevel > -1) {
            nbt.putInt("y", this.yLevel);
        }
    }

    public void setClaimedBy(UUID playerUUID, ResourceLocation faction, int y) {
        this.claimedBy = playerUUID;
        this.claimedByFaction = faction;
        this.yLevel = y;
    }

    public boolean setAffinity(Affinity affinity) {
        if (this.affinity == Affinity.UNKNOWN) {
            this.affinity = affinity;
            return true;
        }
        return false;
    }

    public void clearClaimedBy() {
        this.claimedBy = null;
        this.claimedByFaction = null;
        this.yLevel = -1;
    }

    public boolean isClaimedBy(UUID playerUUID) {
        if (playerUUID == null || this.claimedBy == null) {
            return false;
        }
        return this.claimedBy.equals(playerUUID);
    }

    public boolean isClaimedBy(ResourceLocation faction) {
        if (this.claimedByFaction == null) {
            return false;
        }
        return this.claimedByFaction == faction;
    }

    public boolean isClaimed() {
        return this.claimedBy != null && this.claimedByFaction != null;
    }

    public boolean hasForcedYLevel() {
        return this.yLevel > -1;
    }

    public Affinity getAffinity() {
        return this.affinity;
    }

    public float getStrength() {
        return this.strength;
    }

    @Nullable
    public UUID getClaimedBy() {
        return this.claimedBy;
    }

    public ResourceLocation getClaimedByFaction() {
        return this.claimedByFaction;
    }

    public int getYLevel() {
        return this.yLevel;
    }

    @Nullable
    public static WellspringNode fromNBT(CompoundTag nbt) {
        if (nbt.contains("affinity") && nbt.contains("strength")) {
            WellspringNode lln = new WellspringNode(Affinity.values()[nbt.getInt("affinity")], nbt.getFloat("strength"));
            if (nbt.contains("claimedBy") && nbt.contains("claimedByFaction") && nbt.contains("y")) {
                lln.setClaimedBy(UUID.fromString(nbt.getString("claimedBy")), ResourceLocation.parse(nbt.getString("claimedByFaction")), nbt.getInt("y"));
            }
            return lln;
        }
        return null;
    }
}

