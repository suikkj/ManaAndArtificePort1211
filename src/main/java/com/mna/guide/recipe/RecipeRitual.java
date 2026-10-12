 
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.api.rituals.IRitualReagent;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.recipes.rituals.RitualRecipe;
import com.mna.recipes.rituals.RitualRecipeHelper;
import com.mna.tools.render.GuiRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class RecipeRitual
extends RecipeRendererBase {
    private RitualRecipe pattern;
    static final int POINT_RENDER_SIZE = 13;

    public RecipeRitual(int x, int y) {
        super(x, y);
    }

    @Override
    public void init_internal(ResourceLocation recipeLocation) {
        this.pattern = RitualRecipeHelper.GetRitualRecipe((Level)this.minecraft.level, recipeLocation);
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return null;
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.pattern == null) {
            return;
        }
        int startX = x + this.width - 54;
        int startY = y + 60;
        int pointSize = (this.width - 80) / this.pattern.getPattern().length;
        int centerOffset = (pointSize - 13) / 2;
        int[][] pData = this.pattern.getPattern();
        IRitualReagent[][] reagents = this.pattern.getReagents();
        for (int i = 0; i < pData.length; ++i) {
            for (int j = 0; j < pData[i].length; ++j) {
                if (pData[i][j] == 0) continue;
                pGuiGraphics.blit(GuiTextures.Widgets.GUIDE_WIDGETS, startX - j * pointSize - centerOffset, startY + i * pointSize + centerOffset, 13, 0, 13, 13);
            }
        }
        RenderSystem.enableDepthTest();
        int gridWidth = pData.length * pointSize;
        int gridTop = startY + 1;
        int gridLeft = startX + pointSize - gridWidth - centerOffset - (pointSize - 13) / 2 + 1;
        for (int i = 0; i <= pData.length; ++i) {
            GuiRenderUtils.line2d(pGuiGraphics, gridLeft, gridTop + pointSize * i, gridLeft + gridWidth, gridTop + pointSize * i, 0.0f, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255));
            GuiRenderUtils.line2d(pGuiGraphics, gridLeft + pointSize * i, gridTop, gridLeft + pointSize * i, gridTop + gridWidth, 0.0f, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255));
        }
        int stackStartX = (int)((float)this.getX() / this.scale + (float)this.width - 54.0f);
        int stackStartY = (int)((float)this.getY() / this.scale + 63.0f);
        int stackPointSize = (this.width - 80) / this.pattern.getPattern().length;
        int stackCenterOffset = (pointSize - 13) / 2;
        for (int i = 0; i < reagents.length; ++i) {
            for (int j = 0; j < reagents[i].length; ++j) {
                if (reagents[i][j] == null || reagents[i][j].isDynamic()) continue;
                this.renderItemStack(pGuiGraphics, MATags.smartLookupItem(reagents[i][j].getResourceLocation()).stream().map(item -> new ItemStack((ItemLike)item)).collect(Collectors.toList()), stackStartX - (int)Math.floor(1.0f * this.scale) - j * stackPointSize - stackCenterOffset, stackStartY - (int)Math.ceil(3.0f * this.scale) + i * stackPointSize + stackCenterOffset, 1.0f);
            }
        }
        int tier = this.pattern.getTier();
        MutableInt playerTier = new MutableInt(0);
        MNACap.get(this.minecraft.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
        int color = tier <= playerTier.getValue() ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
        MutableComponent name = Component.translatable((String)this.pattern.getId().toString());
        MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
        String patternSize = String.format("%dx%d", this.pattern.getPattern().length, this.pattern.getPattern().length);
        int stringWidth = this.minecraft.font.width((FormattedText)name);
        int textX = x + this.width / 2 - stringWidth / 2;
        int textY = y + 5;
        pGuiGraphics.drawString(this.minecraft.font, (Component)name, textX, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
        pGuiGraphics.drawString(this.minecraft.font, (Component)tierPrompt, x + this.width / 2 - this.minecraft.font.width((FormattedText)tierPrompt) / 2, y + 15, color, false);
        pGuiGraphics.drawString(this.minecraft.font, patternSize, x + this.width / 2 - this.minecraft.font.width(patternSize) / 2, y + 35, 0x404040, false);
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

