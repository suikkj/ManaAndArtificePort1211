/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.recipes.spells;

import net.minecraft.core.component.DataComponents;
import com.mna.Registries;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Shape;
import com.mna.items.ItemInit;
import com.mna.recipes.ItemAndPatternRecipe;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.spells.ISpellComponentRecipe;
import java.security.InvalidParameterException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

public class ShapeRecipe
extends ItemAndPatternRecipe
implements ISpellComponentRecipe {
    public ShapeRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.SHAPE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.SHAPE_TYPE.get();
    }

    @Override
    protected int maxItems() {
        return 9;
    }

    @Override
    protected int maxPatterns() {
        return 9;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        ISpellComponent me = this.getComponent();
        if (me != null) {
            return new ItemStack((ItemLike)ItemInit.VELLUM.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)me.getRegistryName().toString()));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ISpellComponent getComponent() {
        ResourceLocation myLocation = this.output.getNamespace().equals("mna") ? ResourceLocation.fromNamespaceAndPath(this.output.getNamespace(), "shapes/" + this.output.getPath()) : this.output;
        Shape me = (Shape)Registries.Shape.getValue(myLocation);
        if (me == null) {
            throw new InvalidParameterException("Shape not found, the recipe is incorrect: " + myLocation.toString());
        }
        return me;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }
}

