/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.capabilities.resource.ICastingResourceRegistry;
import net.neoforged.bus.api.Event;

@Deprecated
public class CastingResourceRegistrationEvent
extends Event {
    private final ICastingResourceRegistry _registry;

    public CastingResourceRegistrationEvent(ICastingResourceRegistry registry) {
        this._registry = registry;
    }

    public ICastingResourceRegistry getRegistry() {
        return this._registry;
    }
}


