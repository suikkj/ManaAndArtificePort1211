/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.eldrin;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.eldrin.FumeFilterRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class FumeFilterRecipeSerializer
extends AMRecipeBaseSerializer<FumeFilterRecipe> {
    @Override
    public void writeExtra(FriendlyByteBuf buffer, FumeFilterRecipe recipe) {
        try {
            buffer.writeResourceLocation(recipe.getItemOrTagID());
            buffer.writeInt(recipe.getAffinity().ordinal());
            buffer.writeFloat(recipe.getTotalGeneration());
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing eldrin fume recipe to packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public FumeFilterRecipe readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            FumeFilterRecipe recipe = new FumeFilterRecipe(recipeId);
            recipe.setItemOrTagID(buffer.readResourceLocation());
            recipe.setAffinity(Affinity.values()[buffer.readInt()]);
            recipe.setTotalGeneration(buffer.readFloat());
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading eldrin fume recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    protected FumeFilterRecipe readExtra(ResourceLocation recipeId, JsonObject json) {
        FumeFilterRecipe recipe = new FumeFilterRecipe(recipeId);
        recipe.parseJSON(json);
        return recipe;
    }
}

