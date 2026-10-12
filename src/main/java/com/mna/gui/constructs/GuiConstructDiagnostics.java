 
package com.mna.gui.constructs;

import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.entities.attributes.AttributeInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.gui.GuiTextures;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.gui.widgets.entity.ConstructDiagnosticsDisplay;
import com.mna.network.ClientMessageDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class GuiConstructDiagnostics
extends Screen {
    private Construct eac;
    private static final int WHITE = FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255);
    private List<Component> tooltip = null;
    private int curMouseX = 0;
    private int curMouseY = 0;
    private int hoveredSlot = -1;

    public GuiConstructDiagnostics(Construct eac) {
        super((Component)Component.literal((String)""));
        this.eac = eac;
        eac.setRequestingDiagnostics(true);
        this.tooltip = new ArrayList<Component>();
    }

    protected void init() {
        super.init();
        this.addRenderableOnly((Renderable)new ConstructDiagnosticsDisplay(this.minecraft.screen.width / 2 - 128, this.minecraft.screen.height / 2 - 128, this.eac));
        this.addRenderableWidget(new MNAImageButton(this.minecraft.screen.width / 2 + 1, this.minecraft.screen.height / 2 - 128, 14, 14, 24, 216, 14, GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, 256, 256, button -> {
            if (HUDOverlayRenderer.instance.getConstructDisplay().isValid()) {
                HUDOverlayRenderer.instance.setTrackedConstruct(null);
            } else {
                HUDOverlayRenderer.instance.setTrackedConstruct(this.eac);
            }
        }));
    }

    public void removed() {
        super.removed();
        if (HUDOverlayRenderer.instance.getTrackedConstruct() != this.eac) {
            this.eac.setRequestingDiagnostics(false);
        }
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.tooltip.clear();
        this.hoveredSlot = -1;
        this.curMouseX = mouseX;
        this.curMouseY = mouseY;
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int cX = this.minecraft.screen.width / 2 - 128;
        int cY = this.minecraft.screen.height / 2 - 128;
        pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, cX, cY, 0, 0, 256, 215);
        int yPos = 0;
        int xPos = 0;
        int step = 0;
        if (this.eac.isAlive()) {
            IConstructConstruction data = this.eac.getConstructData();
            xPos = cX + 151;
            yPos = cY + 118;
            step = 13;
            this.renderTierIcon(pGuiGraphics, xPos, yPos, this.eac.getEquivalentTier());
            xPos += step;
            for (ConstructCapability cap : data.getEnabledCapabilities()) {
                this.renderCapabilityIcon(pGuiGraphics, xPos, yPos, cap);
                if ((xPos += step) <= cX + 242) continue;
                xPos = cX + 151;
                yPos += step;
            }
            Fluid stored = this.eac.getStoredFluid();
            MutableComponent storedFluid = Component.translatable((String)"");
            if (stored != null) {
                storedFluid = Component.translatable((String)BuiltInRegistries.FLUID.getKey(this.eac.getStoredFluid()).toString());
            }
            this.renderStatText(pGuiGraphics, cX + 165, cY + 6, (Component)Component.literal((String)String.format("%.1f", Float.valueOf(this.eac.getHealth()))), (Component)Component.translatable((String)"gui.mna.construct.health"));
            this.renderStatText(pGuiGraphics, cX + 165, cY + 19, (Component)Component.literal((String)String.format("%d", (int)this.eac.getAttribute(AttributeInit.INTELLIGENCE).getValue())), (Component)Component.translatable((String)"gui.mna.construct.intelligence"));
            this.renderStatText(pGuiGraphics, cX + 165, cY + 32, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(AttributeInit.PERCEPTION_DISTANCE).getValue()))), (Component)Component.translatable((String)"gui.mna.construct.perception"));
            this.renderStatText(pGuiGraphics, cX + 165, cY + 45, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(Attributes.ATTACK_SPEED).getValue()))), (Component)Component.translatable((String)"gui.mna.construct.attack_speed"));
            this.renderStatText(pGuiGraphics, cX + 165, cY + 58, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(Attributes.ATTACK_DAMAGE).getValue()))), (Component)Component.translatable((String)"gui.mna.construct.damage"));
            this.renderStatText(pGuiGraphics, cX + 165, cY + 71, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(AttributeInit.RANGED_DAMAGE).getValue()))), (Component)Component.translatable((String)"gui.mna.construct.ranged_damage"));
            this.renderStatText(pGuiGraphics, cX + 165, cY + 84, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(Attributes.ATTACK_KNOCKBACK).getValue()))), (Component)Component.translatable((String)"gui.mna.construct.knockback"));
            this.renderStatText(pGuiGraphics, cX + 165, cY + 97, (Component)Component.literal((String)String.format("%.1f", Float.valueOf(this.eac.getBuoyancy()))), (Component)Component.translatable((String)"gui.mna.construct.buoyancy"));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 6, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(Attributes.MOVEMENT_SPEED).getValue() * 10.0f))), (Component)Component.translatable((String)"gui.mna.construct.move_speed"));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 19, (Component)Component.literal((String)String.format("%d", (int)this.eac.getAttribute(Attributes.ARMOR).getValue())), (Component)Component.translatable((String)"gui.mna.construct.armor"));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 32, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()))), (Component)Component.translatable((String)"gui.mna.construct.toughness"));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 45, (Component)Component.literal((String)String.format("%.1f", Float.valueOf(data.calculateExplosionResistance()))), (Component)Component.translatable((String)"gui.mna.construct.explosion_resist"));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 58, (Component)Component.literal((String)String.format("%.1f", Float.valueOf((float)this.eac.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue()))), (Component)Component.translatable((String)"gui.mna.construct.knockback_resist"));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 71, (Component)Component.literal((String)String.format("%d", this.eac.getSlots())), (Component)Component.translatable((String)"gui.mna.construct.inventory_slots"));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 84, (Component)Component.literal((String)String.format("%d", this.eac.getStoredFluidAmount())), (Component)Component.translatable((String)"gui.mna.construct.stored_fluid_amount", (Object[])new Object[]{storedFluid.getString(), this.eac.getTankCapacity(0)}));
            this.renderStatText(pGuiGraphics, cX + 217, cY + 97, (Component)Component.literal((String)String.format("%d", (int)this.eac.getMana())), (Component)Component.translatable((String)"gui.mna.construct.mana", (Object[])new Object[]{(int)this.eac.getMaxMana()}));
            this.renderItem(pGuiGraphics, cX + 152, cY + 168, 0, this.eac.getMainHandItem());
            this.renderItem(pGuiGraphics, cX + 234, cY + 168, 1, this.eac.getOffhandItem());
            this.renderItem(pGuiGraphics, cX + 152, cY + 186, 2, this.eac.getConstructData().getHat());
            this.renderItem(pGuiGraphics, cX + 234, cY + 186, 3, this.eac.getConstructData().getBanner());
            if (this.eac.getSlots() > 0) {
                step = 18;
                for (int i = 0; i < 9 && i < this.eac.getSlots(); ++i) {
                    this.renderItem(pGuiGraphics, cX + 175 + step * (i % 3), cY + 150 + (int)((double)step * Math.floor(i / 3)), i + 4, this.eac.getStackInSlot(i));
                }
                MutableComponent text = Component.literal((String)String.format("%d", this.eac.getSlotLimit(0)));
                int iconWidth = 14;
                int width = this.font.width((FormattedText)text) + iconWidth;
                xPos = cX + 201 - width / 2;
                pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, xPos, cY + 206, ((Integer)ConstructCapability.ITEM_STORAGE.getIconCoords().getFirst()).intValue(), ((Integer)ConstructCapability.ITEM_STORAGE.getIconCoords().getSecond()).intValue(), 11, 11);
                this.renderStatText(pGuiGraphics, xPos + iconWidth, cY + 206, (Component)text, (Component)Component.literal((String)"Storage capacity of each slot in the construct's backpack."));
            }
        }
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.tooltip != null) {
            ArrayList split = new ArrayList();
            this.tooltip.forEach(line -> split.addAll(this.font.split((FormattedText)line, 128)));
            pGuiGraphics.renderTooltip(this.font, split, mouseX, mouseY);
        }
    }

    private void renderStatText(GuiGraphics pGuiGraphics, int x, int y, Component c, Component tooltip) {
        int width = this.font.width((FormattedText)c);
        int iconPadding = 16;
        Objects.requireNonNull(this.font);
        if (this.isCursorWithin(x - iconPadding, y, width + iconPadding, 9)) {
            this.tooltip.add(tooltip);
        }
        pGuiGraphics.drawString(this.font, c, x, y, WHITE, false);
    }

    private void renderCapabilityIcon(GuiGraphics pGuiGraphics, int x, int y, ConstructCapability cap) {
        int size = 11;
        if (this.isCursorWithin(x, y, size, size)) {
            this.tooltip.add((Component)Component.translatable((String)("gui.mna.construct." + cap.name().toLowerCase())));
        }
        pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, x, y, ((Integer)cap.getIconCoords().getFirst()).intValue(), ((Integer)cap.getIconCoords().getSecond()).intValue(), size, size);
    }

    private void renderTierIcon(GuiGraphics pGuiGraphics, int x, int y, int tier) {
        int size = 11;
        if (this.isCursorWithin(x, y, size, size)) {
            this.tooltip.add((Component)Component.translatable((String)"gui.mna.construct.tier", (Object[])new Object[]{tier}));
        }
        pGuiGraphics.blit(GuiTextures.Hud.BARS, x + 2, y, 250, 10 * tier, 6, 10);
    }

    private void renderItem(GuiGraphics pGuiGraphics, int x, int y, int index, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        int size = 18;
        if (this.isCursorWithin(x, y, size, size)) {
            this.tooltip.addAll(stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, (Player)this.minecraft.player, (TooltipFlag)TooltipFlag.Default.NORMAL));
            this.hoveredSlot = index;
        }
        pGuiGraphics.renderItem(stack, x, y);
        pGuiGraphics.renderItemDecorations(this.font, stack, x, y);
    }

    private boolean isCursorWithin(int x, int y, int width, int height) {
        return this.curMouseX > x && this.curMouseX < x + width && this.curMouseY > y && this.curMouseY < y + height;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hoveredSlot > -1) {
            ClientMessageDispatcher.sendAnimatedConstructDropItemMessage(this.eac, this.hoveredSlot);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}


