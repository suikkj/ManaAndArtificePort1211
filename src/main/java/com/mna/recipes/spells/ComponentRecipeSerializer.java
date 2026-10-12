/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.spells;

import com.mna.recipes.ItemAndPatternRecipeSerializer;
import com.mna.recipes.spells.ComponentRecipe;
import net.minecraft.resources.ResourceLocation;

public class ComponentRecipeSerializer
extends ItemAndPatternRecipeSerializer<ComponentRecipe> {
    @Override
    protected ComponentRecipe instantiate(ResourceLocation recipeId) {
        return new ComponentRecipe(recipeId);
    }
}

