 
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.network.ClientMessageDispatcher;
import com.mna.recipes.manaweaving.TransmutationRecipe;
import com.mna.tools.loot.LootDrop;
import com.mna.tools.loot.LootTableCache;
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

public class RecipeTransmute
extends RecipeRendererBase {
    private TransmutationRecipe recipe;
    private List<ItemStack> targetBlock;
    private List<ItemStack> replaceBlock;
    private List<LootDrop> drops;
    private boolean waitingForServer = false;
    private ResourceLocation lootTableID = null;

    public RecipeTransmute(int xIn, int yIn) {
        super(xIn, yIn);
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.recipe == null || this.targetBlock == null && this.drops == null) {
            return;
        }
        if (this.targetBlock != null && this.targetBlock.size() > 0) {
            this.renderItemStack(pGuiGraphics, this.targetBlock, (int)((float)this.getX() / this.scale + 33.0f), (int)((float)this.getY() / this.scale + 119.0f), 1.05f);
        }
        if (this.replaceBlock != null && this.replaceBlock.size() > 0) {
            this.renderReplaceBlock(pGuiGraphics);
        } else if (this.waitingForServer || this.drops != null && this.drops.size() > 0) {
            this.renderDrops(pGuiGraphics, x, y);
        }
        int tier = this.recipe.getTier();
        MutableInt playerTier = new MutableInt(0);
        MNACap.get(this.minecraft.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
        int color = tier <= playerTier.getValue() ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
        MutableComponent name = Component.translatable((String)this.recipe.getResultItem().getDescriptionId().toString());
        MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
        MutableComponent tooltipPrompt = Component.translatable((String)"cantrip.mna.transmute.jei");
        int stringWidth = this.minecraft.font.width((FormattedText)tooltipPrompt);
        int textX = x + this.width / 2 - stringWidth / 2;
        int textY = y + 25;
        pGuiGraphics.drawString(this.minecraft.font, (Component)tooltipPrompt, textX, textY, color, false);
        stringWidth = this.minecraft.font.width((FormattedText)name);
        textX = x + this.width / 2 - stringWidth / 2;
        textY = y + 5;
        pGuiGraphics.drawString(this.minecraft.font, (Component)name, textX, textY, FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
        pGuiGraphics.drawString(this.minecraft.font, (Component)tierPrompt, x + this.width / 2 - this.minecraft.font.width((FormattedText)tierPrompt) / 2, y + 15, color, false);
        if (this.recipe.getFactionRequirement() != null) {
            int xPadding = 3;
            this.renderFactionIcon(pGuiGraphics, this.recipe.getFactionRequirement(), textX + stringWidth + xPadding, textY);
        }
    }

    private void renderReplaceBlock(GuiGraphics pGuiGraphics) {
        this.renderItemStack(pGuiGraphics, this.replaceBlock, (int)((float)this.getX() / this.scale + 168.0f), (int)((float)this.getY() / this.scale + 119.0f), 1.05f);
    }

    private void renderDrops(GuiGraphics pGuiGraphics, int x, int y) {
        int xPos = 32;
        int yPos = 176;
        if (this.waitingForServer) {
            List<LootDrop> drops;
            MutableComponent c = Component.translatable((String)"gui.mna.waiting-for-server");
            pGuiGraphics.drawString(this.minecraft.font, (Component)c, (int)((float)this.getX() / this.scale + (float)(this.getWidth() / 2) - (float)(this.minecraft.font.width((FormattedText)c) / 2)), (int)((float)this.getY() / this.scale + (float)yPos + 3.0f), 0xFF0000, false);
            if (this.minecraft.level.getGameTime() % 20L == 0L && (drops = LootTableCache.getLoot(this.recipe.getLootTable())) != null) {
                this.drops = drops;
                this.waitingForServer = false;
            }
        } else {
            for (LootDrop drop : this.drops) {
                if (drop.minDrop == drop.maxDrop) {
                    this.renderItemStack(pGuiGraphics, drop.item, (int)((float)this.getX() / this.scale + (float)xPos), (int)((float)this.getY() / this.scale + (float)yPos), 0.0f, 0.0f, 1.05f, drop.getTooltipText());
                    String s = drop.toString();
                    int width = this.minecraft.font.width(s);
                    pGuiGraphics.drawString(this.minecraft.font, s, x + xPos - width / 2 + 10, y + yPos + 14, 0x404040, false);
                } else {
                    this.renderItemStack(pGuiGraphics, drop.item, (int)((float)this.getX() / this.scale + (float)xPos), (int)((float)this.getY() / this.scale + (float)yPos), 0.0f, 0.0f, 1.05f, drop.getTooltipText());
                    pGuiGraphics.drawString(this.minecraft.font, drop.toString(), (int)((float)this.getX() + (float)xPos * this.scale), (int)((float)this.getY() + (float)yPos * this.scale), 0x404040, false);
                }
                if ((xPos += 20) < 180) continue;
                xPos = 32;
                yPos += 24;
            }
        }
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return this.replaceBlock != null ? GuiTextures.Recipe.TRANSMUTATION_SINGLE : GuiTextures.Recipe.TRANSMUTATION_MULTI;
    }

    @Override
    public void init_internal(ResourceLocation recipeLocation) {
        Optional pattern = this.minecraft.level.getRecipeManager().byKey(recipeLocation);
        if (pattern.isPresent() && pattern.get() instanceof TransmutationRecipe) {
            this.recipe = (TransmutationRecipe)pattern.get();
            if (this.recipe != null) {
                this.targetBlock = MATags.smartLookupItem(this.recipe.getTargetBlock()).stream().map(i -> new ItemStack((ItemLike)i)).collect(Collectors.toList());
                if (this.recipe.getReplaceBlock() != null) {
                    this.replaceBlock = MATags.smartLookupItem(this.recipe.getReplaceBlock()).stream().map(i -> new ItemStack((ItemLike)i)).collect(Collectors.toList());
                } else if (this.recipe.getLootTable() != null) {
                    List<LootDrop> cached = LootTableCache.getLoot(this.recipe.getLootTable());
                    if (cached == null) {
                        this.waitingForServer = true;
                        ClientMessageDispatcher.requestLootTableDrops(this.recipe.getLootTable());
                    } else {
                        this.drops = cached;
                    }
                }
            }
        }
    }

    @Override
    public int getTier() {
        return this.recipe != null ? this.recipe.getTier() : 1;
    }
}

