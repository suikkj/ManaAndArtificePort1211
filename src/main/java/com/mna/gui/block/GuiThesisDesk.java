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
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.gui.block;

import com.mna.ManaAndArtifice;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.gui.GuiTextures;
import com.mna.gui.block.SimpleWizardLabDeskGui;
import com.mna.gui.containers.block.ContainerThesisDesk;
import com.mna.gui.widgets.SpellPartList;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class GuiThesisDesk
extends SimpleWizardLabDeskGui<ContainerThesisDesk> {
    private Component thesisTooltip;

    public GuiThesisDesk(ContainerThesisDesk container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 256;
        this.imageHeight = 233;
    }

    @Override
    protected void init() {
        super.init();
        SpellPartList spellSelector = new SpellPartList(true, true, this.leftPos, this.topPos, 9, 29, 64, 111, ((ContainerThesisDesk)this.menu)::setSpellComponent, ((ContainerThesisDesk)this.menu)::setSpellComponent, ((ContainerThesisDesk)this.menu)::setSpellComponent, tt -> {
            this.thesisTooltip = tt;
        });
        spellSelector._selected = ((ContainerThesisDesk)this.menu).getSpellComponent();
        this.addRenderableWidget(spellSelector);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.thesisTooltip = null;
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.thesisTooltip != null) {
            pGuiGraphics.renderTooltip(this.font, this.thesisTooltip, mouseX, mouseY);
        }
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.THESIS_DESK;
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
        ItemStack stack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
        pGuiGraphics.pose().translate(0.5, 0.5, 0.0);
        pGuiGraphics.renderItem(stack, 103, 23);
        pGuiGraphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        ISpellComponent selected;
        super.renderBg(pGuiGraphics, partialTicks, mouseX, mouseY);
        pGuiGraphics.blit(GuiTextures.WizardLab.THESIS_DESK_EXTENSION, this.leftPos + 88, this.topPos + 27, 0, 0, 104, 116);
        if (((ContainerThesisDesk)this.menu).isActive()) {
            float pct = ((ContainerThesisDesk)this.menu).getProgress();
            this.blitProgressBarLine(pGuiGraphics, 27, 0, 23, pct / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 50, 23, 23, (pct - 0.2f) / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 73, 46, 20, (pct - 0.4f) / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 93, 66, 22, (pct - 0.6f) / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 115, 88, 28, (pct - 0.8f) / 0.2f);
        }
        pGuiGraphics.blit(GuiTextures.WizardLab.THESIS_DESK_EXTENSION, this.leftPos + 106, this.topPos + 6, 238, 38, 18, 18);
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        float xpPct = MathUtils.clamp01(player.isCreative() ? 1.0f : (float)player.totalExperience / (float)((ContainerThesisDesk)this.menu).getXPCost());
        int VCoord = xpPct < 1.0f ? 5 : 0;
        pGuiGraphics.blit(GuiTextures.WizardLab.THESIS_DESK_EXTENSION, this.leftPos + 206, this.topPos + 81, 220, VCoord, (int)(36.0f * xpPct), 5);
        PowerStatus powerStatus = ((ContainerThesisDesk)this.menu).powerRequirementStatus().getOrDefault(((ContainerThesisDesk)this.menu).getAffinity(), PowerStatus.NOT_REQUESTING);
        this.renderPowerConsumeStatusIcon(pGuiGraphics, mouseX, mouseY, this.leftPos + 29, this.topPos + 11, ((ContainerThesisDesk)this.menu).getAffinity(), powerStatus);
        ItemStack affStack = GuiTextures.affinityIcons.get(((ContainerThesisDesk)this.menu).getAffinity());
        if (!affStack.isEmpty()) {
            pGuiGraphics.renderItem(affStack, this.leftPos + 11, this.topPos + 7);
        }
        if ((selected = ((ContainerThesisDesk)this.menu).getSpellComponent()) != null) {
            int texSize = 32;
            pGuiGraphics.blit(selected.getGuiIcon(), this.leftPos + 208, this.topPos + 9, 0.0f, 0.0f, 32, 32, 32, 32);
        }
    }

    private void blitProgressBarLine(GuiGraphics pGuiGraphics, int y, int v, int h, float pct) {
        int progressWidth = (int)(104.0f * MathUtils.clamp01(pct));
        pGuiGraphics.blit(GuiTextures.WizardLab.THESIS_DESK_EXTENSION, this.leftPos + 88, this.topPos + y, 105, v, progressWidth, h);
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(199, 130);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(231, 10);
    }

    @Override
    protected ResourceLocation goButtonTexture() {
        return GuiTextures.WizardLab.THESIS_DESK_EXTENSION;
    }
}

