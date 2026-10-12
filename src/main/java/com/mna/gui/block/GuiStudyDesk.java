/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.gui.GuiGraphics
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
import com.mna.gui.GuiTextures;
import com.mna.gui.block.SimpleWizardLabDeskGui;
import com.mna.gui.containers.block.ContainerStudyDesk;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class GuiStudyDesk
extends SimpleWizardLabDeskGui<ContainerStudyDesk> {
    public GuiStudyDesk(ContainerStudyDesk container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 176;
        this.imageHeight = 154;
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.STUDY_DESK;
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
        ItemStack stack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
        pGuiGraphics.pose().translate(0.5, 0.5, 0.0);
        pGuiGraphics.renderItem(stack, 35, 2);
        pGuiGraphics.pose().popPose();
        boolean wasActive = this.goButton.active;
        boolean wasVisible = this.goButton.visible;
        this.goButton.active = true;
        this.goButton.visible = true;
        if (this.goButton.isMouseOver((double)x, (double)y) && !wasActive && !((ContainerStudyDesk)this.menu).getSlot(0).getItem().isEmpty()) {
            this.tooltip.add(((ContainerStudyDesk)this.menu).getCantStartReason((Player)this.minecraft.player).tooltip());
        }
        this.goButton.active = wasActive;
        this.goButton.visible = wasVisible;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(pGuiGraphics, partialTicks, mouseX, mouseY);
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        float xpPct = MathUtils.clamp01(player.isCreative() ? 1.0f : (float)player.totalExperience / (float)((ContainerStudyDesk)this.menu).getXPCost());
        int VCoord = xpPct < 1.0f ? 49 : 0;
        pGuiGraphics.blit(this.texture(), this.leftPos + 70, this.topPos + 39, 220, VCoord, (int)(36.0f * xpPct), 5);
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(90, 50);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(231, 5);
    }
}

