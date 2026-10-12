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

import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.gui.GuiTextures;
import com.mna.gui.block.SimpleWizardLabDeskGui;
import com.mna.gui.containers.block.ContainerAffinityTinker;
import com.mna.gui.widgets.ImageItemStackButton;
import com.mna.items.ItemInit;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class GuiAffinityTinker
extends SimpleWizardLabDeskGui<ContainerAffinityTinker> {
    public static HashMap<Affinity, ItemStack> affinityIcons = new HashMap<Affinity, ItemStack>(){
        private static final long serialVersionUID = 1L;
        {
            this.put(Affinity.ARCANE, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_ARCANE.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.arcane")));
            this.put(Affinity.WIND, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_AIR.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.air")));
            this.put(Affinity.EARTH, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_EARTH.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.earth")));
            this.put(Affinity.WATER, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_WATER.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.water")));
            this.put(Affinity.FIRE, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_FIRE.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.fire")));
            this.put(Affinity.ENDER, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_ENDER.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.ender")));
            this.put(Affinity.ICE, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_ICE.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.ice")));
            this.put(Affinity.LIGHTNING, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_LIGHTNING.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.lightning")));
            this.put(Affinity.BLOOD, new ItemStack((ItemLike)ItemInit.GREATER_MOTE_BLOOD.get()).set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"affinity.mna.blood")));
        }
    };

    public GuiAffinityTinker(ContainerAffinityTinker container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 175;
        this.imageHeight = 232;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(39, 37, Affinity.WIND, 6);
        this.addButton(39, 67, Affinity.ENDER, 3);
        this.addButton(39, 97, Affinity.EARTH, 2);
        this.addButton(111, 37, Affinity.FIRE, 4);
        this.addButton(111, 67, Affinity.ARCANE, 1);
        this.addButton(111, 97, Affinity.WATER, 5);
        this.addButton(131, 17, Affinity.LIGHTNING, 8);
        this.addButton(131, 117, Affinity.ICE, 7);
        this.addButton(11, 67, Affinity.BLOOD, 9);
    }

    private void addButton(int x, int y, Affinity affinity, int index) {
        ImageItemStackButton btn_arcane = new ImageItemStackButton(this.leftPos + x, this.topPos + y, 26, 26, 230, 10, 0, this.texture(), 256, 256, b -> {
            if (!((ContainerAffinityTinker)this.menu).isActive()) {
                ((ContainerAffinityTinker)this.menu).clickMenuButton((Player)this.minecraft.player, index);
                this.minecraft.gameMode.handleInventoryButtonClick(((ContainerAffinityTinker)this.menu).containerId, index);
                this.activateButton((ImageItemStackButton)b);
            }
        }, this::setTooltip, affinityIcons.get(affinity)).setAffinityOffset(5, 5).setRenderBackground(((ContainerAffinityTinker)this.menu).getSelectedAffinity() == affinity);
        this.addRenderableWidget(btn_arcane);
    }

    public void setTooltip(List<Component> lines) {
    }

    public void activateButton(ImageItemStackButton b) {
        this.children().forEach(c -> {
            if (c == b) {
                b.setRenderBackground(true);
            } else if (c instanceof ImageItemStackButton) {
                ((ImageItemStackButton)(c)).setRenderBackground(false);
            }
        });
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.AFFINITY_TINKER;
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        Player player;
        Affinity[] values = new Affinity[]{Affinity.WIND, Affinity.FIRE, Affinity.ENDER, Affinity.ARCANE, Affinity.EARTH, Affinity.WATER};
        if (((ContainerAffinityTinker)this.menu).isActive()) {
            float pct = ((ContainerAffinityTinker)this.menu).getProgress();
            pGuiGraphics.blit(this.texture(), 84, 90, 222, 10, 8, (int)(36.0f * pct));
        }
        float xpPct = MathUtils.clamp01((player = ManaAndArtifice.instance.proxy.getClientPlayer()).isCreative() ? 1.0f : (float)player.totalExperience / (float)((ContainerAffinityTinker)this.menu).getXPCost());
        int VCoord = xpPct < 1.0f ? 5 : 0;
        pGuiGraphics.blit(this.texture(), 70, 40, 220, VCoord, (int)(36.0f * xpPct), 5);
        ItemStack stack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
        pGuiGraphics.pose().translate(0.5, 0.25, 0.0);
        pGuiGraphics.renderItem(stack, 35, 3);
        pGuiGraphics.pose().popPose();
        HashMap<Affinity, PowerStatus> powerReqs = ((ContainerAffinityTinker)this.menu).powerRequirementStatus();
        PowerStatus powerStatus = PowerStatus.NOT_REQUESTING;
        Affinity requestingAffinity = Affinity.UNKNOWN;
        for (Affinity aff : values) {
            if (!powerReqs.containsKey(aff)) continue;
            powerStatus = powerReqs.getOrDefault(aff, PowerStatus.NOT_REQUESTING);
            requestingAffinity = aff;
            break;
        }
        this.renderPowerConsumeStatusIcon(pGuiGraphics, mouseX - this.leftPos, mouseY - this.topPos, 104, 132, requestingAffinity, powerStatus);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(231, 36);
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(49, 129);
    }
}

