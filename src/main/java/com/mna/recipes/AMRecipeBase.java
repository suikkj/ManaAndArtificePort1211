/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CustomRecipe
 */
package com.mna.recipes;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mna.Registries;
import com.mna.api.faction.IFaction;
import com.mna.api.recipes.IMARecipe;
import com.mna.recipes.RecipeByproduct;
import java.util.ArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;

public abstract class AMRecipeBase
extends CustomRecipe
implements IMARecipe {
    protected int tier = 1;
    protected IFaction requiredFaction = null;
    protected ImmutableList<RecipeByproduct> byproducts = ImmutableList.of();
    protected ResourceLocation recipeId = null;

    public AMRecipeBase(ResourceLocation idIn) {
        super(CraftingBookCategory.MISC);
        this.recipeId = idIn;
    }

    public ResourceLocation getId() {
        return this.recipeId;
    }

    public final void parseJSON(JsonObject object) {
        this.parseExtraJson(object);
        if (object.has("tier")) {
            this.tier = object.get("tier").getAsInt();
        }
        if (object.has("requiredFaction")) {
            ResourceLocation factionRLoc = ResourceLocation.parse(object.get("requiredFaction").getAsString());
            this.setRequiredFaction(factionRLoc);
        }
        if (object.has("byproducts")) {
            JsonArray byproductArray = object.get("byproducts").getAsJsonArray();
            ArrayList parsedByproducts = new ArrayList();
            byproductArray.forEach(elem -> {
                RecipeByproduct byproduct;
                if (elem instanceof JsonObject && (byproduct = RecipeByproduct.FromJSON((JsonObject)elem)) != null) {
                    parsedByproducts.add(byproduct);
                }
            });
            this.byproducts = ImmutableList.copyOf(parsedByproducts);
        }
        this.runValidation();
    }

    public final ArrayList<ItemStack> rollByproducts(RandomSource randomSource) {
        return this.rollByproducts(randomSource, 1);
    }

    public final ArrayList<ItemStack> rollByproducts(RandomSource randomSource, int stackSize) {
        ArrayList<ItemStack> byproductRolls = new ArrayList<ItemStack>();
        this.byproducts.forEach(bp -> {
            for (int i = 0; i < bp.getRolls() * stackSize; ++i) {
                if (!(randomSource.nextFloat() <= bp.getChance())) continue;
                int numItems = bp.getItems().size();
                int idx = randomSource.nextInt(numItems);
                ItemStack selected = bp.getItems().get(idx);
                byproductRolls.add(selected.copy());
            }
        });
        return byproductRolls;
    }

    public final ImmutableList<RecipeByproduct> getByproducts() {
        return this.byproducts;
    }

    protected abstract void parseExtraJson(JsonObject var1);

    @Override
    public final int getTier() {
        return this.tier;
    }

    @Override
    public abstract ItemStack getGuiRepresentationStack();

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
        return this.getResultItem();
    }

    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public IFaction getFactionRequirement() {
        return this.requiredFaction;
    }

    public void setRequiredFaction(ResourceLocation factionID) {
        this.requiredFaction = (IFaction)Registries.Factions.getValue(factionID);
    }

    protected void runValidation() {
    }
}

