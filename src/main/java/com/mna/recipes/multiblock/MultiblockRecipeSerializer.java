/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.recipes.multiblock;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBaseSerializer;
import com.mna.recipes.multiblock.MultiblockDefinition;
import java.util.HashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class MultiblockRecipeSerializer
extends AMRecipeBaseSerializer<MultiblockDefinition> {
    @Override
    protected MultiblockDefinition readExtra(ResourceLocation recipeId, JsonObject json) {
        MultiblockDefinition recipe = new MultiblockDefinition(recipeId);
        recipe.parseJSON(json);
        return recipe;
    }

    @Override
    protected MultiblockDefinition readExtra(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            MultiblockDefinition recipe = new MultiblockDefinition(recipeId);
            recipe.setStructurePath(buffer.readResourceLocation());
            int count = buffer.readInt();
            HashMap<ResourceLocation, Integer> matchersByBlock = new HashMap<ResourceLocation, Integer>();
            for (int i = 0; i < count; ++i) {
                ResourceLocation rLoc = buffer.readResourceLocation();
                int index = buffer.readInt();
                matchersByBlock.put(rLoc, index);
            }
            recipe.setSpecialBlockMatchersByBlock(matchersByBlock);
            count = buffer.readInt();
            HashMap<Long, Integer> matchersByOffset = new HashMap<Long, Integer>();
            for (int i = 0; i < count; ++i) {
                Long offset = buffer.readLong();
                int index = buffer.readInt();
                matchersByOffset.put(offset, index);
            }
            recipe.setSpecialBlockMatchersByOffset(matchersByOffset);
            recipe.deserializeVariations(buffer.readNbt());
            return recipe;
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Error reading multiblock definition from packet.", (Throwable)e);
            throw e;
        }
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buffer, MultiblockDefinition recipe) {
        try {
            buffer.writeResourceLocation(recipe.getStructurePath());
            HashMap<ResourceLocation, Integer> matchersByBlock = recipe.getSpecialBlockMatchersByBlock();
            buffer.writeInt(matchersByBlock.size());
            matchersByBlock.entrySet().forEach(e -> {
                buffer.writeResourceLocation((ResourceLocation)e.getKey());
                buffer.writeInt(((Integer)e.getValue()).intValue());
            });
            HashMap<Long, Integer> matchersByOffset = recipe.getSpecialBlockMatchersByOffset();
            buffer.writeInt(matchersByOffset.size());
            matchersByOffset.entrySet().forEach(e -> {
                buffer.writeLong(((Long)e.getKey()).longValue());
                buffer.writeInt(((Integer)e.getValue()).intValue());
            });
            buffer.writeNbt(recipe.serializeVariations());
        }
        catch (Exception e2) {
            ManaAndArtifice.LOGGER.error("Error writing multiblock definition to packet.", (Throwable)e2);
            throw e2;
        }
    }
}

