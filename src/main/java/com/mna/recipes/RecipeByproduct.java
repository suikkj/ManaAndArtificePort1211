 
package com.mna.recipes;

import com.google.gson.JsonObject;
import com.mna.api.tools.MATags;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class RecipeByproduct {
    private final ResourceLocation tag;
    private final float chance;
    private final int rolls;
    private List<ItemStack> itemsCached;

    @Nullable
    public static RecipeByproduct FromJSON(JsonObject json) {
        Item item;
        float chance = 1.0f;
        int rolls = 1;
        ItemStack stack = ItemStack.EMPTY;
        ResourceLocation tag = null;
        if (json.has("chance")) {
            chance = json.get("chance").getAsFloat();
        }
        if (json.has("rolls")) {
            rolls = json.get("rolls").getAsInt();
        }
        if (json.has("item") && (item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString()))) != null) {
            stack = new ItemStack((ItemLike)item);
        }
        if (json.has("tag")) {
            tag = ResourceLocation.parse(json.get("tag").getAsString());
        }
        if (tag != null) {
            return new RecipeByproduct(tag, chance, rolls);
        }
        if (!stack.isEmpty()) {
            return new RecipeByproduct(stack, chance, rolls);
        }
        return null;
    }

    private RecipeByproduct(ItemStack stack, float chance, int rolls) {
        this.itemsCached = Arrays.asList(stack);
        this.tag = null;
        this.chance = chance;
        this.rolls = rolls;
    }

    private RecipeByproduct(ResourceLocation tag, float chance, int rolls) {
        this.tag = tag;
        this.chance = chance;
        this.rolls = rolls;
    }

    public List<ItemStack> getItems() {
        if (this.itemsCached == null) {
            this.itemsCached = this.tag != null ? MATags.getItemTagContents(this.tag).stream().map(i -> new ItemStack((ItemLike)i)).toList() : Arrays.asList(ItemStack.EMPTY);
        }
        return this.itemsCached;
    }

    public float getChance() {
        return this.chance;
    }

    public int getRolls() {
        return this.rolls;
    }
}


