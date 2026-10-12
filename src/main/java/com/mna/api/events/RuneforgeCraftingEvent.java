/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.recipes.IRuneforgeRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class RuneforgeCraftingEvent
extends Event {
    private ItemStack output;
    private Player crafter;
    private IRuneforgeRecipe recipe;

    public RuneforgeCraftingEvent(IRuneforgeRecipe recipe, ItemStack output, Player crafter) {
        this.recipe = recipe;
        this.output = output;
        this.crafter = crafter;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public Player getCrafter() {
        return this.crafter;
    }

    public IRuneforgeRecipe getRecipe() {
        return this.recipe;
    }
}


