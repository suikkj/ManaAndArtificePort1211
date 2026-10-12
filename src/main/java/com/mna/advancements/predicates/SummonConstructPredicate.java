/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 */
package com.mna.advancements.predicates;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mna.advancements.predicates.RequiredConstructPartPredicate;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.entities.constructs.animated.Construct;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummonConstructPredicate {
    private static final Codec<Map<ConstructMaterial, Integer>> MATERIAL_MAP = Codec.unboundedMap(Codec.STRING, Codec.INT)
        .xmap(in -> {
            Map<ConstructMaterial, Integer> out = new HashMap<>();
            in.forEach((k, v) -> {
                for (ConstructMaterial cm : ConstructMaterial.ALL_MATERIALS) {
                    if (cm.getId().toString().equals(k)) { out.put(cm, v); break; }
                }
            });
            return out;
        }, in -> {
            Map<String, Integer> out = new HashMap<>();
            in.forEach((mat, v) -> out.put(mat.getId().toString(), v));
            return out;
        });
    private static final Codec<Map<Integer, Integer>> TIER_MAP = Codec.unboundedMap(Codec.STRING, Codec.INT)
        .xmap(in -> {
            Map<Integer, Integer> out = new HashMap<>();
            in.forEach((k, v) -> { if (k.startsWith("tier_")) { try { out.put(Integer.parseInt(k.substring(5)), v); } catch (Exception ignored) {} } });
            return out;
        }, in -> {
            Map<String, Integer> out = new HashMap<>();
            in.forEach((k, v) -> out.put("tier_" + k, v));
            return out;
        });
    public static final Codec<SummonConstructPredicate> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        MATERIAL_MAP.optionalFieldOf("materials", new HashMap<>()).forGetter(p -> p.materialRequirements),
        TIER_MAP.optionalFieldOf("numMaterialsForTier", new HashMap<>()).forGetter(p -> p.tierPartRequirements),
        RequiredConstructPartPredicate.CODEC.listOf().optionalFieldOf("partRequirements", new ArrayList<>()).forGetter(p -> p.requiredParts)
    ).apply(inst, (mats, tiers, parts) -> {
        SummonConstructPredicate p = new SummonConstructPredicate();
        p.materialRequirements.putAll(mats);
        p.tierPartRequirements.putAll(tiers);
        p.requiredParts.addAll(parts);
        return p;
    }));
    private HashMap<ConstructMaterial, Integer> materialRequirements = new HashMap();
    private HashMap<Integer, Integer> tierPartRequirements = new HashMap();
    private ArrayList<RequiredConstructPartPredicate> requiredParts = new ArrayList();

    public boolean test(Construct construct) {
        boolean pass = true;
        for (Map.Entry<ConstructMaterial, Integer> kvp : this.materialRequirements.entrySet()) {
            pass &= this.test_material(construct, kvp.getKey(), kvp.getValue());
        }
        for (RequiredConstructPartPredicate req : this.requiredParts) {
            pass &= req.matches(construct.getConstructData());
        }
        if (this.tierPartRequirements.containsKey(1)) {
            pass &= this.test_material(construct, ConstructMaterial.WICKERWOOD, this.tierPartRequirements.get(1));
        }
        if (this.tierPartRequirements.containsKey(2)) {
            pass &= this.test_material(construct, ConstructMaterial.WOOD, this.tierPartRequirements.get(2)) || this.test_material(construct, ConstructMaterial.STONE, this.tierPartRequirements.get(2));
        }
        if (this.tierPartRequirements.containsKey(3)) {
            pass &= this.test_material(construct, ConstructMaterial.IRON, this.tierPartRequirements.get(3)) || this.test_material(construct, ConstructMaterial.GOLD, this.tierPartRequirements.get(3)) || this.test_material(construct, ConstructMaterial.BONE, this.tierPartRequirements.get(3));
        }
        if (this.tierPartRequirements.containsKey(4)) {
            pass &= this.test_material(construct, ConstructMaterial.OBSIDIAN, this.tierPartRequirements.get(4)) || this.test_material(construct, ConstructMaterial.DIAMOND, this.tierPartRequirements.get(4));
        }
        return pass;
    }

    private boolean test_material(Construct construct, ConstructMaterial material, int minimum) {
        if (minimum < 1) {
            return true;
        }
        return construct.getConstructData().getPartsForMaterial(material).size() >= minimum;
    }

    public static SummonConstructPredicate fromJSON(JsonObject json) {
        SummonConstructPredicate predicate = new SummonConstructPredicate();
        if (json.has("materials") && json.get("materials").isJsonObject()) {
            JsonObject materials = json.getAsJsonObject("materials");
            ConstructMaterial.ALL_MATERIALS.forEach(mat -> {
                if (materials.has(mat.getId().toString())) {
                    predicate.materialRequirements.put((ConstructMaterial)mat, materials.get(mat.getId().toString()).getAsInt());
                }
            });
        }
        if (json.has("numMaterialsForTier") && json.get("numMaterialsForTier").isJsonObject()) {
            JsonObject tierMaterials = json.getAsJsonObject("numMaterialsForTier");
            String tier_prefix = "tier_";
            for (int tier = 1; tier <= 4; ++tier) {
                if (!tierMaterials.has(tier_prefix + tier)) continue;
                predicate.tierPartRequirements.put(tier, tierMaterials.get(tier_prefix + tier).getAsInt());
            }
        }
        if (json.has("partRequirements") && json.get("partRequirements").isJsonArray()) {
            JsonArray partRequirements = json.get("partRequirements").getAsJsonArray();
            partRequirements.forEach(elem -> {
                if (elem.isJsonObject()) {
                    predicate.requiredParts.add(RequiredConstructPartPredicate.fromJSON(elem.getAsJsonObject()));
                }
            });
        }
        return predicate;
    }
}

