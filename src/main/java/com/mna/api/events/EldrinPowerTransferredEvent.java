/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.Cancelable
 *  net.neoforged.bus.api.Event
 *  net.neoforged.bus.api.Event$HasResult
 */
package com.mna.api.events;

import com.mna.api.affinity.Affinity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
/**
 * [PORT 1.21.1] @Event.HasResult removido do bus; este evento nao usa resultado.
 */
public class EldrinPowerTransferredEvent
extends Event {
    final Affinity affinity;
    final float amount;
    final Vec3 from;
    final Vec3 to;

    public EldrinPowerTransferredEvent(Affinity affinity, float amount, Vec3 from, Vec3 to) {
        this.affinity = affinity;
        this.from = from;
        this.to = to;
        this.amount = amount;
    }
}


