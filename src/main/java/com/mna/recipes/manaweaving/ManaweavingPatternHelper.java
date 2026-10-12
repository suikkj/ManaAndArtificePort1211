/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package com.mna.recipes.manaweaving;

import com.mna.recipes.RecipeInit;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.tools.ContainerTools;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ManaweavingPatternHelper {
    @Nullable
    public static ManaweavingPattern GetManaweavingRecipe(Level world, ResourceLocation rLoc) {
        List<ManaweavingPattern> mwpList;
        if (world == null || rLoc == null) {
            return null;
        }
        if (rLoc.getNamespace().equals("mna") && !rLoc.getPath().startsWith("manaweave_patterns/")) {
            rLoc = ResourceLocation.fromNamespaceAndPath(rLoc.getNamespace(), "manaweave_patterns/" + rLoc.getPath());
        }
        if ((mwpList = world.getRecipeManager().getRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get(), (Container)ContainerTools.createTemporaryContainer(ItemStack.EMPTY), world).stream().map(r -> r.value()).collect(java.util.stream.Collectors.toList())) == null || mwpList.size() == 0) {
            return null;
        }
        for (ManaweavingPattern pattern : mwpList) {
            if (pattern.getId().compareTo(rLoc) != 0) continue;
            return pattern;
        }
        return null;
    }

    public static Collection<ManaweavingPattern> getAllPatterns(Level world) {
        return world.getRecipeManager().getRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get(), (Container)ContainerTools.createTemporaryContainer(ItemStack.EMPTY), world).stream().map(r -> r.value()).collect(java.util.stream.Collectors.toList());
    }
}

