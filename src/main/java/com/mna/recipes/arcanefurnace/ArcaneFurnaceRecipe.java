 
package com.mna.recipes.arcanefurnace;

import net.minecraft.core.HolderLookup;
import com.google.gson.JsonObject;
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

public class ArcaneFurnaceRecipe
extends AMRecipeBase {
    private ResourceLocation inputItem;
    private ResourceLocation outputItem;
    private int burnTime;
    private int outputQuantity = 1;
    private Item __outputItem = null;

    public ArcaneFurnaceRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject object) {
        if (object.has("input") && object.has("output") && object.has("burnTime")) {
            this.inputItem = ResourceLocation.parse(object.get("input").getAsString());
            this.outputItem = ResourceLocation.parse(object.get("output").getAsString());
            this.burnTime = object.get("burnTime").getAsInt();
        }
        if (object.has("outputQuantity")) {
            this.outputQuantity = object.get("outputQuantity").getAsInt();
        }
    }

    public void setInputItem(ResourceLocation rLoc) {
        this.inputItem = rLoc;
    }

    public ResourceLocation getInputItem() {
        return this.inputItem;
    }

    public void setOutputItem(ResourceLocation rLoc) {
        this.outputItem = rLoc;
    }

    public ResourceLocation getOutputItem() {
        return this.outputItem;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    public int getOutputQuantity() {
        return this.outputQuantity;
    }

    public void setOutputQuantity(int quantity) {
        this.outputQuantity = quantity;
    }

    public boolean matches(CraftingContainer inv, Level worldIn) {
        if (inv.getContainerSize() != 1) {
            return false;
        }
        ItemStack stack = inv.getItem(0);
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(this.inputItem);
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

    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider access) {
        return this.getResultItem(access);
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.ARCANE_FURNACE_RECIPE_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.ARCANE_FURNACE_TYPE.get();
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


