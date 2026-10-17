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
import com.mna.items.sorcery.MagicStaff;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RandomSpellStaff
extends LootItemConditionalFunction {
    // [PORT 1.21.1] Campos OPCIONAIS com defaults (0f/0f/false) — o 1.20.1 usava defaults
    // quando o campo não estava no JSON; o codec com fieldOf obrigatório quebrava os loot_tables
    // que chamam mna:random_spell_staff sem esses campos.
    public static final com.mojang.serialization.MapCodec<RandomSpellStaff> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chanceForForceSelf", 0.0f).forGetter((RandomSpellStaff f) -> f.chanceForForceSelf),
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chanceForSkipTags", 0.0f).forGetter((RandomSpellStaff f) -> f.chanceForSkipTags),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("allowMessWithMori", false).forGetter((RandomSpellStaff f) -> f.allowMessWithMori)
        ).apply(inst, (a, b, c) -> new RandomSpellStaff(java.util.List.of(), a, b, c)));
    public static final LootItemFunctionType<RandomSpellStaff> TYPE = new LootItemFunctionType<RandomSpellStaff>(CODEC);

    private final float chanceForForceSelf;
    private final float chanceForSkipTags;
    private final boolean allowMessWithMori;

    protected RandomSpellStaff(java.util.List<LootItemCondition> conditionsIn, float chanceForForceSelf, float chanceForSkipTags, boolean allowMessWithMori) {
        super(conditionsIn);
        this.chanceForForceSelf = chanceForForceSelf;
        this.chanceForSkipTags = chanceForSkipTags;
        this.allowMessWithMori = allowMessWithMori;
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        return MagicStaff.buildRandomSpellStaff(this.chanceForForceSelf, this.chanceForSkipTags, this.allowMessWithMori, context.getLevel());
    }

    public LootItemFunctionType<RandomSpellStaff> getType() {
        return TYPE;
    }

}

