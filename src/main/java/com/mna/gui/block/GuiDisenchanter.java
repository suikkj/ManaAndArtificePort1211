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
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.gui.GuiTextures;
import com.mna.gui.block.SimpleWizardLabDeskGui;
import com.mna.gui.containers.block.ContainerDisenchanter;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class GuiDisenchanter
extends SimpleWizardLabDeskGui<ContainerDisenchanter> {
    public GuiDisenchanter(ContainerDisenchanter container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 176;
        this.imageHeight = 153;
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.DISENCHANTER;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(pGuiGraphics, partialTicks, mouseX, mouseY);
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        float xpPct = MathUtils.clamp01(player.isCreative() ? 1.0f : (float)player.totalExperience / (float)((ContainerDisenchanter)this.menu).getXPCost());
        int VCoord = xpPct < 1.0f ? 5 : 0;
        pGuiGraphics.blit(this.texture(), this.leftPos + 101, this.topPos + 49, 220, VCoord, (int)(36.0f * xpPct), 5);
        if (((ContainerDisenchanter)this.menu).isActive()) {
            float pct = ((ContainerDisenchanter)this.menu).getProgress();
            pGuiGraphics.blit(this.texture(), this.leftPos + 83, this.topPos + 25, 223, 10, 8, (int)(22.0f * pct));
        }
        pGuiGraphics.renderItem(GuiTextures.affinityIcons.get(Affinity.FIRE), this.leftPos + 39, this.topPos + 7);
        pGuiGraphics.renderItem(GuiTextures.affinityIcons.get(Affinity.WIND), this.leftPos + 39, this.topPos + 28);
        HashMap<Affinity, PowerStatus> powerRequirements = ((ContainerDisenchanter)this.menu).powerRequirementStatus();
        this.renderPowerConsumeStatusIcon(pGuiGraphics, mouseX, mouseY, this.leftPos + 58, this.topPos + 11, Affinity.FIRE, powerRequirements.getOrDefault(Affinity.FIRE, PowerStatus.NOT_REQUESTING));
        this.renderPowerConsumeStatusIcon(pGuiGraphics, mouseX, mouseY, this.leftPos + 58, this.topPos + 32, Affinity.WIND, powerRequirements.getOrDefault(Affinity.WIND, PowerStatus.NOT_REQUESTING));
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
        ItemStack stack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
        pGuiGraphics.pose().translate(0.0, 0.5, 0.0);
        pGuiGraphics.renderItem(stack, 51, 7);
        pGuiGraphics.pose().popPose();
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(47, 50);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(231, 10);
    }
}

