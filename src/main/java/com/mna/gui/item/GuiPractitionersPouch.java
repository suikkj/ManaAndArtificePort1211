 
package com.mna.gui.item;

import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mna.gui.GuiTextures;
import com.mna.gui.containers.item.ContainerPractitionersPouch;
import com.mna.items.ItemInit;
import com.mna.items.ritual.PractitionersPouchPatches;
import com.mna.tools.render.GuiRenderUtils;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.client.gui.components.ImageButton;

public class GuiPractitionersPouch
extends AbstractContainerScreen<ContainerPractitionersPouch> {
    private static final int PATCH_LEFT = 155;
    private static final int PATCH_BOTTOM = 136;
    private PractitionersPouchPatches currentPatch = null;
    private Component currentTooltip = null;
    private ImageButton closeButton = null;
    private ArrayList<ItemStackButton> patchSelectButtons;

    public GuiPractitionersPouch(ContainerPractitionersPouch screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 226;
        this.patchSelectButtons = new ArrayList();
    }

    protected void init() {
        super.init();
        this.closeButton = (ImageButton)this.addRenderableWidget(new MNAImageButton(this.leftPos + 82, this.topPos + 36, 8, 8, 248, 248, 0, GuiTextures.Items.PRACTITIONERS_POUCH, 256, 256, button -> this.setPatch(null)));
        this.closeButton.active = false;
        this.closeButton.visible = false;
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.DEPTH, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_DEPTH.get()), this.leftPos + 159, this.topPos + 80, b -> {})));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.DEPTH, 2)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_DEPTH_2.get()), this.leftPos + 159, this.topPos + 98, b -> {})));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.SPEED, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_SPEED.get()), this.leftPos + 177, this.topPos + 80, b -> {})));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.SPEED, 2)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_SPEED_2.get()), this.leftPos + 177, this.topPos + 98, b -> {})));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.SPEED, 3)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_SPEED_3.get()), this.leftPos + 177, this.topPos + 116, b -> {})));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.GLYPH, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_GLYPH.get()), this.leftPos + 195, this.topPos + 80, true, b -> this.setPatch(PractitionersPouchPatches.GLYPH))));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.MOTE, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_MOTE.get()), this.leftPos + 195, this.topPos + 98, true, b -> this.setPatch(PractitionersPouchPatches.MOTE))));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.CONVEYANCE, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_CONVEYANCE.get()), this.leftPos + 195, this.topPos + 116, true, b -> this.setPatch(PractitionersPouchPatches.CONVEYANCE))));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.RIFT, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_RIFT.get()), this.leftPos + 213, this.topPos + 80, b -> {})));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.WEAVE, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_WEAVE.get()), this.leftPos + 213, this.topPos + 98, true, b -> this.setPatch(PractitionersPouchPatches.WEAVE))));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.COLLECTION, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_COLLECTION.get()), this.leftPos + 213, this.topPos + 116, b -> {})));
        }
        if (((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.VOID, 1)) {
            this.patchSelectButtons.add((ItemStackButton)this.addRenderableWidget(new ItemStackButton(new ItemStack((ItemLike)ItemInit.PATCH_VOID.get()), this.leftPos + 159, this.topPos + 116, true, b -> this.setPatch(PractitionersPouchPatches.VOID))));
        }
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.PACK_OPEN);
    }

    public void onClose() {
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.PACK_CLOSE);
        super.onClose();
    }

    private void setPatch(PractitionersPouchPatches patch) {
        this.currentPatch = patch;
        ((ContainerPractitionersPouch)this.menu).setPatchActive(patch);
        if (patch != null && patch.hasInventory()) {
            this.closeButton.visible = true;
            this.closeButton.active = true;
            this.closeButton.setX(this.leftPos + 155 + patch.guiW() - 14);
            this.closeButton.setY(this.topPos + 136 - patch.guiH() + 9);
            this.patchSelectButtons.forEach(b -> {
                b.active = false;
                b.visible = false;
            });
        } else {
            this.closeButton.visible = false;
            this.closeButton.active = false;
            this.patchSelectButtons.forEach(b -> {
                b.active = true;
                b.visible = true;
            });
        }
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTick) {
        this.currentTooltip = null;
        super.render(pGuiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        int xPos = this.leftPos + this.imageWidth / 2;
        int yPos = this.topPos + this.imageHeight - 45;
        GuiRenderUtils.renderStandardPlayerInventory(pGuiGraphics, xPos, yPos);
        pGuiGraphics.blit(((ContainerPractitionersPouch)this.menu).patchEnabled(PractitionersPouchPatches.DEPTH, 9999) ? GuiTextures.Items.PRACTITIONERS_POUCH_2 : GuiTextures.Items.PRACTITIONERS_POUCH, xPos - 123, yPos -= 136, 96, 0, 150, 96);
        if (this.currentPatch == null) {
            pGuiGraphics.blit(GuiTextures.Items.PRACTITIONERS_POUCH, this.leftPos + 155, this.topPos + 136 - 60, 105, 183, 78, 60);
        } else {
            this.blitPatch(pGuiGraphics, this.currentPatch.guiSheet() == 2 ? GuiTextures.Items.PRACTITIONERS_POUCH_2 : GuiTextures.Items.PRACTITIONERS_POUCH, this.currentPatch.guiU(), this.currentPatch.guiV(), this.currentPatch.guiW(), this.currentPatch.guiH());
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        if (this.currentTooltip != null) {
            pGuiGraphics.renderTooltip(this.font, this.currentTooltip, mouseX - this.leftPos, mouseY - this.topPos);
        }
        this.renderTooltip(pGuiGraphics, mouseX - this.leftPos, mouseY - this.topPos);
    }

    private void blitPatch(GuiGraphics pGuiGraphics, ResourceLocation textureSheet, int u, int v, int w, int h) {
        pGuiGraphics.blit(textureSheet, this.leftPos + 155 - 4, this.topPos + 136 - h + 5, u, v, w, h);
    }

    class ItemStackButton
    extends Button {
        private final ItemStack displayStack;
        private final boolean renderPlus;

        public ItemStackButton(ItemStack displayStack, int x, int y, Button.OnPress onPress) {
            this(displayStack, x, y, false, onPress);
        }

        public ItemStackButton(ItemStack displayStack, int x, int y, boolean renderPlus, Button.OnPress onPress) {
            super(x, y, 16, 16, (Component)Component.translatable((String)displayStack.getDescriptionId()), onPress, Button.DEFAULT_NARRATION);
            this.displayStack = displayStack;
            this.renderPlus = renderPlus;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTick) {
            pGuiGraphics.renderItem(this.displayStack, this.getX(), this.getY());
            if (this.renderPlus) {
                pGuiGraphics.blit(GuiTextures.Items.PRACTITIONERS_POUCH, this.getX() + this.width - 8, this.getY(), 0, 240.0f, 248.0f, 8, 8, 256, 256);
            }
            if (this.isHoveredOrFocused()) {
                GuiPractitionersPouch.this.currentTooltip = this.getMessage();
            }
        }
    }
}

