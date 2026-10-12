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
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.interop.jei.MARecipeTypes;
import com.mna.items.ItemInit;
import com.mna.recipes.manaweaving.TransmutationRecipe;
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

public class TransmutationRecipeCategory
implements IRecipeCategory<TransmutationRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;
    private int xSize = 152;
    private int ySize = 177;

    public TransmutationRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(GuiTextures.Jei.TRANSMUTATION_MULTI, 0, 0, this.xSize, this.ySize).setTextureSize(152, 177).build();
        this.localizedName = I18n.get((String)"gui.mna.jei.transmutation", (Object[])new Object[0]);
        this.icon = guiHelper.createDrawableIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND_ADVANCED.get()));
    }

    public RecipeType<TransmutationRecipe> getRecipeType() {
        return MARecipeTypes.TRANSMUTATION;
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

    public void setRecipe(IRecipeLayoutBuilder builder, TransmutationRecipe recipe, IFocusGroup focuses) {
        builder.setShapeless();
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 80).addIngredients(Ingredient.of(MATags.smartLookupItem(recipe.getTargetBlock()).stream().map(i -> new ItemStack((ItemLike)i))));
        if (recipe.getReplaceBlock() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 22, 123).addIngredients(Ingredient.of(MATags.smartLookupItem(recipe.getReplaceBlock()).stream().map(i -> new ItemStack((ItemLike)i))));
        } else if (recipe.getLootTable() != null) {
            // empty if block
        }
        if (recipe.hasRepresentationItem()) {
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredients(Ingredient.of(MATags.smartLookupItem(recipe.getRepresentationItem()).stream().map(i -> new ItemStack((ItemLike)i))));
        }
    }

    public void draw(TransmutationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics pGuiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            int playerTier;
            int tier = recipe.getTier();
            int color = tier <= (playerTier = ((IPlayerProgression)mc.player.getCapability(PlayerProgressionProvider.PROGRESSION).resolve().get()).getTier()) ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
            MutableComponent name = Component.translatable((String)recipe.getResultItem().getDescriptionId().toString());
            MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
            int stringWidth = mc.font.width((FormattedText)name);
            int textX = this.xSize / 2 - stringWidth / 2;
            int textY = 5;
            pGuiGraphics.drawString(mc.font, (Component)name, textX, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
            pGuiGraphics.drawString(mc.font, (Component)tierPrompt, this.xSize / 2 - mc.font.width((FormattedText)tierPrompt) / 2, 15, color, false);
            GuiRenderUtils.renderFactionIcon(pGuiGraphics, recipe.getFactionRequirement(), textX + stringWidth + 3, textY);
            if (recipe.getLootTable() != null) {
                pGuiGraphics.drawWordWrap(mc.font, (FormattedText)Component.translatable((String)"gui.mna.use-codex"), 22, 123, 110, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255));
            }
            RenderSystem.enableBlend();
        }
    }
}

