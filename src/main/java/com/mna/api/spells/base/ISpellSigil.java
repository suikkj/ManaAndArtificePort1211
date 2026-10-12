/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 */
package com.mna.api.spells.base;

import java.util.UUID;
import net.minecraft.world.entity.Entity;

public interface ISpellSigil<T extends Entity> {
    public UUID getID();

    public int getCountBonus();
}

