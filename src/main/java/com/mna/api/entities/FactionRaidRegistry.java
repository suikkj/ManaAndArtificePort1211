/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 */
package com.mna.api.entities;

import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mojang.datafixers.util.Pair;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public class FactionRaidRegistry {
    private static final HashMap<IFaction, HashMap<EntityType<? extends IFactionEnemy<? extends Mob>>, HashMap<Integer, Integer>>> soldierMap = new HashMap();

    public static int getStrengthRating(IFaction faction, EntityType<? extends IFactionEnemy<? extends Mob>> entityType, int tier) {
        HashMap<EntityType<? extends IFactionEnemy<? extends Mob>>, HashMap<Integer, Integer>> soldiers = FactionRaidRegistry.getFactionSoliderMap(faction);
        return soldiers.containsKey(entityType) ? FactionRaidRegistry.keyFromValue(soldiers.get(entityType), tier) : -1;
    }

    private static HashMap<EntityType<? extends IFactionEnemy<? extends Mob>>, HashMap<Integer, Integer>> getFactionSoliderMap(IFaction faction) {
        return soldierMap.computeIfAbsent(faction, k -> new HashMap());
    }

    private static int keyFromValue(HashMap<Integer, Integer> values, int target) {
        return values.entrySet().stream().filter(e -> (Integer)e.getValue() == target).map(e -> (Integer)e.getKey()).findFirst().orElse(-1);
    }

    public static void registerSoldier(IFaction faction, EntityType<? extends IFactionEnemy<? extends Mob>> entityType, HashMap<Integer, Integer> tierStrengthRatings) {
        for (Integer strength : tierStrengthRatings.keySet()) {
            if (tierStrengthRatings.get(strength) < 0) {
                throw new InvalidParameterException("Tier minimum is 0!");
            }
            if (strength > 0) continue;
            throw new InvalidParameterException("Strength rating must be greater than zero or you risk infinite loops!");
        }
        try {
            FactionRaidRegistry.getFactionSoliderMap(faction).put(entityType, tierStrengthRatings);
        }
        catch (Throwable t) {
            throw new InvalidParameterException("Cannot register faction enemies for faction " + faction.toString());
        }
    }

    @Nullable
    public static Pair<EntityType<? extends IFactionEnemy<? extends Mob>>, Integer> getSoldier(IFaction faction, int raidStrength) {
        return FactionRaidRegistry.pickSoldier(FactionRaidRegistry.getFactionSoliderMap(faction), raidStrength);
    }

    @Nullable
    private static Pair<EntityType<? extends IFactionEnemy<? extends Mob>>, Integer> pickSoldier(HashMap<EntityType<? extends IFactionEnemy<? extends Mob>>, HashMap<Integer, Integer>> list, int raidStrength) {
        if (list.size() == 0) {
            return null;
        }
        List possibles = list.entrySet().stream().filter(e -> ((HashMap)e.getValue()).keySet().stream().anyMatch(i -> (Integer)i <= raidStrength)).collect(Collectors.toList());
        if (possibles.size() == 0) {
            return null;
        }
        Map.Entry entry = (Map.Entry)possibles.get(new Random().nextInt(possibles.size()));
        int maxStr = (Integer)Collections.max((java.util.Collection)((HashMap)entry.getValue()).keySet().stream().filter(i -> (Integer)i <= raidStrength).collect(Collectors.toList()));
        int tier = (Integer)((HashMap)entry.getValue()).get(maxStr);
        return new Pair(((EntityType)entry.getKey()), tier);
    }
}

