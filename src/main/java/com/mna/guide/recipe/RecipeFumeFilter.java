 
package com.mna.guide.recipe;

import com.mna.api.affinity.Affinity;
import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.guide.recipe.ICyclingRecipeRenderer;
import com.mna.recipes.eldrin.FumeFilterRecipe;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class RecipeFumeFilter
extends RecipeRendererBase
implements ICyclingRecipeRenderer<RecipeFumeFilter> {
    private FumeFilterRecipe[] patterns;

    public RecipeFumeFilter(int xIn, int yIn) {
        super(xIn, yIn);
    }

    @Override
    public void init_cycling(ResourceLocation[] rLocs) {
        ArrayList<FumeFilterRecipe> patternList = new ArrayList<FumeFilterRecipe>();
        for (ResourceLocation rLoc : rLocs) {
            Optional pattern = this.resolveRecipe(rLoc);
            if (!pattern.isPresent() || !(pattern.get() instanceof FumeFilterRecipe)) continue;
            patternList.add((FumeFilterRecipe)pattern.get());
        }
        this.patterns = patternList.toArray(new FumeFilterRecipe[0]);
    }

    @Override
    public void init_internal(ResourceLocation recipeLocation) {
        Optional pattern = this.resolveRecipe(recipeLocation);
        this.patterns = pattern.isPresent() && pattern.get() instanceof FumeFilterRecipe ? new FumeFilterRecipe[]{(FumeFilterRecipe)pattern.get()} : null;
    }

    private Optional<? extends Recipe<?>> resolveRecipe(ResourceLocation recipeLocation) {
        if (recipeLocation.getNamespace().equals("mna") && !recipeLocation.getPath().startsWith("fume_filter/")) {
            recipeLocation = ResourceLocation.fromNamespaceAndPath(recipeLocation.getNamespace(), "fume_filter/" + recipeLocation.getPath());
        }
        return (Optional)this.minecraft.level.getRecipeManager().byKey(recipeLocation);
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return GuiTextures.Recipe.FUME_FILTER;
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int index = this.getIndex();
        if (this.patterns.length == 0) {
            return;
        }
        FumeFilterRecipe pattern = this.patterns[index];
        if (pattern == null) {
            return;
        }
        int cX = this.getX() + (int)((float)(this.width / 2 - 77) * this.scale);
        int affY = this.getY() + (int)(82.0f * this.scale);
        Font fr = this.minecraft.font;
        ItemStack output = pattern.getGuiRepresentationStack();
        this.renderItemStack(pGuiGraphics, output, cX, this.getY() + (int)(88.0f * this.scale), this.scale);
        String vText = String.format("%.0f", Float.valueOf(pattern.getTotalGeneration() * 0.02f));
        int strLen = fr.width(vText);
        int affPosX = this.getX() + (int)((float)(this.width - 70 - strLen / 2) * this.scale);
        Affinity a = pattern.getAffinity();
        ItemStack guiStack = GuiTextures.affinityIcons.get(a);
        int rY = affY;
        this.renderItemStack(pGuiGraphics, guiStack, affPosX, rY - 4, this.scale);
        if (a == Affinity.WIND) {
            pGuiGraphics.drawString(fr, vText, (int)((float)(affPosX + 16) / this.scale), (int)((float)rY / this.scale), FastColor.ARGB32.color((int)255, (int)0, (int)0, (int)0), false);
        } else {
            pGuiGraphics.drawString(fr, vText, (int)((float)(affPosX + 16) / this.scale), (int)((float)rY / this.scale), FastColor.ARGB32.color((int)255, (int)a.getColor()[0], (int)a.getColor()[1], (int)a.getColor()[2]), false);
        }
        int tier = pattern.getTier();
        MutableInt playerTier = new MutableInt(0);
        MNACap.get(this.minecraft.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
        int color = tier <= playerTier.getValue() ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
        MutableComponent name = Component.translatable((String)"gui.mna.jei.eldrin_fume.generation");
        MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
        int stringWidth = this.minecraft.font.width((FormattedText)name);
        int textX = x + this.width / 2 - stringWidth / 2;
        int textY = y + 5;
        pGuiGraphics.drawString(fr, (Component)name, textX, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
        pGuiGraphics.drawString(fr, (Component)tierPrompt, x + this.width / 2 - this.minecraft.font.width((FormattedText)tierPrompt) / 2, y + 15, color, false);
        if (pattern.getFactionRequirement() != null) {
            int xPadding = 3;
            this.renderFactionIcon(pGuiGraphics, pattern.getFactionRequirement(), textX + stringWidth + xPadding, textY);
        }
    }

    @Override
    public int getTier() {
        if (this.patterns == null) {
            return 1;
        }
        int minTier = 5;
        for (FumeFilterRecipe pattern : this.patterns) {
            if (pattern.getTier() >= minTier) continue;
            minTier = pattern.getTier();
        }
        return minTier;
    }

    @Override
    public int countRecipes() {
        return this.patterns.length;
    }
}

