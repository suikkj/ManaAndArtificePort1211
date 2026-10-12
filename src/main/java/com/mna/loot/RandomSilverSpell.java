 
package com.mna.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mna.Registries;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.ItemTornJournalPage;
import java.util.ArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RandomSilverSpell
extends LootItemConditionalFunction {
    public static final com.mojang.serialization.MapCodec<RandomSilverSpell> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
        LootItemConditionalFunction.commonFields(inst),
        ResourceLocation.CODEC.optionalFieldOf("faction").forGetter(f -> java.util.Optional.ofNullable(f.factionID))
    ).apply(inst, (conditions, faction) -> new RandomSilverSpell(conditions, faction.orElse(null), true)));
    public static final LootItemFunctionType<RandomSilverSpell> TYPE = new LootItemFunctionType<RandomSilverSpell>(CODEC);

    ResourceLocation factionID;

    protected RandomSilverSpell(java.util.List<LootItemCondition> conditionsIn, ResourceLocation factionID, boolean thesis) {
        super(conditionsIn);
        this.factionID = factionID;
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        ArrayList candidates = new ArrayList();
        SpellCraftingContext scc = new SpellCraftingContext(null);
        Registries.Shape.forEach(s -> {
            if (!s.isCraftable(scc)) {
                return;
            }
            if (s.isSilverSpell() && (this.factionID == null || s.getFactionRequirement() == null || s.getFactionRequirement().is(this.factionID))) {
                candidates.add(s);
            }
        });
        Registries.SpellEffect.forEach(s -> {
            if (!s.isCraftable(scc)) {
                return;
            }
            if (s.isSilverSpell() && (this.factionID == null || s.getFactionRequirement() == null || s.getFactionRequirement().is(this.factionID))) {
                candidates.add(s);
            }
        });
        Registries.Modifier.forEach(s -> {
            if (!s.isCraftable(scc)) {
                return;
            }
            if (s.isSilverSpell() && (this.factionID == null || s.getFactionRequirement() == null || s.getFactionRequirement().is(this.factionID))) {
                candidates.add(s);
            }
        });
        if (candidates.size() == 0) {
            return stack;
        }
        ISpellComponent selected = (ISpellComponent)candidates.get((int)(Math.random() * (double)candidates.size()));
        ((ItemTornJournalPage)ItemInit.SPELL_PART_THESIS.get()).setComponent(stack, selected);
        return stack;
    }

    public LootItemFunctionType<RandomSilverSpell> getType() {
        return TYPE;
    }

}

