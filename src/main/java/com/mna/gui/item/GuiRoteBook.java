 
package com.mna.gui.item;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.sound.SFX;
import com.mna.api.spells.SpellValidityResult;
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
import com.mna.gui.base.SearchableGuiJeiDisable;
import com.mna.gui.block.GuiInscriptionTable;
import com.mna.gui.containers.item.ContainerRoteBook;
import com.mna.gui.widgets.AttributeButton;
import com.mna.gui.widgets.BorderedImageButton;
import com.mna.gui.widgets.ImageButtonWithAlphaBlend;
import com.mna.gui.widgets.RGBPicker;
import com.mna.gui.widgets.SpellPartList;
import com.mna.items.ItemInit;
import com.mna.items.SpellIconList;
import com.mna.items.sorcery.ItemSpell;
import com.mna.network.ClientMessageDispatcher;
import com.mna.tools.DidYouKnowHelper;
import com.mojang.datafixers.util.Pair;
import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.ImageButton;

public class GuiRoteBook
extends SearchableGuiJeiDisable<ContainerRoteBook> {
    private static final int MAX_COMPONENTS = 5;
    ImageButton[] pieceWidgets;
    ImageButton activeShapeButton;
    ImageButton[] activeComponentButtons;
    ArrayList<ImageButton> shapeAttributeButtons;
    HashMap<Integer, ArrayList<ImageButton>> componentAttributeButtons;
    ArrayList<ImageButton> inactiveCategoryButtons;
    ArrayList<ImageButton> activeCategoryButtons;
    private SpellPartList list;
    private Button nameAndIcon;
    final float textScaleFactor = 0.5f;
    static final int textColor = FastColor.ARGB32.color((int)255, (int)49, (int)49, (int)49);
    static final int textColorLight = FastColor.ARGB32.color((int)255, (int)200, (int)200, (int)200);
    final int colWidth = 38;
    final int rowHeight = 13;
    final int texSize = 32;
    final List<Component> currentTooltip;
    IPlayerRoteSpells playerRote = null;
    IPlayerProgression playerProgression = null;
    private boolean namingSpell = true;
    private int page = 0;
    private int numPages = 0;
    private int hoveredIndex;
    EditBox nameBox;
    Component nameValue;
    ModelButton currentButton;
    RGBPicker colors;
    private static ArrayList<Pair<Integer, Integer>> pipLocations = new ArrayList();
    ArrayList<ModelButton> iconButtons;
    private Button prev;
    private Button next;
    private Button clear;

    public GuiRoteBook(ContainerRoteBook screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.activeComponentButtons = new ImageButton[5];
        this.inactiveCategoryButtons = new ArrayList();
        this.activeCategoryButtons = new ArrayList();
        this.iconButtons = new ArrayList();
        this.numPages = (int)Math.floor((float)SpellIconList.ALL.length / 100.0f);
        this.currentTooltip = new ArrayList<Component>();
    }

    public void addTooltipLine(Component comp) {
        this.currentTooltip.add(comp);
    }

    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_, double p_scrollX) {
        return this.list.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_, 0.0);
    }

    protected void init() {
        this.clearWidgets();
        this.activeCategoryButtons.clear();
        this.inactiveCategoryButtons.clear();
        super.init();
        this.initSpellIcons();
        int tbWidth = 130;
        int x = this.leftPos + this.imageWidth / 2 - tbWidth / 2;
        int y = this.topPos + this.imageHeight - 95;
        this.initSearch(this.leftPos + 6, this.topPos + 185, 59, 18);
        this.playerRote = (IPlayerRoteSpells)this.minecraft.player.getData(PlayerRoteSpellsProvider.ROTE.get());
        this.playerProgression = (IPlayerProgression)this.minecraft.player.getData(PlayerProgressionProvider.PROGRESSION.get());
        this.nameAndIcon = (Button)this.addRenderableWidget(new MNAImageButton(this.leftPos + 228, this.topPos + 12, 16, 16, 228, 12, 0, GuiTextures.Items.BOOK_OF_ROTE, 256, 256, button -> this.toggleNaming()));
        this.list = new SpellPartList(false, true, this.leftPos, this.topPos, 66, 190, 117, 57, s -> this.OnShapeClicked((Shape)s), c -> this.OnComponentClicked((SpellEffect)c), m -> {}, tt -> this.currentTooltip.add((Component)tt));
        this.shapeAttributeButtons = new ArrayList();
        this.componentAttributeButtons = new HashMap();
        for (int i = 0; i < 5; ++i) {
            this.componentAttributeButtons.put(i, new ArrayList());
        }
        int sliderX = this.leftPos - 35;
        this.colors = new RGBPicker(sliderX, this.topPos + 211, this::adjustSpellColors);
        this.addRenderableWidget(this.colors);
        this.mapUIToCurrentSpell();
        x = this.leftPos + 256;
        y = this.topPos + 6;
        for (int i = 0; i < ((ContainerRoteBook)this.menu).getSize(); ++i) {
            int idx = i;
            if (i < 8) {
                this.inactiveCategoryButtons.add((ImageButton)this.addRenderableWidget(new IndexButton(x, y, 37, 22, 0, 0, 22, GuiTextures.Items.BOOK_OF_ROTE_EXTRAS, 256, 256, btn -> {
                    this.setActiveIndex(idx);
                    ((ContainerRoteBook)this.menu).changeIndex(idx);
                    ItemSpell.setCustomIcon(this.currentButton.icon, ((ContainerRoteBook)this.menu).getIconIndex());
                    this.mapUIToCurrentSpell();
                }, i + 1, false)));
                this.activeCategoryButtons.add((ImageButton)this.addRenderableWidget(new IndexButton(x, y, 37, 22, 0, 44, 22, GuiTextures.Items.BOOK_OF_ROTE_EXTRAS, 256, 256, btn -> {}, i + 1, true)));
            } else {
                this.inactiveCategoryButtons.add((ImageButton)this.addRenderableWidget(new IndexButton(x, y, 37, 22, 37, 0, 22, GuiTextures.Items.BOOK_OF_ROTE_EXTRAS, 256, 256, btn -> {
                    this.setActiveIndex(idx);
                    ((ContainerRoteBook)this.menu).changeIndex(idx);
                    ItemSpell.setCustomIcon(this.currentButton.icon, ((ContainerRoteBook)this.menu).getIconIndex());
                    this.mapUIToCurrentSpell();
                }, i + 1, false)));
                this.activeCategoryButtons.add((ImageButton)this.addRenderableWidget(new IndexButton(x, y, 37, 22, 37, 44, 22, GuiTextures.Items.BOOK_OF_ROTE_EXTRAS, 256, 256, btn -> {}, i + 1, true)));
            }
            y += 22;
            if (i != 7) continue;
            x = this.leftPos - 37;
            y = this.topPos + 6;
        }
        this.setActiveIndex(((ContainerRoteBook)this.menu).getActiveIndex());
        this.namingSpell = true;
        this.toggleNaming();
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_OPEN);
    }

    private void initSpellIcons() {
        int nameWidth = 218;
        int nameHeight = 256;
        int i = this.leftPos + this.imageWidth / 2 - nameWidth / 2;
        int j = this.topPos + this.imageHeight / 2 - nameHeight / 2;
        this.currentButton = new ModelButton(this.leftPos + 12, j + 12, -1, btn -> {});
        this.addRenderableWidget(this.currentButton);
        this.nameBox = new EditBox(this.minecraft.font, i + nameWidth / 2 - 80, j + 10, 160, 20, this.nameValue);
        this.nameBox.setValue(((ContainerRoteBook)this.menu).getName());
        this.nameBox.setMaxLength(60);
        this.nameBox.setResponder(this::nameChanged);
        this.addRenderableWidget(this.nameBox);
        this.nameBox.setCanLoseFocus(false);
        this.nameBox.setFocused(true);
        this.setFocused((GuiEventListener)this.nameBox);
        this.page = 0;
        this.clear = (Button)this.addRenderableWidget(new MNAImageButton(this.leftPos + this.imageWidth - 28, this.topPos + 12, 15, 18, 222, 7, 0, GuiTextures.Items.SPELL_CUSTOMIZE, 256, 256, button -> this.toggleNaming()));
        this.prev = (Button)this.addRenderableWidget(new MNAImageButton(this.leftPos + 9, this.topPos + this.imageHeight - 18, 9, 14, 247, 31, 14, GuiTextures.Items.SPELL_CUSTOMIZE, 256, 256, button -> {
            --this.page;
            if (this.page <= 0) {
                this.page = 0;
                this.prev.active = false;
            }
            this.next.active = true;
            this.initIconButtons();
            this.setFocused((GuiEventListener)this.nameBox);
        }));
        this.next = (Button)this.addRenderableWidget(new MNAImageButton(this.leftPos + this.imageWidth - 17, this.topPos + this.imageHeight - 18, 9, 14, 247, 60, 14, GuiTextures.Items.SPELL_CUSTOMIZE, 256, 256, button -> {
            ++this.page;
            if (this.page >= this.numPages) {
                this.page = this.numPages;
                this.next.active = false;
            }
            this.prev.active = true;
            this.initIconButtons();
            this.setFocused((GuiEventListener)this.nameBox);
        }));
        this.prev.active = false;
        i += 3;
        j += 20;
        this.initIconButtons();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (this.namingSpell) {
                this.toggleNaming();
            } else {
                this.onClose();
            }
            return true;
        }
        if (this.nameBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return this.nameBox.isFocused() && this.nameBox.isVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void adjustSpellColors(Integer[] argb) {
        ((ContainerRoteBook)this.menu).setCurColor(FastColor.ARGB32.color((int)argb[3], (int)argb[0], (int)argb[1], (int)argb[2]));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (this.namingSpell) {
            this.setFocused((GuiEventListener)this.nameBox);
            if (button == 1 && this.nameBox.isMouseOver(mouseX, mouseY)) {
                this.nameBox.setValue("");
            }
        } else if (button == 1 && this.searchBox.isMouseOver(mouseX, mouseY)) {
            this.searchBox.setValue("");
        }
        return false;
    }

    private void toggleNaming() {
        if (!this.namingSpell) {
            this.namingSpell = true;
            this.resetActiveSpell();
            this.list._active = false;
            this.nameAndIcon.active = false;
            this.nameAndIcon.visible = false;
            this.searchBox.active = false;
            this.searchBox.visible = false;
            this.colors.active = false;
            this.colors.visible = false;
            this.page = 0;
            this.prev.active = false;
            this.prev.visible = true;
            this.next.active = true;
            this.next.visible = true;
            this.clear.active = true;
            this.clear.visible = true;
            for (ModelButton btn : this.iconButtons) {
                btn.active = true;
                btn.visible = true;
            }
            this.nameBox.active = true;
            this.nameBox.visible = true;
            if (this.activeShapeButton != null) {
                this.activeShapeButton.active = false;
                this.activeShapeButton.visible = false;
            }
            if (this.activeComponentButtons != null) {
                for (int i = 0; i < this.activeComponentButtons.length; ++i) {
                    if (this.activeComponentButtons[i] == null) continue;
                    this.activeComponentButtons[i].active = false;
                    this.activeComponentButtons[i].visible = false;
                }
            }
        } else {
            this.namingSpell = false;
            this.page = 0;
            this.prev.active = false;
            this.prev.visible = false;
            this.next.active = false;
            this.next.visible = false;
            this.clear.active = false;
            this.clear.visible = false;
            for (ModelButton btn : this.iconButtons) {
                btn.active = false;
                btn.visible = false;
            }
            this.nameBox.active = false;
            this.nameBox.visible = false;
            this.nameAndIcon.active = true;
            this.nameAndIcon.visible = true;
            this.searchBox.active = true;
            this.searchBox.visible = true;
            this.list._active = true;
            this.colors.active = true;
            this.colors.visible = true;
            this.mapUIToCurrentSpell();
        }
    }

    private void nameChanged(String newName) {
        ((ContainerRoteBook)this.menu).setName(newName);
    }

    @Override
    protected void searchTermChanged(String newName) {
        this.currentSearchTerm = newName.toLowerCase();
        this.list.clear();
        this.list.reInit(this.currentSearchTerm);
    }

    private void initIconButtons() {
        int nameHeight = 256;
        int top = this.topPos + this.imageHeight / 2 - nameHeight / 2;
        for (ModelButton button2 : this.iconButtons) {
            this.removeWidget((GuiEventListener)button2);
        }
        this.iconButtons.clear();
        int x = this.leftPos + 26;
        int y = top + 45;
        int count = 1;
        for (int i = this.page * 100; i < SpellIconList.ALL.length; ++i) {
            int idx = i;
            ModelButton btn = new ModelButton(x, y, i, button -> {
                ((ContainerRoteBook)this.menu).setIconIndex(idx);
                ItemSpell.setCustomIcon(this.currentButton.icon, idx);
            });
            this.addRenderableWidget(btn);
            this.iconButtons.add(btn);
            x += 21;
            if (count % 10 == 0 && count != 0) {
                x = this.leftPos + 26;
                y += 21;
            }
            if (count == 100) break;
            ++count;
        }
        ItemSpell.setCustomIcon(this.currentButton.icon, ((ContainerRoteBook)this.menu).getIconIndex());
    }

    private void setActiveIndex(int index) {
        if (index < 0 || index >= this.activeCategoryButtons.size()) {
            return;
        }
        this.activeCategoryButtons.forEach(b -> {
            b.active = false;
            b.visible = false;
        });
        this.inactiveCategoryButtons.forEach(b -> {
            b.active = true;
            b.visible = true;
        });
        this.activeCategoryButtons.get((int)index).active = true;
        this.activeCategoryButtons.get((int)index).visible = true;
        this.inactiveCategoryButtons.get((int)index).active = false;
        this.inactiveCategoryButtons.get((int)index).visible = false;
    }

    private void resetActiveSpell() {
        this.setupShapeAttributeButtons(null);
        if (this.activeShapeButton != null) {
            this.removeWidget((GuiEventListener)this.activeShapeButton);
        }
        for (int index : this.componentAttributeButtons.keySet()) {
            this.clearAttributeButtons(this.componentAttributeButtons.get(index));
        }
    }

    private void mapUIToCurrentSpell() {
        int i;
        this.resetActiveSpell();
        for (i = 0; i < this.activeComponentButtons.length; ++i) {
            this.removeWidget((GuiEventListener)this.activeComponentButtons[i]);
            this.activeComponentButtons[i] = null;
        }
        if (((ContainerRoteBook)this.menu).getShape() != null) {
            this.setupShapeWidgetsFor(((ContainerRoteBook)this.menu).getShape().getPart());
        }
        for (i = 0; i < 5; ++i) {
            if (((ContainerRoteBook)this.menu).getComponent(i) == null) continue;
            this.setupComponentWidgetsFor(((ContainerRoteBook)this.menu).getComponent(i).getPart(), i);
        }
        this.nameBox.setValue(((ContainerRoteBook)this.menu).getName());
        int color = ((ContainerRoteBook)this.menu).getCurColor();
        this.colors.setValue(FastColor.ARGB32.red((int)color), FastColor.ARGB32.green((int)color), FastColor.ARGB32.blue((int)color), FastColor.ARGB32.alpha((int)color));
    }

    private void setupShapeAttributeButtons(Shape shape) {
        this.clearAttributeButtons(this.shapeAttributeButtons);
        if (shape == null) {
            return;
        }
        this.setupAttributeButtons(60, 48, this.shapeAttributeButtons, shape);
    }

    private void setupAttributeButtons(int xStart, int yStart, ArrayList<ImageButton> addTo, IModifiable<? extends ISpellComponent> modifiable) {
        int count = 0;
        for (AttributeValuePair attribute : modifiable.getModifiableAttributes()) {
            boolean attributeModifiable = false;
            for (Modifier m : Registries.Modifier.getValues()) {
                if (!m.modifiesType(attribute.getAttribute()) || (!this.playerRote.isRote(m) || this.playerProgression.getTier() < m.getTier(ManaAndArtifice.instance.proxy.getClientWorld())) && !this.minecraft.player.isCreative()) continue;
                attributeModifiable = true;
                break;
            }
            int row = (int)Math.floor(count / 2);
            int col = count % 2;
            int xOffset = xStart + 38 * col;
            int yOffset = yStart + 13 * row;
            if (attributeModifiable) {
                ImageButtonWithAlphaBlend upButton = (ImageButtonWithAlphaBlend)this.addRenderableWidget(new ImageButtonWithAlphaBlend(this.leftPos + xOffset + 1, this.topPos + yOffset - 3, 8, 5, 0, 48, 0, GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 128, 128, button -> {
                    ((ContainerRoteBook)this.menu).increaseAttribute((Player)this.minecraft.player, modifiable, attribute.getAttribute(), (Level)this.minecraft.level, Screen.hasShiftDown());
                    GuiInscriptionTable.checkAndShowShiftTooltip();
                }));
                ImageButtonWithAlphaBlend downButton = (ImageButtonWithAlphaBlend)this.addRenderableWidget(new ImageButtonWithAlphaBlend(this.leftPos + xOffset + 1, this.topPos + yOffset + 2, 8, 5, 0, 53, 0, GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, 128, 128, button -> {
                    ((ContainerRoteBook)this.menu).decreaseAttribute((Player)this.minecraft.player, modifiable, attribute.getAttribute(), (Level)this.minecraft.level, Screen.hasShiftDown());
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

    private void setupShapeWidgetsFor(Shape shape) {
        int texSize = 32;
        if (shape != null) {
            if (this.activeShapeButton != null) {
                this.removeWidget((GuiEventListener)this.activeShapeButton);
            }
            this.activeShapeButton = (ImageButton)this.addRenderableWidget(new BorderedImageButton(this.leftPos + 13, this.topPos + 47, texSize, texSize, 0, 0, 0, shape.getGuiIcon(), texSize, texSize, button -> {
                ((ContainerRoteBook)this.menu).setShape(null);
                this.mapUIToCurrentSpell();
            }).setBorder(shape.isSilverSpell()));
        }
        this.setupShapeAttributeButtons(shape);
    }

    private void OnShapeClicked(Shape shape) {
        ((ContainerRoteBook)this.menu).setShape(shape);
        this.mapUIToCurrentSpell();
        if (this.searchBox != null) {
            this.searchBox.setHighlightPos(0);
        }
    }

    private void setupComponentWidgetsFor(SpellEffect component, int index) {
        int texSize = 32;
        if (component != null) {
            if (this.activeComponentButtons[index] != null) {
                this.removeWidget((GuiEventListener)this.activeComponentButtons[index]);
            }
            AbstractMap.SimpleEntry<Integer, Integer> coords = this.getComponentRenderCoordinates(index);
            this.activeComponentButtons[index] = (ImageButton)this.addRenderableWidget(new BorderedImageButton(coords.getKey(), coords.getValue(), 32, 32, 0, 0, 0, component.getGuiIcon(), 32, 32, button -> {
                ((ContainerRoteBook)this.menu).removeComponent(index);
                this.mapUIToCurrentSpell();
            }).setBorder(component.isSilverSpell()));
        }
        this.setupComponentAttributeButtons(component, index);
    }

    private void setupComponentAttributeButtons(SpellEffect component, int index) {
        this.clearAttributeButtons(this.componentAttributeButtons.get(index));
        if (component == null) {
            return;
        }
        AbstractMap.SimpleEntry<Integer, Integer> coords = this.getComponentRenderCoordinates(index, -this.leftPos + 47, -this.topPos + 1);
        this.setupAttributeButtons(coords.getKey(), coords.getValue(), this.componentAttributeButtons.get(index), component);
    }

    private void OnComponentClicked(SpellEffect component) {
        if (this.searchBox != null) {
            this.searchBox.setHighlightPos(0);
        }
        for (int i = 0; i < 5; ++i) {
            if (((ContainerRoteBook)this.menu).getComponent(i) != null) continue;
            ((ContainerRoteBook)this.menu).addComponent(component);
            this.mapUIToCurrentSpell();
            return;
        }
    }

    private AbstractMap.SimpleEntry<Integer, Integer> getComponentRenderCoordinates(int index) {
        return this.getComponentRenderCoordinates(index, 0, 0);
    }

    private AbstractMap.SimpleEntry<Integer, Integer> getComponentRenderCoordinates(int index, int offsetX, int offsetY) {
        int x = this.leftPos + 13;
        int y = this.topPos + 94;
        if (index == 1 || index == 4) {
            y += 47;
        }
        if (index == 2) {
            y -= 47;
        }
        if (index > 1) {
            x += 124;
        }
        return new AbstractMap.SimpleEntry<Integer, Integer>(x + offsetX, y + offsetY);
    }

    public void onClose() {
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_CLOSE);
        super.onClose();
    }

    public void removed() {
        this.checkAndShowDamageStackingTip();
        super.removed();
        ((ContainerRoteBook)this.menu).copySpellChangesToInventory();
        ClientMessageDispatcher.sendRoteSpellsUpdate((ContainerRoteBook)this.menu);
    }

    private void checkAndShowDamageStackingTip() {
        Minecraft mc = Minecraft.getInstance();
        if (((ContainerRoteBook)this.menu).countNonDelayedDamageComponents() > 1) {
            DidYouKnowHelper.CheckAndShowDidYouKnow((Player)mc.player, "helptip.mna.damage_type_stacking");
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.currentTooltip.clear();
        this.hoveredIndex = -1;
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.currentTooltip.size() > 0) {
            pGuiGraphics.renderTooltip(this.font, this.currentTooltip, Optional.empty(), mouseX, mouseY);
        }
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        if (this.namingSpell) {
            return;
        }
        int xPos = 71;
        int yPos = 48;
        int count = 0;
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
        if (((ContainerRoteBook)this.menu).getShape() != null) {
            for (Attribute attribute : ((ContainerRoteBook)this.menu).getShape().getContainedAttributes()) {
                int row = (int)Math.floor(count / 2);
                int col = count % 2;
                this.drawAttributeValue(pGuiGraphics, xPos + col * 38, yPos + row * 13, ((ContainerRoteBook)this.menu).getShape().getValueWithoutMultipliers(attribute));
                ++count;
            }
        }
        for (int i = 0; i < 5; ++i) {
            AbstractMap.SimpleEntry<Integer, Integer> coords = this.getComponentRenderCoordinates(i, -this.leftPos + 58, -this.topPos + 1);
            xPos = coords.getKey();
            yPos = coords.getValue();
            count = 0;
            if (((ContainerRoteBook)this.menu).getComponent(i) == null) continue;
            for (Attribute attribute : ((ContainerRoteBook)this.menu).getComponent(i).getContainedAttributes()) {
                int row = (int)Math.floor(count / 2);
                int col = count % 2;
                this.drawAttributeValue(pGuiGraphics, xPos + col * 38, yPos + row * 13, ((ContainerRoteBook)this.menu).getComponent(i).getValueWithoutMultipliers(attribute));
                ++count;
            }
        }
        String header = I18n.get((String)"gui.mna.mana_cost", (Object[])new Object[0]) + ":";
        String manaCost = String.format("%.1f", Float.valueOf(((ContainerRoteBook)this.menu).getManaCost((Player)this.minecraft.player)));
        String complexityHeader = I18n.get((String)"gui.mna.complexity", (Object[])new Object[0]) + ":";
        String complexity = String.format("%.1f / %d", Float.valueOf(((ContainerRoteBook)this.menu).getComplexity((Player)this.minecraft.player)), this.playerProgression.getTierMaxComplexity());
        float manaCostWidth = (float)this.font.width(manaCost) * 0.5f;
        float headerWidth = (float)this.font.width(header) * 0.5f;
        float complexityHeaderWidth = (float)this.font.width(complexityHeader) * 0.5f;
        float padding = 2.0f;
        float summaryXPos = 35.0f;
        float summaryYPos = 25.0f;
        pGuiGraphics.drawString(this.font, complexityHeader, (int)(summaryXPos / 0.5f), (int)(summaryYPos / 0.5f), textColor, false);
        summaryXPos += complexityHeaderWidth + padding;
        if (((ContainerRoteBook)this.menu).getComplexity((Player)this.minecraft.player) <= (float)this.playerProgression.getTierMaxComplexity()) {
            pGuiGraphics.drawString(this.font, complexity, (int)(summaryXPos / 0.5f), (int)(summaryYPos / 0.5f), textColor, false);
        } else {
            pGuiGraphics.drawString(this.font, complexity, (int)(summaryXPos / 0.5f), (int)(summaryYPos / 0.5f), ChatFormatting.RED.getColor().intValue(), false);
        }
        summaryXPos = (float)(this.imageWidth - 33) - headerWidth - manaCostWidth - padding;
        summaryYPos = 25.0f;
        pGuiGraphics.drawString(this.font, header, (int)(summaryXPos / 0.5f), (int)(summaryYPos / 0.5f), textColor, false);
        pGuiGraphics.drawString(this.font, manaCost, (int)((summaryXPos += headerWidth + padding) / 0.5f), (int)(summaryYPos / 0.5f), textColor, false);
        pGuiGraphics.pose().popPose();
        MutableComponent curSpellName = Component.literal((String)((ContainerRoteBook)this.menu).getName());
        int curNameWidth = this.font.width((FormattedText)curSpellName);
        pGuiGraphics.drawString(this.font, (Component)curSpellName, this.imageWidth / 2 - curNameWidth / 2, 12, textColor, false);
        for (int d = 0; d < 16; ++d) {
            Pair<Integer, Integer> pip = pipLocations.get(d);
            if (((ContainerRoteBook)this.menu).getActiveIndex() == d) {
                pGuiGraphics.blit(GuiTextures.Items.BOOK_OF_ROTE_EXTRAS, ((Integer)pip.getFirst()).intValue(), ((Integer)pip.getSecond()).intValue(), 0.0f, 126.0f, 2, 2, 256, 256);
            }
            if (this.hoveredIndex != d) continue;
            pGuiGraphics.blit(GuiTextures.Items.BOOK_OF_ROTE_EXTRAS, ((Integer)pip.getFirst()).intValue(), ((Integer)pip.getSecond()).intValue(), 0.0f, 124.0f, 2, 2, 256, 256);
        }
        SpellValidityResult craftFeedback = ((ContainerRoteBook)this.menu).calculateCraftFeedback((Player)this.minecraft.player, this.playerProgression);
        if (craftFeedback != SpellValidityResult.READY) {
            MutableComponent craftFeedbackMessage = Component.translatable((String)craftFeedback.getTranslationKey());
            int craftFeedbackMessageWidth = this.font.width((FormattedText)craftFeedbackMessage);
            pGuiGraphics.drawString(this.font, (Component)craftFeedbackMessage, this.imageWidth / 2 - craftFeedbackMessageWidth / 2, -12, ChatFormatting.YELLOW.getColor().intValue(), false);
        }
    }

    private void drawAttributeValue(GuiGraphics pGuiGraphics, int x, int y, float value) {
        String valueString = String.format("%.1f", Float.valueOf(value));
        pGuiGraphics.drawString(this.font, valueString, (int)((float)x / 0.5f), (int)((float)y / 0.5f), textColor, false);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(GuiTextures.Items.BOOK_OF_ROTE, i, j, 0, 0, this.imageWidth, this.imageHeight);
        pGuiGraphics.blit(GuiTextures.Items.BOOK_OF_ROTE, i + 54, j + 178, 54, 178, 148, 78);
        this.list.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.namingSpell) {
            int bannerWidth = 246;
            int bannerHeight = 30;
            pGuiGraphics.blit(GuiTextures.Items.SPELL_CUSTOMIZE, i + this.imageWidth / 2 - bannerWidth / 2, j + 5, 0.0f, 0.0f, bannerWidth, bannerHeight, this.imageWidth, this.imageHeight);
            int iconsWidth = 219;
            int iconsHeight = 219;
            pGuiGraphics.blit(GuiTextures.Items.SPELL_CUSTOMIZE, i + this.imageWidth / 2 - iconsWidth / 2, j + 38, 0.0f, 31.0f, iconsWidth, iconsHeight, this.imageWidth, this.imageHeight);
        }
    }

    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (this.list.isScrolling()) {
            return this.list.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }
        if (this.colors.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_)) {
            return true;
        }
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    static {
        pipLocations.add((Pair<Integer, Integer>)new Pair(224, 202));
        pipLocations.add((Pair<Integer, Integer>)new Pair(230, 203));
        pipLocations.add((Pair<Integer, Integer>)new Pair(236, 206));
        pipLocations.add((Pair<Integer, Integer>)new Pair(239, 212));
        pipLocations.add((Pair<Integer, Integer>)new Pair(240, 218));
        pipLocations.add((Pair<Integer, Integer>)new Pair(239, 224));
        pipLocations.add((Pair<Integer, Integer>)new Pair(236, 230));
        pipLocations.add((Pair<Integer, Integer>)new Pair(230, 233));
        pipLocations.add((Pair<Integer, Integer>)new Pair(224, 234));
        pipLocations.add((Pair<Integer, Integer>)new Pair(218, 233));
        pipLocations.add((Pair<Integer, Integer>)new Pair(212, 230));
        pipLocations.add((Pair<Integer, Integer>)new Pair(209, 224));
        pipLocations.add((Pair<Integer, Integer>)new Pair(208, 218));
        pipLocations.add((Pair<Integer, Integer>)new Pair(209, 212));
        pipLocations.add((Pair<Integer, Integer>)new Pair(212, 206));
        pipLocations.add((Pair<Integer, Integer>)new Pair(218, 203));
    }

    public class IndexButton
    extends MNAImageButton {
        boolean isActive;
        int index;

        public IndexButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int hoverOffset, ResourceLocation textureFile, int texWidth, int texHeight, Button.OnPress clickHandler, int index, boolean active) {
            super(x, y, width, height, xTexStart, yTexStart, hoverOffset, textureFile, texWidth, texHeight, clickHandler, (Component)Component.literal((String)""));
            this.index = index;
            this.isActive = active;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mousex, int mousey, float partialTick) {
            super.renderWidget(pGuiGraphics, mousex, mousey, partialTick);
            if (this.active && this.visible) {
                boolean left = this.index > 8;
                ItemStack spellStack = ((ContainerRoteBook)GuiRoteBook.this.menu).getStack(this.index - 1);
                pGuiGraphics.renderItem(spellStack, left ? this.getX() + 18 : this.getX() + 3, this.getY() + 3);
                if (this.isHoveredOrFocused()) {
                    GuiRoteBook.this.hoveredIndex = this.index - 1;
                }
            }
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
        }
    }

    class ModelButton
    extends Button {
        public ItemStack icon;

        public ModelButton(int x, int y, int index, Button.OnPress pressedAction) {
            super(x, y, 16, 16, (Component)Component.literal((String)""), pressedAction, DEFAULT_NARRATION);
            this.icon = new ItemStack((ItemLike)ItemInit.SPELL.get());
            ItemSpell.setCustomIcon(this.icon, index);
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            pGuiGraphics.renderItem(this.icon, this.getX(), this.getY());
        }

        public void setFocused(boolean focus) {
        }
    }
}

