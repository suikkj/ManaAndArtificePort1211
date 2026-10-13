 
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.recipes.runeforging.RuneforgingRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class RecipeRunesmith
extends RecipeRendererBase {
    private RuneforgingRecipe pattern;
    static final int POINT_RENDER_SIZE = 13;
    private ItemStack _metal;
    private ItemStack _pattern;
    private ItemStack _output;

    public RecipeRunesmith(int x, int y) {
        super(x, y);
    }

    @Override
    public void init_internal(ResourceLocation recipeLocation) {
        Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.minecraft.level.getRecipeManager().byKey(recipeLocation);
        if (pattern.isPresent() && pattern.get().value() instanceof RuneforgingRecipe) {
            this.pattern = (RuneforgingRecipe)pattern.get().value();
            this._metal = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(this.pattern.getMaterial()));
            this._pattern = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(this.pattern.getPatternResource()));
            this._output = this.pattern.getResultItem();
        } else {
            this.pattern = null;
        }
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return GuiTextures.Recipe.RUNESMITHING;
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.pattern == null) {
            return;
        }
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)GuiTextures.Widgets.GUIDE_WIDGETS);
        this.renderItemStack(pGuiGraphics, this._pattern, (int)((float)this.getX() / this.scale + 51.0f), (int)((float)this.getY() / this.scale + 108.0f), 1.0f);
        this.renderItemStack(pGuiGraphics, this._metal, (int)((float)this.getX() / this.scale + 51.0f), (int)((float)this.getY() / this.scale + 64.0f), 1.0f);
        this.renderItemStack(pGuiGraphics, this._output, (int)((float)this.getX() / this.scale) + 180, (int)((float)this.getY() / this.scale + 90.0f), 1.0f);
        String hits = String.format("x %d", this.pattern.getHits());
        pGuiGraphics.drawString(this.minecraft.font, hits, x + 123 - this.minecraft.font.width(hits) / 2, y + 100, -12105913, false);
        if (this._output.getCount() > 1) {
            String count = String.format("x %d", this._output.getCount());
            pGuiGraphics.drawString(this.minecraft.font, count, x + 186 - this.minecraft.font.width(count) / 2, y + 110, -12105913, false);
        }
        this.renderByproducts(pGuiGraphics, this.getWidth() - 55, this.getHeight() / 2 - 5, this.pattern);
        int tier = this.pattern.getTier();
        MutableInt playerTier = new MutableInt(0);
        MNACap.get(this.minecraft.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
        int color = tier <= playerTier.getValue() ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
        MutableComponent name = Component.translatable((String)this.pattern.getResultItem().getDescriptionId().toString());
        MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
        int stringWidth = this.minecraft.font.width((FormattedText)name);
        int textX = x + this.width / 2 - stringWidth / 2;
        int textY = y + 5;
        pGuiGraphics.drawString(this.minecraft.font, (Component)name, textX, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
        pGuiGraphics.drawString(this.minecraft.font, (Component)tierPrompt, x + this.width / 2 - this.minecraft.font.width((FormattedText)tierPrompt) / 2, y + 15, color, false);
        if (this.pattern.getFactionRequirement() != null) {
            int xPadding = 3;
            this.renderFactionIcon(pGuiGraphics, this.pattern.getFactionRequirement(), textX + stringWidth + xPadding, textY);
        }
    }

    @Override
    public int getTier() {
        return this.pattern != null ? this.pattern.getTier() : 1;
    }
}


