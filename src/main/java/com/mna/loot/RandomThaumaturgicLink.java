/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.ItemStack
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
import com.mna.items.ritual.ThaumaturgicLink;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RandomThaumaturgicLink
extends LootItemConditionalFunction {
    public static final com.mojang.serialization.MapCodec<RandomThaumaturgicLink> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> LootItemConditionalFunction.commonFields(inst).apply(inst, RandomThaumaturgicLink::new));
    public static final LootItemFunctionType<RandomThaumaturgicLink> TYPE = new LootItemFunctionType<RandomThaumaturgicLink>(CODEC);

    protected RandomThaumaturgicLink(java.util.List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    public LootItemFunctionType<RandomThaumaturgicLink> getType() {
        return TYPE;
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        return ((ThaumaturgicLink)(ItemInit.THAUMATURGIC_LINK.get())).getRandomLink(context.getLevel());
    }

}

