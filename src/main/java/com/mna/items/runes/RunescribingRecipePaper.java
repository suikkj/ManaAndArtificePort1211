 
package com.mna.items.runes;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.TieredItem;
import com.mna.recipes.runeforging.RunescribingRecipe;
import com.mna.recipes.runeforging.RunescribingRecipeSerializer;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class RunescribingRecipePaper
extends TieredItem {
    private static final String NBT_RECIPEID = "runescribe_recipe_id";

    public RunescribingRecipePaper() {
        super(new Item.Properties());
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        RunescribingRecipe recipe = this.getRecipe(stack, worldIn.level());
        if (recipe == null || recipe.getResultItem() == null || recipe.getResultItem().isEmpty()) {
            return;
        }
        tooltip.add(recipe.getResultItem().getHoverName());
    }

    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack((ItemLike)this);
        if (RunescribingRecipeSerializer.ALL_RECIPES.size() > 0) {
            RunescribingRecipePaper.setRecipe(stack, RunescribingRecipeSerializer.ALL_RECIPES.values().iterator().next());
        }
        return stack;
    }

    @Nullable
    public RunescribingRecipe getRecipe(ItemStack stack, Level world) {
        if (world == null || world.getRecipeManager() == null || !stack.has(DataComponents.CUSTOM_DATA) || stack.getItem() != this) {
            return null;
        }
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.contains(NBT_RECIPEID)) {
            return null;
        }
        ResourceLocation rLoc = ResourceLocation.parse(nbt.get(NBT_RECIPEID).getAsString());
        Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> optRecipe = world.getRecipeManager().byKey(rLoc);
        if (!optRecipe.isPresent() || !(optRecipe.get().value() instanceof RunescribingRecipe)) {
            return null;
        }
        return (RunescribingRecipe)optRecipe.get().value();
    }

    public static void setRecipe(ItemStack stack, RunescribingRecipe recipe) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        nbt.putString(NBT_RECIPEID, recipe.getId().toString());
    }
}

