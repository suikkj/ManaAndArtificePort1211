/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class MagicXPGainedEvent
extends Event {
    private final Player player;
    private int amount;

    public MagicXPGainedEvent(Player player, int amount) {
        this.player = player;
        this.amount = amount;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isCancelable() {
        return true;
    }
}


