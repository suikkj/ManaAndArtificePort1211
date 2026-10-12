/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Ingredient$Value
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.common.crafting.IIngredientSerializer
 */
package com.mna.interop.jei.ingredients;

import com.mna.interop.jei.ingredients.ManaweavePatternIngredientSerializer;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.IIngredientSerializer;

public class ManaweavePatternIngredient
extends Ingredient {
    private ResourceLocation pattern;
    private ManaweavingPattern _cached;

    public ManaweavePatternIngredient(ResourceLocation rLoc) {
        this(Stream.empty());
        this.pattern = rLoc;
    }

    protected ManaweavePatternIngredient(Stream<? extends Ingredient.Value> itemLists) {
        super(itemLists);
    }

    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return new ManaweavePatternIngredientSerializer();
    }

    public ManaweavingPattern getPattern(Level world) {
        if (this._cached == null) {
            this._cached = ManaweavingPatternHelper.GetManaweavingRecipe(world, this.pattern);
        }
        return this._cached;
    }

    public ResourceLocation getWeaveId() {
        return this.pattern;
    }

    public ManaweavePatternIngredient copy() {
        return new ManaweavePatternIngredient(this.pattern);
    }
}


