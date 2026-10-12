 
package com.mna.gui.item;

import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.item.ContainerGrimoire;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiGrimoire
extends GuiJEIDisable<ContainerGrimoire> {
    public GuiGrimoire(ContainerGrimoire book, Inventory inv, Component comp) {
        super(book, inv, (Component)Component.literal((String)""));
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        float textScaleFactor = 0.5f;
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(textScaleFactor, textScaleFactor, textScaleFactor);
        for (int i = 0; i < 16; ++i) {
            if (!((ContainerGrimoire)this.menu).getSlot(i).hasItem()) continue;
            float x = (float)(i < 8 ? 36 : 151) / textScaleFactor;
            float y = (float)(i < 8 ? 8 + 18 * i : 8 + 18 * (i - 8)) / textScaleFactor;
            pGuiGraphics.drawString(this.font, ((ContainerGrimoire)this.menu).getSlot(i).getItem().getHoverName().getString(), (int)x, (int)y, 0x404040, false);
        }
        pGuiGraphics.pose().popPose();
    }

    public ResourceLocation texture() {
        return GuiTextures.Items.GRIMOIRE;
    }

    public int rows() {
        return 8;
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void init() {
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_OPEN);
        super.init();
    }

    public void onClose() {
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_CLOSE);
        super.onClose();
    }
}

