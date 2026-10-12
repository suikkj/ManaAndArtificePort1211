/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.progression;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.progression.ProgressionCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ProgressionConditionSerializer
extends AMRecipeBaseSerializer<ProgressionCondition> {
    @Override
    protected ProgressionCondition readExtra(ResourceLocation recipeId, JsonObject json) {
        ProgressionCondition recipe = new ProgressionCondition(recipeId);
        recipe.parseJSON(json);
        return recipe;
    }

    @Override
    protected ProgressionCondition readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            ProgressionCondition recipe = new ProgressionCondition(recipeId);
            recipe.setAdvancementID(buffer.readResourceLocation());
            recipe.setDescriptionID(buffer.readUtf());
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading progression condition from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buffer, ProgressionCondition recipe) {
        try {
            buffer.writeResourceLocation(recipe.getAdvancementID());
            buffer.writeUtf(recipe.getDescriptionID());
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing progression condition to packet.", (Throwable)e);
            throw e;
        }
    }
}

