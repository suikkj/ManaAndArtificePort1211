/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nonnull
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.storage.loot.functions.LootItemFunction
 *  net.minecraft.world.level.storage.loot.predicates.LootItemCondition
 */
package com.mna.tools.loot;

import com.mna.tools.loot.Conditional;
import com.mna.tools.loot.LootConditionHelper;
import com.mna.tools.loot.LootFunctionHelper;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootDrop
implements Comparable<LootDrop> {
    public int minDrop;
    public int maxDrop;
    public ItemStack item = ItemStack.EMPTY;
    public ItemStack smeltedItem = ItemStack.EMPTY;
    public float chance;
    private Set<Conditional> conditionals;
    public int fortuneLevel;
    public boolean enchanted;
    private float sortIndex;

    public LootDrop(ItemStack item) {
        this(item, item.getCount());
    }

    public LootDrop(ItemStack item, float chance) {
        this(item, chance, 0);
    }

    public LootDrop(ItemStack item, float chance, int fortuneLevel) {
        this(item, (int)Math.floor(chance), (int)Math.ceil(chance), chance, fortuneLevel, new Conditional[0]);
    }

    public LootDrop(ItemStack item, int minDrop, int maxDrop, Conditional ... conditionals) {
        this(item, minDrop, maxDrop, 1.0f, 0, conditionals);
    }

    public LootDrop(ItemStack item, int minDrop, int maxDrop, float chance, int fortuneLevel, Conditional ... conditionals) {
        this.item = item;
        this.smeltedItem = ItemStack.EMPTY;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
        this.chance = chance;
        this.sortIndex = Math.min(chance, 1.0f) * (float)(minDrop + maxDrop);
        this.conditionals = new HashSet<Conditional>();
        Collections.addAll(this.conditionals, conditionals);
        this.fortuneLevel = fortuneLevel;
    }

    public LootDrop(Item item, int minDrop, int maxDrop, Conditional ... conditionals) {
        this(new ItemStack((ItemLike)item), minDrop, maxDrop, 1.0f, 0, conditionals);
    }

    public LootDrop(Item item, CompoundTag tag, int minDrop, int maxDrop, Conditional ... conditionals) {
        this(new ItemStack((ItemLike)item, 1, tag), minDrop, maxDrop, 1.0f, 0, conditionals);
    }

    public LootDrop(Item item, int minDrop, int maxDrop, float chance, Conditional ... conditionals) {
        this(new ItemStack((ItemLike)item), minDrop, maxDrop, chance, 0, conditionals);
    }

    public LootDrop(Item item, CompoundTag tag, int minDrop, int maxDrop, float chance, Conditional ... conditionals) {
        this(new ItemStack((ItemLike)item, 1, tag), minDrop, maxDrop, chance, 0, conditionals);
    }

    public LootDrop(ItemStack item, int minDrop, int maxDrop, float chance, Conditional ... conditionals) {
        this(item, minDrop, maxDrop, chance, 0, conditionals);
    }

    public LootDrop(ServerLevel serverLevel, Item item, float chance, LootItemFunction ... lootFunctions) {
        this(new ItemStack((ItemLike)item), chance);
        this.enchanted = false;
        this.addLootFunctions(serverLevel, lootFunctions);
    }

    public LootDrop(ServerLevel serverLevel, Item item, float chance, LootItemCondition[] lootConditions, LootItemFunction ... lootFunctions) {
        this(serverLevel, item, chance, lootFunctions);
        this.addLootConditions(lootConditions);
        this.addLootFunctions(serverLevel, lootFunctions);
    }

    public LootDrop applyLootRolls(ServerLevel serverLevel, Pair<Integer, Integer> rolls) {
        this.minDrop *= ((Integer)rolls.getFirst()).intValue();
        this.maxDrop *= ((Integer)rolls.getSecond()).intValue();
        return this;
    }

    public LootDrop addLootConditions(LootItemCondition[] lootConditions) {
        return this.addLootConditions(Arrays.asList(lootConditions));
    }

    public LootDrop addLootConditions(Collection<LootItemCondition> lootConditions) {
        lootConditions.forEach(this::addLootCondition);
        return this;
    }

    public LootDrop addLootCondition(LootItemCondition condition) {
        try {
            LootConditionHelper.applyCondition(condition, this);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return this;
    }

    public LootDrop addLootFunctions(ServerLevel serverLevel, LootItemFunction[] lootFunctions) {
        return this.addLootFunctions(serverLevel, Arrays.asList(lootFunctions));
    }

    public LootDrop addLootFunctions(ServerLevel serverLevel, Collection<LootItemFunction> lootFunctions) {
        lootFunctions.forEach(lf -> this.addLootFunction(serverLevel, (LootItemFunction)lf));
        return this;
    }

    public LootDrop addLootFunction(ServerLevel serverLevel, LootItemFunction lootFunction) {
        try {
            LootFunctionHelper.applyFunction(serverLevel, lootFunction, this);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return this;
    }

    public boolean canBeCooked() {
        return !this.smeltedItem.isEmpty();
    }

    public List<ItemStack> getDrops() {
        LinkedList<ItemStack> list = new LinkedList<ItemStack>();
        if (!this.item.isEmpty()) {
            list.add(this.item);
        }
        if (!this.smeltedItem.isEmpty()) {
            list.add(this.smeltedItem);
        }
        return list;
    }

    public String toString() {
        if (this.minDrop == this.maxDrop) {
            return "x" + this.minDrop + this.getDropChance();
        }
        return this.minDrop + "-" + this.maxDrop + this.getDropChance();
    }

    private String getDropChance() {
        return this.chance < 1.0f ? " (" + this.formatChance() + "%)" : "";
    }

    public String formatChance() {
        float chance = this.chance * 100.0f;
        if (chance < 10.0f) {
            return String.format("%.1f", Float.valueOf(chance));
        }
        return String.format("%2d", (int)chance);
    }

    public boolean isAffectedBy(Conditional conditional) {
        return this.conditionals.contains(conditional);
    }

    public String chanceString() {
        if (this.chance >= 0.995f) {
            return String.format("%.2G", Float.valueOf(this.chance));
        }
        return String.format("%.2G%%", Float.valueOf(this.chance * 100.0f));
    }

    public List<Component> getTooltipText() {
        return this.getTooltipText(false);
    }

    public List<Component> getTooltipText(boolean smelted) {
        List<Component> list = this.conditionals.stream().map(Conditional::toStringTextComponent).collect(Collectors.toList());
        if (smelted) {
            list.add((Component)Conditional.burning.toStringTextComponent());
        }
        return list;
    }

    public void addConditional(Conditional conditional) {
        this.conditionals.add(conditional);
    }

    public void addConditionals(List<Conditional> conditionals) {
        this.conditionals.addAll(conditionals);
    }

    public float getSortIndex() {
        return this.sortIndex;
    }

    public MutableComponent toStringTextComponent() {
        return Component.literal((String)this.toString());
    }

    @Override
    public int compareTo(@Nonnull LootDrop o) {
        if (ItemStack.isSameItem((ItemStack)this.item, (ItemStack)o.item)) {
            return Integer.compare(o.fortuneLevel, this.fortuneLevel);
        }
        int cmp = Float.compare(o.getSortIndex(), this.getSortIndex());
        return cmp != 0 ? cmp : this.item.getDisplayName().toString().compareTo(o.item.getDisplayName().toString());
    }

    public static LootDrop readFrom(FriendlyByteBuf buf) {
        LootDrop drop = new LootDrop(ItemStack.EMPTY);
        drop.minDrop = buf.readInt();
        drop.maxDrop = buf.readInt();
        drop.fortuneLevel = buf.readInt();
        drop.item = buf.readItem();
        drop.smeltedItem = buf.readItem();
        drop.chance = buf.readFloat();
        drop.sortIndex = buf.readFloat();
        drop.enchanted = buf.readBoolean();
        return drop;
    }

    public void writeTo(FriendlyByteBuf buf) {
        buf.writeInt(this.minDrop);
        buf.writeInt(this.maxDrop);
        buf.writeInt(this.fortuneLevel);
        buf.writeItem(this.item);
        buf.writeItem(this.smeltedItem);
        buf.writeFloat(this.chance);
        buf.writeFloat(this.sortIndex);
        buf.writeBoolean(this.enchanted);
    }
}

