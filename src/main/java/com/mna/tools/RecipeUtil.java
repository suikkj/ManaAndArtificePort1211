/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package com.mna.tools;

import com.mna.tools.ContainerTools;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class RecipeUtil {
    public static Optional<net.minecraft.world.item.crafting.RecipeHolder<CraftingRecipe>> lookupCraftingRecipe(Level world, ItemStack[] items) {
        NonNullList invItems = NonNullList.create();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int index = j + i * 3;
                invItems.add((index >= items.length ? ItemStack.EMPTY : items[index]));
            }
        }
        CraftingContainer inventory = ContainerTools.createTemporaryContainer(3, 3, (NonNullList<ItemStack>)invItems);
        return world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (Container)inventory, world);
    }
}

