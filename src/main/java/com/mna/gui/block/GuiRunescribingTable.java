 
package com.mna.gui.block;

import com.mna.ManaAndArtifice;
import com.mna.api.config.ClientConfigValues;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.sound.SFX;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.gui.containers.block.ContainerRunescribingTable;
import com.mna.guide.recipe.RecipeRunescribing;
import com.mna.items.ItemInit;
import com.mna.items.runes.RunescribingRecipePaper;
import com.mna.recipes.runeforging.RunescribingRecipe;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class GuiRunescribingTable
extends AbstractContainerScreen<ContainerRunescribingTable> {
    public static final int LEFT_PANEL_WIDTH = 198;
    public static final int RIGHT_PANEL_WIDTH = 111;
    public static final int PANEL_PADDING = 5;
    public static final int PANEL_HEIGHT = 238;
    long mutex_h = 0L;
    long mutex_v = 0L;
    RecipeRunescribing recipe;
    boolean hasRequiredItems;

    public GuiRunescribingTable(ContainerRunescribingTable screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 314;
        this.imageHeight = 256;
        this.recipe = new RecipeRunescribing(324, 70);
    }

    protected void init() {
        super.init();
        this.reinitializeButtons();
    }

    private void reinitializeButtons() {
        this.clearWidgets();
        this.mutex_h = ((ContainerRunescribingTable)this.menu).getHMutex();
        this.mutex_v = ((ContainerRunescribingTable)this.menu).getVMutex();
        if (!((ContainerRunescribingTable)this.menu).hasRequiredItems()) {
            return;
        }
        long count = 0L;
        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 8; ++j) {
                long loopCount = count;
                this.addRenderableWidget(new ChiselImageButton(this.leftPos + 49 + i * 16, this.topPos + 13 + j * 16, 4, 16, 215, 224, 16, GuiTextures.WizardLab.RUNESCRIBING_TABLE, 256, 256, (this.mutex_v & 1L << (int)loopCount) != 0L, button -> {
                    boolean value = ((ChiselImageButton)button).getValue();
                    if (value && !((ContainerRunescribingTable)this.menu).canUndo(ManaAndArtifice.instance.proxy.getClientPlayer())) {
                        return;
                    }
                    ((ChiselImageButton)button).toggle();
                    this.mutex_v = !value ? (this.mutex_v |= 1L << (int)loopCount) : (this.mutex_v &= 1L << (int)loopCount ^ 0xFFFFFFFFFFFFFFFFL);
                    this.onMutexChanged((Player)this.minecraft.player, value);
                }));
                this.addRenderableWidget(new ChiselImageButton(this.leftPos + 36 + j * 16, this.topPos + 27 + i * 16, 16, 4, 224, 211, 4, GuiTextures.WizardLab.RUNESCRIBING_TABLE, 256, 256, (this.mutex_h & 1L << (int)loopCount) != 0L, button -> {
                    boolean value = ((ChiselImageButton)button).getValue();
                    if (value && !((ContainerRunescribingTable)this.menu).canUndo(ManaAndArtifice.instance.proxy.getClientPlayer())) {
                        return;
                    }
                    ((ChiselImageButton)button).toggle();
                    this.mutex_h = !value ? (this.mutex_h |= 1L << (int)loopCount) : (this.mutex_h &= 1L << (int)loopCount ^ 0xFFFFFFFFFFFFFFFFL);
                    this.onMutexChanged((Player)this.minecraft.player, value);
                }));
                ++count;
            }
        }
        this.setGuideRecipe();
    }

    private void setGuideRecipe() {
        RunescribingRecipe r;
        if (this.recipe == null) {
            return;
        }
        ItemStack stack = ((ContainerRunescribingTable)this.menu).getSlot(3).getItem();
        if (!stack.isEmpty() && (r = ((RunescribingRecipePaper)ItemInit.RECIPE_SCRAP_RUNESCRIBING.get()).getRecipe(stack, (Level)this.minecraft.level)) != null) {
            this.recipe.init(new ResourceLocation[]{r.getId()});
            this.recipe.disablePaperBackground();
            this.recipe.disableLabels();
            this.recipe.disableOutputRender();
            this.recipe.active = true;
            this.recipe.visible = true;
            return;
        }
        this.recipe.active = false;
        this.recipe.visible = false;
    }

    private void onMutexChanged(Player player, boolean isRemove) {
        if (player == null) {
            return;
        }
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> ((ContainerRunescribingTable)this.menu).writeMutex(this.mutex_h, this.mutex_v, player, p.getTier(), isRemove));
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        boolean requiredItems = ((ContainerRunescribingTable)this.menu).hasRequiredItems();
        if (!requiredItems && this.children().size() > 0 || requiredItems && !this.hasRequiredItems) {
            this.reinitializeButtons();
        }
        this.hasRequiredItems = requiredItems;
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        ItemStack xpBottleStack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
        pGuiGraphics.renderItem(xpBottleStack, 10, 79);
        pGuiGraphics.pose().pushPose();
        float scale = 0.479f;
        pGuiGraphics.pose().scale(scale, scale, scale);
        pGuiGraphics.pose().translate(108.0f, -90.0f, 0.0f);
        this.setGuideRecipe();
        if (this.recipe != null && this.recipe.active) {
            this.recipe.render(pGuiGraphics, mouseX, mouseY, 0.0f);
        }
        pGuiGraphics.pose().popPose();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        if (!((ContainerRunescribingTable)this.menu).hasPattern()) {
            return;
        }
        long count = 0L;
        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 8; ++j) {
                if ((this.mutex_h & 1L << (int)count) != 0L) {
                    pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, 34 + j * 16, 27 + i * 16, 224, 215, 16, 4);
                }
                if ((this.mutex_v & 1L << (int)count) != 0L) {
                    pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, 49 + i * 16, 13 + j * 16, 215, 240, 4, 16);
                }
                ++count;
            }
        }
        for (int x = 0; x < 9; ++x) {
            for (int y = 0; y < 9; ++y) {
                pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, 34 + x * 16, 12 + y * 16, 2, 2, 250.0f, 208.0f, 6, 6, 256, 256);
            }
        }
        ItemStack stack = ((ContainerRunescribingTable)this.menu).getSlot(3).getItem();
        if (!stack.isEmpty() && this.recipe != null) {
            int tier = this.recipe.getTier();
            MutableInt playerTier = new MutableInt(0);
            MNACap.get(this.minecraft.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
            int color = tier <= playerTier.getValue() ? FastColor.ARGB32.color((int)255, (int)0, (int)128, (int)0) : FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
            MutableComponent name = Component.translatable((String)this.recipe.getOutputTranslationKey());
            MutableComponent tierPrompt = Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier});
            Object nameContents = name.getString();
            scale = 0.65f;
            int nameLen = (int)((float)this.font.width((String)nameContents) * scale);
            boolean adjusted = false;
            while (nameLen > 70) {
                nameContents = ((String)nameContents).substring(1);
                nameLen = (int)((float)this.font.width((String)nameContents) * scale);
                adjusted = true;
            }
            if (adjusted) {
                nameContents = "..." + (String)nameContents;
                nameLen = (int)((float)this.font.width((String)nameContents) * scale);
            }
            int tierLen = (int)((float)this.font.width((FormattedText)tierPrompt) * scale);
            int textX = 219;
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().scale(scale, scale, scale);
            pGuiGraphics.drawString(this.font, (String)nameContents, (int)((float)(textX + 40 - nameLen / 2) / scale), (int)(115.5f / scale), FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255), false);
            pGuiGraphics.drawString(this.font, (Component)tierPrompt, (int)((float)(textX + 40 - tierLen / 2) / scale), (int)(125.5f / scale), color, false);
            pGuiGraphics.pose().popPose();
        }
        if (ClientConfigValues.ShowRunescribingRecipeMutex) {
            String s = String.format("H: %d", ((ContainerRunescribingTable)this.menu).getHMutex());
            s = String.format("V: %d", ((ContainerRunescribingTable)this.menu).getVMutex());
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, i, j, 0, 0, 198, 238);
        if (((ContainerRunescribingTable)this.menu).getSlot(0).getItem().isEmpty()) {
            pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, i + 8, j + 8, 240, 0, 16, 16);
        }
        if (((ContainerRunescribingTable)this.menu).getSlot(1).getItem().isEmpty()) {
            pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, i + 8, j + 32, 240, 17, 16, 16);
        }
        if (((ContainerRunescribingTable)this.menu).hasPattern()) {
            float scale = 3.815f;
            pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, i + 31, j + 9, (float)((int)(220.0f * scale)), (float)((int)(220.0f * scale)), (int)(36.0f * scale), (int)(36.0f * scale), (int)(256.0f * scale), (int)(256.0f * scale));
        } else {
            this.clearWidgets();
        }
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        float xpPct = GeneralConfigValues.ExperiencePerRunescribeUndo <= 0 ? 1.0f : MathUtils.clamp01(player.isCreative() ? 1.0f : (float)player.totalExperience / (float)GeneralConfigValues.ExperiencePerRunescribeUndo);
        pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_TABLE, this.leftPos + 9, this.topPos + 75, 236, 0, 3, (int)(20.0f * xpPct));
        pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_EXTRAS, i + 198 + 5, j, 0, 0, 111, 238);
        ItemStack stack = ((ContainerRunescribingTable)this.menu).getSlot(3).getItem();
        if (!stack.isEmpty()) {
            pGuiGraphics.blit(GuiTextures.WizardLab.RUNESCRIBING_EXTRAS, i + 198 + 5 + 17, j + 30, 178, 178, 78, 78);
        }
    }

    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slotIn, slotId, mouseButton, type);
        this.reinitializeButtons();
    }

    public class ChiselImageButton
    extends MNAImageButton {
        private int padding;
        private boolean isSet;

        public ChiselImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int p_i51135_9_, int p_i51135_10_, boolean initiallySet, Button.OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, p_i51135_9_, p_i51135_10_, onPressIn);
            this.padding = 2;
            this.isSet = false;
            this.isSet = initiallySet;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                boolean bl = this.isHovered = this.isMouseOver(mouseX, mouseY) || this.isSet;
                if (this.isHovered) {
                    super.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
                }
            }
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            if (this.width > this.height) {
                return this.active && this.visible && mouseX >= (double)this.getX() + (double)this.padding && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width - this.padding) && mouseY < (double)(this.getY() + this.height);
            }
            return this.active && this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() + (double)this.padding && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height - this.padding);
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.CHISEL, (float)1.0f));
        }

        public boolean getValue() {
            return this.isSet;
        }

        public void toggle() {
            this.isSet = !this.isSet;
        }
    }
}

