/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.arcanefurnace;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.arcanefurnace.ArcaneFurnaceRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ArcaneFurnaceRecipeSerializer
extends AMRecipeBaseSerializer<ArcaneFurnaceRecipe> {
    @Override
    public ArcaneFurnaceRecipe readExtra(ResourceLocation recipeId, JsonObject json) {
        ArcaneFurnaceRecipe recipe = new ArcaneFurnaceRecipe(recipeId);
        recipe.parseJSON(json);
        return recipe;
    }

    @Override
    public ArcaneFurnaceRecipe readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            ArcaneFurnaceRecipe recipe = new ArcaneFurnaceRecipe(recipeId);
            recipe.setInputItem(buffer.readResourceLocation());
            recipe.setOutputItem(buffer.readResourceLocation());
            recipe.setBurnTime(buffer.readInt());
            recipe.setOutputQuantity(buffer.readInt());
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading arcane furnace recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public void writeExtra(FriendlyByteBuf buffer, ArcaneFurnaceRecipe recipe) {
        try {
            buffer.writeResourceLocation(recipe.getInputItem());
            buffer.writeResourceLocation(recipe.getOutputItem());
            buffer.writeInt(recipe.getBurnTime());
            buffer.writeInt(recipe.getOutputQuantity());
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing arcane furnace recipe to packet.", (Throwable)e);
            throw e;
        }
    }
}

