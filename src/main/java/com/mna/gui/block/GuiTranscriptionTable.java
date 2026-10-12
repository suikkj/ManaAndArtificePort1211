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
import com.mna.gui.containers.block.ContainerTranscriptionTable;
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

public class GuiTranscriptionTable
extends SimpleWizardLabDeskGui<ContainerTranscriptionTable> {
    public GuiTranscriptionTable(ContainerTranscriptionTable container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 176;
        this.imageHeight = 159;
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.TRANSCRIPTION_TABLE;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        Player player;
        super.renderBg(pGuiGraphics, partialTicks, mouseX, mouseY);
        if (((ContainerTranscriptionTable)this.menu).isActive()) {
            float pct = ((ContainerTranscriptionTable)this.menu).getProgress();
            pGuiGraphics.blit(this.texture(), this.leftPos + 59, this.topPos + 10, 198, 38, 58, (int)(58.0f * pct));
        }
        float xpPct = MathUtils.clamp01((player = ManaAndArtifice.instance.proxy.getClientPlayer()).isCreative() ? 1.0f : (float)player.totalExperience / (float)((ContainerTranscriptionTable)this.menu).getXPCost());
        int VCoord = xpPct < 1.0f ? 5 : 0;
        pGuiGraphics.blit(this.texture(), this.leftPos + 128, this.topPos + 52, 220, VCoord, (int)(36.0f * xpPct), 5);
        HashMap<Affinity, PowerStatus> powerRequirements = ((ContainerTranscriptionTable)this.menu).powerRequirementStatus();
        if (powerRequirements.size() == 1) {
            powerRequirements.forEach((a, b) -> {
                this.renderPowerConsumeStatusIcon(pGuiGraphics, mouseX, mouseY, this.leftPos + 26, this.topPos + 58, (Affinity)(a), (PowerStatus)(b));
                ItemStack renderStack = GuiTextures.affinityIcons.get(a);
                if (!renderStack.isEmpty()) {
                    pGuiGraphics.renderItem(renderStack, this.leftPos + 8, this.topPos + 54);
                }
            });
        }
        pGuiGraphics.blit(this.texture(), this.leftPos + 7, this.topPos + 30, 202, 0, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int x, int y) {
        ItemStack stack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
        pGuiGraphics.pose().translate(0.5, 0.0, 0.0);
        pGuiGraphics.renderItem(stack, 64, 9);
        pGuiGraphics.pose().popPose();
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(30, 32);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(231, 10);
    }
}

