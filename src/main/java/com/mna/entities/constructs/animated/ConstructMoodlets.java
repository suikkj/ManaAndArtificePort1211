/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package com.mna.entities.constructs.animated;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.mutable.MutableInt;

public class ConstructMoodlets {
    private HashMap<Integer, Integer> moodlets = new HashMap();

    public void tick() {
        this.moodlets.forEach((k, v) -> {
            v = v - 1;
            this.moodlets.put((Integer)k, (Integer)v);
        });
        this.moodlets.remove(1, 0);
        this.moodlets.remove(2, 0);
        this.moodlets.remove(4, 0);
        this.moodlets.remove(8, 0);
        this.moodlets.remove(16, 0);
        this.moodlets.remove(32, 0);
    }

    public void addMoodlet(int mutex, int strength) {
        int existing = this.moodlets.getOrDefault(mutex, 0);
        if (strength > existing) {
            this.moodlets.put(mutex, strength);
        }
    }

    public void clearMoodlet(int mutex) {
        this.moodlets.remove(mutex);
    }

    public int getStrongestMoodlet() {
        if (this.moodlets.size() == 0) {
            return 16;
        }
        MutableInt mood = new MutableInt(16);
        MutableInt strength = new MutableInt(0);
        this.moodlets.forEach((k, v) -> {
            if (v > strength.intValue()) {
                strength.setValue((Number)v);
                mood.setValue((Number)k);
            }
        });
        return mood.getValue();
    }

    public void writeToNBT(CompoundTag nbt) {
        ArrayList data = new ArrayList();
        this.moodlets.forEach((k, v) -> {
            data.add(k);
            data.add(v);
        });
        nbt.putIntArray("moods", data.stream().mapToInt(i -> i).toArray());
    }

    public void readFromNBT(CompoundTag nbt) {
        int[] moods = nbt.getIntArray("moods");
        this.moodlets.clear();
        for (int i = 0; i < moods.length; i += 2) {
            this.moodlets.put(moods[i], moods[i + 1]);
        }
    }
}

