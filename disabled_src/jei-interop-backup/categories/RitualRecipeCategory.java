/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  mezz.jei.api.constants.VanillaTypes
 *  mezz.jei.api.gui.builder.IRecipeLayoutBuilder
 *  mezz.jei.api.gui.drawable.IDrawable
 *  mezz.jei.api.gui.ingredient.IRecipeSlotsView
 *  mezz.jei.api.helpers.IGuiHelper
 *  mezz.jei.api.ingredients.IIngredientType
 *  mezz.jei.api.recipe.IFocusGroup
 *  mezz.jei.api.recipe.RecipeIngredientRole
 *  mezz.jei.api.recipe.RecipeType
 *  mezz.jei.api.recipe.category.IRecipeCategory
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.interop.jei.categories;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualReagent;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.interop.jei.MARecipeTypes;
import com.mna.items.ItemInit;
import com.mna.recipes.rituals.RitualRecipe;
import com.mna.tools.render.GuiRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class RitualRecipeCategory
implements IRecipeCategory<RitualRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;
    private int xSize = 144;
    private int ySize = 169;

    public RitualRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(GuiTextures.Jei.RITUAL, 0, 0, this.xSize, this.ySize).setTextureSize(144, 169).build();
        this.localizedName = I18n.get((String)"gui.mna.jei.ritual", (Object[])new Object[0]);
        this.icon = guiHelper.createDrawableIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)new ItemStack((ItemLike)ItemInit.RUNE_RITUAL_METAL.get()));
    }

    public RecipeType<RitualRecipe> getRecipeType() {
        return MARecipeTypes.RITUAL;
    }

    public Component getTitle() {
        return Component.translatable((String)this.localizedName);
    }

    public IDrawable getBackground() {
        return this.background;
    }

    public IDrawable getIcon() {
        return this.icon;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, RitualRecipe recipe, IFocusGroup focuses) {
        int xPosOrigin;
        int xc = this.xSize / 2 - 8;
        int yc = this.ySize / 2 + 3;
        int bounds = recipe.getLowerBound();
        IRitualReagent[][] reagents = recipe.getReagents();
        int xPos = xPosOrigin = xc + 16 * bounds;
        int yPos = yc - 16 * bounds;
        for (int i = 0; i < reagents.length; ++i) {
            xPos = xPosOrigin;
            for (int j = 0; j < reagents[i].length; ++j) {
                if (reagents[i][j] != null && !reagents[i][j].isEmpty() && !reagents[i][j].isDynamic()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, xPos, yPos).addIngredients(Ingredient.of(MATags.smartLookupItem(reagents[i][j].getResourceLocation()).stream().map(item -> new ItemStack((ItemLike)item))));
                }
                xPos -= 16;
            }
            yPos += 16;
        }
        if (!recipe.getResultItem().isEmpty()) {
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)recipe.getResultItem());
        }
    }

    public void draw(RitualRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics pGuiGraphics, double mouseX, double mouseY) {
        int xPosOrigin;
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            int playerTier;
            int tier = recipe.getTier();
            int color = tier <= (playerTier = ((IPlayerProgression)mc.player.getCapability(PlayerProgressionProvider.PROGRESSION).resolve().get()).getTier()) ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
            MutableComponent name = Component.translatable((String)recipe.getId().toString());
            MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
            int stringWidth = mc.font.width((FormattedText)name);
            int textX = this.xSize / 2 - stringWidth / 2;
            int textY = 5;
            pGuiGraphics.drawString(mc.font, (Component)name, textX, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
            pGuiGraphics.drawString(mc.font, (Component)tierPrompt, this.xSize / 2 - mc.font.width((FormattedText)tierPrompt) / 2, 15, color, false);
            GuiRenderUtils.renderFactionIcon(pGuiGraphics, recipe.getFactionRequirement(), textX + stringWidth + 3, textY);
        }
        int squareSize = 16;
        int xc = this.xSize / 2 - 9;
        int yc = this.ySize / 2 + 2;
        int bounds = recipe.getLowerBound();
        int xPos = xPosOrigin = xc + squareSize * bounds;
        int yPos = yc - squareSize * bounds;
        RenderSystem.enableBlend();
        int[][] pattern = recipe.getPattern();
        for (int i = 0; i < pattern.length; ++i) {
            xPos = xPosOrigin;
            for (int j = 0; j < pattern[i].length; ++j) {
                if (pattern[i][j] != 0) {
                    pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, xPos + 1, yPos + 1, 112.0f, 0.0f, squareSize, squareSize, 128, 128);
                }
                xPos -= squareSize;
            }
            yPos += squareSize;
        }
        RenderSystem.disableBlend();
    }
}

