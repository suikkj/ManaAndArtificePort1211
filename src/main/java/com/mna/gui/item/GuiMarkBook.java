/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.item;

import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiBagBase;
import com.mna.gui.containers.item.ContainerMarkBook;
import com.mna.items.ItemInit;
import com.mna.items.runes.ItemRuneMarking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiMarkBook
extends GuiBagBase<ContainerMarkBook> {
    public GuiMarkBook(ContainerMarkBook book, Inventory inv, Component comp) {
        super(inv, book);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        Component blockName;
        Direction markFace;
        BlockPos markPos;
        ItemStack stack;
        int i;
        float textScaleFactor = 0.5f;
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(textScaleFactor, textScaleFactor, textScaleFactor);
        for (i = 0; i < 8; ++i) {
            if (!((ContainerMarkBook)this.menu).getSlot(i).hasItem()) continue;
            stack = ((ContainerMarkBook)this.menu).getSlot(i).getItem();
            markPos = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getLocation(stack);
            markFace = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getFace(stack);
            blockName = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getBlockName(stack);
            if (blockName == null) {
                blockName = Component.translatable((String)"item.mna.rune_marking.noPosition");
            }
            pGuiGraphics.drawString(this.font, (Component)Component.literal((String)stack.getHoverName().getString()).append(" (").append(blockName).append(")"), (int)(36.0f / textScaleFactor), (int)((float)(8 + 18 * i) / textScaleFactor), 0x404040, false);
            if (markPos == null) continue;
            pGuiGraphics.drawString(this.font, markPos.toShortString() + (String)(markFace != null ? " (" + markFace.getName() + ")" : ""), (int)(36.0f / textScaleFactor), (int)((float)(15 + 18 * i) / textScaleFactor), 0x404040, false);
        }
        for (i = 8; i < 16; ++i) {
            if (!((ContainerMarkBook)this.menu).getSlot(i).hasItem()) continue;
            stack = ((ContainerMarkBook)this.menu).getSlot(i).getItem();
            markPos = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getLocation(stack);
            markFace = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getFace(stack);
            blockName = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getBlockName(stack);
            if (blockName == null) {
                blockName = Component.translatable((String)"item.mna.rune_marking.noPosition");
            }
            pGuiGraphics.drawString(this.font, (Component)Component.literal((String)stack.getHoverName().getString()).append(" (").append(blockName).append(")"), (int)(151.0f / textScaleFactor), (int)((float)(8 + 18 * (i - 8)) / textScaleFactor), 0x404040, false);
            if (markPos == null) continue;
            pGuiGraphics.drawString(this.font, markPos.toShortString() + (String)(markFace != null ? " (" + markFace.getName() + ")" : ""), (int)(151.0f / textScaleFactor), (int)((float)(15 + 18 * (i - 8)) / textScaleFactor), 0x404040, false);
        }
        pGuiGraphics.pose().popPose();
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.Items.BOOK_MARKS;
    }

    @Override
    public String name() {
        return "Book of Marks";
    }

    protected void init() {
        super.init();
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_OPEN);
    }

    public void onClose() {
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_CLOSE);
        super.onClose();
    }
}

