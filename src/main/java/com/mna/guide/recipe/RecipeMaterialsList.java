 
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.recipes.multiblock.MultiblockDefinition;
import com.mna.tools.render.MultiblockRenderer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class RecipeMaterialsList
extends RecipeRendererBase {
    private MultiblockDefinition _recipe;
    private List<List<ItemStack>> _ingredients;
    private static final int ITEMSTACK_WIDTH = 16;
    private boolean visualizeHovered = false;

    public RecipeMaterialsList(int xIn, int yIn) {
        super(xIn, yIn);
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this._recipe == null) {
            return;
        }
        if (this._ingredients == null) {
            this._ingredients = this._recipe.getItemsList((Level)this.minecraft.level);
        }
        if (this._ingredients == null) {
            return;
        }
        int rX = (int)(16.0f * this.scale);
        float f = (float)this.getY() / this.scale + 35.0f;
        Objects.requireNonNull(this.minecraft.font);
        int rY = (int)(f + 9.0f);
        int column_width = (int)(24.0f * this.scale);
        for (List<ItemStack> stacks : this._ingredients) {
            this.renderItemStack(pGuiGraphics, stacks, x + rX, rY, 1.0f);
            if (!((float)(rX = (int)((float)rX + (16.0f / this.scale + (float)column_width))) >= (float)this.width - 16.0f * this.scale - (float)column_width)) continue;
            rY += 20;
            rX = (int)(16.0f * this.scale);
        }
        int tier = this._recipe.getTier();
        MutableInt playerTier = new MutableInt(0);
        MNACap.get(this.minecraft.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
        int color = tier <= playerTier.getValue() ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
        MutableComponent name = Component.translatable((String)"mna:multiblock_recipe").append((Component)Component.translatable((String)this._recipe.getId().toString()));
        MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
        int tierWidth = this.minecraft.font.width((FormattedText)tierPrompt);
        int tierX = x + this.width / 2 - tierWidth / 2;
        pGuiGraphics.drawString(this.minecraft.font, (Component)name, x + this.width / 2 - this.minecraft.font.width((FormattedText)name) / 2, y + 10, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
        pGuiGraphics.drawString(this.minecraft.font, (Component)tierPrompt, tierX, y + 20, color, false);
        int n = x + this.width / 2;
        Objects.requireNonNull(this.minecraft.font);
        this.drawVisualizeButton(pGuiGraphics, n, y + 20 + 9, mouseX, mouseY);
    }

    private void drawVisualizeButton(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY) {
        Objects.requireNonNull(this.minecraft.font);
        int width = 9;
        Objects.requireNonNull(this.minecraft.font);
        int height = 9;
        pGuiGraphics.blit(GuiTextures.Widgets.VISUALIZE_MULTIBLOCK, x -= width / 2, y, 0.0f, 0.0f, width, height, width, height);
        this.visualizeHovered = false;
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            this.visualizeHovered = true;
            List<Component> toolTip = Arrays.asList(Component.translatable((String)"gui.mna:visualize_multiblock"));
            this.getTooltipFunction().accept(toolTip);
        }
    }

    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        if (this.visualizeHovered) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            MultiblockRenderer.setMultiblock(this._recipe, (Component)Component.translatable((String)this._recipe.getId().toString()), false);
            this.minecraft.setScreen(null);
            return true;
        }
        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return null;
    }

    @Override
    protected void init_internal(ResourceLocation recipeLocation) {
        Optional pattern = this.minecraft.level.getRecipeManager().byKey(recipeLocation);
        if (pattern.isPresent() && pattern.get() instanceof MultiblockDefinition) {
            this._recipe = (MultiblockDefinition)pattern.get();
        }
    }

    @Override
    public int getTier() {
        return 0;
    }
}

