 
package com.mna.gui.block;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.spells.SpellValidityResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiable;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.SearchableGui;
import com.mna.gui.containers.block.ContainerInscriptionTable;
import com.mna.gui.widgets.AttributeButton;
import com.mna.gui.widgets.BorderedImageButton;
import com.mna.gui.widgets.ReagentList;
import com.mna.gui.widgets.SpellPartList;
import com.mna.network.messages.to_client.ShowDidYouKnow;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.client.gui.components.ImageButton;

public class GuiInscriptionTable
extends SearchableGui<ContainerInscriptionTable> {
    ImageButton buildButton;
    ImageButton[] pieceWidgets;
    ImageButton activeShapeButton;
    ImageButton activeComponentButton;
    ImageButton[] activeModifierButtons;
    ArrayList<ImageButton> shapeAttributeButtons;
    ArrayList<ImageButton> componentAttributeButtons;
    private SpellPartList spellPartList;
    private ReagentList reagentList;
    final float textScaleFactor = 0.5f;
    final int textColor = 4467972;
    final int colWidth = 38;
    final int rowHeight = 13;
    final int texSize = 32;
    final List<Component> currentTooltip;
    MutableComponent craftMessage = null;
    SpellValidityResult lastResult;
    IPlayerProgression playerProgression;
    private static long lastModifierClickTime = -1L;
    private static long clickCount = 0L;

    public GuiInscriptionTable(ContainerInscriptionTable screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.activeModifierButtons = new ImageButton[3];
        this.currentTooltip = new ArrayList<Component>();
    }

    public void addTooltipLine(Component comp) {
        this.currentTooltip.add(comp);
    }

    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_, double p_scrollX) {
        if (this.spellPartList.isMouseOver(p_mouseScrolled_1_, p_mouseScrolled_3_)) {
            return this.spellPartList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_, 0.0);
        }
        if (this.reagentList.isMouseOver(p_mouseScrolled_1_, p_mouseScrolled_3_)) {
            return this.reagentList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_, 0.0);
        }
        return true;
    }

    protected void init() {
        super.init();
        this.playerProgression = (IPlayerProgression)this.minecraft.player.getData(PlayerProgressionProvider.PROGRESSION.get());
        this.spellPartList = new SpellPartList(true, false, this.leftPos, this.topPos, 4, 53, 64, 111, s -> this.OnShapeClicked((Shape)s), c -> this.OnComponentClicked((SpellEffect)c), m -> this.OnModifierClicked((Modifier)m), tt -> this.currentTooltip.add((Component)tt)).enableTierChecks();
        this.addRenderableWidget(this.spellPartList);
        this.reagentList = new ReagentList(this.leftPos, this.topPos, 196, 111, 51, 53, this::addTooltipLine);
        this.addRenderableWidget(this.reagentList);
        int tbWidth = 66;
        int x = this.leftPos + 6;
        int y = this.topPos + 30;
        this.shapeAttributeButtons = new ArrayList();
        this.componentAttributeButtons = new ArrayList();
        this.setupBuildButton();
        if (((ContainerInscriptionTable)this.menu).getCurrentShape() != null) {
            this.setupShapeWidgetsFor(((ContainerInscriptionTable)this.menu).getCurrentShape().getPart());
        }
        if (((ContainerInscriptionTable)this.menu).getCurrentComponent() != null) {
            this.setupComponentWidgetsFor(((ContainerInscriptionTable)this.menu).getCurrentComponent().getPart());
        }
        for (int i = 0; i < 3; ++i) {
            if (((ContainerInscriptionTable)this.menu).getCurrentModifier(i) == null) continue;
            this.setupModifierWidgetsFor(((ContainerInscriptionTable)this.menu).getCurrentModifier(i), i);
        }
        this.reagentList.reInit(((ContainerInscriptionTable)this.menu).getCurrentReagents());
        this.initSearch(x, y, tbWidth, 16);
    }

    @Override
    protected void searchTermChanged(String newTerm) {
        this.currentSearchTerm = newTerm.toLowerCase();
        this.spellPartList.clear();
        this.spellPartList.reInit(this.currentSearchTerm);
        this.spellPartList.setScrollAmount(0.0);
    }

    private void setupShapeAttributeButtons(Shape shape) {
        this.clearAttributeButtons(this.shapeAttributeButtons);
        if (shape == null) {
            return;
        }
        this.setupAttributeButtons(115, 34, this.shapeAttributeButtons, shape);
    }

    private void setupComponentAttributeButtons(SpellEffect component) {
        this.clearAttributeButtons(this.componentAttributeButtons);
        if (component == null) {
            return;
        }
        this.setupAttributeButtons(115, 81, this.componentAttributeButtons, component);
    }

    private void setupAttributeButtons(int xStart, int yStart, ArrayList<ImageButton> addTo, IModifiable<? extends ISpellComponent> modifiable) {
        int count = 0;
        for (AttributeValuePair attribute : modifiable.getModifiableAttributes()) {
            boolean attributeModifiable = false;
            for (int i = 0; i < 3; ++i) {
                Modifier modifier = ((ContainerInscriptionTable)this.menu).getCurrentModifier(i);
                if (modifier == null || !modifier.modifiesType(attribute.getAttribute())) continue;
                attributeModifiable = true;
                break;
            }
            int row = (int)Math.floor(count / 2);
            int col = count % 2;
            int xOffset = xStart + 38 * col;
            int yOffset = yStart + 13 * row;
            if (attributeModifiable) {
                ImageButton upButton = (ImageButton)this.addRenderableWidget(new MNAImageButton(this.leftPos + xOffset + 16, this.topPos + yOffset + 1, 6, 3, 1, 49, 0, GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 128, 128, button -> {
                    ((ContainerInscriptionTable)this.menu).increaseAttribute((Player)this.minecraft.player, modifiable, attribute.getAttribute(), (Level)this.minecraft.level, Screen.hasShiftDown());
                    GuiInscriptionTable.checkAndShowShiftTooltip();
                }));
                ImageButton downButton = (ImageButton)this.addRenderableWidget(new MNAImageButton(this.leftPos + xOffset + 16, this.topPos + yOffset + 6, 6, 3, 1, 54, 0, GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 128, 128, button -> {
                    ((ContainerInscriptionTable)this.menu).decreaseAttribute((Player)this.minecraft.player, modifiable, attribute.getAttribute(), (Level)this.minecraft.level, Screen.hasShiftDown());
                    GuiInscriptionTable.checkAndShowShiftTooltip();
                }));
                addTo.add(upButton);
                addTo.add(downButton);
            }
            Point texCoord = GuiTextures.Attribute_Icon_Mappings.get(attribute.getAttribute());
            int blitSize = 8;
            float scaleFactor = (float)blitSize / 52.0f;
            ImageButton iconButton = (ImageButton)this.addRenderableWidget(new AttributeButton(this.leftPos + xOffset + 6, this.topPos + yOffset + 1, blitSize, blitSize, (int)((float)texCoord.x * scaleFactor), (int)((float)texCoord.y * scaleFactor), 0, GuiTextures.Widgets.ATTRIBUTE_ICONS, (int)(208.0f * scaleFactor), (int)(208.0f * scaleFactor), button -> {}, new String[]{attribute.getAttribute().getLocaleKey(), ((ISpellComponent)(modifiable)).getDescriptionTooltip(attribute.getAttribute())}, this::addTooltipLine));
            addTo.add(iconButton);
            ++count;
        }
    }

    public static void checkAndShowShiftTooltip() {
        Minecraft mc = Minecraft.getInstance();
        if (!Screen.hasShiftDown()) {
            if (mc.level.getGameTime() - lastModifierClickTime < 200L) {
                if (++clickCount >= 4L) {
                    ManaAndArtifice.instance.proxy.showDidYouKnow((Player)mc.player, ShowDidYouKnow.Messages.MODIFIER_SHIFT);
                }
            } else {
                clickCount = 0L;
            }
            lastModifierClickTime = mc.level.getGameTime();
        }
    }

    private void clearAttributeButtons(ArrayList<ImageButton> attributeButtons) {
        for (ImageButton button : attributeButtons) {
            this.removeWidget((GuiEventListener)button);
        }
        attributeButtons.clear();
    }

    private void setupBuildButton() {
        this.buildButton = (ImageButton)this.addRenderableWidget(new MNAImageButton(this.leftPos + 81, this.topPos + 9, 25, 14, 0, 20, 0, GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 128, 128, button -> ((ContainerInscriptionTable)this.menu).sendStartBuild()));
        this.buildButton.visible = false;
        this.buildButton.active = false;
    }

    private void setupShapeWidgetsFor(Shape shape) {
        int texSize = 32;
        if (shape != null) {
            if (this.activeShapeButton != null) {
                this.removeWidget((GuiEventListener)this.activeShapeButton);
            }
            this.activeShapeButton = (ImageButton)this.addRenderableWidget(new BorderedImageButton(this.leftPos + 82, this.topPos + 36, texSize, texSize, 0, 0, 0, shape.getGuiIcon(), texSize, texSize, button -> {
                if (!((ContainerInscriptionTable)this.menu).setCurrentShape(null, (Level)this.minecraft.level)) {
                    return;
                }
                this.removeWidget((GuiEventListener)button);
                this.setupShapeWidgetsFor(null);
                this.reagentList.reInit(((ContainerInscriptionTable)this.menu).getCurrentReagents());
            }).setBorder(shape.isSilverSpell()));
        }
        this.setupShapeAttributeButtons(shape);
    }

    private void OnShapeClicked(Shape shape) {
        if (!((ContainerInscriptionTable)this.menu).setCurrentShape(shape, (Level)this.minecraft.level)) {
            return;
        }
        this.setupShapeWidgetsFor(shape);
        if (this.searchBox != null) {
            this.searchBox.setHighlightPos(0);
        }
        this.reagentList.reInit(((ContainerInscriptionTable)this.menu).getCurrentReagents());
    }

    private void setupComponentWidgetsFor(SpellEffect component) {
        int texSize = 32;
        if (component != null) {
            if (this.activeComponentButton != null) {
                this.removeWidget((GuiEventListener)this.activeComponentButton);
            }
            this.activeComponentButton = (ImageButton)this.addRenderableWidget(new BorderedImageButton(this.leftPos + 82, this.topPos + 83, 32, 32, 0, 0, 0, component.getGuiIcon(), 32, 32, button -> {
                if (!((ContainerInscriptionTable)this.menu).setCurrentComponent(null, (Level)this.minecraft.level)) {
                    return;
                }
                this.removeWidget((GuiEventListener)button);
                this.setupComponentWidgetsFor(null);
                this.reagentList.reInit(((ContainerInscriptionTable)this.menu).getCurrentReagents());
            }).setBorder(component.isSilverSpell()));
        }
        this.setupComponentAttributeButtons(component);
    }

    private void OnComponentClicked(SpellEffect component) {
        if (!((ContainerInscriptionTable)this.menu).setCurrentComponent(component, (Level)this.minecraft.level)) {
            return;
        }
        this.setupComponentWidgetsFor(component);
        if (this.searchBox != null) {
            this.searchBox.setHighlightPos(0);
        }
        this.reagentList.reInit(((ContainerInscriptionTable)this.menu).getCurrentReagents());
    }

    private void setupModifierWidgetsFor(Modifier modifier, int index) {
        ModifiedSpellPart<SpellEffect> curComp;
        ModifiedSpellPart<Shape> curShape;
        if (modifier != null) {
            this.activeModifierButtons[index] = (ImageButton)this.addRenderableWidget(new BorderedImageButton(this.leftPos + 82 + 37 * index, this.topPos + 130, 32, 32, 0, 0, 0, modifier.getGuiIcon(), 32, 32, button -> {
                if (!((ContainerInscriptionTable)this.menu).setCurrentModifier(index, null, (Level)this.minecraft.level)) {
                    return;
                }
                this.removeWidget((GuiEventListener)button);
                this.setupModifierWidgetsFor(null, index);
                this.activeModifierButtons[index] = null;
                this.reagentList.reInit(((ContainerInscriptionTable)this.menu).getCurrentReagents());
            }).setBorder(modifier.isSilverSpell()));
        }
        if ((curShape = ((ContainerInscriptionTable)this.menu).getCurrentShape()) != null) {
            this.setupShapeAttributeButtons(curShape.getPart());
        }
        if ((curComp = ((ContainerInscriptionTable)this.menu).getCurrentComponent()) != null) {
            this.setupComponentAttributeButtons(curComp.getPart());
        }
    }

    private void OnModifierClicked(Modifier modifier) {
        int i;
        if (this.searchBox != null) {
            this.searchBox.setHighlightPos(0);
        }
        for (i = 0; i < 3; ++i) {
            if (((ContainerInscriptionTable)this.menu).getCurrentModifier(i) != modifier) continue;
            return;
        }
        for (i = 0; i < 3; ++i) {
            if (this.activeModifierButtons[i] != null) continue;
            if (!((ContainerInscriptionTable)this.menu).setCurrentModifier(i, modifier, (Level)this.minecraft.level)) {
                return;
            }
            this.setupModifierWidgetsFor(modifier, i);
            break;
        }
        this.reagentList.reInit(((ContainerInscriptionTable)this.menu).getCurrentReagents());
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.currentTooltip.clear();
        this.lastResult = ((ContainerInscriptionTable)this.menu).isReadyToBuild(this.playerProgression);
        if (this.lastResult == SpellValidityResult.READY) {
            this.buildButton.visible = true;
            this.buildButton.active = true;
            this.craftMessage = null;
        } else {
            this.buildButton.visible = false;
            this.buildButton.active = false;
            if (!this.lastResult.getTranslationKey().isEmpty()) {
                this.craftMessage = Component.translatable((String)this.lastResult.getTranslationKey()).withStyle(ChatFormatting.RED);
            }
        }
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.currentTooltip.size() > 0) {
            pGuiGraphics.renderTooltip(this.font, this.currentTooltip, Optional.empty(), mouseX, mouseY);
        } else {
            this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        float pct;
        int col;
        int row;
        int xPos = 140;
        int yPos = 37;
        int count = 0;
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
        if (((ContainerInscriptionTable)this.menu).getCurrentShape() != null) {
            for (Attribute attribute : ((ContainerInscriptionTable)this.menu).getCurrentShape().getContainedAttributes()) {
                row = (int)Math.floor(count / 2);
                col = count % 2;
                this.drawAttributeValue(pGuiGraphics, xPos + col * 38, yPos + row * 13, ((ContainerInscriptionTable)this.menu).getCurrentShape().getValueWithoutMultipliers(attribute));
                ++count;
            }
        }
        xPos = 140;
        count = 0;
        yPos = 84;
        if (((ContainerInscriptionTable)this.menu).getCurrentComponent() != null) {
            for (Attribute attribute : ((ContainerInscriptionTable)this.menu).getCurrentComponent().getContainedAttributes()) {
                row = (int)Math.floor(count / 2);
                col = count % 2;
                this.drawAttributeValue(pGuiGraphics, xPos + col * 38, yPos + row * 13, ((ContainerInscriptionTable)this.menu).getCurrentComponent().getValueWithoutMultipliers(attribute));
                ++count;
            }
        }
        for (int i = 0; i < 3; ++i) {
            if (((ContainerInscriptionTable)this.menu).getCurrentModifier(i) == null) continue;
            String s = I18n.get((String)((ContainerInscriptionTable)this.menu).getCurrentModifier(i).getRegistryName().toString(), (Object[])new Object[0]);
            pGuiGraphics.drawString(this.font, s, (int)((float)(81 + i * 37) / 0.5f), 244, 4467972, false);
        }
        float summaryXPos = 202.0f;
        float summaryYPos = 33.0f;
        pGuiGraphics.drawString(this.font, I18n.get((String)"gui.mna.complexity", (Object[])new Object[0]) + ":", (int)(summaryXPos / 0.5f), (int)(summaryYPos / 0.5f), 4467972, false);
        if (((ContainerInscriptionTable)this.menu).getComplexity() <= (float)this.playerProgression.getTierMaxComplexity()) {
            pGuiGraphics.drawString(this.font, String.format("%.1f / %d", Float.valueOf(((ContainerInscriptionTable)this.menu).getComplexity()), this.playerProgression.getTierMaxComplexity()), (int)(summaryXPos / 0.5f), (int)((summaryYPos + 5.0f) / 0.5f), 4467972, false);
        } else {
            pGuiGraphics.drawString(this.font, String.format("%.1f / %d", Float.valueOf(((ContainerInscriptionTable)this.menu).getComplexity()), this.playerProgression.getTierMaxComplexity()), (int)(summaryXPos / 0.5f), (int)((summaryYPos + 5.0f) / 0.5f), ChatFormatting.DARK_RED.getColor().intValue(), false);
        }
        pGuiGraphics.drawString(this.font, I18n.get((String)"gui.mna.mana_cost", (Object[])new Object[0]) + ":", (int)(summaryXPos / 0.5f), (int)((summaryYPos + 13.0f) / 0.5f), 4467972, false);
        pGuiGraphics.drawString(this.font, String.format("%.1f", Float.valueOf(((ContainerInscriptionTable)this.menu).getManaCost((Player)this.minecraft.player))), (int)(summaryXPos / 0.5f), (int)((summaryYPos + 18.0f) / 0.5f), 4467972, false);
        if (((ContainerInscriptionTable)this.menu).getCraftTicks() <= 0) {
            RenderSystem.enableBlend();
            pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, (int)((summaryXPos + 3.0f) / 0.5f), (int)((summaryYPos + 25.0f) / 0.5f), 29.0f, 20.0f, 16, 16, 128, 128);
            pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, (int)((summaryXPos + 18.0f) / 0.5f), (int)((summaryYPos + 25.0f) / 0.5f), 45.0f, 20.0f, 16, 16, 128, 128);
            pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, (int)((summaryXPos + 33.0f) / 0.5f), (int)((summaryYPos + 25.0f) / 0.5f), 61.0f, 20.0f, 16, 16, 128, 128);
            pGuiGraphics.drawString(this.font, String.format("x%d", ((ContainerInscriptionTable)this.menu).getRequiredInk()), (int)((summaryXPos + 4.0f) / 0.5f), (int)((summaryYPos + 35.0f) / 0.5f), 4467972, false);
            pGuiGraphics.drawString(this.font, String.format("x%d", ((ContainerInscriptionTable)this.menu).getRequiredPaper()), (int)((summaryXPos + 19.0f) / 0.5f), (int)((summaryYPos + 35.0f) / 0.5f), 4467972, false);
            pGuiGraphics.drawString(this.font, String.format("x%d", ((ContainerInscriptionTable)this.menu).getRequiredAsh()), (int)((summaryXPos + 34.0f) / 0.5f), (int)((summaryYPos + 35.0f) / 0.5f), 4467972, false);
            int offsetY = 44;
            if (this.craftMessage != null) {
                List split_string = this.font.getSplitter().splitLines(this.craftMessage.getString(), 95, Style.EMPTY);
                for (FormattedText prop : (java.util.List<FormattedText>)split_string) {
                    pGuiGraphics.drawString(this.font, prop.getString(), (int)(summaryXPos / 0.5f), (int)((summaryYPos + (float)offsetY) / 0.5f), ChatFormatting.DARK_PURPLE.getColor().intValue(), false);
                    offsetY += 5;
                }
                offsetY += 5;
            }
            offsetY = 50;
            pGuiGraphics.drawString(this.font, I18n.get((String)"gui.mna.spell_reagents", (Object[])new Object[0]), (int)((summaryXPos - 2.0f) / 0.5f), (int)((summaryYPos + (float)offsetY + 22.0f) / 0.5f), 4467972, false);
        }
        pGuiGraphics.pose().popPose();
        if (((ContainerInscriptionTable)this.menu).getCraftTicks() > 0 && (pct = (float)((ContainerInscriptionTable)this.menu).getCraftTicksConsumed() / (float)((ContainerInscriptionTable)this.menu).getCraftTicks()) > 0.0f) {
            pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 110, 6, 0.0f, 0.0f, (int)(115.0f * pct), 20, 128, 128);
        }
    }

    private void drawAttributeValue(GuiGraphics pGuiGraphics, int x, int y, float value) {
        String valueString = String.format("%.1f", Float.valueOf(value));
        pGuiGraphics.drawString(this.font, valueString, (int)((float)x / 0.5f), (int)((float)y / 0.5f), 4467972, false);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (this.spellPartList.isScrolling()) {
            return this.spellPartList.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }
        if (this.reagentList.isScrolling()) {
            return this.reagentList.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
    }
}

