/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events.construct;

import com.mna.api.entities.construct.IConstruct;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class ConstructCraftedEvent
extends Event {
    IConstruct<?> construct;
    Player crafter;

    public ConstructCraftedEvent(IConstruct<?> construct, Player crafter) {
        this.construct = construct;
        this.crafter = crafter;
    }

    public IConstruct<?> getConstruct() {
        return this.construct;
    }

    public Player getCrafter() {
        return this.crafter;
    }

    public boolean isCancelable() {
        return false;
    }
}


