/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.crush;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.crush.CrushingRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class CrushingRecipeSerializer
extends AMRecipeBaseSerializer<CrushingRecipe> {
    @Override
    public CrushingRecipe readExtra(ResourceLocation recipeId, JsonObject json) {
        CrushingRecipe recipe = new CrushingRecipe(recipeId);
        recipe.parseJSON(json);
        return recipe;
    }

    @Override
    public CrushingRecipe readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            CrushingRecipe recipe = new CrushingRecipe(recipeId);
            recipe.setInputResource(buffer.readResourceLocation());
            recipe.setOutputResource(buffer.readResourceLocation());
            recipe.setOutputQuantity(buffer.readInt());
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading crushing recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public void writeExtra(FriendlyByteBuf buffer, CrushingRecipe recipe) {
        try {
            buffer.writeResourceLocation(recipe.getInputResource());
            buffer.writeResourceLocation(recipe.getOutputResource());
            buffer.writeInt(recipe.getOutputQuantity());
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing crushing recipe to packet.", (Throwable)e);
            throw e;
        }
    }
}

