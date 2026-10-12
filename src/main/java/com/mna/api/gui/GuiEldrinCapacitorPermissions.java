 
package com.mna.api.gui;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.gui.EldrinCapacitorPermissionsContainer;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class GuiEldrinCapacitorPermissions
extends AbstractContainerScreen<EldrinCapacitorPermissionsContainer> {
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("mna", "textures/gui/gui_conduit.png");
    private static final ItemStack ARCANE = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "greater_mote_arcane")));
    private static final ItemStack ENDER = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "greater_mote_ender")));
    private static final ItemStack EARTH = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "greater_mote_earth")));
    private static final ItemStack WATER = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "greater_mote_water")));
    private static final ItemStack FIRE = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "greater_mote_fire")));
    private static final ItemStack AIR = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "greater_mote_air")));
    private static final Affinity[] AffinitiesInOrder = new Affinity[]{Affinity.ARCANE, Affinity.WIND, Affinity.FIRE, Affinity.WATER, Affinity.EARTH, Affinity.ENDER};
    protected ArrayList<Component> tooltip = new ArrayList();

    public GuiEldrinCapacitorPermissions(EldrinCapacitorPermissionsContainer pMenu, Inventory pPlayerInventory, Component title) {
        super((AbstractContainerMenu)pMenu, pPlayerInventory, (Component)Component.empty());
        this.imageWidth = 222;
        this.imageHeight = 143;
    }

    protected void init() {
        super.init();
        if (((EldrinCapacitorPermissionsContainer)this.menu).userCanEdit()) {
            this.addRenderableWidget(new MNAImageButton(this.leftPos + 79, this.topPos + 37, 66, 18, 0, 220, 18, texture, 256, 256, b -> this.minecraft.gameMode.handleInventoryButtonClick(((EldrinCapacitorPermissionsContainer)this.menu).containerId, 0), (Component)Component.translatable((String)"gui.mna.share-same-team")));
            this.addRenderableWidget(new MNAImageButton(this.leftPos + 79, this.topPos + 61, 66, 18, 0, 220, 18, texture, 256, 256, b -> this.minecraft.gameMode.handleInventoryButtonClick(((EldrinCapacitorPermissionsContainer)this.menu).containerId, 1), (Component)Component.translatable((String)"gui.mna.share-same-faction")));
            this.addRenderableWidget(new MNAImageButton(this.leftPos + 79, this.topPos + 85, 66, 18, 0, 220, 18, texture, 256, 256, b -> this.minecraft.gameMode.handleInventoryButtonClick(((EldrinCapacitorPermissionsContainer)this.menu).containerId, 2), (Component)Component.translatable((String)"gui.mna.share-same-faction")));
        }
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.tooltip.clear();
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        if (this.tooltip.size() > 0) {
            pGuiGraphics.renderTooltip(this.font, this.tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i;
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int barWidgetX = this.leftPos + 10;
        int barWidgetStep = 34;
        int barWidgetY = this.topPos - 12;
        for (i = 0; i < 6; ++i) {
            this.blitFillBar(pGuiGraphics, barWidgetX, barWidgetY);
            this.blitFillBarInner(pGuiGraphics, AffinitiesInOrder[i], barWidgetX, barWidgetY, this.leftPos + pMouseX, this.topPos + pMouseY);
            barWidgetX += barWidgetStep;
        }
        i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(texture, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int textColor = ((EldrinCapacitorPermissionsContainer)this.menu).userCanEdit() ? 0 : 0xFFFFFF;
        int textX = this.imageWidth / 2 + 1;
        MutableComponent c = Component.translatable((String)"gui.mna.share-same-team");
        pGuiGraphics.drawString(this.font, (Component)c, textX - this.font.width((FormattedText)c) / 2, 42, textColor, false);
        c = Component.translatable((String)"gui.mna.share-same-faction");
        pGuiGraphics.drawString(this.font, (Component)c, textX - this.font.width((FormattedText)c) / 2, 66, textColor, false);
        c = Component.translatable((String)"gui.mna.share-public");
        pGuiGraphics.drawString(this.font, (Component)c, textX - this.font.width((FormattedText)c) / 2, 90, textColor, false);
        int affinityY = 12;
        pGuiGraphics.renderItem(ARCANE, 12, affinityY);
        pGuiGraphics.renderItem(AIR, 46, affinityY);
        pGuiGraphics.renderItem(FIRE, 80, affinityY);
        pGuiGraphics.renderItem(WATER, 114, affinityY);
        pGuiGraphics.renderItem(EARTH, 148, affinityY);
        pGuiGraphics.renderItem(ENDER, 182, affinityY);
        int affinitySupplyY = 16;
        this.blitSupplyStatus(pGuiGraphics, Affinity.ARCANE, 30, affinitySupplyY, pMouseX, pMouseY);
        this.blitSupplyStatus(pGuiGraphics, Affinity.WIND, 64, affinitySupplyY, pMouseX, pMouseY);
        this.blitSupplyStatus(pGuiGraphics, Affinity.FIRE, 98, affinitySupplyY, pMouseX, pMouseY);
        this.blitSupplyStatus(pGuiGraphics, Affinity.WATER, 132, affinitySupplyY, pMouseX, pMouseY);
        this.blitSupplyStatus(pGuiGraphics, Affinity.EARTH, 166, affinitySupplyY, pMouseX, pMouseY);
        this.blitSupplyStatus(pGuiGraphics, Affinity.ENDER, 200, affinitySupplyY, pMouseX, pMouseY);
        int shareX = 68;
        this.blitShareStatus(pGuiGraphics, ((EldrinCapacitorPermissionsContainer)this.menu).shareTeam(), shareX, 38);
        this.blitShareStatus(pGuiGraphics, ((EldrinCapacitorPermissionsContainer)this.menu).shareFaction(), shareX, 62);
        this.blitShareStatus(pGuiGraphics, ((EldrinCapacitorPermissionsContainer)this.menu).sharePublic(), shareX, 86);
        int textY = 127;
        if (((EldrinCapacitorPermissionsContainer)this.menu).getPlacedByPlayerName() != null) {
            c = Component.literal((String)((EldrinCapacitorPermissionsContainer)this.menu).getPlacedByPlayerName());
            pGuiGraphics.drawString(this.font, (Component)c, 22, textY, 0xFFFFFF, false);
        }
        if (this.cursorIsWithin(pMouseX, pMouseY, 19, 125, 83, 12)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.owner").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
        }
        if (((EldrinCapacitorPermissionsContainer)this.menu).getShareTeam() != null) {
            c = Component.literal((String)((EldrinCapacitorPermissionsContainer)this.menu).getShareTeam());
            pGuiGraphics.drawString(this.font, (Component)c, this.imageWidth - 22 - this.font.width((FormattedText)c), textY, 0xFFFFFF, false);
        }
        if (this.cursorIsWithin(pMouseX, pMouseY, 119, 125, 83, 12)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.team").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
        }
        c = Component.literal((String)String.format("%.1f", Float.valueOf(((EldrinCapacitorPermissionsContainer)this.menu).getChargeRate())));
        pGuiGraphics.drawString(this.font, (Component)c, 34 - this.font.width((FormattedText)c) / 2, 45, 0xFFFFFF, false);
        if (this.cursorIsWithin(pMouseX, pMouseY, 5, 41, 48, 18)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.charge_rate").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
        }
        c = Component.literal((String)String.format("%.1f", Float.valueOf(((EldrinCapacitorPermissionsContainer)this.menu).getDispersalRate())));
        pGuiGraphics.drawString(this.font, (Component)c, 34 - this.font.width((FormattedText)c) / 2, 64, 0xFFFFFF, false);
        if (this.cursorIsWithin(pMouseX, pMouseY, 5, 60, 48, 18)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.dispersal_rate").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
        }
        c = Component.literal((String)String.format("%.1f", Float.valueOf(((EldrinCapacitorPermissionsContainer)this.menu).getDispersalRadius())));
        pGuiGraphics.drawString(this.font, (Component)c, 200 - this.font.width((FormattedText)c) / 2, 45, 0xFFFFFF, false);
        if (this.cursorIsWithin(pMouseX, pMouseY, 170, 41, 48, 18)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.dispersal_radius").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
        }
        if (((EldrinCapacitorPermissionsContainer)this.menu).getShareFaction() != null) {
            ManaAndArtificeMod.getGuiRenderHelper().renderFactionIcon(pGuiGraphics, ((EldrinCapacitorPermissionsContainer)this.menu).getShareFaction(), 151, 67);
        }
    }

    private void blitFillBar(GuiGraphics pGuiGraphics, int x, int y) {
        pGuiGraphics.blit(texture, x, y, 0, 208, 32, 12);
    }

    private void blitFillBarInner(GuiGraphics pGuiGraphics, Affinity affinity, int x, int y, int mouseX, int mouseY) {
        int colorOne = FastColor.ABGR32.color((int)255, (int)affinity.getColor()[0], (int)affinity.getColor()[1], (int)affinity.getColor()[2]);
        int colorTwo = FastColor.ABGR32.color((int)255, (int)affinity.getSecondaryColor()[0], (int)affinity.getSecondaryColor()[1], (int)affinity.getSecondaryColor()[2]);
        float capacity = ((EldrinCapacitorPermissionsContainer)this.menu).getCapacity(affinity);
        if (capacity > 0.0f) {
            float amount = ((EldrinCapacitorPermissionsContainer)this.menu).getAmount(affinity);
            float pct = Math.min(amount / capacity, 1.0f);
            int fillAmt = pct <= 0.0f ? 0 : (int)(20.0f * pct);
            pGuiGraphics.fillGradient(x + 6, y + 6, x + 6 + fillAmt, y + 9, colorOne, colorTwo);
            if (this.cursorIsWithin(mouseX, mouseY, x + 6, y + 6, 20, 3)) {
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.buffer", (Object[])new Object[]{Float.valueOf(amount), Float.valueOf(capacity), affinity.toString()}).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
            }
        }
    }

    private void blitSupplyStatus(GuiGraphics pGuiGraphics, Affinity affinity, int x, int y, int mouseX, int mouseY) {
        if (((EldrinCapacitorPermissionsContainer)this.menu).supplies(affinity)) {
            pGuiGraphics.blit(texture, x, y, 248, 0, 8, 8);
            if (this.cursorIsWithin(mouseX, mouseY, x, y, 8, 8)) {
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.provides_type", (Object[])new Object[]{affinity.toString()}).withStyle(ChatFormatting.GREEN));
            }
        } else {
            pGuiGraphics.blit(texture, x, y, 248, 8, 8, 8);
            if (this.cursorIsWithin(mouseX, mouseY, x, y, 8, 8)) {
                this.tooltip.add((Component)Component.translatable((String)"gui.mna.conduit.does_not_provide_type", (Object[])new Object[]{affinity.toString()}).withStyle(ChatFormatting.RED));
            }
        }
    }

    private void blitShareStatus(GuiGraphics pGuiGraphics, boolean shared, int x, int y) {
        if (shared) {
            pGuiGraphics.blit(texture, x, y, 248, 0, 8, 8);
        } else {
            pGuiGraphics.blit(texture, x, y + 9, 248, 8, 8, 8);
        }
    }

    protected boolean cursorIsWithin(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= this.leftPos + x && mouseX <= this.leftPos + x + w && mouseY >= this.topPos + y && mouseY <= this.topPos + y + h;
    }
}


