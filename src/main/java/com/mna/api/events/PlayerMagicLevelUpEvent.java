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

public class PlayerMagicLevelUpEvent
extends Event {
    private Player player;
    private int magicLevel;

    public PlayerMagicLevelUpEvent(Player player, int newLevel) {
        this.player = player;
        this.magicLevel = newLevel;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getMagicLevel() {
        return this.magicLevel;
    }
}


