/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 */
package com.mna.advancements.predicates;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mna.Registries;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.base.ISpellDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CraftSpellPredicate {
    public static final Codec<CraftSpellPredicate> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.INT.optionalFieldOf("tier", -1).forGetter(p -> p.tier),
        Codec.INT.optionalFieldOf("complexity", -1).forGetter(p -> p.complexity),
        ResourceLocation.CODEC.optionalFieldOf("faction").forGetter(p -> Optional.ofNullable(p.factionRequirement)),
        ResourceLocation.CODEC.listOf().optionalFieldOf("requiredParts", new ArrayList<>()).forGetter(p -> p.requiredParts)
    ).apply(inst, (tier, complexity, faction, parts) -> {
        CraftSpellPredicate pp = new CraftSpellPredicate(tier, complexity, faction.orElse(null));
        pp.requiredParts.addAll(parts);
        return pp;
    }));
    int complexity;
    int tier;
    ResourceLocation factionRequirement;
    ArrayList<ResourceLocation> requiredParts;

    public CraftSpellPredicate(int min_tier, int min_complexity, ResourceLocation factionRequirement) {
        this.tier = min_tier;
        this.complexity = min_complexity;
        this.factionRequirement = factionRequirement;
        this.requiredParts = new ArrayList();
    }

    public boolean test(ISpellDefinition craftedSpell, Level world) {
        IFaction faction;
        boolean pass = true;
        if (this.complexity > -1) {
            pass &= craftedSpell.getComplexity() >= (float)this.complexity;
        }
        if (this.tier > -1) {
            pass &= craftedSpell.getTier(world) >= this.tier;
        }
        if (this.factionRequirement != null && (faction = (IFaction)Registries.Factions.getValue(this.factionRequirement)) != null) {
            pass &= craftedSpell.isFactionSpell(faction);
        }
        for (ResourceLocation part : this.requiredParts) {
            pass &= craftedSpell.containsPart(part);
        }
        return pass;
    }

    public static CraftSpellPredicate fromJSON(JsonObject json) {
        CraftSpellPredicate predicate = new CraftSpellPredicate(-1, -1, null);
        if (json.has("tier")) {
            predicate.tier = json.get("tier").getAsInt();
        }
        if (json.has("complexity")) {
            predicate.complexity = json.get("complexity").getAsInt();
        }
        if (json.has("faction")) {
            predicate.factionRequirement = ResourceLocation.parse(json.get("faction").getAsString());
        }
        if (json.has("requiredParts") && json.get("requiredParts").isJsonArray()) {
            JsonArray requiredParts = json.getAsJsonArray("requiredParts");
            requiredParts.forEach(e -> predicate.requiredParts.add(ResourceLocation.parse(e.getAsString())));
        }
        return predicate;
    }
}

