/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.cantrips.ICantrip;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class CantripCastEvent
extends Event {
    ICantrip cantrip;
    Player caster;

    public CantripCastEvent(ICantrip spell, Player caster) {
        this.cantrip = spell;
        this.caster = caster;
    }

    public ICantrip getCantrip() {
        return this.cantrip;
    }

    public Player getCrafter() {
        return this.caster;
    }

    public boolean isCancelable() {
        return false;
    }
}


