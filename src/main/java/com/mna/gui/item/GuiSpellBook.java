/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 */
package com.mna.gui.item;

import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiBagBase;
import com.mna.gui.containers.item.ContainerSpellBook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSpellBook
extends GuiBagBase<ContainerSpellBook> {
    public GuiSpellBook(ContainerSpellBook book, Inventory inv, Component comp) {
        super(inv, book);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    protected void init() {
        super.init();
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_OPEN);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        float textScaleFactor = 0.5f;
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(textScaleFactor, textScaleFactor, textScaleFactor);
        for (int i = 0; i < 8; ++i) {
            if (!((ContainerSpellBook)this.menu).getSlot(i).hasItem()) continue;
            pGuiGraphics.drawString(this.font, ((ContainerSpellBook)this.menu).getSlot(i).getItem().getHoverName().getString(), (int)(36.0f / textScaleFactor), (int)((float)(8 + 18 * i) / textScaleFactor), 0x404040, false);
        }
        pGuiGraphics.pose().popPose();
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.Items.SPELL_BOOK;
    }

    @Override
    public String name() {
        return "Spell Book";
    }

    public void onClose() {
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_CLOSE);
        ((ContainerSpellBook)this.menu).onClose();
        super.onClose();
    }
}

