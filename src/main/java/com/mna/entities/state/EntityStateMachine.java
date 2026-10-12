/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities.state;

import com.mna.entities.state.SequenceEntry;
import com.mna.entities.state.StateSequence;
import java.util.HashMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;

public class EntityStateMachine<T extends Entity> {
    private HashMap<String, StateSequence<T>> sequences = new HashMap();
    private String curSequence = "";
    private EntityDataAccessor<String> CURRENT_SEQUENCE;
    private T entityRef;

    public EntityStateMachine(T entityRef, EntityDataAccessor<String> currentSequence) {
        this.CURRENT_SEQUENCE = currentSequence;
        this.entityRef = entityRef;
    }

    public SequenceEntry<T> addSequenceEntry(String name, int delay) {
        if (!this.sequences.containsKey(name)) {
            this.sequences.put(name, new StateSequence());
        }
        StateSequence<T> sequence = this.sequences.get(name);
        return sequence.AddEntry(delay);
    }

    public void runSequence(String name) {
        if (!this.sequences.containsKey(name) || this.curSequence.equals(name)) {
            return;
        }
        this.entityRef.getEntityData().set(this.CURRENT_SEQUENCE, name);
        this.curSequence = name;
        StateSequence<T> current = this.sequences.get(this.curSequence);
        current.reset();
    }

    public void tick() {
        StateSequence<T> current;
        String seq = (String)this.entityRef.getEntityData().get(this.CURRENT_SEQUENCE);
        if (!this.curSequence.equals(seq)) {
            this.runSequence(seq);
        }
        if ((current = this.sequences.get(this.curSequence)) != null && !current.isComplete()) {
            current.tick();
        }
    }

    public boolean isComplete() {
        if (!this.sequences.containsKey(this.curSequence)) {
            return true;
        }
        StateSequence<T> current = this.sequences.get(this.curSequence);
        return current.isComplete();
    }
}

