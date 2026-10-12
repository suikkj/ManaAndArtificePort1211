/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.manaweaving;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.manaweaving.TransmutationRecipe;
import java.util.HashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TransmutationRecipeSerializer
extends AMRecipeBaseSerializer<TransmutationRecipe> {
    public static HashMap<ResourceLocation, TransmutationRecipe> ALL_RECIPES = new HashMap();

    @Override
    public TransmutationRecipe readExtra(ResourceLocation recipeId, JsonObject json) {
        TransmutationRecipe recipe = new TransmutationRecipe(recipeId);
        recipe.parseJSON(json);
        ALL_RECIPES.put(recipeId, recipe);
        return recipe;
    }

    @Override
    public TransmutationRecipe readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            TransmutationRecipe recipe = new TransmutationRecipe(recipeId);
            recipe.setTargetBlock(buffer.readResourceLocation());
            if (buffer.readBoolean()) {
                recipe.setReplaceBlock(buffer.readResourceLocation());
            }
            if (buffer.readBoolean()) {
                recipe.setLootTable(buffer.readResourceLocation());
            }
            if (buffer.readBoolean()) {
                recipe.setRepresentationItem(buffer.readResourceLocation());
            }
            ALL_RECIPES.put(recipeId, recipe);
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading transmutation recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public void writeExtra(FriendlyByteBuf buffer, TransmutationRecipe recipe) {
        try {
            buffer.writeResourceLocation(recipe.getTargetBlock());
            if (recipe.hasReplaceBlock()) {
                buffer.writeBoolean(true);
                buffer.writeResourceLocation(recipe.getReplaceBlock());
            } else {
                buffer.writeBoolean(false);
            }
            if (recipe.hasLootTable()) {
                buffer.writeBoolean(true);
                buffer.writeResourceLocation(recipe.getLootTable());
            } else {
                buffer.writeBoolean(false);
            }
            if (recipe.hasRepresentationItem()) {
                buffer.writeBoolean(true);
                buffer.writeResourceLocation(recipe.getRepresentationItem());
            } else {
                buffer.writeBoolean(false);
            }
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing transmutation recipe to packet.", (Throwable)e);
            throw e;
        }
    }
}

