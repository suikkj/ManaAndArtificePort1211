 
package com.mna.gui.item;

import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.item.ContainerEnderDisc;
import com.mna.items.ItemInit;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.gui.widgets.MNAImageButton;

public class GuiEnderDisc
extends GuiJEIDisable<ContainerEnderDisc> {
    private int[] rune_indices = new int[8];
    private int remove_index_hover = -1;
    private ArrayList<EnderDiscTabButton> index_buttons;
    EditBox nameBox;
    Component nameValue;
    ItemStack dragStack = ItemStack.EMPTY;
    private ItemStack[] rune_stacks = new ItemStack[]{new ItemStack((ItemLike)Items.AIR), new ItemStack((ItemLike)ItemInit.STONE_RUNE_BLACK.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_BLUE.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_BROWN.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_CYAN.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_GRAY.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_GREEN.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_LIGHT_BLUE.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_LIGHT_GRAY.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_LIME.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_MAGENTA.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_ORANGE.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_PINK.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_PURPLE.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_RED.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_WHITE.get()), new ItemStack((ItemLike)ItemInit.STONE_RUNE_YELLOW.get())};

    public GuiEnderDisc(ContainerEnderDisc screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 255;
        this.imageHeight = 191;
    }

    protected void init() {
        super.init();
        this.index_buttons = new ArrayList();
        int x = this.leftPos;
        int y = this.topPos;
        x = this.leftPos + 92;
        y = this.topPos + 124;
        for (int i = 0; i < 8; ++i) {
            this.addIndexButton(x, y, i);
            x += 20;
        }
        this.addRenderableWidget(new EnderDiscDimensionButton(this.leftPos + 61, this.topPos + 142, btn -> this.minecraft.gameMode.handleInventoryButtonClick(((ContainerEnderDisc)this.menu).containerId, 0)));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 141, this.topPos + 40, this.rune_stacks[12], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 161, this.topPos + 40, this.rune_stacks[3], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 181, this.topPos + 40, this.rune_stacks[14], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 201, this.topPos + 40, this.rune_stacks[11], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 221, this.topPos + 40, this.rune_stacks[16], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 131, this.topPos + 60, this.rune_stacks[10], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 151, this.topPos + 60, this.rune_stacks[15], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 171, this.topPos + 60, this.rune_stacks[8], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 191, this.topPos + 60, this.rune_stacks[5], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 211, this.topPos + 60, this.rune_stacks[1], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 231, this.topPos + 60, this.rune_stacks[9], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 141, this.topPos + 80, this.rune_stacks[13], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 161, this.topPos + 80, this.rune_stacks[2], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 181, this.topPos + 80, this.rune_stacks[7], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 201, this.topPos + 80, this.rune_stacks[4], this::onGlyphButtonClicked));
        this.addRenderableWidget(new GlyphButton(this.leftPos + 221, this.topPos + 80, this.rune_stacks[6], this::onGlyphButtonClicked));
        this.addRenderableWidget(new MNAImageButton(this.leftPos + 84, this.topPos + 161, 7, 7, 249, 242, 7, GuiTextures.Items.ENDER_DISC, b -> {
            ResourceLocation empty = BuiltInRegistries.ITEM.getKey(Items.AIR);
            for (int i = 0; i < 8; ++i) {
                ((ContainerEnderDisc)this.menu).setPattern(empty, i);
            }
            this.setCurPattern(((ContainerEnderDisc)this.menu).getCurPatternIndex());
        }));
        this.nameBox = new EditBox(this.minecraft.font, this.leftPos + 98, this.topPos + 146, 144, 16, this.nameValue);
        this.nameBox.setMaxLength(23);
        this.nameBox.setResponder(this::patternNameChanged);
        this.setCurPattern(((ContainerEnderDisc)this.menu).getCurPatternIndex());
        this.nameBox.setValue(((ContainerEnderDisc)this.menu).getCurPatternName());
        this.addRenderableWidget(this.nameBox);
        this.nameBox.setCanLoseFocus(false);
        this.nameBox.setFocused(true);
        this.setFocused((GuiEventListener)this.nameBox);
    }

    public void onGlyphButtonClicked(Button button) {
        if (button instanceof GlyphButton) {
            GlyphButton gb = (GlyphButton)button;
            ItemStack stack = gb.getStack();
            this.dragStack = stack.copy();
        }
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
        if (this.remove_index_hover != -1) {
            ResourceLocation empty = BuiltInRegistries.ITEM.getKey(Items.AIR);
            if (!((ContainerEnderDisc)this.menu).getCurPattern().get(this.remove_index_hover).equals(empty)) {
                ((ContainerEnderDisc)this.menu).setPattern(empty, this.remove_index_hover);
                this.setCurPattern(((ContainerEnderDisc)this.menu).getCurPatternIndex());
                Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
            }
        }
        if (button == 1 && this.nameBox.isMouseOver(mouseX, mouseY)) {
            this.nameBox.setValue("");
        }
        super.mouseClicked(mouseX, mouseY, button);
        this.setFocused((GuiEventListener)this.nameBox);
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.dragStack.isEmpty()) {
            int index;
            if (mouseY - (double)this.topPos >= 167.0 && mouseY - (double)this.topPos <= 183.0 && (index = (int)Math.floor(mouseX - (double)this.leftPos - 100.0) / 18) >= 0 && index < 8) {
                ((ContainerEnderDisc)this.menu).setPattern(BuiltInRegistries.ITEM.getKey(Items.AIR), index);
                this.setCurPattern(((ContainerEnderDisc)this.menu).getCurPatternIndex());
            }
            this.children().forEach(child -> {
                if (child instanceof GlyphButton && child.isMouseOver(mouseX, mouseY) && ((GlyphButton)(child)).getStack().getItem() == this.dragStack.getItem()) {
                    int index2;
                    for (index2 = 0; index2 < this.rune_indices.length && this.rune_indices[index2] != 0; ++index2) {
                    }
                    ((ContainerEnderDisc)this.menu).setPattern(BuiltInRegistries.ITEM.getKey(this.dragStack.getItem()), index2);
                    this.setCurPattern(((ContainerEnderDisc)this.menu).getCurPatternIndex());
                }
            });
            this.dragStack = ItemStack.EMPTY;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void patternNameChanged(String newName) {
        ((ContainerEnderDisc)this.menu).setName(newName);
    }

    private void setCurPattern(int index) {
        ((ContainerEnderDisc)this.menu).changePatternIndex(index);
        ArrayList<ResourceLocation> curPattern = ((ContainerEnderDisc)this.menu).getCurPattern();
        block0: for (int i = 0; i < this.rune_indices.length && curPattern.size() > i; ++i) {
            ResourceLocation rLoc = curPattern.get(i);
            int count = 0;
            for (ItemStack stack : this.rune_stacks) {
                if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(rLoc)) {
                    this.rune_indices[i] = count;
                    continue block0;
                }
                ++count;
            }
        }
        this.index_buttons.get(index).setHighlighted(true);
        this.nameBox.setValue(((ContainerEnderDisc)this.menu).getCurPatternName());
    }

    private void addIndexButton(int x, int y, int index) {
        this.index_buttons.add((EnderDiscTabButton)this.addRenderableWidget(new EnderDiscTabButton(x, y, 18 * index, 220, b -> {
            this.setCurPattern(index);
            this.index_buttons.forEach(ib -> ib.setHighlighted(b == ib));
        }, String.format("%d", index + 1))));
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (!this.dragStack.isEmpty()) {
            pGuiGraphics.renderItem(this.dragStack, mouseX - 8, mouseY - 8);
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GuiTextures.Items.ENDER_DISC, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        this.remove_index_hover = -1;
        for (int i = 0; i < this.rune_indices.length; ++i) {
            this.renderActiveGlyph(pGuiGraphics, 100 + i * 18, 167, mouseX, mouseY, i);
        }
    }

    private void renderActiveGlyph(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, int index) {
        pGuiGraphics.renderItem(this.rune_stacks[this.rune_indices[index]], x, y);
        if (mouseX >= this.leftPos + x && mouseX <= this.leftPos + x + 16 && mouseY >= this.topPos + y && mouseY <= this.topPos + y + 16) {
            this.remove_index_hover = index;
        }
    }

    class EnderDiscDimensionButton
    extends MNAImageButton {
        private final int v = 244;

        public EnderDiscDimensionButton(int xIn, int yIn, Button.OnPress onPressIn) {
            super(xIn, yIn, 12, 12, 0, 0, 0, GuiTextures.Items.ENDER_DISC, 256, 256, onPressIn, null);
            // [PORT 1.21.1] v final no parente - atribuicao removida
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            int u = 237;
            switch (((ContainerEnderDisc)GuiEnderDisc.this.menu).getCurDimension().toString()) {
                case "mna:dimension_current": {
                    u = 189;
                    break;
                }
                case "minecraft:overworld": {
                    u = 201;
                    break;
                }
                case "minecraft:the_nether": {
                    u = 213;
                    break;
                }
                case "minecraft:the_end": {
                    u = 225;
                }
            }
            RenderSystem.enableDepthTest();
            int n = this.getX();
            int n2 = this.getY();
            float f = u;
            Objects.requireNonNull(this);
            pGuiGraphics.blit(GuiTextures.Items.ENDER_DISC, n, n2, f, 244.0f, this.width, this.height, 256, 256);
        }
    }

    class GlyphButton
    extends Button {
        private final ItemStack renderStack;

        public GlyphButton(int x, int y, ItemStack stack, Button.OnPress clickHandler) {
            super(x, y, 16, 16, (Component)Component.literal((String)""), clickHandler, DEFAULT_NARRATION);
            this.renderStack = stack;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            pGuiGraphics.renderItem(this.renderStack, this.getX(), this.getY());
        }

        public ItemStack getStack() {
            return this.renderStack;
        }
    }

    class EnderDiscTabButton
    extends MNAImageButton {
        private boolean highlighted;
        private String text;
        private final int TEXT_COLOR;

        public EnderDiscTabButton(int xIn, int yIn, int uIn, int vIn, Button.OnPress onPressIn, String text) {
            super(xIn, yIn, 18, 18, uIn, vIn, 18, GuiTextures.Items.ENDER_DISC, 256, 256, onPressIn);
            this.highlighted = false;
            this.TEXT_COLOR = FastColor.ARGB32.color((int)255, (int)40, (int)40, (int)40);
            this.text = text;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
            Font font = GuiEnderDisc.this.font;
            int n = (int)((float)(this.getX() + this.width / 2 - GuiEnderDisc.this.font.width(this.text) / 2) + 0.5f);
            int n2 = this.getY() + this.height / 2;
            Objects.requireNonNull(GuiEnderDisc.this.font);
            pGuiGraphics.drawString(font, this.text, n, (int)((float)(n2 - 9 / 2) + 0.5f), this.TEXT_COLOR, false);
        }

        public boolean isHoveredOrFocused() {
            return this.highlighted;
        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
        }
    }
}


