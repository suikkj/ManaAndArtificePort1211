/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events.construct;

import com.mna.api.entities.construct.IConstruct;
import net.neoforged.bus.api.Event;

public class ConstructDiagnosticMessageLoggedEvent
extends Event {
    private IConstruct<?> construct;
    private String message;

    public ConstructDiagnosticMessageLoggedEvent(IConstruct<?> construct, String message) {
        this.construct = construct;
        this.message = message;
    }

    public IConstruct<?> getConstruct() {
        return this.construct;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String newMessage) {
        this.message = newMessage;
    }
}


