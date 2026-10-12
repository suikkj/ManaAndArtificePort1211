 
package com.mna.items.manaweaving;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.TieredItem;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternSerializer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class ManaweavingPatternPaper
extends TieredItem {
    private static final String NBT_RECIPEID = "manaweave_pattern_id";

    public ManaweavingPatternPaper() {
        super(new Item.Properties());
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ResourceLocation recipe = this.getRecipe(stack, worldIn);
        if (recipe == null) {
            return;
        }
        tooltip.add((Component)Component.translatable((String)recipe.toString()));
    }

    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack((ItemLike)this);
        if (ManaweavingPatternSerializer.ALL_RECIPES.size() > 0) {
            ManaweavingPatternPaper.setRecipe(stack, ManaweavingPatternSerializer.ALL_RECIPES.values().iterator().next());
        }
        return stack;
    }

    @Nullable
    public ResourceLocation getRecipe(ItemStack stack, Level world) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || stack.getItem() != this) {
            return null;
        }
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.contains(NBT_RECIPEID)) {
            return null;
        }
        ResourceLocation rLoc = ResourceLocation.parse(nbt.get(NBT_RECIPEID).getAsString());
        return rLoc;
    }

    public static void setRecipe(ItemStack stack, ManaweavingPattern recipe) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        nbt.putString(NBT_RECIPEID, recipe.getId().toString());
    }
}

