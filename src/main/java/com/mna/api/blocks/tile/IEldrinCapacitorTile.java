/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerData
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.blocks.tile;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.PlayerOwnershipRecord;
import com.mna.api.faction.IFaction;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.phys.Vec3;

public interface IEldrinCapacitorTile
extends ContainerData {
    public static final int DATA_SHARE_TEAM = 0;
    public static final int DATA_SHARE_FACTION = 1;
    public static final int DATA_SHARE_PUBLIC = 2;
    public static final int DATA_AFFINITY_AMOUNT_ARCANE = 3;
    public static final int DATA_AFFINITY_AMOUNT_ENDER = 4;
    public static final int DATA_AFFINITY_AMOUNT_FIRE = 5;
    public static final int DATA_AFFINITY_AMOUNT_WATER = 6;
    public static final int DATA_AFFINITY_AMOUNT_EARTH = 7;
    public static final int DATA_AFFINITY_AMOUNT_WIND = 8;
    public static final int DATA_CHARGE_RATE = 9;
    public static final int DATA_DISPERSAL_RADIUS = 10;
    public static final int DATA_DISPERSAL_RATE = 11;

    public List<Affinity> getAffinities();

    default public float getChargeRate() {
        return 1.0f;
    }

    default public float getRateLimit() {
        return -1.0f;
    }

    @Deprecated(forRemoval=true)
    default public float getChargeRadius() {
        return this.getDispersalRadius();
    }

    default public float getDispersalRadius() {
        return 16.0f;
    }

    public float getCapacity(Affinity var1);

    public float getCharge(Affinity var1);

    public void setCharge(Affinity var1, float var2);

    public float charge(Affinity var1, float var2);

    public float consume(Affinity var1, float var2);

    default public boolean supplies(Affinity affinity) {
        return this.getAffinities().contains(affinity);
    }

    @Deprecated
    default public boolean canSupply(Affinity affinity, Player crafter) {
        return this.canSupply(affinity, PlayerOwnershipRecord.of(crafter));
    }

    default public boolean canSupply(Affinity affinity, PlayerOwnershipRecord owner) {
        if (affinity != Affinity.UNKNOWN && !this.supplies(affinity)) {
            return false;
        }
        if (owner == null || this.getPlacedBy() == null) {
            return false;
        }
        if (owner.getPlayerProfileID().equals(this.getPlacedBy())) {
            return true;
        }
        if (this.shareWithTeam() && this.getPlacedByTeam() != null && owner.getTeam() != null && owner.getTeam().equals(this.getPlacedByTeam())) {
            return true;
        }
        if (this.shareWithFaction()) {
            IFaction crafterFaction = owner.getFaction();
            IFaction placedByFaction = this.getPlacedByFaction();
            if (crafterFaction != null && placedByFaction != null && crafterFaction.isAlliedTo(placedByFaction)) {
                return true;
            }
        }
        return this.isPublic();
    }

    public boolean isPublic();

    public boolean shareWithTeam();

    public boolean shareWithFaction();

    public void setPublic(boolean var1);

    public void setTeamShare(boolean var1);

    public void setFactionShare(boolean var1);

    public UUID getPlacedBy();

    public String getPlacedByPlayerName();

    @Nullable
    public String getPlacedByTeam();

    @Nullable
    public IFaction getPlacedByFaction();

    public void setPlacedBy(Player var1);

    default public float supply(PlayerOwnershipRecord requester, Vec3 destPos, Affinity affinity, float amount) {
        return this.supply(requester, destPos, affinity, amount, false);
    }

    @Deprecated
    default public float supply(Player requester, Vec3 destPos, Affinity affinity, float amount) {
        return this.supply(PlayerOwnershipRecord.of(requester), destPos, affinity, amount);
    }

    default public float supply(PlayerOwnershipRecord requester, Vec3 destPos, Affinity affinity, float amount, boolean simulate) {
        if (!this.canSupply(affinity, requester)) {
            return 0.0f;
        }
        float rateLimit = this.getRateLimit();
        if (rateLimit > 0.0f && amount > rateLimit) {
            amount = rateLimit;
        }
        if (simulate) {
            float current = this.getCharge(affinity);
            if (current < amount) {
                return current;
            }
            return amount;
        }
        return this.consume(affinity, amount);
    }

    default public float supply(Player requester, Vec3 destPos, Affinity affinity, float amount, boolean simulate) {
        return this.supply(PlayerOwnershipRecord.of(requester), destPos, affinity, amount, simulate);
    }

    default public int getCount() {
        return 12;
    }

    default public void set(int pIndex, int pValue) {
        switch (pIndex) {
            case 0: {
                this.setTeamShare(pValue == 1);
                break;
            }
            case 1: {
                this.setFactionShare(pValue == 1);
                break;
            }
            case 2: {
                this.setPublic(pValue == 1);
                break;
            }
            case 3: {
                this.setCharge(Affinity.ARCANE, (float)pValue / 100.0f);
                break;
            }
            case 4: {
                this.setCharge(Affinity.ENDER, (float)pValue / 100.0f);
                break;
            }
            case 5: {
                this.setCharge(Affinity.FIRE, (float)pValue / 100.0f);
                break;
            }
            case 6: {
                this.setCharge(Affinity.WATER, (float)pValue / 100.0f);
                break;
            }
            case 7: {
                this.setCharge(Affinity.EARTH, (float)pValue / 100.0f);
                break;
            }
            case 8: {
                this.setCharge(Affinity.WIND, (float)pValue / 100.0f);
            }
        }
    }

    default public int get(int pIndex) {
        switch (pIndex) {
            case 0: {
                return this.shareWithTeam() ? 1 : 0;
            }
            case 1: {
                return this.shareWithFaction() ? 1 : 0;
            }
            case 2: {
                return this.isPublic() ? 1 : 0;
            }
            case 3: {
                return (int)(this.getCharge(Affinity.ARCANE) * 100.0f);
            }
            case 4: {
                return (int)(this.getCharge(Affinity.ENDER) * 100.0f);
            }
            case 5: {
                return (int)(this.getCharge(Affinity.FIRE) * 100.0f);
            }
            case 6: {
                return (int)(this.getCharge(Affinity.WATER) * 100.0f);
            }
            case 7: {
                return (int)(this.getCharge(Affinity.EARTH) * 100.0f);
            }
            case 8: {
                return (int)(this.getCharge(Affinity.WIND) * 100.0f);
            }
            case 9: {
                return (int)(this.getChargeRate() * 100.0f);
            }
            case 10: {
                return (int)(this.getDispersalRadius() * 100.0f);
            }
            case 11: {
                return (int)(this.getRateLimit() * 100.0f);
            }
        }
        return 0;
    }
}

