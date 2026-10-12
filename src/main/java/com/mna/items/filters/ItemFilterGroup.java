/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.items.filters.AllItemFilter;
import com.mna.items.filters.EnchantedRuneFilter;
import com.mna.items.filters.GroundRuneNonPlaceable;
import com.mna.items.filters.ItemFilter;
import com.mna.items.filters.MarkingRuneFilter;
import com.mna.items.filters.PhylacteryFilter;
import com.mna.items.filters.RuneItemFilter;
import com.mna.items.filters.SpellItemFilter;
import com.mna.items.filters.VellumSpellFilter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class ItemFilterGroup {
    public static final ItemFilterGroup GROUND_RUNE_NON_PLACEABLE = new ItemFilterGroup(new GroundRuneNonPlaceable());
    public static final ItemFilterGroup ANY_STONE_RUNE = new ItemFilterGroup(new RuneItemFilter());
    public static final ItemFilterGroup ANY_SPELL = new ItemFilterGroup(new SpellItemFilter());
    public static final ItemFilterGroup VELLUM_SPELL = new ItemFilterGroup(new VellumSpellFilter());
    public static final ItemFilterGroup ALL_ITEMS = new ItemFilterGroup(new AllItemFilter());
    public static final ItemFilterGroup ANY_NON_EMPTY_PHYLACTERY = new ItemFilterGroup(new PhylacteryFilter());
    public static final ItemFilterGroup ANY_MARKING_RUNE = new ItemFilterGroup(new MarkingRuneFilter());
    public static final ItemFilterGroup ANY_ENCHANTED_RUNE = new ItemFilterGroup(new EnchantedRuneFilter());
    List<ItemFilter> filters = new ArrayList<ItemFilter>();

    public ItemFilterGroup(ItemFilter filter) {
        this.filters.add(filter);
    }

    public ItemFilterGroup(List<ItemFilter> filters) {
        this.filters.addAll(filters);
    }

    public boolean anyMatchesFilter(ItemStack stack) {
        for (ItemFilter filter : this.filters) {
            if (!filter.IsValidItem(stack)) continue;
            return true;
        }
        return false;
    }
}

