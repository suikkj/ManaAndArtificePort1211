/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
 *  net.minecraft.world.level.storage.loot.predicates.LootItemCondition
 *  net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition
 *  net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
 *  net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition
 */
package com.mna.tools.loot;

import com.mna.tools.loot.Conditional;
import com.mna.tools.loot.LootDrop;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;

public class LootConditionHelper {
    public static void applyCondition(LootItemCondition condition, LootDrop lootDrop) {
        if (condition instanceof LootItemKilledByPlayerCondition) {
            lootDrop.addConditional(Conditional.playerKill);
        } else if (condition instanceof LootItemRandomChanceCondition) {
            lootDrop.chance = 1.0f; // [PORT 1.21.1] probability virou NumberProvider (exige LootContext)
        } else if (condition instanceof LootItemRandomChanceWithEnchantedBonusCondition) {
            lootDrop.chance = ((LootItemRandomChanceWithEnchantedBonusCondition)condition).unenchantedChance();
            lootDrop.addConditional(Conditional.affectedByLooting);
        } else if (condition instanceof LootItemBlockStatePropertyCondition) {
            // empty if block
        
            return;
        }
    }
}

