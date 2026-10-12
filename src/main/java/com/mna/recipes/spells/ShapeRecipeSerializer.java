/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.spells;

import com.mna.recipes.ItemAndPatternRecipeSerializer;
import com.mna.recipes.spells.ShapeRecipe;
import net.minecraft.resources.ResourceLocation;

public class ShapeRecipeSerializer
extends ItemAndPatternRecipeSerializer<ShapeRecipe> {
    @Override
    protected ShapeRecipe instantiate(ResourceLocation recipeId) {
        return new ShapeRecipe(recipeId);
    }
}

