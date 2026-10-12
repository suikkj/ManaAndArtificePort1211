/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class CalculatingManaCostEvent
extends Event {
    private float manaCost;
    private final LivingEntity caster;
    private final ISpellDefinition spell;

    public CalculatingManaCostEvent(LivingEntity caster, ISpellDefinition spell, float manaCost) {
        this.manaCost = manaCost;
        this.caster = caster;
        this.spell = spell;
    }

    public ISpellDefinition getSpell() {
        return this.spell;
    }

    public LivingEntity getCaster() {
        return this.caster;
    }

    public float getManaCost() {
        return this.manaCost;
    }

    public void setManaCost(float newCost) {
        this.manaCost = newCost;
    }
}


