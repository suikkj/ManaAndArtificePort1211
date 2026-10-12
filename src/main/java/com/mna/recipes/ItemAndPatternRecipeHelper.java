/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package com.mna.recipes;

import com.mna.recipes.ItemAndPatternRecipe;
import com.mna.tools.ContainerTools;
import java.util.Collection;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ItemAndPatternRecipeHelper {
    public static <T extends ItemAndPatternRecipe> T GetRecipe(Level world, ResourceLocation rLoc, RecipeType<T> type) {
        List<net.minecraft.world.item.crafting.RecipeHolder<T>> iapList = world.getRecipeManager().getRecipesFor(type, net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(ItemStack.EMPTY)), world);
        if (iapList == null || iapList.size() == 0) {
            return null;
        }
        for (net.minecraft.world.item.crafting.RecipeHolder<T> __h : iapList) {
            ItemAndPatternRecipe recipe = __h.value();
            if (recipe.getId().compareTo(rLoc) != 0) continue;
            return (T)recipe;
        }
        return null;
    }

    public static <T extends ItemAndPatternRecipe> Collection<T> getAllRecipes(Level world, RecipeType<T> type) {
        return world.getRecipeManager().getRecipesFor(type, net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(ItemStack.EMPTY)), world).stream().map(h -> h.value()).collect(java.util.stream.Collectors.toList());
    }
}

