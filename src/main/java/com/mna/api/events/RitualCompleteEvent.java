/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.recipes.IRitualRecipe;
import com.mna.api.rituals.RitualEffect;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class RitualCompleteEvent
extends Event {
    private IRitualRecipe ritual;
    private Player caster;
    private List<ItemStack> collectedReagents;
    private NonNullList<RitualEffect> handlers;
    private BlockPos center;

    public RitualCompleteEvent(IRitualRecipe ritual, NonNullList<RitualEffect> handlers, BlockPos center, Player caster, List<ItemStack> collectedReagents) {
        this.ritual = ritual;
        this.caster = caster;
        this.center = center;
        this.handlers = handlers;
        this.collectedReagents = collectedReagents;
    }

    public NonNullList<RitualEffect> getHandlers() {
        return this.handlers;
    }

    public IRitualRecipe getRitual() {
        return this.ritual;
    }

    public Player getCaster() {
        return this.caster;
    }

    public List<ItemStack> getCollectedReagents() {
        return this.collectedReagents;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public boolean isCancelable() {
        return false;
    }
}


