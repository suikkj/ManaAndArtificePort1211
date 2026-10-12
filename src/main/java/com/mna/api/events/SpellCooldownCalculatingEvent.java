/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class SpellCooldownCalculatingEvent
extends Event {
    private int cooldownTicks;
    private final ISpellDefinition spell;
    private final Player caster;

    public SpellCooldownCalculatingEvent(Player caster, ISpellDefinition spell, int cooldownTicks) {
        this.caster = caster;
        this.spell = spell;
        this.cooldownTicks = cooldownTicks;
    }

    public ISpellDefinition getSpell() {
        return this.spell;
    }

    public Player getCaster() {
        return this.caster;
    }

    public int getCooldown() {
        return this.cooldownTicks;
    }

    public void setCooldown(int value) {
        this.cooldownTicks = value;
    }
}


