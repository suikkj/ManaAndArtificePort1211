 
package com.mna.recipes.runeforging;
import net.minecraft.world.item.crafting.CraftingInput;

import net.minecraft.core.HolderLookup;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IRunescribeRecipe;
import com.mna.items.ItemInit;
import com.mna.items.runes.RunePattern;
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

public class RunescribingRecipe
extends AMRecipeBase
implements IRunescribeRecipe {
    private long hMutex;
    private long vMutex;
    private ResourceLocation patternItem = ResourceLocation.parse("");
    private Item __outputItem = null;

    public RunescribingRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject recipeJSON) {
        this.hMutex = recipeJSON.get("mutex_h").getAsLong();
        this.vMutex = recipeJSON.get("mutex_v").getAsLong();
        this.patternItem = ResourceLocation.parse(recipeJSON.get("output").getAsString());
    }

    public boolean matches(net.minecraft.world.item.crafting.CraftingInput inv, Level worldIn) {
        ItemStack checkStack = inv.getItem(0);
        if (inv.size() == 1 && checkStack.getItem() == ItemInit.RUNE_PATTERN.get()) {
            return ((RunePattern)ItemInit.RUNE_PATTERN.get()).getHMutex(checkStack) == this.hMutex && ((RunePattern)ItemInit.RUNE_PATTERN.get()).getVMutex(checkStack) == this.vMutex;
        }
        return false;
    }

    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput inv, HolderLookup.Provider access) {
        return this.getResultItem();
    }

    @Override
    public ItemStack getResultItem() {
        if (this.__outputItem == null) {
            this.__outputItem = (Item)BuiltInRegistries.ITEM.get(this.patternItem);
        }
        if (this.__outputItem != null) {
            return new ItemStack((ItemLike)this.__outputItem);
        }
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.RUNESCRIBING_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.RUNESCRIBING_TYPE.get();
    }

    @Override
    public long getHMutex() {
        return this.hMutex;
    }

    public void setHMutex(long mutex) {
        this.hMutex = mutex;
    }

    @Override
    public long getVMutex() {
        return this.vMutex;
    }

    public void setVMutex(long mutex) {
        this.vMutex = mutex;
    }

    public ResourceLocation getOutputResource() {
        return this.patternItem;
    }

    public void setOutputResource(String output) {
        this.patternItem = ResourceLocation.parse(output);
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


