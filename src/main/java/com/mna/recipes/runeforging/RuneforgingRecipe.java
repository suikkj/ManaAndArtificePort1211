 
package com.mna.recipes.runeforging;

import net.minecraft.core.HolderLookup;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IRuneforgeRecipe;
import com.mna.items.ItemInit;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeInit;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class RuneforgingRecipe
extends AMRecipeBase
implements IRuneforgeRecipe {
    private ResourceLocation patternItem = ResourceLocation.parse("");
    private ResourceLocation runeItem = ResourceLocation.parse("");
    private ResourceLocation materialItem;
    private Item __outputItem = null;
    private int hits = 10;
    private int outputQuantity = 1;

    public RuneforgingRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject recipeJSON) {
        this.patternItem = ResourceLocation.parse(recipeJSON.get("pattern").getAsString());
        this.runeItem = ResourceLocation.parse(recipeJSON.get("output").getAsString());
        if (recipeJSON.has("material")) {
            this.materialItem = ResourceLocation.parse(recipeJSON.get("material").getAsString());
        }
        if (recipeJSON.has("hits")) {
            this.hits = recipeJSON.get("hits").getAsInt();
        }
        if (recipeJSON.has("output_quantity")) {
            this.outputQuantity = recipeJSON.get("output_quantity").getAsInt();
        }
    }

    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack patternStack = inv.getItem(0);
        ItemStack materialStack = inv.getItem(1);
        return inv.getContainerSize() == 2 && BuiltInRegistries.ITEM.getKey(patternStack.getItem()).compareTo(this.patternItem) == 0 && (this.materialItem == null ? materialStack.getItem() == ItemInit.VINTEUM_INGOT_SUPERHEATED.get() : BuiltInRegistries.ITEM.getKey(materialStack.getItem()).compareTo(this.materialItem) == 0);
    }

    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider access) {
        return this.getResultItem();
    }

    @Override
    public ItemStack getResultItem() {
        if (this.__outputItem == null) {
            this.__outputItem = (Item)BuiltInRegistries.ITEM.get(this.runeItem);
        }
        if (this.__outputItem != null) {
            return new ItemStack((ItemLike)this.__outputItem, this.outputQuantity);
        }
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.RUNEFORGING_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.RUNEFORGING_TYPE.get();
    }

    public int getHits() {
        return this.hits;
    }

    public int getOutputQuantity() {
        return this.outputQuantity;
    }

    public ResourceLocation getMaterial() {
        return this.materialItem == null ? ItemInit.VINTEUM_INGOT_SUPERHEATED.getId() : this.materialItem;
    }

    public ResourceLocation getPatternResource() {
        return this.patternItem;
    }

    public void setPatternResource(ResourceLocation pattern) {
        this.patternItem = pattern;
    }

    public ResourceLocation getOutputResource() {
        return this.runeItem;
    }

    public void setOutputResource(ResourceLocation output) {
        this.runeItem = output;
    }

    public void setMaterial(ResourceLocation material) {
        this.materialItem = material;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public void setOutputQuantity(int outputQuantity) {
        this.outputQuantity = outputQuantity;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return this.getResultItem();
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }
}


