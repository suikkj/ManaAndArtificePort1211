 
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.recipes.runeforging.RunescribingRecipe;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class RecipeRunescribing
extends RecipeRendererBase {
    private RunescribingRecipe pattern;
    static final int POINT_RENDER_SIZE = 13;
    private boolean disableLabels = false;
    private boolean disableOutputRender = false;

    public RecipeRunescribing(int x, int y) {
        super(x, y);
    }

    @Override
    public void init_internal(ResourceLocation recipeLocation) {
        Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.minecraft.level.getRecipeManager().byKey(recipeLocation);
        this.pattern = pattern.isPresent() && pattern.get().value() instanceof RunescribingRecipe ? (RunescribingRecipe)pattern.get().value() : null;
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return GuiTextures.Recipe.RUNESCRIBING;
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.pattern == null) {
            return;
        }
        float scaleX = 1.5f;
        float scaleY = 1.5f;
        int startX = (int)((float)(x + 37) / scaleX);
        int startY = (int)((float)(y + 110) / scaleY);
        int grid = 12;
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(scaleX, scaleY, 1.0f);
        int count = 0;
        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 8; ++j) {
                if ((this.pattern.getHMutex() & 1L << count) != 0L) {
                    pGuiGraphics.blit(GuiTextures.Widgets.GUIDE_WIDGETS, startX + j * grid, startY + i * grid, 26, 0, grid, 1);
                }
                if ((this.pattern.getVMutex() & 1L << count) != 0L) {
                    pGuiGraphics.blit(GuiTextures.Widgets.GUIDE_WIDGETS, startX + (i + 1) * grid, startY + (j - 1) * grid, 26, 0, 1, grid);
                }
                ++count;
            }
        }
        pGuiGraphics.pose().popPose();
        if (!this.disableOutputRender) {
            ItemStack output = this.pattern.getResultItem();
            this.renderItemStack(pGuiGraphics, output, (int)((float)this.getX() / this.scale + (float)(this.width / 2 - 8)), (int)((float)this.getY() / this.scale + 57.0f), 1.0f);
        }
        if (!this.disableLabels) {
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
    }

    @Override
    public int getTier() {
        return this.pattern != null ? this.pattern.getTier() : 1;
    }

    public String getOutputTranslationKey() {
        if (this.pattern == null) {
            return "";
        }
        return this.pattern.getResultItem().getDescriptionId().toString();
    }

    public void disableLabels() {
        this.disableLabels = true;
    }

    public void disableOutputRender() {
        this.disableOutputRender = true;
    }
}

