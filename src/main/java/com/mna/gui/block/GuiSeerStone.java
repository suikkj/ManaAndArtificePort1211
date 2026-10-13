 
package com.mna.gui.block;

import com.mna.gui.GuiTextures;
import com.mna.gui.containers.block.ContainerSeerStone;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiSeerStone
extends AbstractContainerScreen<ContainerSeerStone> {
    public GuiSeerStone(ContainerSeerStone sentry, Inventory inv, Component comp) {
        super(sentry, inv, (Component)Component.literal((String)""));
        this.imageWidth = 234;
        this.imageHeight = 209;
    }

    public ResourceLocation texture() {
        return GuiTextures.Blocks.SEER_STONE;
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        MutableComponent tooltip = null;
        if (mouseY >= 29 && mouseY <= 43) {
            if (mouseX >= 20 && mouseX <= 34 || mouseX >= 200 && mouseX <= 214) {
                tooltip = Component.translatable((String)"block.mna.seer_stone.whitelist");
            }
        } else if (mouseY >= 72 && mouseY <= 76) {
            if (mouseX >= 20 && mouseX <= 34 || mouseX >= 200 && mouseX <= 214) {
                tooltip = Component.translatable((String)"block.mna.seer_stone.blacklist");
            }
        } else if (mouseY >= 94 && mouseY <= 112 && (mouseX >= 82 && mouseX <= 98 || mouseX >= 136 && mouseX <= 152)) {
            tooltip = Component.translatable((String)"block.mna.seer_stone.area");
        }
        if (tooltip != null) {
            List split = this.minecraft.font.split((FormattedText)tooltip, this.imageWidth / 2);
            pGuiGraphics.renderTooltip(this.font, split, mouseX, mouseY);
        } else {
            this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem._setShaderTexture((int)0, (ResourceLocation)this.texture());
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, this.imageWidth, this.imageHeight);
        int crystalHalfWidth = 20;
        int crystalHeight = 74;
        int crystalX = this.leftPos + this.imageWidth / 2;
        int crystalY = this.topPos - crystalHeight;
        pGuiGraphics.blit(this.texture(), crystalX - crystalHalfWidth, crystalY, 236, crystalHeight, crystalHalfWidth, crystalHeight);
        pGuiGraphics.blit(this.texture(), crystalX, crystalY, 236, 0, crystalHalfWidth, crystalHeight);
    }
}

