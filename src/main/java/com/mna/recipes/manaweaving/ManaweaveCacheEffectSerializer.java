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
import com.mna.recipes.manaweaving.ManaweaveCacheEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ManaweaveCacheEffectSerializer
extends AMRecipeBaseSerializer<ManaweaveCacheEffect> {
    @Override
    public ManaweaveCacheEffect readExtra(ResourceLocation recipeId, JsonObject json) {
        ManaweaveCacheEffect recipe = new ManaweaveCacheEffect(recipeId);
        recipe.parseJSON(json);
        return recipe;
    }

    @Override
    public ManaweaveCacheEffect readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            ManaweaveCacheEffect recipe = new ManaweaveCacheEffect(recipeId);
            recipe.setEffect(buffer.readResourceLocation());
            recipe.setDurationMin(buffer.readInt());
            recipe.setDurationMax(buffer.readInt());
            recipe.setMagnitude(buffer.readInt());
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading manaweave cache effect recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public void writeExtra(FriendlyByteBuf buffer, ManaweaveCacheEffect recipe) {
        try {
            buffer.writeResourceLocation(recipe.getEffect());
            buffer.writeInt(recipe.getDurationMin());
            buffer.writeInt(recipe.getDurationMax());
            buffer.writeInt(recipe.getMagnitude());
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing manaweave cache effect recipe to packet.", (Throwable)e);
            throw e;
        }
    }
}

