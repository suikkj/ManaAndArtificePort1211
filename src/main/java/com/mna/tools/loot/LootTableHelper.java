package com.mna.tools.loot;

import com.mna.tools.loot.LootDrop;
import com.mojang.datafixers.util.Either;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * [PORT 1.21.1] Migrado de LootDataManager (removido) para ReloadableServerRegistries,
 * de LootTableReference (removido) para NestedLootTable, e de ObfuscationReflectionHelper
 * (removido) para java.lang.reflect puro. Campos privados acessados por reflexao.
 */
public class LootTableHelper {
    private static LootTableHelper instance;
    private static final Field FIELD_POOLS = field(LootTable.class, "pools");
    private static final Field FIELD_ENTRIES = field(LootPool.class, "entries");
    private static final Field FIELD_POOL_CONDITIONS = field(LootPool.class, "conditions");
    private static final Field FIELD_WEIGHT = field(LootPoolSingletonContainer.class, "weight");
    private static final Field FIELD_ITEM = field(LootItem.class, "item");
    private static final Field FIELD_ENTRY_CONDITIONS = field(LootPoolEntryContainer.class, "conditions");
    private static final Field FIELD_ENTRY_FUNCTIONS = field(LootPoolSingletonContainer.class, "functions");
    private static final Field FIELD_NESTED = field(NestedLootTable.class, "contents");

    private static Field field(Class<?> c, String name) {
        try {
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[MNA] campo nao encontrado: " + c.getSimpleName() + "." + name, e);
        }
    }

    public static LootTableHelper instance() {
        if (instance == null) {
            instance = new LootTableHelper();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public static List<LootPool> getPools(LootTable table) {
        try {
            return (List<LootPool>)FIELD_POOLS.get(table);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public NumberProvider getRolls(LootPool pool) {
        return pool.getRolls();
    }

    public NumberProvider getBonusRolls(LootPool pool) {
        return pool.getBonusRolls();
    }

    @SuppressWarnings("unchecked")
    public static List<LootPoolEntryContainer> getLootEntries(LootPool pool) {
        try {
            return (List<LootPoolEntryContainer>)FIELD_ENTRIES.get(pool);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<LootItemCondition> getLootConditions(LootPool pool) {
        try {
            return (List<LootItemCondition>)FIELD_POOL_CONDITIONS.get(pool);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static int weight(LootPoolSingletonContainer entry) {
        try {
            return (Integer)FIELD_WEIGHT.get(entry);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Item item(LootItem entry) {
        try {
            return ((Holder<Item>)FIELD_ITEM.get(entry)).value();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static LootItemCondition[] entryConditions(LootPoolSingletonContainer entry) {
        try {
            return ((List<LootItemCondition>)FIELD_ENTRY_CONDITIONS.get(entry)).toArray(new LootItemCondition[0]);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static LootItemFunction[] entryFunctions(LootPoolSingletonContainer entry) {
        try {
            return ((List<LootItemFunction>)FIELD_ENTRY_FUNCTIONS.get(entry)).toArray(new LootItemFunction[0]);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<LootDrop> toDrops(ServerLevel serverLevel, LootTable table) {
        ArrayList<LootDrop> drops = new ArrayList<LootDrop>();
        LootContext baseContext = new LootContext.Builder(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY)).create(Optional.empty());
        LootTableHelper.getPools(table).forEach(pool -> {
            List<LootPoolEntryContainer> entries = LootTableHelper.getLootEntries(pool);
            float totalWeight = (float)entries.stream().filter(entry -> entry instanceof LootPoolSingletonContainer).map(entry -> (LootPoolSingletonContainer)entry).mapToInt(LootTableHelper::weight).sum();
            List<LootItemCondition> poolConditions = LootTableHelper.getLootConditions(pool);
            entries.stream().filter(entry -> entry instanceof LootItem).map(entry -> (LootItem)entry).map(entry -> new LootDrop(serverLevel, LootTableHelper.item(entry), totalWeight == 0.0f ? 0.0f : (float)LootTableHelper.weight(entry) / totalWeight, LootTableHelper.entryConditions(entry), LootTableHelper.entryFunctions(entry))).map(drop -> drop.addLootConditions(poolConditions)).forEach(d -> {
                d.minDrop *= pool.getRolls().getInt(baseContext);
                d.maxDrop *= pool.getRolls().getInt(baseContext) + pool.getBonusRolls().getInt(baseContext);
                drops.add((LootDrop)d);
            });
            entries.stream().filter(entry -> entry instanceof NestedLootTable).map(entry -> (NestedLootTable)entry).forEach(entry -> {
                try {
                    Either<ResourceKey<LootTable>, LootTable> contents = (Either<ResourceKey<LootTable>, LootTable>)FIELD_NESTED.get(entry);
                    contents.ifLeft(key -> drops.addAll(LootTableHelper.toDrops(serverLevel, serverLevel.getServer().reloadableRegistries().getLootTable(key))));
                    contents.ifRight(nested -> drops.addAll(LootTableHelper.toDrops(serverLevel, nested)));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        drops.removeIf(Objects::isNull);
        return drops;
    }

    public static List<LootDrop> toDrops(ServerLevel serverLevel, ResourceLocation lootTable) {
        return LootTableHelper.toDrops(serverLevel, serverLevel.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable)));
    }
}
