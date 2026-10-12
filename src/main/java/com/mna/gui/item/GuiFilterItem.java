/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.ImageButton
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package com.mna.gui.item;

import com.mna.gui.GuiTextures;
import com.mna.gui.containers.item.ContainerFilterItem;
import com.mna.tools.render.GuiRenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiFilterItem
extends AbstractContainerScreen<ContainerFilterItem> {
    public GuiFilterItem(ContainerFilterItem container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 188;
    }

    protected void init() {
        super.init();
        this.addRenderableWidget(new Checkbox(((ContainerFilterItem)this.menu).getDurabilityMatching(), this.leftPos + 72, this.topPos + 80, 44, 80, 0, 248, b -> {
            ((ContainerFilterItem)this.menu).clickMenuButton((Player)this.minecraft.player, 0);
            this.minecraft.gameMode.handleInventoryButtonClick(((ContainerFilterItem)this.menu).containerId, 0);
        }, (Component)Component.translatable((String)"gui.mna.filter.durability")));
        this.addRenderableWidget(new Checkbox(((ContainerFilterItem)this.menu).getTagMatching(), this.leftPos + 90, this.topPos + 80, 62, 80, 8, 248, b -> {
            ((ContainerFilterItem)this.menu).clickMenuButton((Player)this.minecraft.player, 1);
            this.minecraft.gameMode.handleInventoryButtonClick(((ContainerFilterItem)this.menu).containerId, 1);
        }, (Component)Component.translatable((String)"gui.mna.filter.nbt")));
    }

    public void render(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y, float partialTick) {
        super.render(pGuiGraphics, mouse_x, mouse_y, partialTick);
        this.renderTooltip(pGuiGraphics, mouse_x, mouse_y);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouse_x, int mouse_y) {
        this.renderBackground(pGuiGraphics, 0, 0, 0.0f);
        int xPos = this.leftPos;
        int yPos = this.topPos;
        GuiRenderUtils.renderStandardPlayerInventory(pGuiGraphics, xPos + this.imageWidth / 2, yPos + this.imageHeight - 45);
        pGuiGraphics.blit(GuiTextures.Items.FILTER_ITEM, xPos + 28, yPos, 0, 0, 120, 98);
    }

    protected void renderLabels(GuiGraphics p_97808_, int p_97809_, int p_97810_) {
    }

    private class Checkbox
    extends MNAImageButton {
        private boolean checked;
        private final int check_u;
        private final int check_v;

        public Checkbox(boolean initialValue, int x, int y, int u, int v, int check_u, int check_v, Button.OnPress clickHandler, Component tooltip) {
            super(x, y, 14, 14, u, v, 0, GuiTextures.Items.FILTER_ITEM, clickHandler);
            this.checked = false;
            this.check_u = check_u;
            this.check_v = check_v;
            this.checked = initialValue;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y, float partial_tick) {
            super.renderWidget(pGuiGraphics, mouse_x, mouse_y, partial_tick);
            if (this.checked) {
                pGuiGraphics.blit(GuiTextures.Items.FILTER_ITEM, this.getX() + 3, this.getY() + 3, this.check_u, this.check_v, 8, 8);
            }
        }

        public void onClick(double mouse_x, double mouse_y) {
            this.checked = !this.checked;
            super.onClick(mouse_x, mouse_y);
        }
    }
}

