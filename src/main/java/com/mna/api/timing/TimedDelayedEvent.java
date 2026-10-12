/*
 * Decompiled with CFR 0.152.
 */
package com.mna.api.timing;

import com.mna.api.timing.IDelayedEvent;
import java.util.function.BiConsumer;

public class TimedDelayedEvent<T>
implements IDelayedEvent {
    private int delay;
    private BiConsumer<String, T> callback;
    private T data;
    private String identifier;

    public TimedDelayedEvent(String identifier, int delay, T data, BiConsumer<String, T> callback) {
        this.delay = delay;
        this.callback = callback;
        this.data = data;
        this.identifier = identifier;
    }

    @Override
    public boolean tick() {
        --this.delay;
        if (this.delay == 0) {
            if (this.callback != null) {
                this.callback.accept(this.identifier, (String)this.data);
            }
            return true;
        }
        return false;
    }

    @Override
    public String getID() {
        return this.identifier;
    }
}

