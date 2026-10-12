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
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package com.mna.interop.jei.categories;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.interop.jei.MARecipeTypes;
import com.mna.recipes.RecipeByproduct;
import com.mna.recipes.eldrin.EldrinAltarRecipe;
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
import org.apache.commons.lang3.mutable.MutableInt;

public class EldrinAltarRecipeCategory
implements IRecipeCategory<EldrinAltarRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;
    private int xSize = 152;
    private int ySize = 177;

    public EldrinAltarRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(GuiTextures.Jei.ELDRIN, 0, 0, this.xSize, this.ySize).setTextureSize(152, 177).build();
        this.localizedName = I18n.get((String)"gui.mna.jei.eldrin_altar", (Object[])new Object[0]);
        this.icon = guiHelper.createDrawableIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)new ItemStack((ItemLike)BlockInit.ELDRIN_ALTAR.get()));
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

    public void setRecipe(IRecipeLayoutBuilder builder, EldrinAltarRecipe recipe, IFocusGroup focuses) {
        int cX = this.xSize / 2 - 14;
        int cY = this.ySize / 2 + 15;
        float radians = -1.5707964f;
        float radianStep = 0.7853982f;
        int ingredDist = 44;
        builder.setShapeless();
        int count = 0;
        for (ResourceLocation rLoc : recipe.getRequiredItems()) {
            int ingredX = cX + (int)Math.round(Math.cos(radians) * (double)ingredDist);
            int ingredY = cY + (int)Math.round(Math.sin(radians) * (double)ingredDist);
            if (count++ == 0) {
                ingredX = cX;
                ingredY = cY;
            }
            if (count == 2 || count == 4) {
                ingredX += 4;
            } else if (count == 6 || count == 8) {
                ingredX -= 4;
            } else if (count == 5) {
                ++ingredY;
            } else if (count == 9) {
                --ingredY;
            }
            builder.addSlot(RecipeIngredientRole.INPUT, ingredX - 8, ingredY).addIngredients(Ingredient.of(MATags.smartLookupItem(rLoc).stream().map(item -> {
                ItemStack stack = new ItemStack((ItemLike)item);
                if (stack.getItem() == Items.POTION && stack.getTag() == null) {
                    stack = PotionUtils.setPotion((ItemStack)stack, (Potion)Potions.WATER);
                }
                return stack;
            })));
            radians += radianStep;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 54, 29).addIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)recipe.getResultItem());
        int byproductX = this.xSize / 2 + 5;
        int byproductY = 15;
        int byproductCount = Math.min(recipe.getByproducts().size(), 2);
        for (int i = 0; i < byproductCount; ++i) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, byproductX, byproductY).addIngredients(Ingredient.of(((RecipeByproduct)recipe.getByproducts().get(i)).getItems().stream()));
            byproductY += 19;
        }
    }

    public void draw(EldrinAltarRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics pGuiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            int playerTier;
            int tier = recipe.getTier();
            int color = tier <= (playerTier = ((IPlayerProgression)mc.player.getCapability(PlayerProgressionProvider.PROGRESSION).resolve().get()).getTier()) ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
            MutableComponent name = Component.translatable((String)recipe.getResultItem().getDescriptionId().toString());
            MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier-short", (Object[])new Object[]{tier});
            int textY = 5;
            pGuiGraphics.drawString(mc.font, (Component)name, 0, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
            pGuiGraphics.drawString(mc.font, (Component)tierPrompt, 0, 15, color, false);
            GuiRenderUtils.renderFactionIcon(pGuiGraphics, recipe.getFactionRequirement(), this.xSize - 19, 16);
            GuiRenderUtils.renderByproducts(pGuiGraphics, this.xSize / 2 + 1, 13, recipe, true);
            int affStep = 24;
            int affX = this.xSize - 27;
            int affY = (Boolean)mc.options.forceUnicodeFont.get() != false ? 22 : 24;
            MutableInt count = new MutableInt(0);
            recipe.getPowerRequirements().forEach((a, v) -> {
                int rY = affY + count.getAndIncrement() * affStep;
                String aText = a.name();
                float scale = (Boolean)mc.options.forceUnicodeFont.get() != false ? 1.0f : 0.7f;
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().scale(scale, scale, scale);
                pGuiGraphics.drawString(mc.font, aText, (int)((float)affX / scale), (int)((float)(rY + 6) / scale), FastColor.ARGB32.color((int)255, (int)a.getColor()[0], (int)a.getColor()[1], (int)a.getColor()[2]), false);
                String vText = String.format("%.0f", v);
                pGuiGraphics.drawString(mc.font, vText, (int)((float)affX / scale), (int)((float)(rY + 16) / scale), FastColor.ARGB32.color((int)255, (int)a.getColor()[0], (int)a.getColor()[1], (int)a.getColor()[2]), false);
                pGuiGraphics.pose().popPose();
            });
            RenderSystem.enableBlend();
        }
    }

    public RecipeType<EldrinAltarRecipe> getRecipeType() {
        return MARecipeTypes.ELDRIN_ALTAR;
    }
}

