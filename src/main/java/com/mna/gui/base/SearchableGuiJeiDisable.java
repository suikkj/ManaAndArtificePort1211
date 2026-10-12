/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package com.mna.gui.base;

import com.mna.gui.base.GuiJEIDisable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class SearchableGuiJeiDisable<T extends AbstractContainerMenu>
extends GuiJEIDisable<T> {
    protected String currentSearchTerm = null;
    protected EditBox searchBox;
    protected Component searchValue;

    public SearchableGuiJeiDisable(T container, Inventory playerInv, Component title) {
        super(container, playerInv, title);
    }

    protected void initSearch(int searchX, int searchY) {
        this.initSearch(searchX, searchY, 130, 16);
    }

    protected void initSearch(int searchX, int searchY, int width, int height) {
        this.searchBox = new EditBox(this.minecraft.font, searchX, searchY, width, height, this.searchValue);
        this.searchBox.setMaxLength(60);
        this.searchBox.setResponder(this::searchTermChanged);
        this.searchBox.setCanLoseFocus(false);
        this.searchBox.setFocused(true);
        this.setFocused((GuiEventListener)this.searchBox);
    }

    public void render(GuiGraphics pGuiGraphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(pGuiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
        this.searchBox.render(pGuiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
    }

    protected abstract void searchTermChanged(String var1);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.setFocused((GuiEventListener)this.searchBox);
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        return this.getFocused() != null && this.getFocused().charTyped(pCodePoint, pModifiers);
    }
}

