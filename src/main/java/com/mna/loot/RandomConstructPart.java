/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.ItemStack
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
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.items.ItemInit;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RandomConstructPart
extends LootItemConditionalFunction {
    public static final com.mojang.serialization.MapCodec<RandomConstructPart> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> LootItemConditionalFunction.commonFields(inst).apply(inst, RandomConstructPart::new));
    public static final LootItemFunctionType<RandomConstructPart> TYPE = new LootItemFunctionType<RandomConstructPart>(CODEC);

    protected RandomConstructPart(java.util.List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        List parts = ItemInit.ITEMS.getEntries().stream().filter(i -> i.isBound() && i.get() instanceof ItemConstructPart && ((ItemConstructPart)i.get()).canBeInLoot(context)).map(i -> (ItemConstructPart)i.get()).collect(Collectors.toList());
        if (parts.size() == 0) {
            return ItemStack.EMPTY;
        }
        ItemConstructPart selected = (ItemConstructPart)parts.get((int)(Math.random() * (double)parts.size()));
        return new ItemStack((ItemLike)selected);
    }

    public LootItemFunctionType<RandomConstructPart> getType() {
        return TYPE;
    }

}

