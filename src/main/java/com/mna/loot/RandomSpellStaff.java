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
    public static final com.mojang.serialization.MapCodec<RandomSpellStaff> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> LootItemConditionalFunction.commonFields(inst).and(inst.group(
            com.mojang.serialization.Codec.FLOAT.fieldOf("chanceForForceSelf").forGetter((RandomSpellStaff f) -> f.chanceForForceSelf),
        com.mojang.serialization.Codec.FLOAT.fieldOf("chanceForSkipTags").forGetter((RandomSpellStaff f) -> f.chanceForSkipTags),
        com.mojang.serialization.Codec.BOOL.fieldOf("allowMessWithMori").forGetter((RandomSpellStaff f) -> f.allowMessWithMori)
    ).apply(inst, (conditions, a, b, c) -> new RandomSpellStaff(conditions, a, b, c))));
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

