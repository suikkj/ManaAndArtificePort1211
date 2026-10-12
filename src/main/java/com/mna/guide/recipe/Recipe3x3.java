/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.item.crafting.ShapelessRecipe
 */
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.gui.GuiTextures;
import com.mna.guide.recipe.ICyclingRecipeRenderer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class Recipe3x3
extends RecipeRendererBase
implements ICyclingRecipeRenderer<Recipe3x3> {
    private CraftingRecipe[] recipes;
    static final int POINT_RENDER_SIZE = 13;

    public Recipe3x3(int x, int y) {
        super(x, y);
    }

    @Override
    public void init_internal(ResourceLocation recipeLocation) {
        Optional pattern = this.minecraft.level.getRecipeManager().byKey(recipeLocation);
        if (pattern.isPresent()) {
            this.recipes = new CraftingRecipe[]{(CraftingRecipe)pattern.get()};
        }
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return GuiTextures.Recipe.CRAFT_3X3;
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.recipes.length == 0) {
            return;
        }
        ClientLevel level = this.minecraft.level;
        if (level == null) {
            return;
        }
        RegistryAccess access = level.registryAccess();
        if (access == null) {
            return;
        }
        CraftingRecipe recipe = this.recipes[this.getIndex()];
        if (recipe instanceof ShapedRecipe) {
            this.renderShapedRecipe(pGuiGraphics, x, y, (ShapedRecipe)recipe, access);
        } else if (recipe instanceof ShapelessRecipe) {
            this.renderShapelessRecipe(pGuiGraphics, x, y, (ShapelessRecipe)recipe, access);
        }
    }

    private void renderShapedRecipe(GuiGraphics pGuiGraphics, int x, int y, ShapedRecipe recipe, RegistryAccess access) {
        if (recipe.getWidth() > 3 || recipe.getHeight() > 3) {
            return;
        }
        int startX = (int)((float)this.getX() / this.scale + 35.0f);
        int startY = (int)((float)this.getY() / this.scale + 90.0f);
        int pointSizeX = 54;
        int pointSizeY = 50;
        int padding = 6;
        for (int j = 0; j < recipe.getHeight(); ++j) {
            for (int i = 0; i < recipe.getWidth(); ++i) {
                Ingredient ingredient;
                List<ItemStack> stacks;
                int ingredientIndex = j * recipe.getWidth() + i;
                if (ingredientIndex >= recipe.getIngredients().size() || (stacks = Arrays.asList((ingredient = (Ingredient)recipe.getIngredients().get(ingredientIndex)).getItems())).size() <= 0) continue;
                this.renderItemStack(pGuiGraphics, stacks, startX + (pointSizeX + padding) * i, startY + (pointSizeY + padding) * j, 2.0f);
            }
        }
        this.renderItemStack(pGuiGraphics, recipe.getResultItem(access), (int)((float)this.getX() / this.scale + 93.0f), (int)((float)this.getY() / this.scale + 23.0f), 2.0f);
    }

    private void renderShapelessRecipe(GuiGraphics pGuiGraphics, int x, int y, ShapelessRecipe recipe, RegistryAccess access) {
        if (recipe.getIngredients().size() > 9) {
            return;
        }
        int startX = (int)((float)this.getX() / this.scale + 35.0f);
        int startY = (int)((float)this.getY() / this.scale + 90.0f);
        int pointSizeX = 54;
        int pointSizeY = 50;
        int padding = 6;
        int count = 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length > 0) {
                this.renderItemStack(pGuiGraphics, Arrays.asList(stacks), startX + (pointSizeX + padding) * Math.floorDiv(count, 3), startY + (pointSizeY + padding) * (count % 3), 2.0f);
            }
            ++count;
        }
        this.renderItemStack(pGuiGraphics, recipe.getResultItem(access), (int)((float)this.getX() / this.scale + 93.0f), (int)((float)this.getY() / this.scale + 23.0f), 2.0f);
        Font fr = this.minecraft.font;
        String lineFormatted = I18n.get((String)"gui.mna.shapeless", (Object[])new Object[0]);
        pGuiGraphics.drawString(fr, lineFormatted, this.getX() + this.width / 2 - fr.width(lineFormatted) / 2, this.getY() + 67, 0x404040, false);
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public void init_cycling(ResourceLocation[] rLocs) {
        ArrayList<CraftingRecipe> recipes = new ArrayList<CraftingRecipe>();
        for (ResourceLocation rLoc : rLocs) {
            Optional pattern = this.minecraft.level.getRecipeManager().byKey(rLoc);
            if (!pattern.isPresent()) continue;
            recipes.add((CraftingRecipe)pattern.get());
        }
        this.recipes = recipes.toArray(new CraftingRecipe[0]);
    }

    @Override
    public int countRecipes() {
        return this.recipes.length;
    }
}

