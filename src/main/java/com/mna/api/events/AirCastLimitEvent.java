/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.spells.base.ISpellDefinition;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class AirCastLimitEvent
extends Event {
    private final Player player;
    private final ISpellDefinition spell;
    private final int count;
    private int limit;

    public AirCastLimitEvent(Player player, @Nullable ISpellDefinition spell, int count, int limit) {
        this.player = player;
        this.spell = spell;
        this.count = count;
        this.limit = limit;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Nullable
    public ISpellDefinition getSpell() {
        return this.spell;
    }

    public int getCount() {
        return this.count;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}


