/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package com.mna.guide;

import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.guide.recipe.init.RecipeRenderers;
import com.mna.items.ItemInit;
import com.mna.recipes.spells.ISpellComponentRecipe;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class RelatedRecipe {
    private String _type;
    private int _tier = -1;
    private ResourceLocation[] _rLocs;
    private NonNullList<ItemStack> _outputItems;
    RecipeRendererBase renderer = null;
    private String key = "";

    public RelatedRecipe(String type, ResourceLocation ... rLocs) {
        this._type = type.replace("-", "_");
        this._rLocs = rLocs;
        StringBuilder sb = new StringBuilder();
        sb.append(this._type);
        for (ResourceLocation rLoc : this._rLocs) {
            sb.append(rLoc.toString());
        }
        this.key = sb.toString();
    }

    public String getType() {
        return this._type;
    }

    public ResourceLocation[] getResourceLocations() {
        return this._rLocs;
    }

    @Nullable
    public RecipeRendererBase constructRenderer(int x, int y, Consumer<List<Component>> tooltipFunction) {
        if (this.renderer == null || this.renderer.getX() != x || this.renderer.getY() != y || this.renderer.getTooltipFunction() != tooltipFunction) {
            this.createRenderer(x, y, tooltipFunction);
        }
        return this.renderer;
    }

    public NonNullList<ItemStack> getOutputItems(Level world) {
        if (this._outputItems == null) {
            this._outputItems = NonNullList.create();
            RecipeManager recipes = world.getRecipeManager();
            for (ResourceLocation rLoc : this.getResourceLocations()) {
                Optional recipe = recipes.byKey(rLoc);
                if (recipe == null || !recipe.isPresent()) continue;
                if (recipe.get() instanceof ISpellComponentRecipe) {
                    ItemStack dummy = new ItemStack((ItemLike)ItemInit.ENCHANTED_VELLUM.get());
                    ISpellComponent relatedSpell = ((ISpellComponentRecipe)recipe.get()).getComponent();
                    if (relatedSpell != null) {
                        dummy.set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)relatedSpell.getRegistryName().toString()));
                        this._outputItems.add(dummy);
                        continue;
                    }
                    ManaAndArtifice.LOGGER.warn("Error resolving component");
                    continue;
                }
                this._outputItems.add(((Recipe)recipe.get()).getResultItem(world.registryAccess()));
            }
        }
        return this._outputItems;
    }

    private void createRenderer(int x, int y, Consumer<List<Component>> tooltipFunction) {
        this.renderer = RecipeRenderers.instantiate(this._type, x, y);
        if (this.renderer != null) {
            this.renderer.setTooltipFunction(tooltipFunction);
            this.renderer.init(this.getResourceLocations());
        }
    }

    public int getTier() {
        if (this._tier == -1) {
            this.createRenderer(0, 0, null);
            if (this.renderer != null) {
                this._tier = this.renderer.getTier();
                this.renderer = null;
            } else {
                this._tier = 1;
            }
        }
        return this._tier;
    }

    public String toString() {
        return this.key;
    }
}

