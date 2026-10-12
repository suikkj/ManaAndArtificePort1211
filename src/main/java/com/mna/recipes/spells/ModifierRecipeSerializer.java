/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.spells;

import com.mna.recipes.ItemAndPatternRecipeSerializer;
import com.mna.recipes.spells.ModifierRecipe;
import net.minecraft.resources.ResourceLocation;

public class ModifierRecipeSerializer
extends ItemAndPatternRecipeSerializer<ModifierRecipe> {
    @Override
    protected ModifierRecipe instantiate(ResourceLocation recipeId) {
        return new ModifierRecipe(recipeId);
    }
}

