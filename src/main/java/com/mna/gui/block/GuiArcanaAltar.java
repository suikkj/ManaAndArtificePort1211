/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 */
package com.mna.gui.block;

import com.mna.gui.GuiTextures;
import com.mna.gui.block.SimpleWizardLabDeskGui;
import com.mna.gui.containers.block.ContainerArcaneAltar;
import com.mna.gui.widgets.ReagentList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiArcanaAltar
extends SimpleWizardLabDeskGui<ContainerArcaneAltar> {
    ReagentList rList;
    ReagentList mwList;

    public GuiArcanaAltar(ContainerArcaneAltar container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 256;
        this.imageHeight = 226;
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.ALTAR_OF_ARCANA;
    }

    @Override
    protected void init() {
        super.init();
        this.rList = new ReagentList(this.leftPos, this.topPos, 172, 14, 71, 75, this::addTooltipLine);
        this.addRenderableWidget(this.rList);
        this.mwList = new ReagentList(this.leftPos, this.topPos, 172, 103, 71, 31, this::addTooltipLine);
        this.mwList.setFullTooltip(true);
        this.addRenderableWidget(this.mwList);
        this.rList.reInit(((ContainerArcaneAltar)this.menu).getRemainingReagents());
        this.mwList.reInit(((ContainerArcaneAltar)this.menu).getRemainingManaweaves());
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (((ContainerArcaneAltar)this.menu).shouldRecalculateMaterials()) {
            this.rList.reInit(((ContainerArcaneAltar)this.menu).getRemainingReagents());
            this.mwList.reInit(((ContainerArcaneAltar)this.menu).getRemainingManaweaves());
            ((ContainerArcaneAltar)this.menu).materialsRecalculated();
        }
        super.renderBg(pGuiGraphics, partialTicks, mouseX, mouseY);
        if (((ContainerArcaneAltar)this.menu).isActive()) {
            float pct = ((ContainerArcaneAltar)this.menu).getProgress();
            pGuiGraphics.blit(GuiTextures.WizardLab.ALTAR_OF_ARCANA_EXT, this.leftPos + 13, this.topPos + 13, 0.0f, 0.0f, 120, (int)(120.0f * pct), 128, 128);
        }
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(66, 66);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(0, 242);
    }

    @Override
    protected Pair<Integer, Integer> goButtonSize() {
        return new Pair(14, 14);
    }

    @Override
    protected int goButtonHoverOffset() {
        return 0;
    }
}

