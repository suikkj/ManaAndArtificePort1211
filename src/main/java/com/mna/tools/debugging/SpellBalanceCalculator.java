/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.item.crafting.RecipeManager
 *  org.apache.commons.lang3.mutable.MutableFloat
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package com.mna.tools.debugging;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.spells.ComponentRecipe;
import com.mna.recipes.spells.ShapeRecipe;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class SpellBalanceCalculator {
    public static void calculateMinMaxComplexityPerTier() {
        for (int i = 1; i <= 5; ++i) {
            List<Shape> tierShapes = SpellBalanceCalculator.getShapesForTier(i);
            List<SpellEffect> tierComponents = SpellBalanceCalculator.getComponentsForTier(i);
            float min = SpellBalanceCalculator.calculateMinComplexity(tierShapes, tierComponents);
            float max = SpellBalanceCalculator.calculateMaxComplexity(tierShapes, tierComponents);
            float avg = SpellBalanceCalculator.calculateAverageDefaultComplexity(tierShapes, tierComponents);
            ManaAndArtifice.LOGGER.debug(String.format("Tier %d min complexity %.2f max complexity %.2f avg baseline complexity %.2f", i, Float.valueOf(min), Float.valueOf(max), Float.valueOf(avg)));
        }
    }

    private static float calculateMinComplexity(List<Shape> shapes, List<SpellEffect> components) {
        MutableFloat minComplexity = new MutableFloat(9999999.0f);
        shapes.forEach(s -> components.forEach(c -> {
            SpellRecipe sr = new SpellRecipe();
            sr.setShape((Shape)s);
            sr.addComponent((SpellEffect)c);
            if (sr.getComplexity() < minComplexity.getValue().floatValue()) {
                minComplexity.setValue(sr.getComplexity());
            }
        }));
        return minComplexity.getValue().floatValue();
    }

    private static float calculateMaxComplexity(List<Shape> shapes, List<SpellEffect> components) {
        MutableFloat maxComplexity = new MutableFloat(0.0f);
        shapes.forEach(s -> components.forEach(c -> {
            SpellRecipe sr = new SpellRecipe();
            sr.setShape((Shape)s);
            sr.addComponent((SpellEffect)c);
            sr.maximize();
            if (sr.getComplexity() > maxComplexity.getValue().floatValue()) {
                maxComplexity.setValue(sr.getComplexity());
            }
        }));
        return maxComplexity.getValue().floatValue();
    }

    private static float calculateAverageDefaultComplexity(List<Shape> shapes, List<SpellEffect> components) {
        MutableFloat complexity = new MutableFloat(0.0f);
        MutableInt count = new MutableInt(0);
        shapes.forEach(s -> components.forEach(c -> {
            SpellRecipe sr = new SpellRecipe();
            sr.setShape((Shape)s);
            sr.addComponent((SpellEffect)c);
            complexity.add(sr.getComplexity());
            count.add(1);
        }));
        int c = count.getValue();
        if (c == 0) {
            c = 1;
        }
        return complexity.getValue().floatValue() / (float)c;
    }

    private static List<Shape> getShapesForTier(int tier) {
        Minecraft mc = Minecraft.getInstance();
        RecipeManager rm = mc.level.getRecipeManager();
        return rm.getRecipes().stream().filter(r -> r.value().getType() == RecipeInit.SHAPE_TYPE.get()).map(r -> (ShapeRecipe)r.value()).filter(r -> r.getTier() <= tier).map(r -> (Shape)Registries.Shape.getValue(r.getId())).filter(r -> r != null).collect(Collectors.toList());
    }

    private static List<SpellEffect> getComponentsForTier(int tier) {
        Minecraft mc = Minecraft.getInstance();
        RecipeManager rm = mc.level.getRecipeManager();
        return rm.getRecipes().stream().filter(r -> r.value().getType() == RecipeInit.COMPONENT_TYPE.get()).map(r -> (ComponentRecipe)r.value()).filter(r -> r.getTier() <= tier).map(r -> (SpellEffect)Registries.SpellEffect.getValue(r.getId())).filter(r -> r != null).collect(Collectors.toList());
    }
}

