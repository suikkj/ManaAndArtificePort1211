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
        Optional<RitualRecipe> recipe = world.getRecipeManager().getRecipesFor(RecipeInit.RITUAL_TYPE.get(), null, world).stream().filter(r -> r.id().toString().equals(rLoc.toString())).map(r -> r.value()).findFirst();
        return recipe.isPresent() ? recipe.get() : null;
    }

    public static Collection<RitualRecipe> getAllRituals(Level world) {
        List<RitualRecipe> collection = world.getRecipeManager().getRecipesFor(RecipeInit.RITUAL_TYPE.get(), null, world).stream().map(r -> r.value()).collect(java.util.stream.Collectors.toList());
        return collection.stream().sorted((c1, c2) -> c2.getLowerBound() - c1.getLowerBound()).collect(Collectors.toList());
    }
}

