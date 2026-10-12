/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Cancelable
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
public class SpellCastEvent
extends Event {
    ISpellDefinition spell;
    SpellSource source;
    SpellContext context;
    ItemStack stack;

    public SpellCastEvent(ISpellDefinition spell, SpellSource source, ItemStack stack, @Nullable SpellContext context) {
        this.spell = spell;
        this.source = source;
        this.stack = stack;
        this.context = context;
    }

    public ISpellDefinition getSpell() {
        return this.spell;
    }

    public SpellSource getSource() {
        return this.source;
    }

    @Nullable
    public SpellContext getContext() {
        return this.context;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Nullable
    @Deprecated(forRemoval=true)
    public Player getCaster() {
        if (this.source.isPlayerCaster()) {
            return this.source.getPlayer();
        }
        return null;
    }
}


