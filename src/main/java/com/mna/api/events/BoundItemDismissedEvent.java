/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class BoundItemDismissedEvent
extends Event {
    private ItemStack boundStack;
    private ItemStack restoredStack;

    public BoundItemDismissedEvent(ItemStack boundStack, ItemStack restoredStack) {
        this.boundStack = boundStack;
        this.restoredStack = restoredStack;
    }

    public ItemStack getBoundStack() {
        return this.boundStack;
    }

    public ItemStack getRestoredStack() {
        return this.restoredStack;
    }
}


