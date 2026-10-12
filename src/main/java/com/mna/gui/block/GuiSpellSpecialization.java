 
package com.mna.gui.block;

import com.mna.ManaAndArtifice;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.gui.GuiTextures;
import com.mna.gui.block.SimpleWizardLabDeskGui;
import com.mna.gui.containers.block.ContainerSpellSpecialization;
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
import com.mna.api.tools.MNACap;

public class GuiSpellSpecialization
extends SimpleWizardLabDeskGui<ContainerSpellSpecialization> {
    private Component specializationTooltip;

    public GuiSpellSpecialization(ContainerSpellSpecialization container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 256;
        this.imageHeight = 233;
    }

    @Override
    protected void init() {
        super.init();
        SpellPartList spellSelector = new SpellPartList(false, true, this.leftPos, this.topPos, 9, 29, 64, 111, s -> ((ContainerSpellSpecialization)this.menu).setSpellComponent((ISpellComponent)s), c -> ((ContainerSpellSpecialization)this.menu).setSpellComponent((ISpellComponent)c), m -> {}, tt -> {
            this.specializationTooltip = tt;
        });
        spellSelector._selected = ((ContainerSpellSpecialization)this.menu).getSpellComponent();
        this.addRenderableWidget(spellSelector);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.specializationTooltip = null;
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.specializationTooltip != null) {
            pGuiGraphics.renderTooltip(this.font, this.specializationTooltip, mouseX, mouseY);
        }
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.SPELL_SPECIALIZATION;
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
        Player player;
        super.renderBg(pGuiGraphics, partialTicks, mouseX, mouseY);
        ISpellComponent selected = ((ContainerSpellSpecialization)this.menu).getSpellComponent();
        if (selected != null) {
            MNACap.get(ManaAndArtifice.instance.proxy.getClientPlayer(), PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
                float mastery = r.getMastery(selected);
                for (int i = 0; i < 10 && !((float)(i + 1) * 0.05f > mastery); ++i) {
                    int y;
                    int x;
                    if (i < 3) {
                        x = 208 + i % 3 * 12;
                        y = 94;
                    } else if (i < 7) {
                        x = 202 + (i - 3) % 4 * 12;
                        y = 104;
                    } else {
                        x = 208 + (i - 7) % 3 * 12;
                        y = 114;
                    }
                    pGuiGraphics.blit(GuiTextures.WizardLab.SPELL_SPECIALIZATION_EXTENSION, this.leftPos + x, this.topPos + y, 0, 248, 8, 8);
                }
            });
        }
        pGuiGraphics.blit(GuiTextures.WizardLab.SPELL_SPECIALIZATION_EXTENSION, this.leftPos + 88, this.topPos + 27, 0, 0, 104, 116);
        if (((ContainerSpellSpecialization)this.menu).isActive()) {
            float pct = ((ContainerSpellSpecialization)this.menu).getProgress();
            this.blitProgressBarLine(pGuiGraphics, 27, 0, 27, pct / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 54, 27, 21, (pct - 0.2f) / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 75, 48, 20, (pct - 0.4f) / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 95, 68, 22, (pct - 0.6f) / 0.2f);
            this.blitProgressBarLine(pGuiGraphics, 117, 90, 26, (pct - 0.8f) / 0.2f);
        }
        float xpPct = MathUtils.clamp01((player = ManaAndArtifice.instance.proxy.getClientPlayer()).isCreative() ? 1.0f : (float)player.totalExperience / (float)((ContainerSpellSpecialization)this.menu).getXPCost());
        int VCoord = xpPct < 1.0f ? 223 : 218;
        pGuiGraphics.blit(GuiTextures.WizardLab.SPELL_SPECIALIZATION_EXTENSION, this.leftPos + 206, this.topPos + 81, 0, VCoord, (int)(36.0f * xpPct), 5);
        PowerStatus powerStatus = ((ContainerSpellSpecialization)this.menu).powerRequirementStatus().getOrDefault(((ContainerSpellSpecialization)this.menu).getAffinity(), PowerStatus.NOT_REQUESTING);
        this.renderPowerConsumeStatusIcon(pGuiGraphics, mouseX, mouseY, this.leftPos + 29, this.topPos + 11, ((ContainerSpellSpecialization)this.menu).getAffinity(), powerStatus);
        ItemStack affStack = GuiTextures.affinityIcons.get(((ContainerSpellSpecialization)this.menu).getAffinity());
        if (!affStack.isEmpty()) {
            pGuiGraphics.renderItem(affStack, this.leftPos + 11, this.topPos + 7);
        }
        if (selected != null) {
            int texSize = 32;
            pGuiGraphics.blit(selected.getGuiIcon(), this.leftPos + 208, this.topPos + 9, 0.0f, 0.0f, 32, 32, 32, 32);
        }
    }

    private void blitProgressBarLine(GuiGraphics pGuiGraphics, int y, int v, int h, float pct) {
        int progressWidth = (int)(104.0f * MathUtils.clamp01(pct));
        pGuiGraphics.blit(GuiTextures.WizardLab.SPELL_SPECIALIZATION_EXTENSION, this.leftPos + 88, this.topPos + y, 105, v, progressWidth, h);
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(211, 130);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(11, 228);
    }

    @Override
    protected ResourceLocation goButtonTexture() {
        return GuiTextures.WizardLab.SPELL_SPECIALIZATION_EXTENSION;
    }
}

