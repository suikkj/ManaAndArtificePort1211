/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.Level
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package com.mna.api.items;

import com.mna.tools.EnchantmentUtils;
import com.mna.api.recipes.IMARecipe;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public interface ITieredItem<T extends Item> {
    public static final int TIER_NOT_CHECKED = -1;
    public static final int TIER_NO_RECIPE = -2;
    public static final String TAG_HIDE_TIER_IN_TOOLTIP = "hideTier";

    public void setCachedTier(int var1);

    public int getCachedTier();

    default public int getTier(Level world, ItemStack stack) {
        if (EnchantmentUtils.enchantmentMap((ItemStack)stack).size() > 0) {
            return this.resolveTier(world, stack);
        }
        if (this.getCachedTier() == -1) {
            this.setCachedTier(this.resolveTier(world, stack));
        }
        return this.getCachedTier();
    }

    default public int resolveTier(Level world, ItemStack stack) {
        List<net.minecraft.world.item.crafting.RecipeHolder<?>> recipes = world.getRecipeManager().getRecipes().parallelStream().filter(r -> {
            if (r == null) {
                return false;
            }
            ItemStack res = r.value().getResultItem(world.registryAccess());
            return res != null && res.getItem() == this;
        }).collect(Collectors.toList());
        if (recipes.size() == 0) {
            return -2;
        }
        Map itemEnchantments = EnchantmentUtils.enchantmentMap((ItemStack)stack);
        if (itemEnchantments.size() == 1) {
            recipes = recipes.stream().filter(r -> {
                Map enchs = EnchantmentUtils.enchantmentMap((ItemStack)r.value().getResultItem(world.registryAccess()));
                if (enchs.size() != 1) {
                    return false;
                }
                MutableBoolean match = new MutableBoolean(false);
                itemEnchantments.entrySet().forEach(e -> enchs.entrySet().forEach(e2 -> {
                    if (e.getKey() == e2.getKey() && e.getValue() == e2.getValue()) {
                        match.setTrue();
                    }
                }));
                return match.getValue();
            }).collect(Collectors.toList());
        }
        MutableInt lowest = new MutableInt(-2);
        recipes.stream().sorted((a, b) -> {
            if (a.value() instanceof IMARecipe) {
                IMARecipe aR = (IMARecipe)a.value();
                if (b.value() instanceof IMARecipe) {
                    IMARecipe bR = (IMARecipe)b.value();
                    return aR.getTier() < bR.getTier() ? -1 : (aR.getTier() > bR.getTier() ? 1 : 0);
                }
            }
            return 0;
        }).findFirst().ifPresent(r -> {
            if (r.value() instanceof IMARecipe) {
                IMARecipe mR = (IMARecipe)r.value();
                lowest.setValue(mR.getTier());
            } else {
                lowest.setValue(0);
            }
        });
        return lowest.getValue();
    }
}

