 
package com.mna.recipes.eldrin;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.affinity.Affinity;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.eldrin.EldrinAltarRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class EldrinAltarRecipeSerializer
extends AMRecipeBaseSerializer<EldrinAltarRecipe> {
    @Override
    public void writeExtra(FriendlyByteBuf buffer, EldrinAltarRecipe recipe) {
        try {
            int i;
            buffer.writeResourceLocation(recipe.getOutput());
            buffer.writeInt(recipe.getOutputQuantity());
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
            buffer.writeInt(recipe.getPowerRequirements().size());
            recipe.getPowerRequirements().entrySet().forEach(r -> {
                buffer.writeInt(((Affinity)((r.getKey()))).ordinal());
                buffer.writeFloat(((Float)r.getValue()).floatValue());
            });
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error writing eldrin altar recipe to packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public EldrinAltarRecipe readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            EldrinAltarRecipe recipe = new EldrinAltarRecipe(recipeId);
            recipe.setOutput(buffer.readResourceLocation());
            recipe.setOutputQuantity(buffer.readInt());
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
            int num_power_requirements = buffer.readInt();
            for (int i = 0; i < num_power_requirements; ++i) {
                int ordinal = buffer.readInt();
                float power = buffer.readFloat();
                recipe.getPowerRequirements().put(Affinity.values()[ordinal], Float.valueOf(power));
            }
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading eldrin altar recipe from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    protected EldrinAltarRecipe readExtra(ResourceLocation recipeId, JsonObject json) {
        EldrinAltarRecipe recipe = new EldrinAltarRecipe(recipeId);
        recipe.parseJSON(json);
        return recipe;
    }
}

