 
package com.mna.api.guidebook;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.faction.IFaction;
import com.mna.gui.GuiTextures;
import com.mna.guide.recipe.ICyclingRecipeRenderer;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeByproduct;
import com.mna.tools.render.GuiRenderUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionContents;

public abstract class RecipeRendererBase
extends AbstractWidget {
    private static final int CYCLE_TICKS = 40;
    protected Minecraft minecraft = Minecraft.getInstance();
    private ResourceLocation[] rLocs = new ResourceLocation[0];
    private boolean initialized = false;
    private ResourceLocation registryName;
    private int curMouseX = 0;
    private int curMouseY = 0;
    private static final int ITEMSTACK_WIDTH = 16;
    private Consumer<List<Component>> tooltipFunction;
    public int lockIndex = -1;
    protected float scale = 1.0f;
    private boolean disablePaperBackground = false;

    public RecipeRendererBase(int xIn, int yIn) {
        this(xIn, yIn, 218, 256);
    }

    public RecipeRendererBase(int xIn, int yIn, int xSize, int ySize) {
        super(xIn, yIn, xSize, ySize, (Component)Component.literal((String)""));
    }

    public final ResourceLocation getRegistryName() {
        return this.registryName;
    }

    public final ResourceLocation[] getRecipeIds() {
        return this.rLocs;
    }

    public final void setTooltipFunction(Consumer<List<Component>> tooltipFunction) {
        this.tooltipFunction = tooltipFunction;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(this.scale, this.scale, this.scale);
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int i = (int)((float)this.getX() / this.scale);
        int j = (int)((float)this.getY() / this.scale);
        if (!this.disablePaperBackground) {
            pGuiGraphics.blit(GuiTextures.Items.SPELL_RECIPE, i + 3, j, 0.0f, 0.0f, this.width, this.height, 256, 256);
        }
        if (this.backgroundTexture() != null) {
            pGuiGraphics.blit(this.backgroundTexture(), i, j, 0.0f, 0.0f, this.width, this.height, 218, 256);
        }
        this.curMouseX = (int)((float)mouseX / this.scale);
        this.curMouseY = (int)((float)mouseY / this.scale);
        this.drawForeground(pGuiGraphics, i, j, this.curMouseX, this.curMouseY, partialTicks);
        pGuiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    protected boolean isValidClickButton(int p_isValidClickButton_1_) {
        return false;
    }

    protected abstract void drawForeground(GuiGraphics var1, int var2, int var3, int var4, int var5, float var6);

    protected final void renderItemStack(GuiGraphics pGuiGraphics, ItemStack stackToRender, int x, int y) {
        this.renderItemStack(pGuiGraphics, Arrays.asList(stackToRender), x, y, 1.0f);
    }

    protected final void renderItemStack(GuiGraphics pGuiGraphics, ItemStack stackToRender, int x, int y, float scale) {
        this.renderItemStack(pGuiGraphics, Arrays.asList(stackToRender), x, y, scale);
    }

    protected final void renderItemStack(GuiGraphics pGuiGraphics, List<ItemStack> stackToRender, int x, int y) {
        this.renderItemStack(pGuiGraphics, stackToRender, x, y, 1.0f);
    }

    protected final void renderItemStack(GuiGraphics pGuiGraphics, List<ItemStack> stackToRender, int x, int y, float scale) {
        this.renderItemStack(pGuiGraphics, stackToRender, x, y, 0.0f, 0.0f, scale, Arrays.asList(new Component[0]));
    }

    protected final void renderItemStack(GuiGraphics pGuiGraphics, ItemStack stackToRender, int x, int y, float nudgeX, float nudgeY, float scale, List<Component> additionalTooltip) {
        this.renderItemStack(pGuiGraphics, Arrays.asList(stackToRender), x, y, nudgeX, nudgeY, scale, additionalTooltip);
    }

    protected final void renderItemStack(GuiGraphics pGuiGraphics, List<ItemStack> stackToRender, int x, int y, float nudgeX, float nudgeY, float scale, List<Component> additionalTooltip) {
        if (stackToRender == null || stackToRender.size() == 0) {
            return;
        }
        ItemStack stack = this.getCurrentIndex(stackToRender);
        if (stack.getItem() == Items.POTION && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() == null) {
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        }
        if (stack.getCount() > 1) {
            float oX = 8.0f / scale;
            Font font = this.minecraft.font;
            String string = "x" + stack.getCount();
            float f = (float)x + (16.0f + nudgeX + oX) * scale;
            float f2 = (float)y + (16.0f + nudgeY) * scale;
            Objects.requireNonNull(this.minecraft.font);
            pGuiGraphics.drawString(font, string, f, f2 - 9.0f, 0x404040, false);
        }
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(scale, scale, scale);
        pGuiGraphics.pose().translate(nudgeX, nudgeY, 0.0f);
        pGuiGraphics.renderItem(stack, (int)((float)x / scale), (int)((float)y / scale));
        pGuiGraphics.pose().popPose();
        int adjustedX = (int)((float)x + 16.0f * scale);
        int adjustedY = (int)((float)y + 16.0f * scale);
        if (this.tooltipFunction != null && adjustedX >= this.curMouseX && (float)adjustedX <= (float)this.curMouseX + 16.0f * scale && adjustedY >= this.curMouseY && (float)adjustedY <= (float)this.curMouseY + 16.0f * scale) {
            List toolTip = stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, null, (TooltipFlag)TooltipFlag.Default.NORMAL);
            ArrayList<Component> tt = new ArrayList<Component>();
            tt.addAll(toolTip);
            tt.addAll(additionalTooltip);
            this.tooltipFunction.accept(tt);
        }
    }

    protected final void renderByproducts(GuiGraphics pGuiGraphics, int posX, int posY, AMRecipeBase recipe) {
        this.renderByproducts(pGuiGraphics, posX, posY, recipe, false);
    }

    protected final void renderByproducts(GuiGraphics pGuiGraphics, int posX, int posY, AMRecipeBase recipe, boolean plusOnLeft) {
        if (recipe.getByproducts().size() == 0) {
            return;
        }
        RenderSystem.enableBlend();
        int x = (int)((float)this.getX() / this.scale + (float)posX);
        int y = (int)((float)this.getY() / this.scale + (float)posY);
        GuiRenderUtils.renderByproducts(pGuiGraphics, x, y, recipe, plusOnLeft);
        int step = 19;
        int byproductsToRender = Math.min(recipe.getByproducts().size(), 2);
        for (int i = 0; i < byproductsToRender; ++i) {
            RecipeByproduct byproduct = (RecipeByproduct)recipe.getByproducts().get(i);
            this.renderItemStack(pGuiGraphics, byproduct.getItems(), x + 4, y + 2);
            y += 19;
        }
        RenderSystem.disableBlend();
    }

    private ItemStack getCurrentIndex(List<ItemStack> stacks) {
        if (stacks.size() == 1) {
            return stacks.get(0);
        }
        return stacks.get((int)(this.minecraft.level.getGameTime() / 40L % (long)stacks.size()));
    }

    protected abstract ResourceLocation backgroundTexture();

    protected abstract void init_internal(ResourceLocation var1);

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void disablePaperBackground() {
        this.disablePaperBackground = true;
    }

    public void init(ResourceLocation[] recipeLocations) {
        if (this.initialized) {
            return;
        }
        this.registryName = recipeLocations[0];
        this.rLocs = recipeLocations;
        if (this instanceof ICyclingRecipeRenderer) {
            ((ICyclingRecipeRenderer)(this)).init_cycling(this.rLocs);
        } else {
            this.init_internal(this.rLocs[0]);
        }
    }

    public RecipeRendererBase clone(int x, int y, boolean lock_index) {
        Class<?> clazz = ((this)).getClass();
        try {
            RecipeRendererBase inst = (RecipeRendererBase)(clazz.getDeclaredConstructor(Integer.TYPE, Integer.TYPE).newInstance(x, y));
            inst.init(this.rLocs);
            if (lock_index && this instanceof ICyclingRecipeRenderer) {
                inst.lockIndex = ((ICyclingRecipeRenderer)(this)).getIndex();
            }
            return inst;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof RecipeRendererBase) {
            RecipeRendererBase other = (RecipeRendererBase)(obj);
            if (this.rLocs.length != other.rLocs.length) {
                return false;
            }
            for (int i = 0; i < this.rLocs.length; ++i) {
                if (this.rLocs[i].equals(other.rLocs[i])) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public abstract int getTier();

    public void renderFactionIcon(GuiGraphics pGuiGraphics, IFaction faction, int x, int y) {
        ManaAndArtificeMod.getGuiRenderHelper().renderFactionIcon(pGuiGraphics, faction, x, y);
    }

    public Consumer<List<Component>> getTooltipFunction() {
        return this.tooltipFunction;
    }

    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}

