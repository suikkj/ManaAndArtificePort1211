/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class RuneforgeEnchantEvent
extends Event implements net.neoforged.bus.api.ICancellableEvent {
    private ItemStack output;
    private Player crafter;

    public RuneforgeEnchantEvent(ItemStack output, Player crafter) {
        this.output = output;
        this.crafter = crafter;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public Player getCrafter() {
        return this.crafter;
    }
}


