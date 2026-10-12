/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.item.alchemy.PotionUtils
 *  net.minecraft.world.item.alchemy.Potions
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.interop.jei.categories;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.interop.jei.JEIInterop;
import com.mna.interop.jei.MARecipeTypes;
import com.mna.interop.jei.ingredients.ManaweavePatternIngredient;
import com.mna.recipes.RecipeByproduct;
import com.mna.recipes.manaweaving.ManaweavingRecipe;
import com.mna.tools.render.GuiRenderUtils;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ManaweavingAltarRecipeCategory
implements IRecipeCategory<ManaweavingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;
    private int xSize = 152;
    private int ySize = 177;

    public ManaweavingAltarRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(GuiTextures.Jei.MANAWEAVING_ALTAR, 0, 0, this.xSize, this.ySize).setTextureSize(152, 177).build();
        this.localizedName = I18n.get((String)"gui.mna.jei.manaweaving_altar", (Object[])new Object[0]);
        this.icon = guiHelper.createDrawableIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)new ItemStack((ItemLike)BlockInit.MANAWEAVING_ALTAR.get()));
    }

    public RecipeType<ManaweavingRecipe> getRecipeType() {
        return MARecipeTypes.MANAWEAVING;
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

    public void setRecipe(IRecipeLayoutBuilder builder, ManaweavingRecipe recipe, IFocusGroup focuses) {
        int x = 29;
        int y = 30;
        int reagentIdx = 0;
        builder.setShapeless();
        for (ResourceLocation rLoc : recipe.getRequiredItems()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x + reagentIdx % 3 * 22, y + (int)Math.floor(reagentIdx / 3) * 22).addIngredients(this.resolveIngredient(rLoc));
            ++reagentIdx;
        }
        int xStep = 24;
        x = 8;
        y = 150;
        for (ResourceLocation rLoc : recipe.getRequiredPatterns()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredient((IIngredientType)JEIInterop.MANAWEAVE_PATTERN, (Object)new ManaweavePatternIngredient(rLoc));
            ++reagentIdx;
            x += xStep;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 52).addIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)recipe.getResultItem());
        int byproductX = this.xSize / 2 + 20;
        int byproductY = 80;
        int byproductCount = Math.min(recipe.getByproducts().size(), 2);
        for (int i = 0; i < byproductCount; ++i) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, byproductX, byproductY).addIngredients(Ingredient.of(((RecipeByproduct)recipe.getByproducts().get(i)).getItems().stream()));
            byproductY += 19;
        }
    }

    private Ingredient resolveIngredient(ResourceLocation rLoc) {
        return Ingredient.of(MATags.smartLookupItem(rLoc).stream().map(item -> {
            ItemStack stack = new ItemStack((ItemLike)item);
            if (stack.getItem() == Items.POTION && stack.getTag() == null) {
                stack = PotionUtils.setPotion((ItemStack)stack, (Potion)Potions.WATER);
            }
            return stack;
        }));
    }

    public void draw(ManaweavingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics pGuiGraphics, double mouseX, double mouseY) {
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
            GuiRenderUtils.renderByproducts(pGuiGraphics, this.xSize / 2 + 16, 78, recipe, false);
        }
    }
}

