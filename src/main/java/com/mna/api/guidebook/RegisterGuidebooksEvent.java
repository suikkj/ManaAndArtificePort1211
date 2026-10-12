/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.guidebook;

import com.mna.api.guidebook.IGuideBookRegistry;
import net.neoforged.bus.api.Event;

public class RegisterGuidebooksEvent
extends Event {
    private IGuideBookRegistry registry;

    public RegisterGuidebooksEvent(IGuideBookRegistry registry) {
        this.registry = registry;
    }

    public IGuideBookRegistry getRegistry() {
        return this.registry;
    }
}


