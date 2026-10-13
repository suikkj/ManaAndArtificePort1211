 
package com.mna.gui.item;

import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.item.ContainerSpellName;
import com.mna.items.ItemInit;
import com.mna.items.SpellIconList;
import com.mna.items.sorcery.ItemSpell;
import java.util.ArrayList;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class GuiSpellNaming
extends GuiJEIDisable<ContainerSpellName> {
    private int page = 0;
    private int numPages = 0;
    EditBox nameBox;
    Component nameValue;
    ModelButton currentButton;
    ArrayList<ModelButton> iconButtons;
    private Button prev;
    private Button next;

    public GuiSpellNaming(ContainerSpellName screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.iconButtons = new ArrayList();
        this.numPages = (int)Math.floor((float)SpellIconList.ALL.length / 100.0f);
    }

    protected void init() {
        super.init();
        int i = this.leftPos;
        int j = this.topPos;
        this.currentButton = new ModelButton(i + 12, j + 7, -1, btn -> {});
        this.addRenderableOnly((Renderable)this.currentButton);
        this.nameBox = new EditBox(this.minecraft.font, i + this.imageWidth / 2 - 80, j + 7, 160, 16, this.nameValue);
        this.nameBox.setValue(((ContainerSpellName)this.menu).getName());
        this.nameBox.setMaxLength(60);
        this.nameBox.setResponder(this::nameChanged);
        this.nameBox.setCanLoseFocus(false);
        this.nameBox.setFocused(true);
        this.setFocused((GuiEventListener)this.nameBox);
        this.page = 0;
        this.addRenderableWidget(new MNAImageButton(this.leftPos + this.imageWidth - 28, this.topPos + 7, 15, 18, 222, 7, 0, GuiTextures.Items.SPELL_CUSTOMIZE, 256, 256, button -> this.onClose()));
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
        this.addRenderableWidget(this.nameBox);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        if (this.nameBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return this.nameBox.isFocused() && this.nameBox.isVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && this.nameBox.isMouseOver(mouseX, mouseY)) {
            this.nameBox.setValue("");
        }
        super.mouseClicked(mouseX, mouseY, button);
        this.setFocused((GuiEventListener)this.nameBox);
        return false;
    }

    private void nameChanged(String newName) {
        ((ContainerSpellName)this.menu).setName(newName);
    }

    private void initIconButtons() {
        for (ModelButton button2 : this.iconButtons) {
            this.removeWidget((GuiEventListener)button2);
        }
        this.iconButtons.clear();
        int x = this.leftPos + 26;
        int y = this.topPos + 40;
        int count = 1;
        for (int i = this.page * 100; i < SpellIconList.ALL.length; ++i) {
            int idx = i;
            ModelButton btn = new ModelButton(x, y, i, button -> {
                ((ContainerSpellName)this.menu).setIconIndex(idx);
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
        ItemSpell.setCustomIcon(this.currentButton.icon, ((ContainerSpellName)this.menu).getIconIndex());
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        int i = this.leftPos;
        int j = this.topPos;
        int bannerWidth = 246;
        int bannerHeight = 30;
        pGuiGraphics.blit(GuiTextures.Items.SPELL_CUSTOMIZE, i + this.imageWidth / 2 - bannerWidth / 2, j, 0.0f, 0.0f, bannerWidth, bannerHeight, this.imageWidth, this.imageHeight);
        int iconsWidth = 219;
        int iconsHeight = 219;
        pGuiGraphics.blit(GuiTextures.Items.SPELL_CUSTOMIZE, i + this.imageWidth / 2 - iconsWidth / 2, j + 33, 0.0f, 31.0f, iconsWidth, iconsHeight, this.imageWidth, this.imageHeight);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    class ModelButton
    extends Button {
        public ItemStack icon;

        public ModelButton(int x, int y, int index, Button.OnPress pressedAction) {
            super(x, y, 16, 16, (Component)Component.literal((String)""), pressedAction, new Button.CreateNarration(){

                public MutableComponent createNarrationMessage(Supplier<MutableComponent> pMessageSupplier) {
                    return Component.empty();
                }
            });
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

