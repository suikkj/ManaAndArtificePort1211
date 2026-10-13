 
package com.mna.gui.block;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.gui.GuiTextures;
import com.mna.gui.containers.block.SimpleWizardLabContainer;
import com.mna.network.ClientMessageDispatcher;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.components.ImageButton;

public abstract class SimpleWizardLabDeskGui<T extends SimpleWizardLabContainer<?, ?>>
extends AbstractContainerScreen<T> {
    protected ImageButton goButton;
    protected final int WHITE = FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255);
    protected ArrayList<Component> tooltip;

    public SimpleWizardLabDeskGui(T screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 170;
        this.tooltip = new ArrayList();
    }

    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(8, 82);
    }

    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(0, 228);
    }

    protected Pair<Integer, Integer> goButtonSize() {
        return new Pair(25, 14);
    }

    protected int goButtonHoverOffset() {
        return 14;
    }

    protected ResourceLocation goButtonTexture() {
        return this.texture();
    }

    protected void addTooltipLine(Component line) {
        this.tooltip.add(line);
    }

    protected void init() {
        super.init();
        this.goButton = new MNAImageButton(this.leftPos + (Integer)this.goButtonPos().getFirst(), this.topPos + (Integer)this.goButtonPos().getSecond(), ((Integer)this.goButtonSize().getFirst()).intValue(), ((Integer)this.goButtonSize().getSecond()).intValue(), ((Integer)this.goButtonUV().getFirst()).intValue(), ((Integer)this.goButtonUV().getSecond()).intValue(), this.goButtonHoverOffset(), this.goButtonTexture(), b -> {
            ((SimpleWizardLabContainer)this.menu).clickMenuButton((Player)this.minecraft.player, 0);
            this.minecraft.gameMode.handleInventoryButtonClick(((SimpleWizardLabContainer)this.menu).containerId, 0);
        });
        this.addRenderableWidget(this.goButton);
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.goButton.visible = this.goButton.active = ((SimpleWizardLabContainer)this.menu).canActivate();
        this.tooltip.clear();
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        if (this.tooltip.size() > 0) {
            pGuiGraphics.renderTooltip(this.font, this.tooltip, Optional.empty(), mouseX, mouseY);
        }
        if (ManaAndArtifice.instance.proxy.getGameTicks() % 20L == 0L) {
            ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(false);
        }
    }

    protected boolean cursorIsWithin(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= this.leftPos + x && mouseX <= this.leftPos + x + w && mouseY >= this.topPos + y && mouseY <= this.topPos + y + h;
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouesX, int mouseY) {
        if (((SimpleWizardLabContainer)this.menu).getXPCost() > 0) {
            Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
            if (player.totalExperience >= ((SimpleWizardLabContainer)this.menu).getXPCost() || player.isCreative()) {
                MutableComponent mutableComponent = Component.translatable((String)"gui.mna.wizard_lab.xp_cost", (Object[])new Object[]{((SimpleWizardLabContainer)this.menu).getXPCost(), player.totalExperience});
                Objects.requireNonNull(this.font);
                pGuiGraphics.drawString(this.font, (Component)mutableComponent, 0, -9, this.WHITE, false);
            } else {
                MutableComponent mutableComponent = Component.translatable((String)"gui.mna.wizard_lab.xp_insufficient", (Object[])new Object[]{((SimpleWizardLabContainer)this.menu).getXPCost(), player.totalExperience});
                Objects.requireNonNull(this.font);
                pGuiGraphics.drawString(this.font, (Component)mutableComponent, 0, -9, this.WHITE, false);
            }
        }
    }

    protected void renderPowerConsumeStatusIcon(GuiGraphics pGuiGraphics, int mouseX, int mouseY, int x, int y, Affinity affinity, PowerStatus status) {
        switch (status) {
            case NO_POWER: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 88.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.no_power", (Object[])new Object[]{affinity}).withStyle(ChatFormatting.RED));
                break;
            }
            case NOT_REQUESTING: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 96.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.not_requesting").withStyle(ChatFormatting.GRAY));
                break;
            }
            case SUPPLIED: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 104.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.supplied", (Object[])new Object[]{affinity}).withStyle(ChatFormatting.GREEN));
                break;
            }
            case NO_CONDUIT: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 112.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.conduit_missing", (Object[])new Object[]{affinity}).withStyle(ChatFormatting.RED));
                break;
            }
            case PARTIAL: {
                pGuiGraphics.blit(GuiTextures.Widgets.WIDGETS, x, y, 120.0f, 120.0f, 8, 8, 128, 128);
                if (!this.cursorIsWithin(mouseX, mouseY, x - this.leftPos, y - this.topPos, 8, 8)) break;
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.wizard_lab.supplied_partial", (Object[])new Object[]{affinity}).withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected abstract ResourceLocation texture();
}

