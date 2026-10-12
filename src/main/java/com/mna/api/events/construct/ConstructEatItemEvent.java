/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events.construct;

import com.mna.api.entities.construct.IConstruct;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ConstructEatItemEvent
extends Event {
    IConstruct<?> construct;
    ItemStack consumedStack;

    public ConstructEatItemEvent(IConstruct<?> construct, ItemStack consumedStack) {
        this.construct = construct;
        this.consumedStack = consumedStack;
    }

    public IConstruct<?> getConstruct() {
        return this.construct;
    }

    public ItemStack getStack() {
        return this.consumedStack;
    }

    public boolean isCancelable() {
        return false;
    }
}


