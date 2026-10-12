 
package com.mna.recipes.manaweaving;

import net.minecraft.core.HolderLookup;
import com.google.gson.JsonObject;
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
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class TransmutationRecipe
extends AMRecipeBase {
    private ResourceLocation targetBlock;
    private ResourceLocation replaceBlock;
    private ResourceLocation lootTable;
    private ResourceLocation representationItem;

    public TransmutationRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject recipeJSON) {
        if (recipeJSON.has("targetBlock")) {
            this.targetBlock = ResourceLocation.parse(recipeJSON.get("targetBlock").getAsString());
        }
        if (recipeJSON.has("replaceBlock")) {
            this.replaceBlock = ResourceLocation.parse(recipeJSON.get("replaceBlock").getAsString());
        }
        if (recipeJSON.has("lootTable")) {
            this.lootTable = ResourceLocation.parse(recipeJSON.get("lootTable").getAsString());
        }
        if (recipeJSON.has("representationItem")) {
            this.representationItem = ResourceLocation.parse(recipeJSON.get("representationItem").getAsString());
        }
    }

    @Override
    public void runValidation() {
        super.runValidation();
        if (this.targetBlock == null) {
            throw new RuntimeException("Transmutation recipe must have targetBlock directive.");
        }
        if (this.replaceBlock == null && this.lootTable == null) {
            throw new RuntimeException("Transmutation recipe must have either replaceBlock or lootTable.");
        }
        if (this.lootTable != null && this.representationItem == null) {
            throw new RuntimeException("Transmutation recipe using lootTable must specify representationItem (for gui purposes).");
        }
    }

    public boolean hasLootTable() {
        return this.lootTable != null;
    }

    public boolean hasReplaceBlock() {
        return this.replaceBlock != null;
    }

    public boolean hasRepresentationItem() {
        return this.representationItem != null;
    }

    public ResourceLocation getTargetBlock() {
        return this.targetBlock;
    }

    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    public ResourceLocation getReplaceBlock() {
        return this.replaceBlock;
    }

    public ResourceLocation getRepresentationItem() {
        return this.representationItem;
    }

    public void setTargetBlock(ResourceLocation targetBlock) {
        this.targetBlock = targetBlock;
    }

    public void setReplaceBlock(ResourceLocation replaceBlock) {
        this.replaceBlock = replaceBlock;
    }

    public void setLootTable(ResourceLocation lootTable) {
        this.lootTable = lootTable;
    }

    public void setRepresentationItem(ResourceLocation representationItem) {
        this.representationItem = representationItem;
    }

    public boolean matches(CraftingContainer inv, Level worldIn) {
        return false;
    }

    public ItemStack assemble(CraftingContainer p_44001_, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        Block block;
        if (this.representationItem != null) {
            Item item = (Item)BuiltInRegistries.ITEM.get(this.representationItem);
            if (item != null) {
                return new ItemStack((ItemLike)item);
            }
        } else if (this.replaceBlock != null && (block = (Block)BuiltInRegistries.BLOCK.get(this.replaceBlock)) != null) {
            return new ItemStack((ItemLike)block);
        }
        return new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND.get());
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.TRANSMUTATION_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.TRANSMUTATION_TYPE.get();
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


