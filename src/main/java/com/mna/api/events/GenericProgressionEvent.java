/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class GenericProgressionEvent
extends Event {
    private Player player;
    private ResourceLocation id;

    public GenericProgressionEvent(Player player, ResourceLocation eventType) {
        this.player = player;
        this.id = eventType;
    }

    public ResourceLocation getEventType() {
        return this.id;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isCancelable() {
        return false;
    }
}


