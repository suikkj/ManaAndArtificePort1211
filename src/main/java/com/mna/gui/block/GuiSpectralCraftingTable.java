 
package com.mna.gui.block;

import com.mna.gui.containers.block.ContainerSpectralCraftingTable;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.components.ImageButton;

public class GuiSpectralCraftingTable
extends AbstractContainerScreen<ContainerSpectralCraftingTable> {
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = ResourceLocation.parse("textures/gui/container/crafting_table.png");
    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = ResourceLocation.parse("textures/gui/recipe_button.png");
    private final RecipeBookComponent recipeBookGui = new RecipeBookComponent();
    private boolean widthTooNarrow;

    public GuiSpectralCraftingTable(ContainerSpectralCraftingTable screenContainer, Inventory inv, Component titleIn) {
        super((AbstractContainerMenu)screenContainer, inv, titleIn);
    }

    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookGui.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (RecipeBookMenu)this.menu);
        this.leftPos = this.recipeBookGui.updateScreenPosition(this.width, this.imageWidth);
        this.addWidget((GuiEventListener)this.recipeBookGui);
        this.setInitialFocus((GuiEventListener)this.recipeBookGui);
        this.addRenderableWidget(new MNAImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, button -> {
            this.recipeBookGui.initVisuals();
            this.recipeBookGui.toggleVisibility();
            this.leftPos = this.recipeBookGui.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton)button).setPosition(this.leftPos + 5, this.height / 2 - 49);
        }));
        this.titleLabelX = 29;
    }

    public void containerTick() {
        this.recipeBookGui.tick();
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
            this.renderBg(pGuiGraphics, partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookGui.render(pGuiGraphics, mouseX, mouseY, partialTicks);
            super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(pGuiGraphics, this.leftPos, this.topPos, true, partialTicks);
        }
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        this.recipeBookGui.renderTooltip(pGuiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int x, int y) {
        this.renderBackground(pGuiGraphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(CRAFTING_TABLE_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBookGui.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused((GuiEventListener)this.recipeBookGui);
            return true;
        }
        return this.widthTooNarrow && this.recipeBookGui.isVisible() ? true : super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        boolean flag = mouseX < (double)guiLeftIn || mouseY < (double)guiTopIn || mouseX >= (double)(guiLeftIn + this.imageWidth) || mouseY >= (double)(guiTopIn + this.imageHeight);
        return this.recipeBookGui.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && flag;
    }

    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slotIn, slotId, mouseButton, type);
        this.recipeBookGui.slotClicked(slotIn);
    }

    public void recipesUpdated() {
        this.recipeBookGui.recipesUpdated();
    }

    public RecipeBookComponent getRecipeGui() {
        return this.recipeBookGui;
    }
}

