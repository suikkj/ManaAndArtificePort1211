/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.ItemAndPatternRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public abstract class ItemAndPatternRecipeSerializer<T extends ItemAndPatternRecipe>
extends AMRecipeBaseSerializer<T> {
    @Override
    public T readExtra(ResourceLocation recipeId, JsonObject json) {
        T recipe = this.instantiate(recipeId);
        ((AMRecipeBase)recipe).parseJSON(json);
        return recipe;
    }

    @Override
    public T readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            T recipe = this.instantiate(recipeId);
            ((ItemAndPatternRecipe)recipe).setOutput(buffer.readResourceLocation());
            ((ItemAndPatternRecipe)recipe).outputQuantity = Math.max(buffer.readInt(), 1);
            int numItems = buffer.readInt();
            ResourceLocation[] requiredItems = new ResourceLocation[numItems];
            for (int i = 0; i < numItems; ++i) {
                requiredItems[i] = buffer.readResourceLocation();
            }
            ((ItemAndPatternRecipe)recipe).setRequiredItems(requiredItems);
            int numPatterns = buffer.readInt();
            ResourceLocation[] requiredPatterns = new ResourceLocation[numPatterns];
            for (int i = 0; i < numPatterns; ++i) {
                requiredPatterns[i] = buffer.readResourceLocation();
            }
            ((ItemAndPatternRecipe)recipe).setRequiredPatterns(requiredPatterns);
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading item and pattern recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public void writeExtra(FriendlyByteBuf buffer, ItemAndPatternRecipe recipe) {
        try {
            int i;
            buffer.writeResourceLocation(recipe.getOutput());
            buffer.writeInt(recipe.outputQuantity);
            ResourceLocation[] items = recipe.getRequiredItems();
            ResourceLocation[] patterns = recipe.getRequiredPatterns();
            buffer.writeInt(items.length);
            for (i = 0; i < items.length; ++i) {
                buffer.writeResourceLocation(items[i]);
            }
            buffer.writeInt(patterns.length);
            for (i = 0; i < patterns.length; ++i) {
                buffer.writeResourceLocation(patterns[i]);
            }
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing item and pattern recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    protected abstract T instantiate(ResourceLocation var1);
}

