 
package com.mna.recipes.manaweaving;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.manaweaving.ManaweavingRecipe;
import java.util.HashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ManaweavingRecipeSerializer
extends AMRecipeBaseSerializer<ManaweavingRecipe> {
    public static HashMap<ResourceLocation, ManaweavingRecipe> ALL_RECIPES = new HashMap();

    @Override
    public ManaweavingRecipe readExtra(ResourceLocation recipeId, JsonObject json) {
        ManaweavingRecipe recipe = new ManaweavingRecipe(recipeId);
        recipe.parseJSON(json);
        ALL_RECIPES.put(recipeId, recipe);
        return recipe;
    }

    @Override
    public ManaweavingRecipe readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            ManaweavingRecipe recipe = new ManaweavingRecipe(recipeId);
            recipe.setOutput(buffer.readResourceLocation());
            recipe.setOutputQuantity(buffer.readInt());
            recipe.setEnchantment(buffer.readResourceLocation());
            recipe.setEnchantmentMagnitude(buffer.readInt());
            recipe.setCopyNBT(buffer.readBoolean());
            if (buffer.readBoolean()) {
                recipe.setRequiredFaction(buffer.readResourceLocation());
            }
            int numItems = buffer.readInt();
            ResourceLocation[] requiredItems = new ResourceLocation[numItems];
            for (int i = 0; i < numItems; ++i) {
                requiredItems[i] = buffer.readResourceLocation();
            }
            recipe.setRequiredItems(requiredItems);
            int numPatterns = buffer.readInt();
            ResourceLocation[] requiredPatterns = new ResourceLocation[numPatterns];
            for (int i = 0; i < numPatterns; ++i) {
                requiredPatterns[i] = buffer.readResourceLocation();
            }
            recipe.setRequiredPatterns(requiredPatterns);
            ALL_RECIPES.put(recipeId, recipe);
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading manaweaving recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public void writeExtra(FriendlyByteBuf buffer, ManaweavingRecipe recipe) {
        try {
            int i;
            buffer.writeResourceLocation(recipe.getOutput());
            buffer.writeInt(recipe.getOutputQuantity());
            buffer.writeResourceLocation(recipe.getEnchantment());
            buffer.writeInt(recipe.getEnchantmentMagnitude());
            buffer.writeBoolean(recipe.getCopyNBT());
            if (recipe.getFactionRequirement() != null) {
                buffer.writeBoolean(true);
                buffer.writeResourceLocation(Registries.Factions.getKey(recipe.getFactionRequirement()));
            } else {
                buffer.writeBoolean(false);
            }
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
            ManaAndArtifice.LOGGER.error("Error writing manaweaving recipe to packet.", (Throwable)e);
            throw e;
        }
    }
}

