 
package com.mna.gui.block;

import com.google.common.collect.Lists;
import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mna.blocks.tileentities.wizard_lab.MagiciansWorkbenchTile;
import com.mna.gui.GuiTextures;
import com.mna.gui.containers.block.ContainerMagiciansWorkbench;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import com.mna.gui.widgets.MNAImageButton;

public class GuiMagiciansWorkbench
extends AbstractContainerScreen<ContainerMagiciansWorkbench> {
    List<Component> currentTooltip;

    public GuiMagiciansWorkbench(ContainerMagiciansWorkbench p_i51105_1_, Inventory p_i51105_2_, Component p_i51105_3_) {
        super((AbstractContainerMenu)p_i51105_1_, p_i51105_2_, p_i51105_3_);
        this.imageWidth = 256;
        this.imageHeight = 202;
        this.currentTooltip = new ArrayList<Component>();
    }

    protected void init() {
        super.init();
        for (int i = 0; i < 8; ++i) {
            int index = i;
            this.addRenderableWidget(new IndexButton(this.leftPos + 10 + i % 2 * 26, this.topPos + 10 + 26 * Math.floorDiv(i, 2), 22, 22, 23 * i, 234, 0, GuiTextures.WizardLab.MAGICIANS_WORKBENCH, 256, 256, i, button -> {
                if (button instanceof IndexButton) {
                    IndexButton idxBtn = (IndexButton)button;
                    if (idxBtn.getClickButton() == 0) {
                        ((ContainerMagiciansWorkbench)this.menu).moveRecipeToCraftingGrid(index);
                    } else {
                        this.minecraft.gameMode.handleInventoryButtonClick(((ContainerMagiciansWorkbench)this.menu).containerId, index);
                    }
                }
            }));
        }
        this.addRenderableWidget(new MNAImageButton(this.leftPos + 185, this.topPos + 73, 7, 7, 184, 249, 0, GuiTextures.WizardLab.MAGICIANS_WORKBENCH, 256, 256, button -> ((ContainerMagiciansWorkbench)this.menu).tryClearGrid(true)));
        this.addRenderableWidget(new MNAImageButton(this.leftPos + 64, this.topPos + 73, 7, 7, 184, 249, 0, GuiTextures.WizardLab.MAGICIANS_WORKBENCH, 256, 256, button -> ((ContainerMagiciansWorkbench)this.menu).tryClearGrid(false)));
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTick) {
        this.currentTooltip.clear();
        super.render(pGuiGraphics, mouseX, mouseY, partialTick);
        if (this.currentTooltip.size() > 0) {
            pGuiGraphics.renderTooltip(this.font, Lists.transform(this.currentTooltip, Component::getVisualOrderText), mouseX, mouseY);
        } else {
            this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        this.renderBackground(pGuiGraphics);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(GuiTextures.WizardLab.MAGICIANS_WORKBENCH, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int p_230451_2_, int p_230451_3_) {
    }

    public class IndexButton
    extends MNAImageButton {
        int index;
        int clickButton;
        final ResourceLocation texture;

        public IndexButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int hoverOffset, ResourceLocation textureFile, int texWidth, int texHeight, int index, Button.OnPress clickHandler) {
            super(x, y, width, height, xTexStart, yTexStart, hoverOffset, textureFile, texWidth, texHeight, clickHandler, (Component)Component.literal((String)""));
            this.index = index;
            this.texture = textureFile;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(pGuiGraphics, mouseX, mouseY, partialTick);
            if (((ContainerMagiciansWorkbench)GuiMagiciansWorkbench.this.menu).getWorkbench().getRememberedRecipeItems().size() > this.index) {
                MagiciansWorkbenchTile.RememberedRecipe recipe = ((ContainerMagiciansWorkbench)GuiMagiciansWorkbench.this.menu).getWorkbench().getRememberedRecipeItems().get(this.index);
                ItemStack stack = recipe.getOutput(ManaAndArtifice.instance.proxy.getClientWorld());
                pGuiGraphics.renderItem(stack, this.getX() + 3, this.getY() + 3);
                if (mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight()) {
                    MutableComponent component = Component.translatable((String)stack.getDescriptionId());
                    if (((ContainerMagiciansWorkbench)GuiMagiciansWorkbench.this.menu).hasComponents(this.index) && !((ContainerMagiciansWorkbench)GuiMagiciansWorkbench.this.menu).isRecipeAlreadyInGrid(this.index) && ((ContainerMagiciansWorkbench)GuiMagiciansWorkbench.this.menu).gridIsFreeFor(this.index)) {
                        component.withStyle(ChatFormatting.GREEN);
                    } else {
                        component.withStyle(ChatFormatting.RED);
                    }
                    GuiMagiciansWorkbench.this.currentTooltip.add((Component)component);
                    GuiMagiciansWorkbench.this.currentTooltip.add((Component)Component.translatable((String)"block.mna.magicians_workbench.lock_prompt").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
                }
                if (recipe.isLocked()) {
                    pGuiGraphics.blit(this.texture, this.getX() + this.width - 4, this.getY() - 1, 192, 250, 5, 6);
                }
            }
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
        }

        protected boolean isValidClickButton(int button) {
            if (button == 0 || button == 1) {
                this.clickButton = button;
                return true;
            }
            return false;
        }

        public int getClickButton() {
            return this.clickButton;
        }
    }
}

