/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 */
package com.mna.api.timing;

import com.mna.api.timing.IDelayedEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class TimedDelayedDamage<T>
implements IDelayedEvent {
    private int delay;
    private float damage;
    private Entity target;
    private DamageSource source;
    private String identifier;

    public TimedDelayedDamage(String identifier, int delay, float damage, Entity target, DamageSource source) {
        this.delay = delay;
        this.damage = damage;
        this.source = source;
        this.target = target;
        this.identifier = identifier;
    }

    @Override
    public boolean tick() {
        --this.delay;
        if (this.delay == 0) {
            if (this.source != null && this.target != null && this.target.isAlive()) {
                this.target.hurt(this.source, this.damage);
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

