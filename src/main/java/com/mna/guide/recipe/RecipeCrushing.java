 
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.recipes.crush.CrushingRecipe;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class RecipeCrushing
extends RecipeRendererBase {
    private CrushingRecipe recipe;
    private List<ItemStack> inputs;
    private List<ItemStack> outputs;

    public RecipeCrushing(int xIn, int yIn) {
        super(xIn, yIn);
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.recipe == null || this.inputs == null || this.outputs == null) {
            return;
        }
        if (this.inputs != null && this.inputs.size() > 0) {
            this.renderItemStack(pGuiGraphics, this.inputs, (int)((float)this.getX() / this.scale + 27.0f), (int)((float)this.getY() / this.scale + 69.0f), 2.0f);
        }
        if (this.outputs != null && this.outputs.size() > 0) {
            this.renderItemStack(pGuiGraphics, this.outputs, (int)((float)this.getX() / this.scale + 159.0f), (int)((float)this.getY() / this.scale + 69.0f), 2.0f);
        }
        this.renderByproducts(pGuiGraphics, this.getWidth() / 2 - 23, 40, this.recipe);
        int tier = this.recipe.getTier();
        MutableInt playerTier = new MutableInt(0);
        MNACap.get(this.minecraft.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
        int color = tier <= playerTier.getValue() ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
        MutableComponent name = Component.translatable((String)this.recipe.getResultItem().getDescriptionId().toString());
        MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
        int stringWidth = this.minecraft.font.width((FormattedText)name);
        int textX = x + this.width / 2 - stringWidth / 2;
        int textY = y + 5;
        pGuiGraphics.drawString(this.minecraft.font, (Component)name, textX, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
        pGuiGraphics.drawString(this.minecraft.font, (Component)tierPrompt, x + this.width / 2 - this.minecraft.font.width((FormattedText)tierPrompt) / 2, y + 15, color, false);
        if (this.recipe.getFactionRequirement() != null) {
            int xPadding = 3;
            this.renderFactionIcon(pGuiGraphics, this.recipe.getFactionRequirement(), textX + stringWidth + xPadding, textY);
        }
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return GuiTextures.Recipe.CRUSHING;
    }

    @Override
    public void init_internal(ResourceLocation recipeLocation) {
        Optional pattern = this.minecraft.level.getRecipeManager().byKey(recipeLocation);
        if (pattern.isPresent() && pattern.get() instanceof CrushingRecipe) {
            this.recipe = (CrushingRecipe)pattern.get();
            if (this.recipe != null) {
                this.inputs = MATags.smartLookupItem(this.recipe.getInputResource()).stream().map(i -> new ItemStack((ItemLike)i)).collect(Collectors.toList());
                this.outputs = MATags.smartLookupItem(this.recipe.getOutputResource()).stream().map(i -> new ItemStack((ItemLike)i)).collect(Collectors.toList());
            }
        }
    }

    @Override
    public int getTier() {
        return this.recipe != null ? this.recipe.getTier() : 1;
    }
}

