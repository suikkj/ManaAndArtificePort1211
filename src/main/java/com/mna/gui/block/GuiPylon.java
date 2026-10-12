 
package com.mna.gui.block;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.gui.ContainerPylon;
import com.mna.gui.GuiTextures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiPylon
extends AbstractContainerScreen<ContainerPylon> {
    protected ArrayList<Component> tooltip = new ArrayList();

    public GuiPylon(ContainerPylon container, Inventory inv, Component comp) {
        super((AbstractContainerMenu)container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 234;
        this.imageHeight = 155;
    }

    protected void init() {
        super.init();
        this.addRenderableWidget(new MNAImageButton(this.leftPos + 208, this.topPos + 24, 6, 3, 244, 250, 3, GuiTextures.WizardLab.PYLON, 256, 256, b -> this.minecraft.gameMode.handleInventoryButtonClick(((ContainerPylon)this.menu).containerId, 1)));
        this.addRenderableWidget(new MNAImageButton(this.leftPos + 208, this.topPos + 30, 6, 3, 238, 250, 3, GuiTextures.WizardLab.PYLON, 256, 256, b -> this.minecraft.gameMode.handleInventoryButtonClick(((ContainerPylon)this.menu).containerId, 2)));
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.tooltip.clear();
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        if (this.tooltip.size() > 0) {
            pGuiGraphics.renderTooltip(this.font, this.tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GuiTextures.WizardLab.PYLON, i, j, 0, 0, this.imageWidth, this.imageHeight);
        int powerX = 18;
        int powerY = 42;
        int powerXStep = 34;
        HashMap<Affinity, PowerStatus> powerRequirements = ((ContainerPylon)this.menu).powerRequirementStatus();
        for (Affinity aff : Affinity.CoreSix()) {
            pGuiGraphics.renderItem(GuiTextures.affinityIcons.get(aff), this.leftPos + powerX, this.topPos + powerY);
            this.renderPowerConsumeStatusIcon(pGuiGraphics, mouseX, mouseY, this.leftPos + powerX + 18, this.topPos + powerY + 4, aff, powerRequirements.getOrDefault(aff, PowerStatus.NOT_REQUESTING));
            powerX += powerXStep;
        }
        if (((ContainerPylon)this.menu).isRedstonePowered()) {
            pGuiGraphics.blit(GuiTextures.Blocks.LODESTAR_MAIN, this.leftPos + 155, this.topPos + 24, 0, 240, 8, 8);
            if (this.cursorIsWithin(mouseX, mouseY, 155, 24, 8, 8)) {
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.pylon.redstone_disabled").withStyle(ChatFormatting.GRAY));
            }
        } else {
            pGuiGraphics.blit(GuiTextures.Blocks.LODESTAR_MAIN, this.leftPos + 155, this.topPos + 24, 0, 248, 8, 8);
            if (this.cursorIsWithin(mouseX, mouseY, 155, 24, 8, 8)) {
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.pylon.active").withStyle(ChatFormatting.GRAY));
            }
        }
        if (((ContainerPylon)this.menu).affectFactionOnly()) {
            pGuiGraphics.blit(GuiTextures.WizardLab.PYLON, this.leftPos + 33, this.topPos + 24, 250, 250, 6, 6);
        }
        if (this.cursorIsWithin(mouseX, mouseY, 33, 23, 8, 8)) {
            this.tooltip.add((Component)Component.translatable((String)((ContainerPylon)this.menu).getFactionCheckboxTooltip()).withStyle(ChatFormatting.GRAY));
        }
        MutableComponent mutableComponent = Component.literal((String)String.format("%d", ((ContainerPylon)this.menu).getRadius()));
        Objects.requireNonNull(this.font);
        pGuiGraphics.drawCenteredString(this.font, (Component)mutableComponent, this.leftPos + 186, this.topPos + 20 + 9 / 2, 0xFFFFFF);
        if (this.cursorIsWithin(mouseX, mouseY, 167, 17, 38, 20)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.pylon.radius").withStyle(ChatFormatting.GRAY));
        }
        MutableComponent mutableComponent2 = Component.literal((String)String.format("%.1f", Float.valueOf(((ContainerPylon)this.menu).getMultiplier())));
        Objects.requireNonNull(this.font);
        pGuiGraphics.drawCenteredString(this.font, (Component)mutableComponent2, this.leftPos + 219, this.topPos + 77 + 9 / 2, 0xFFFFFF);
        if (this.cursorIsWithin(mouseX, mouseY, 210, 79, 19, 10)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.pylon.multiplier").withStyle(ChatFormatting.GRAY));
        }
        if (((ContainerPylon)this.menu).getErrorMessage() != null) {
            pGuiGraphics.blit(GuiTextures.Blocks.LODESTAR_MAIN, this.leftPos + 56, this.topPos + 21, 154, 244, 12, 12);
            if (this.cursorIsWithin(mouseX, mouseY, 55, 20, 14, 14)) {
                this.tooltip.add((Component)Component.translatable((String)((ContainerPylon)this.menu).getErrorMessage()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.cursorIsWithin((int)pMouseX, (int)pMouseY, 33, 23, 8, 8)) {
            this.minecraft.gameMode.handleInventoryButtonClick(((ContainerPylon)this.menu).containerId, 0);
            this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    protected void renderPowerConsumeStatusIcon(GuiGraphics pGuiGraphics, int mouseX, int mouseY, int x, int y, Affinity affinity, PowerStatus status) {
        float powerRequestRate = (float)Math.round(((ContainerPylon)this.menu).getPowerRequestRate(affinity) * 1000.0f) / 1000.0f;
        switch (status) {
            case NO_POWER: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 88.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.no_power", (Object[])new Object[]{affinity}).withStyle(ChatFormatting.RED).append((Component)Component.translatable((String)"gui.mna.wizard_lab.quantity", (Object[])new Object[]{Float.valueOf(powerRequestRate)}).withStyle(ChatFormatting.AQUA)));
                break;
            }
            case NOT_REQUESTING: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 96.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.not_requesting", (Object[])new Object[]{affinity}).withStyle(ChatFormatting.GRAY));
                break;
            }
            case SUPPLIED: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 104.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.supplied", (Object[])new Object[]{affinity, Float.valueOf(powerRequestRate)}).withStyle(ChatFormatting.GREEN).append((Component)Component.translatable((String)"gui.mna.wizard_lab.quantity", (Object[])new Object[]{Float.valueOf(powerRequestRate)}).withStyle(ChatFormatting.AQUA)));
                break;
            }
            case NO_CONDUIT: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 112.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.conduit_missing", (Object[])new Object[]{affinity, Float.valueOf(powerRequestRate)}).withStyle(ChatFormatting.RED));
                break;
            }
            case PARTIAL: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 120.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.supplied_partial", (Object[])new Object[]{affinity, Float.valueOf(powerRequestRate)}).withStyle(ChatFormatting.YELLOW).append((Component)Component.translatable((String)"gui.mna.wizard_lab.quantity", (Object[])new Object[]{Float.valueOf(powerRequestRate)}).withStyle(ChatFormatting.AQUA)));
            }
        }
    }

    protected boolean cursorIsWithin(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= this.leftPos + x && mouseX <= this.leftPos + x + w && mouseY >= this.topPos + y && mouseY <= this.topPos + y + h;
    }
}

