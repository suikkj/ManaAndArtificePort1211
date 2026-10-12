/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events.construct;

import com.mna.api.entities.construct.FluidParameterRegistry;
import net.neoforged.bus.api.Event;

public class FluidParameterRegistrationEvent
extends Event {
    final FluidParameterRegistry registry;

    public FluidParameterRegistrationEvent(FluidParameterRegistry registry) {
        this.registry = registry;
    }

    public FluidParameterRegistry registry() {
        return this.registry;
    }
}


