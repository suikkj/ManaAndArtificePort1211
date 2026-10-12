/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.spells.base.ISpellComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class MasteryGainedEvent
extends Event
implements net.neoforged.bus.api.ICancellableEvent {
    private final Player player;
    private final ISpellComponent part;
    private float amount;

    public MasteryGainedEvent(Player player, ISpellComponent part, float amount) {
        this.player = player;
        this.amount = amount;
        this.part = part;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ISpellComponent getPart() {
        return this.part;
    }

    public float getAmount() {
        return this.amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public boolean isCancelable() {
        return true;
    }
}


