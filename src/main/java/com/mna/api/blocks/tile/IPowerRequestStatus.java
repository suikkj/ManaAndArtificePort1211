/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package com.mna.api.blocks.tile;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.PowerStatus;
import java.util.HashMap;
import net.minecraft.nbt.CompoundTag;

public interface IPowerRequestStatus {
    default public void writePowerConsumeStatus(PowerStatus value, CompoundTag tag) {
        this.writePowerConsumeStatus(value, tag, "powerStatus");
    }

    default public void writePowerConsumeStatus(PowerStatus value, CompoundTag tag, String key) {
        tag.putInt(key, value.ordinal());
    }

    default public void writePowerConsumeStatus(CompoundTag tag, HashMap<Affinity, PowerStatus> values) {
        int[] affinities = new int[values.size()];
        int[] statuses = new int[values.size()];
        int count = 0;
        for (Affinity aff : values.keySet()) {
            affinities[count] = aff.ordinal();
            statuses[count] = values.get(aff).ordinal();
            ++count;
        }
        tag.putIntArray("powerStatusKeys", affinities);
        tag.putIntArray("powerStatusValues", statuses);
    }

    default public PowerStatus readPowerConsumeStatus(CompoundTag tag) {
        return this.readPowerConsumeStatus(tag, "powerStatus");
    }

    default public PowerStatus readPowerConsumeStatus(CompoundTag tag, String key) {
        if (tag.contains(key, 3)) {
            return PowerStatus.values()[tag.getInt(key)];
        }
        return PowerStatus.NOT_REQUESTING;
    }

    default public void readPowerConsumeStatusList(CompoundTag tag, HashMap<Affinity, PowerStatus> output) {
        int[] statuses;
        int[] affinities;
        output.clear();
        if (tag.contains("powerStatusKeys", 11) && tag.contains("powerStatusValues", 11) && (affinities = tag.getIntArray("powerStatusKeys")).length == (statuses = tag.getIntArray("powerStatusValues")).length) {
            for (int i = 0; i < affinities.length; ++i) {
                Affinity aff = Affinity.values()[affinities[i]];
                PowerStatus stat = PowerStatus.values()[statuses[i]];
                output.put(aff, stat);
            }
        }
    }

    default public PowerStatus calculatePowerStatus(float requested, float obtained) {
        if (obtained < 0.0f) {
            return PowerStatus.NO_CONDUIT;
        }
        if (obtained == 0.0f) {
            return PowerStatus.NO_POWER;
        }
        return obtained < requested ? PowerStatus.PARTIAL : PowerStatus.SUPPLIED;
    }
}

