/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package com.mna.recipes.eldrin;
import net.minecraft.world.item.crafting.CraftingInput;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mna.api.affinity.Affinity;
import com.mna.recipes.ItemAndPatternCraftingInventory;
import com.mna.recipes.ItemAndPatternRecipe;
import com.mna.recipes.RecipeInit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class EldrinAltarRecipe
extends ItemAndPatternRecipe {
    private HashMap<Affinity, Float> powerRequirements;
    private int[] colors = new int[]{0, 0};
    public static final int MAX_ITEMS = 9;

    public EldrinAltarRecipe(ResourceLocation idIn) {
        super(idIn);
        this.powerRequirements = new HashMap();
    }

    @Override
    public void parseExtraJson(JsonObject recipeJSON) {
        JsonElement colorElem;
        super.parseExtraJson(recipeJSON);
        JsonElement power_reqs = recipeJSON.get("power_requirements");
        if (power_reqs != null && power_reqs.isJsonArray()) {
            JsonArray reqsArray = power_reqs.getAsJsonArray();
            reqsArray.forEach(e -> {
                if (!e.isJsonObject()) {
                    return;
                }
                JsonObject elem = (JsonObject)e;
                if (elem.has("affinity") && elem.has("amount")) {
                    String affinity = elem.get("affinity").getAsString();
                    float amount = elem.get("amount").getAsFloat();
                    Affinity parsedAffinity = Affinity.UNKNOWN;
                    try {
                        parsedAffinity = Affinity.valueOf(affinity);
                    }
                    catch (Exception ex) {
                        return;
                    }
                    this.powerRequirements.put(parsedAffinity, Float.valueOf(amount));
                }
            });
        }
        if (recipeJSON.has("colors") && (colorElem = recipeJSON.get("colors")).isJsonArray()) {
            JsonArray colors = colorElem.getAsJsonArray();
            for (int i = 0; i < this.colors.length; ++i) {
                if (colors.size() < i || !(colorElem = colors.get(i)).isJsonPrimitive()) continue;
                this.colors[i] = colorElem.getAsInt();
            }
        }
    }

    @Override
    protected int maxItems() {
        return 9;
    }

    @Override
    protected int maxPatterns() {
        return 0;
    }

    public int getColorOne() {
        return this.colors[0];
    }

    public int getColorTwo() {
        return this.colors[1];
    }

    public HashMap<Affinity, Float> getPowerRequirements() {
        return this.powerRequirements;
    }

    public void setPowerRequirements(HashMap<Affinity, Float> map) {
        this.powerRequirements = map;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.ELDRIN_ALTAR_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.ELDRIN_ALTAR_TYPE.get();
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return this.getResultItem();
    }

    @Override
    public boolean matches(net.minecraft.world.item.crafting.CraftingInput inv, Level worldIn) {
        if (!((Object)inv instanceof ItemAndPatternCraftingInventory)) {
            return false;
        }
        ItemAndPatternCraftingInventory mwInv = (ItemAndPatternCraftingInventory)(Object)inv;
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ArrayList<String> patterns = new ArrayList<String>((Collection<String>)mwInv.getPatterns());
        for (int i = 0; i < 9; ++i) {
            if (inv.getItem(i).isEmpty()) continue;
            items.add(inv.getItem(i));
        }
        return this.itemsMatchShapeless(items) && this.patternsMatchShaped(patterns);
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }
}

