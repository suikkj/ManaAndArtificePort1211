/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.recipes;

import com.mna.api.recipes.IMARecipe;
import net.minecraft.resources.ResourceLocation;

public interface IItemAndPatternRecipe
extends IMARecipe {
    public ResourceLocation[] getRequiredItems();

    public ResourceLocation[] getRequiredPatterns();
}

