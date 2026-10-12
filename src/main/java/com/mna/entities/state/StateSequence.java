/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities.state;

import com.mna.entities.state.SequenceEntry;
import java.util.ArrayList;
import net.minecraft.world.entity.Entity;

public class StateSequence<T extends Entity> {
    private ArrayList<SequenceEntry<T>> sequence = new ArrayList();
    private int curIndex = 0;
    private int timer = 0;

    public SequenceEntry<T> AddEntry(int delay) {
        SequenceEntry entry = new SequenceEntry(delay);
        this.sequence.add(entry);
        return entry;
    }

    public void tick() {
        if (this.curIndex >= this.sequence.size()) {
            return;
        }
        ++this.timer;
        SequenceEntry<T> curEntry = this.sequence.get(this.curIndex);
        if (this.timer > curEntry.getDelay()) {
            curEntry.complete();
            this.timer = 0;
            ++this.curIndex;
        } else {
            curEntry.tick(this.timer);
        }
    }

    public void reset() {
        this.timer = 0;
        this.curIndex = 0;
    }

    public boolean isComplete() {
        return this.curIndex >= this.sequence.size();
    }
}

