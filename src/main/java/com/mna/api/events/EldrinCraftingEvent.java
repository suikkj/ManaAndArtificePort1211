/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.recipes.IMARecipe;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class EldrinCraftingEvent
extends Event {
    private ItemStack output;
    private Player crafter;
    private IMARecipe recipe;

    public EldrinCraftingEvent(IMARecipe recipe, ItemStack output, @Nullable Player crafter) {
        this.recipe = recipe;
        this.output = output;
        this.crafter = crafter;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    @Nullable
    public Player getCrafter() {
        return this.crafter;
    }

    public IMARecipe getRecipe() {
        return this.recipe;
    }
}


