/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package com.mna.recipes.rituals;

import com.mna.recipes.RecipeInit;
import com.mna.recipes.rituals.RitualRecipe;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class RitualRecipeHelper {
    public static RitualRecipe GetRitualRecipe(Level world, ResourceLocation rLoc) {
        if (world == null) {
            return null;
        }
        Optional<RitualRecipe> recipe = world.getRecipeManager().getRecipesFor((RecipeType)RecipeInit.RITUAL_TYPE.get(), null, world).stream().filter(r -> r.getId().toString().equals(rLoc.toString())).findFirst();
        return recipe.isPresent() ? recipe.get() : null;
    }

    public static Collection<RitualRecipe> getAllRituals(Level world) {
        List collection = world.getRecipeManager().getRecipesFor((RecipeType)RecipeInit.RITUAL_TYPE.get(), null, world);
        return collection.stream().sorted((c1, c2) -> c2.getLowerBound() - c1.getLowerBound()).collect(Collectors.toList());
    }
}

