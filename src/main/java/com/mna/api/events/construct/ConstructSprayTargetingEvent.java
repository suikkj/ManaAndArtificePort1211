/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events.construct;

import com.mna.api.entities.construct.IConstruct;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.Event;

public class ConstructSprayTargetingEvent
extends Event implements net.neoforged.bus.api.ICancellableEvent {
    final IConstruct<?> construct;
    final Entity potentialTarget;
    final Fluid fluid;
    final boolean isTargetOwner;
    final boolean isTargetFriendly;

    public ConstructSprayTargetingEvent(IConstruct<?> construct, Entity target, Fluid fluid, boolean isTargetOwner, boolean isTargetFriendly) {
        this.construct = construct;
        this.potentialTarget = target;
        this.fluid = fluid;
        this.isTargetOwner = isTargetOwner;
        this.isTargetFriendly = isTargetFriendly;
    }

    public IConstruct<?> getConstruct() {
        return this.construct;
    }

    public Entity getTarget() {
        return this.potentialTarget;
    }

    public Fluid getFluid() {
        return this.fluid;
    }

    public boolean isTargetOwner() {
        return this.isTargetOwner;
    }

    public boolean isTargetFriendly() {
        return this.isTargetFriendly;
    }

    public boolean isCancelable() {
        return true;
    }
}


