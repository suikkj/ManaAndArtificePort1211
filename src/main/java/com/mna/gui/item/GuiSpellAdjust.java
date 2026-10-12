 
package com.mna.gui.item;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiable;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.block.GuiInscriptionTable;
import com.mna.gui.containers.item.ContainerSpellAdjustments;
import com.mna.gui.widgets.AttributeButton;
import com.mna.gui.widgets.ImageButtonWithAlphaBlend;
import com.mna.gui.widgets.RGBPicker;
import com.mna.network.ClientMessageDispatcher;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuiSpellAdjust
extends GuiJEIDisable<ContainerSpellAdjustments> {
    ArrayList<ImageButton> shapeAttributeButtons;
    ArrayList<ImageButton> componentAttributeButtons;
    final float textScaleFactor;
    static final int textColor = FastColor.ARGB32.color((int)255, (int)49, (int)49, (int)49);
    static final int textColorLight = FastColor.ARGB32.color((int)255, (int)200, (int)200, (int)200);
    int colWidth = 38;
    int rowHeight = 13;
    int texSize = 32;
    int spellNameWidth;
    String spellName;
    final List<Component> currentTooltip;
    IPlayerRoteSpells playerRote;
    IPlayerProgression playerProgression;
    RGBPicker color;

    public GuiSpellAdjust(ContainerSpellAdjustments screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.textScaleFactor = Minecraft.getInstance().options.forceUnicodeFont().get() ? 1.0f : 0.7f;
        this.colWidth = 38;
        this.rowHeight = 13;
        this.texSize = 32;
        this.spellNameWidth = 1;
        this.spellName = "";
        this.playerRote = null;
        this.playerProgression = null;
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.currentTooltip = new ArrayList<Component>();
    }

    protected void init() {
        super.init();
        this.playerRote = (IPlayerRoteSpells)this.minecraft.player.getData(PlayerRoteSpellsProvider.ROTE.get());
        this.playerProgression = (IPlayerProgression)this.minecraft.player.getData(PlayerProgressionProvider.PROGRESSION.get());
        this.shapeAttributeButtons = new ArrayList();
        this.componentAttributeButtons = new ArrayList();
        this.spellName = ((ContainerSpellAdjustments)this.menu).getSpellName();
        this.spellNameWidth = this.font.width(this.spellName);
        this.addRenderableWidget(new ImageButtonWithAlphaBlend(this.leftPos + 223, this.topPos + 7, 16, 16, 223, 152, 0, GuiTextures.Items.SPELL_VALUES, 256, 256, button -> {
            this.onClose();
            ClientMessageDispatcher.sendRequestOpenSpellNameAndIconGUI();
        }));
        int overrideColor = ((ContainerSpellAdjustments)this.menu).getRecipe().getParticleColorOverride();
        this.color = new RGBPicker(this.leftPos + 80, this.topPos + 202, this::adjustSpellColors);
        this.color.setValue(FastColor.ARGB32.red((int)overrideColor), FastColor.ARGB32.green((int)overrideColor), FastColor.ARGB32.blue((int)overrideColor), FastColor.ARGB32.alpha((int)overrideColor));
        this.addRenderableWidget(this.color);
        this.setupShapeAttributeButtons(((ContainerSpellAdjustments)this.menu).getShape().getPart());
        this.setupComponentAttributeButtons(((ContainerSpellAdjustments)this.menu).getComponent().getPart());
    }

    private void setupShapeAttributeButtons(Shape shape) {
        this.clearAttributeButtons(this.shapeAttributeButtons);
        if (shape == null || ((ContainerSpellAdjustments)this.menu).isTranscribed()) {
            return;
        }
        this.setupAttributeButtons(122, 45, this.shapeAttributeButtons, shape);
    }

    private void setupAttributeButtons(int xStart, int yStart, ArrayList<ImageButton> addTo, IModifiable<? extends ISpellComponent> modifiable) {
        int count = 0;
        for (AttributeValuePair attribute : modifiable.getModifiableAttributes()) {
            boolean attributeModifiable = false;
            for (int i = 0; i < 3; ++i) {
                Modifier modifier = ((ContainerSpellAdjustments)this.menu).getModifier(i);
                if (modifier == null || !modifier.modifiesType(attribute.getAttribute())) continue;
                attributeModifiable = true;
                break;
            }
            int row = (int)Math.floor(count / 2);
            int col = count % 2;
            int xOffset = xStart + 38 * col;
            int yOffset = yStart + 13 * row;
            if (attributeModifiable) {
                ImageButtonWithAlphaBlend upButton = (ImageButtonWithAlphaBlend)this.addRenderableWidget(new ImageButtonWithAlphaBlend(this.leftPos + xOffset + 2, this.topPos + yOffset - 2, 6, 3, 1, 49, 0, GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 128, 128, button -> {
                    ((ContainerSpellAdjustments)this.menu).increaseAttribute((Player)this.minecraft.player, modifiable, attribute.getAttribute(), (Level)this.minecraft.level, Screen.hasShiftDown());
                    GuiInscriptionTable.checkAndShowShiftTooltip();
                }));
                ImageButtonWithAlphaBlend downButton = (ImageButtonWithAlphaBlend)this.addRenderableWidget(new ImageButtonWithAlphaBlend(this.leftPos + xOffset + 2, this.topPos + yOffset + 3, 6, 3, 1, 54, 0, GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 128, 128, button -> {
                    ((ContainerSpellAdjustments)this.menu).decreaseAttribute((Player)this.minecraft.player, modifiable, attribute.getAttribute(), (Level)this.minecraft.level, Screen.hasShiftDown());
                    GuiInscriptionTable.checkAndShowShiftTooltip();
                }));
                addTo.add(upButton);
                addTo.add(downButton);
            }
            Point texCoord = GuiTextures.Attribute_Icon_Mappings.get(attribute.getAttribute());
            int blitSize = 8;
            float scaleFactor = (float)blitSize / 52.0f;
            ImageButton iconButton = (ImageButton)this.addRenderableWidget(new AttributeButton(this.leftPos + xOffset - 8, this.topPos + yOffset - 2, blitSize, blitSize, (int)((float)texCoord.x * scaleFactor), (int)((float)texCoord.y * scaleFactor), 0, GuiTextures.Widgets.ATTRIBUTE_ICONS, (int)(208.0f * scaleFactor), (int)(208.0f * scaleFactor), button -> {}, new String[]{attribute.getAttribute().getLocaleKey(), ((ISpellComponent)(modifiable)).getDescriptionTooltip(attribute.getAttribute())}, this::addTooltipLine));
            addTo.add(iconButton);
            ++count;
        }
    }

    private void clearAttributeButtons(ArrayList<ImageButton> attributeButtons) {
        for (ImageButton button : attributeButtons) {
            this.removeWidget((GuiEventListener)button);
        }
        attributeButtons.clear();
    }

    private void setupComponentAttributeButtons(SpellEffect component) {
        this.clearAttributeButtons(this.componentAttributeButtons);
        if (component == null || ((ContainerSpellAdjustments)this.menu).isTranscribed()) {
            return;
        }
        this.setupAttributeButtons(122, 92, this.componentAttributeButtons, component);
    }

    private void adjustSpellColors(Integer[] colors) {
        ((ContainerSpellAdjustments)this.menu).getRecipe().setParticleColorOverride(FastColor.ARGB32.color((int)colors[3], (int)colors[0], (int)colors[1], (int)colors[2]));
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.color.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void removed() {
        super.removed();
        if (((ContainerSpellAdjustments)this.menu).getRecipe().isValid()) {
            ClientMessageDispatcher.sendSpellAdjustmentMessage(((ContainerSpellAdjustments)this.menu).getRecipe(), ((ContainerSpellAdjustments)this.menu).getHand());
        }
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.currentTooltip.clear();
        RenderSystem.enableBlend();
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.currentTooltip != null) {
            pGuiGraphics.renderTooltip(this.font, this.currentTooltip, Optional.empty(), mouseX, mouseY);
        }
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        int col;
        int row;
        int xPos = 133;
        int yPos = 45;
        int count = 0;
        int text_y = 12;
        List split_lines = this.font.getSplitter().splitLines(this.spellName, 150, Style.EMPTY);
        for (FormattedText line : (java.util.List<FormattedText>)split_lines) {
            String textLine = line.getString();
            int snw = this.font.width(textLine);
            pGuiGraphics.drawString(this.font, textLine, this.imageWidth / 2 - snw / 2, text_y, textColor, false);
            Objects.requireNonNull(this.font);
            text_y += 9;
        }
        ItemStack stack = ((ContainerSpellAdjustments)this.menu).getSpellStack();
        pGuiGraphics.renderItem(stack, 7, 7);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
        if (((ContainerSpellAdjustments)this.menu).getShape() != null) {
            pGuiGraphics.blit(((ContainerSpellAdjustments)this.menu).getShape().getPart().getGuiIcon(), 150, 88, 0.0f, 0.0f, 64, 64, 64, 64);
            for (Attribute attribute : ((ContainerSpellAdjustments)this.menu).getShape().getContainedAttributes()) {
                row = (int)Math.floor(count / 2);
                col = count % 2;
                this.drawAttributeValue(pGuiGraphics, xPos + col * 38, yPos + row * 13, ((ContainerSpellAdjustments)this.menu).getShape().getValueWithoutMultipliers(attribute));
                ++count;
            }
        }
        yPos += 47;
        count = 0;
        if (((ContainerSpellAdjustments)this.menu).getComponent() != null) {
            pGuiGraphics.blit(((ContainerSpellAdjustments)this.menu).getComponent().getPart().getGuiIcon(), 150, 182, 0.0f, 0.0f, 64, 64, 64, 64);
            for (Attribute attribute : ((ContainerSpellAdjustments)this.menu).getComponent().getContainedAttributes()) {
                row = (int)Math.floor(count / 2);
                col = count % 2;
                this.drawAttributeValue(pGuiGraphics, xPos + col * 38, yPos + row * 13, ((ContainerSpellAdjustments)this.menu).getComponent().getValueWithoutMultipliers(attribute));
                ++count;
            }
        }
        for (int i = 0; i < 3; ++i) {
            Modifier m = ((ContainerSpellAdjustments)this.menu).getModifier(i);
            if (m == null) continue;
            pGuiGraphics.blit(m.getGuiIcon(), 150 + 74 * i, 276, 0.0f, 0.0f, 64, 64, 64, 64);
        }
        pGuiGraphics.pose().popPose();
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(this.textScaleFactor, this.textScaleFactor, this.textScaleFactor);
        String header = I18n.get((String)"gui.mna.mana_cost", (Object[])new Object[0]) + ":";
        String manaCost = String.format("%.1f", Float.valueOf(((ContainerSpellAdjustments)this.menu).getManaCost((Player)this.minecraft.player)));
        String complexityHeader = I18n.get((String)"gui.mna.complexity", (Object[])new Object[0]) + ":";
        String complexity = String.format("%.1f / %d", Float.valueOf(((ContainerSpellAdjustments)this.menu).getComplexity((Player)this.minecraft.player)), this.playerProgression.getTierMaxComplexity());
        float manaCostWidth = (float)this.font.width(manaCost) * this.textScaleFactor;
        float headerWidth = (float)this.font.width(header) * this.textScaleFactor;
        float complexityHeaderWidth = (float)this.font.width(complexityHeader) * this.textScaleFactor;
        float complexityWidth = (float)this.font.width(complexity) * this.textScaleFactor;
        float padding = 2.0f;
        float summaryXPos = (float)(this.imageWidth / 2) - (complexityHeaderWidth + complexityWidth + padding) / 2.0f;
        float summaryYPos = 182.0f;
        pGuiGraphics.drawString(this.font, complexityHeader, (int)(summaryXPos / this.textScaleFactor), (int)(summaryYPos / this.textScaleFactor), textColor, false);
        summaryXPos += complexityHeaderWidth + padding;
        if (((ContainerSpellAdjustments)this.menu).getComplexity((Player)this.minecraft.player) <= (float)this.playerProgression.getTierMaxComplexity()) {
            pGuiGraphics.drawString(this.font, complexity, (int)(summaryXPos / this.textScaleFactor), (int)(summaryYPos / this.textScaleFactor), textColor, false);
        } else {
            pGuiGraphics.drawString(this.font, complexity, (int)(summaryXPos / this.textScaleFactor), (int)(summaryYPos / this.textScaleFactor), ChatFormatting.RED.getColor().intValue(), false);
        }
        summaryXPos = (float)(this.imageWidth / 2) - (headerWidth + manaCostWidth + padding) / 2.0f;
        float f = 184;
        Objects.requireNonNull(this.font);
        summaryYPos = f + 9.0f * this.textScaleFactor;
        pGuiGraphics.drawString(this.font, header, (int)(summaryXPos / this.textScaleFactor), (int)(summaryYPos / this.textScaleFactor), textColor, false);
        pGuiGraphics.drawString(this.font, manaCost, (int)((summaryXPos += headerWidth + padding) / this.textScaleFactor), (int)(summaryYPos / this.textScaleFactor), textColor, false);
        pGuiGraphics.pose().popPose();
    }

    private void drawAttributeValue(GuiGraphics pGuiGraphics, int x, int y, float value) {
        String valueString = String.format("%.1f", Float.valueOf(value));
        pGuiGraphics.drawString(this.font, valueString, (int)((float)x / 0.5f), (int)((float)y / 0.5f), textColor, false);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(GuiTextures.Items.SPELL_VALUES, i, j, 0.0f, 145.0f, 247, 30, this.imageWidth, this.imageHeight);
        pGuiGraphics.blit(GuiTextures.Items.SPELL_VALUES, i + this.imageWidth / 2 - 50, j + 170, 0.0f, 176.0f, 100, 30, this.imageWidth, this.imageHeight);
        pGuiGraphics.blit(GuiTextures.Items.SPELL_VALUES, i + this.imageWidth / 2 - 60, j + 35, 0.0f, 0.0f, 120, 144, this.imageWidth, this.imageHeight);
    }

    public void addTooltipLine(Component comp) {
        this.currentTooltip.add(comp);
    }
}

