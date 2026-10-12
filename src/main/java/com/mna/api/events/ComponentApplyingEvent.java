/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.Cancelable
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import net.neoforged.bus.api.Event;
public class ComponentApplyingEvent
extends Event {
    SpellSource source;
    SpellContext context;
    SpellTarget target;
    SpellEffect component;

    public ComponentApplyingEvent(SpellSource source, SpellContext context, SpellTarget target, SpellEffect component) {
        this.source = source;
        this.context = context;
        this.target = target;
        this.component = component;
    }

    public SpellSource getSource() {
        return this.source;
    }

    public SpellContext getContext() {
        return this.context;
    }

    public SpellTarget getTarget() {
        return this.target;
    }

    public SpellEffect getComponent() {
        return this.component;
    }

    public boolean isCancelable() {
        return true;
    }
}


