 
package com.mna.gui.block;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.gui.GuiTextures;
import com.mna.gui.block.SimpleWizardLabDeskGui;
import com.mna.gui.containers.block.ContainerEldrinFume;
import com.mna.network.ClientMessageDispatcher;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuiEldrinFume
extends SimpleWizardLabDeskGui<ContainerEldrinFume> {
    public GuiEldrinFume(ContainerEldrinFume container, Inventory inv, Component comp) {
        super(container, inv, (Component)Component.literal((String)""));
        this.imageWidth = 176;
        this.imageHeight = 158;
    }

    @Override
    public ResourceLocation texture() {
        return GuiTextures.WizardLab.ELDRIN_FUME;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (ManaAndArtifice.instance.proxy.getGameTicks() % 20L == 0L) {
            ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(false);
        }
        IWorldMagic magic = (IWorldMagic)this.minecraft.level.getData(WorldMagicProvider.MAGIC.get());
        IWellspringNodeRegistry wsRegistry = null;
        if (magic != null) {
            wsRegistry = magic.getWellspringRegistry();
        }
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.texture(), i, j, 0, 0, this.imageWidth, this.imageHeight);
        float motePct = ((ContainerEldrinFume)this.menu).getMotePctRemaining();
        pGuiGraphics.blit(this.texture(), this.leftPos + 58, this.topPos + 8, 196, 0, (int)(60.0f * motePct), 2);
        if (((ContainerEldrinFume)this.menu).isActive()) {
            float pct = ((ContainerEldrinFume)this.menu).getFuelPctRemaining();
            int progressHeight = 16 - (int)(16.0f * pct);
            pGuiGraphics.blit(this.texture(), this.leftPos + 79, this.topPos + 33 + 16 - progressHeight, 238, 18 - progressHeight, 20, progressHeight);
        }
        Affinity[] affinities = ((ContainerEldrinFume)this.menu).getGeneratingAffinities();
        int count = 0;
        for (Affinity toGenerate : affinities) {
            float x;
            ItemStack affStack = GuiTextures.affinityIcons.get(toGenerate);
            if (affStack.isEmpty()) continue;
            float scale = count > 0 ? 0.75f : 1.0f;
            float f = x = count == 0 ? 102.0f : 104.0f;
            float y = count == 0 ? 33.0f : (count == 1 ? 17.0f : 54.0f);
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().scale(scale, scale, scale);
            pGuiGraphics.renderItem(affStack, (int)(((float)this.leftPos + x) / scale), (int)(((float)this.topPos + y) / scale));
            pGuiGraphics.pose().popPose();
            int size = (int)(18.0f * scale);
            if ((float)mouseX >= (float)this.leftPos + x && (float)mouseX <= (float)this.leftPos + x + (float)size && (float)mouseY >= (float)this.topPos + y && (float)mouseY <= (float)this.topPos + y + (float)size) {
                float mult = wsRegistry == null ? 1.0f : wsRegistry.getEldrinGenerationMultiplierFor((Player)this.minecraft.player, toGenerate);
                float powerPerSecond = ((ContainerEldrinFume)this.menu).getPowerPerSecond(toGenerate) * mult;
                float generationRate = (float)(Math.floor(powerPerSecond * 10.0f) / 10.0);
                pGuiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"gui.mna.fume.power_gen", (Object[])new Object[]{Float.valueOf(generationRate), toGenerate.toString()}), mouseX, mouseY);
            }
            ++count;
        }
        if (((ContainerEldrinFume)this.menu).isPowerFull((Level)this.minecraft.level) && mouseX >= this.leftPos + (Integer)this.goButtonPos().getFirst() && mouseX <= this.leftPos + (Integer)this.goButtonPos().getFirst() + (Integer)this.goButtonSize().getFirst() && mouseY >= this.topPos + (Integer)this.goButtonPos().getSecond() && mouseY <= this.topPos + (Integer)this.goButtonPos().getSecond() + (Integer)this.goButtonSize().getSecond()) {
            Object affinityNames = "";
            if (affinities.length == 1) {
                affinityNames = affinities[0].toString();
            } else {
                for (Affinity toGenerate : affinities) {
                    affinityNames = (String)affinityNames + toGenerate.toString() + ", ";
                }
                affinityNames = ((String)affinityNames).substring(0, ((String)affinityNames).length() - 2);
            }
            pGuiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"gui.mna.fume.power_full", (Object[])new Object[]{affinityNames}), mouseX, mouseY);
        }
    }

    @Override
    protected Pair<Integer, Integer> goButtonPos() {
        return new Pair(63, 36);
    }

    @Override
    protected Pair<Integer, Integer> goButtonUV() {
        return new Pair(246, 18);
    }

    @Override
    protected Pair<Integer, Integer> goButtonSize() {
        return new Pair(10, 10);
    }

    @Override
    protected int goButtonHoverOffset() {
        return 0;
    }
}

