/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.storage.loot.LootContext
 *  net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
 *  net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction$Serializer
 *  net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
 *  net.minecraft.world.level.storage.loot.functions.LootItemFunctions
 *  net.minecraft.world.level.storage.loot.predicates.LootItemCondition
 */
package com.mna.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mna.items.ItemInit;
import com.mna.items.runes.RunescribingRecipePaper;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.runeforging.RunescribingRecipe;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RandomRunescribeRecipe
extends LootItemConditionalFunction {
    public static final com.mojang.serialization.MapCodec<RandomRunescribeRecipe> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> LootItemConditionalFunction.commonFields(inst).apply(inst, RandomRunescribeRecipe::new));
    public static final LootItemFunctionType<RandomRunescribeRecipe> TYPE = new LootItemFunctionType<RandomRunescribeRecipe>(CODEC);

    protected RandomRunescribeRecipe(java.util.List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        ItemStack mwRecipe = new ItemStack((ItemLike)ItemInit.RECIPE_SCRAP_RUNESCRIBING.get());
        List<RunescribingRecipe> patterns = context.getLevel().getRecipeManager().getAllRecipesFor(RecipeInit.RUNESCRIBING_TYPE.get()).stream().map(r -> r.value()).collect(java.util.stream.Collectors.toList());
        if (patterns.size() == 0) {
            return ItemStack.EMPTY;
        }
        RunescribingRecipePaper.setRecipe(mwRecipe, (RunescribingRecipe)patterns.get((int)(Math.random() * (double)patterns.size())));
        return mwRecipe;
    }

    public LootItemFunctionType<RandomRunescribeRecipe> getType() {
        return TYPE;
    }

}

