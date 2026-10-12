/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.recipes;

import com.mna.api.faction.IFaction;
import com.mna.api.recipes.IItemAndPatternRecipe;
import net.minecraft.resources.ResourceLocation;

public interface IManaweavingRecipe
extends IItemAndPatternRecipe {
    public boolean isEnchantment();

    public ResourceLocation getEnchantment();

    public int getEnchantmentMagnitude();

    public boolean getCopyNBT();

    public IFaction getFactionRequirement();

    @Override
    public int getTier();
}

