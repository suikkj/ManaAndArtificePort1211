/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 */
package com.mna.api.entities;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import net.minecraft.world.entity.Entity;

public interface ISpellInteractibleEntity<T extends Entity> {
    public boolean onShapeTarget(ISpellDefinition var1, SpellSource var2);
}

