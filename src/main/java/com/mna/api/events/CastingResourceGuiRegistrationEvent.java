/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.Event
 *  net.neoforged.fml.event.IModBusEvent
 */
package com.mna.api.events;

import com.mna.api.capabilities.resource.ICastingResourceGuiRegistry;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

@Deprecated
public class CastingResourceGuiRegistrationEvent
extends Event
implements IModBusEvent {
    private final ICastingResourceGuiRegistry _registry;

    public CastingResourceGuiRegistrationEvent(ICastingResourceGuiRegistry registry) {
        this._registry = registry;
    }

    public ICastingResourceGuiRegistry getRegistry() {
        return this._registry;
    }
}


