/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.runeforging;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.runeforging.RunescribingRecipe;
import java.util.HashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class RunescribingRecipeSerializer
extends AMRecipeBaseSerializer<RunescribingRecipe> {
    public static HashMap<ResourceLocation, RunescribingRecipe> ALL_RECIPES = new HashMap();

    @Override
    public RunescribingRecipe readExtra(ResourceLocation recipeId, JsonObject json) {
        RunescribingRecipe recipe = new RunescribingRecipe(recipeId);
        recipe.parseJSON(json);
        ALL_RECIPES.put(recipeId, recipe);
        return recipe;
    }

    @Override
    public RunescribingRecipe readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            RunescribingRecipe recipe = new RunescribingRecipe(recipeId);
            recipe.setHMutex(buffer.readLong());
            recipe.setVMutex(buffer.readLong());
            recipe.setOutputResource(buffer.readUtf());
            ALL_RECIPES.put(recipeId, recipe);
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading runescribing recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public void writeExtra(FriendlyByteBuf buffer, RunescribingRecipe recipe) {
        try {
            buffer.writeLong(recipe.getHMutex());
            buffer.writeLong(recipe.getVMutex());
            buffer.writeUtf(recipe.getOutputResource().toString());
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing runescribing recipe from packet.", (Throwable)e);
            throw e;
        }
    }
}

