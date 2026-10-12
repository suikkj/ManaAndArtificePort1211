 
package com.mna.recipes.crush;
import net.minecraft.world.item.crafting.CraftingInput;

import net.minecraft.core.HolderLookup;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IRuneforgeRecipe;
import com.mna.api.tools.MATags;
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

public class CrushingRecipe
extends AMRecipeBase
implements IRuneforgeRecipe {
    private ResourceLocation inputItem = ResourceLocation.parse("");
    private ResourceLocation outputItem = ResourceLocation.parse("");
    private Item __outputItem = null;
    private int outputQuantity = 1;

    public CrushingRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject recipeJSON) {
        this.inputItem = ResourceLocation.parse(recipeJSON.get("input").getAsString());
        this.outputItem = ResourceLocation.parse(recipeJSON.get("output").getAsString());
        if (recipeJSON.has("output_quantity")) {
            this.outputQuantity = recipeJSON.get("output_quantity").getAsInt();
        }
    }

    public boolean matches(net.minecraft.world.item.crafting.CraftingInput inv, Level worldIn) {
        ItemStack inputStack;
        return inv.size() == 1 && MATags.isItemEqual(inputStack = inv.getItem(0), this.inputItem);
    }

    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput inv, HolderLookup.Provider access) {
        return this.getResultItem();
    }

    @Override
    public ItemStack getResultItem() {
        if (this.__outputItem == null) {
            this.__outputItem = (Item)BuiltInRegistries.ITEM.get(this.outputItem);
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
        return (RecipeSerializer)RecipeInit.CRUSHING_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.CRUSHING_TYPE.get();
    }

    public int getOutputQuantity() {
        return this.outputQuantity;
    }

    public ResourceLocation getInputResource() {
        return this.inputItem;
    }

    public void setInputResource(ResourceLocation pattern) {
        this.inputItem = pattern;
    }

    public ResourceLocation getOutputResource() {
        return this.outputItem;
    }

    public void setOutputResource(ResourceLocation output) {
        this.outputItem = output;
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


