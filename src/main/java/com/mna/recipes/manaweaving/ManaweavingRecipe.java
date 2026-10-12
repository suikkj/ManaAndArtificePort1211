 
package com.mna.recipes.manaweaving;
import net.minecraft.world.item.crafting.CraftingInput;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.api.recipes.IManaweavingRecipe;
import com.mna.recipes.ItemAndPatternCraftingInventory;
import com.mna.recipes.ItemAndPatternRecipe;
import com.mna.recipes.RecipeInit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class ManaweavingRecipe
extends ItemAndPatternRecipe
implements IManaweavingRecipe {
    private static final ResourceLocation NO_ENCHANT = ResourceLocation.fromNamespaceAndPath("mna", "none");
    private static Registry<Enchantment> __enchantRegistry;
    private ResourceLocation enchantment;
    private int enchantmentMagnitude;
    private boolean copyNBT = false;

    public ManaweavingRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject recipeJSON) {
        super.parseExtraJson(recipeJSON);
        if (recipeJSON.has("enchant")) {
            this.enchantment = ResourceLocation.parse(recipeJSON.get("enchant").getAsString());
            this.enchantmentMagnitude = recipeJSON.has("magnitude") ? recipeJSON.get("magnitude").getAsInt() : 1;
        }
        if (recipeJSON.has("copy_nbt")) {
            this.copyNBT = recipeJSON.get("copy_nbt").getAsBoolean();
        }
    }

    @Override
    public void runValidation() {
        super.runValidation();
        if (this.requiredItems.length > 9) {
            ManaAndArtifice.LOGGER.error("Manaweaving recipe can't have more than 9 items; this will be uncraftable! [" + this.getId().toString() + "]");
        }
        if (this.requiredPatterns.length > 6) {
            ManaAndArtifice.LOGGER.error("Manaweaving recipe can't have more than 6 patterns; this will be uncraftable! [" + this.getId().toString() + "]");
        }
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
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, net.minecraft.core.HolderLookup.Provider registries) {
        return this.getResultItem().copy();
    }

    public boolean matches(Map<net.minecraft.core.Holder<Enchantment>, Integer> runeEnchantment) {
        if (runeEnchantment.size() != 1 || this.enchantment == null) {
            return false;
        }
        for (net.minecraft.core.Holder<Enchantment> ench : runeEnchantment.keySet()) {
            if (!this.enchantment.equals(com.mna.api.enchantments.MNAEnchantmentRegistry.idOf(ench.value())) || this.enchantmentMagnitude != runeEnchantment.get(ench)) continue;
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        if ((this.__outputItem == null || this.__outputItem.isEmpty())) {
            Item outputItem = (Item)BuiltInRegistries.ITEM.get(this.output);
            if (outputItem == null) {
                return ItemStack.EMPTY;
            }
            this.__outputItem = new ItemStack((ItemLike)outputItem, this.outputQuantity);
            if (this.__outputNBT != null) {
                this.__outputItem.set(DataComponents.CUSTOM_DATA, CustomData.of(this.__outputNBT.copy()));
            }
            if (this.enchantment != null) {
                net.minecraft.core.Holder<Enchantment> resolvedHolder = com.mna.api.enchantments.MNAEnchantmentRegistry.holder(this.enchantment);
                if (resolvedHolder != null) {
                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    mutable.set(resolvedHolder, this.enchantmentMagnitude);
                    EnchantmentHelper.setEnchantments(this.__outputItem, mutable.toImmutable());
                }
            }
        }
        return this.__outputItem;
    }

    @Override
    public boolean isEnchantment() {
        return !this.getEnchantment().equals(NO_ENCHANT);
    }

    @Override
    public ResourceLocation getEnchantment() {
        return this.enchantment == null ? NO_ENCHANT : this.enchantment;
    }

    public void setEnchantment(ResourceLocation location) {
        this.enchantment = location;
    }

    @Override
    public int getEnchantmentMagnitude() {
        return this.enchantmentMagnitude;
    }

    public void setEnchantmentMagnitude(int magnitude) {
        this.enchantmentMagnitude = magnitude;
    }

    @Override
    public boolean getCopyNBT() {
        return this.copyNBT;
    }

    public void setCopyNBT(boolean copy) {
        this.copyNBT = copy;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.MANAWEAVING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.MANAWEAVING_RECIPE_TYPE.get();
    }

    @Override
    protected int maxItems() {
        return 9;
    }

    @Override
    protected int maxPatterns() {
        return 6;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        net.minecraft.core.Holder<Enchantment> enchHolder;
        ItemStack out = this.getResultItem();
        if (this.isEnchantment() && (enchHolder = com.mna.api.enchantments.MNAEnchantmentRegistry.holder(this.getEnchantment())) != null) {
            out.set(DataComponents.ITEM_NAME, Enchantment.getFullname(enchHolder, this.enchantmentMagnitude));
        }
        return out;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }
}


