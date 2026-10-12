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

public class EnderfeatherCharmUsedEvent
extends Event {
    Player player;

    public EnderfeatherCharmUsedEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isCancelable() {
        return true;
    }
}


