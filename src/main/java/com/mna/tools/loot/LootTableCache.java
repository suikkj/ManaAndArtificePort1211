/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.tools.loot;

import com.mna.tools.loot.LootDrop;
import java.util.HashMap;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class LootTableCache {
    private static HashMap<ResourceLocation, List<LootDrop>> __cachedLoot = new HashMap();

    public static List<LootDrop> getLoot(ResourceLocation lootTableID) {
        return __cachedLoot.get(lootTableID);
    }

    public static void cacheLoot(ResourceLocation lootTableID, List<LootDrop> drops) {
        __cachedLoot.put(lootTableID, drops);
    }
}

