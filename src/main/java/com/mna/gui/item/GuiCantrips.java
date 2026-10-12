 
package com.mna.gui.item;

import com.mna.ManaAndArtifice;
import com.mna.api.cantrips.ICantrip;
import com.mna.api.capabilities.IPlayerCantrip;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.sound.SFX;
import com.mna.cantrips.CantripRegistry;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.GuiTextures;
import com.mna.gui.containers.item.ContainerCantrips;
import com.mna.gui.item.GuiGuideBook;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ItemManaweaverWand;
import com.mna.network.ClientMessageDispatcher;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.tools.render.GuiRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.ImageButton;

public class GuiCantrips
extends AbstractContainerScreen<ContainerCantrips> {
    ArrayList<ManaweavingPattern> playerKnownPatterns;
    IPlayerProgression progression;
    IPlayerMagic magic;
    int numPages;
    int page;
    ICantrip selectedCantrip;
    ICantrip hoveredCantrip;
    Point[] buttonPositions = new Point[]{new Point(94, 0), new Point(130, 0), new Point(166, 0), new Point(94, 39), new Point(130, 39), new Point(166, 39)};
    Point[] cantripPositions;
    ImageButton[] adjustButtons = new ImageButton[6];
    HashMap<ResourceLocation, Integer[]> cantripPatterns;
    int patternSpacing = 13;

    public GuiCantrips(ContainerCantrips screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = 194;
        this.imageHeight = 256;
    }

    protected void init() {
        super.init();
        this.playerKnownPatterns = new ArrayList();
        this.progression = (IPlayerProgression)this.minecraft.player.getData(PlayerProgressionProvider.PROGRESSION.get());
        this.magic = (IPlayerMagic)this.minecraft.player.getData(PlayerMagicProvider.MAGIC.get());
        this.minecraft.level.getRecipeManager().getAllRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get()).stream().forEach(p -> {
            if (p.value().getTier() <= this.progression.getTier()) {
                this.playerKnownPatterns.add(p.value());
            }
        });
        this.cantripPatterns = new HashMap();
        RecipeManager clientRecipeManager = this.minecraft.level.getRecipeManager();
        CantripRegistry.INSTANCE.getCantrips((Player)this.minecraft.player).forEach(cantrip -> {
            if (cantrip.getTier() > this.progression.getTier()) {
                return;
            }
            this.cantripPatterns.put(cantrip.getId(), new Integer[3]);
            for (int j = 0; j < 3; ++j) {
                Optional<IPlayerCantrip> playerCantrip = this.magic.getCantripData().getCantrip(cantrip.getId());
                Optional pattern = clientRecipeManager.byKey(playerCantrip.get().getPattern(j));
                this.cantripPatterns.get(cantrip.getId())[j] = pattern.isPresent() && pattern.get() instanceof ManaweavingPattern ? Integer.valueOf(pattern != null ? this.playerKnownPatterns.indexOf(pattern.get()) : -1) : Integer.valueOf(-1);
            }
        });
        this.numPages = (int)Math.ceil(this.cantripPatterns.size() / 8);
        if (this.cantripPatterns.size() % 8 == 0) {
            --this.numPages;
        }
        this.setupPage(0);
    }

    private List<ICantrip> getCantripsForPage(int page) {
        if (page > this.numPages) {
            return new ArrayList<ICantrip>();
        }
        int skip = page * this.cantripPositions.length;
        if (skip >= this.cantripPatterns.size()) {
            return new ArrayList<ICantrip>();
        }
        return CantripRegistry.INSTANCE.getCantrips((Player)this.minecraft.player).stream().skip(skip).limit(8L).collect(Collectors.toList());
    }

    private void setupPage(int page) {
        this.page = page;
        this.clearWidgets();
        int i = this.leftPos;
        int j = this.topPos;
        if (this.page > 0) {
            this.addRenderableWidget(new PaperImageButton(i + 178, j + 68, 14, 9, 242, 0, 9, GuiTextures.Items.CANTRIPS, 256, 256, button -> this.setupPage(this.page - 1)));
        }
        if (this.page < this.numPages) {
            this.addRenderableWidget(new PaperImageButton(i + 178, j + 154, 14, 9, 242, 20, 9, GuiTextures.Items.CANTRIPS, 256, 256, button -> this.setupPage(this.page + 1)));
        }
        this.initRowSelectionButtons();
    }

    public void removed() {
        super.removed();
        this.cantripPatterns.entrySet().forEach(data -> {
            int patternIndex;
            ArrayList<ResourceLocation> patternIDs = new ArrayList<ResourceLocation>();
            for (int j = 0; j < ((Integer[])data.getValue()).length && (patternIndex = ((Integer[])data.getValue())[j].intValue()) >= 0 && patternIndex < this.playerKnownPatterns.size(); ++j) {
                patternIDs.add(this.playerKnownPatterns.get(patternIndex).getId());
            }
            this.magic.getCantripData().setPattern((ResourceLocation)data.getKey(), patternIDs);
        });
        ClientMessageDispatcher.sendCantripUpdateMessage(this.magic);
    }

    private void initRowSelectionButtons() {
        if (this.selectedCantrip == null) {
            return;
        }
        int xOffset = 0;
        int i = 0;
        while (i < 3) {
            int count = i++;
            this.addRenderableWidget(new MNAImageButton(this.leftPos + 94 + xOffset, this.topPos + 0, 14, 9, 242, 0, 9, GuiTextures.Items.CANTRIPS, 256, 256, button -> {
                int idx = this.cantripPatterns.get(this.selectedCantrip.getId())[count] - 1;
                if (idx < (count < 2 ? 0 : -1)) {
                    idx = this.playerKnownPatterns.size() - 1;
                }
                this.cantripPatterns.get(this.selectedCantrip.getId())[count] = idx;
            }));
            this.addRenderableWidget(new MNAImageButton(this.leftPos + 94 + xOffset, this.topPos + 39, 14, 9, 242, 20, 9, GuiTextures.Items.CANTRIPS, 256, 256, button -> {
                int idx = this.cantripPatterns.get(this.selectedCantrip.getId())[count] + 1;
                if (idx >= this.playerKnownPatterns.size()) {
                    idx = count < 2 ? 0 : -1;
                }
                this.cantripPatterns.get(this.selectedCantrip.getId())[count] = idx;
            }));
            xOffset += 36;
        }
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.hoveredCantrip = null;
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        ItemStack wandStack;
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        MutableObject tt = new MutableObject(null);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        int topLeftCornerX = (this.width - this.imageWidth) / 2;
        int topLeftCornerY = (this.height - this.imageHeight) / 2;
        ItemStack mainHand = ManaAndArtifice.instance.proxy.getClientPlayer().getMainHandItem();
        ItemStack offHand = ManaAndArtifice.instance.proxy.getClientPlayer().getOffhandItem();
        ItemStack itemStack = wandStack = mainHand.getItem() instanceof ItemManaweaverWand ? mainHand : offHand;
        if (wandStack.getItem() == ItemInit.MANAWEAVER_WAND.get()) {
            pGuiGraphics.blit(GuiTextures.Items.CANTRIPS_WANDS, topLeftCornerX, topLeftCornerY, 40, 0, 21, 256);
        } else if (wandStack.getItem() == ItemInit.MANAWEAVER_WAND_ADVANCED.get()) {
            pGuiGraphics.blit(GuiTextures.Items.CANTRIPS_WANDS, topLeftCornerX, topLeftCornerY, 0, 0, 33, 256);
        } else if (wandStack.getItem() == ItemInit.MANAWEAVER_WAND_IMPROVISED.get()) {
            pGuiGraphics.blit(GuiTextures.Items.CANTRIPS_WANDS, topLeftCornerX, topLeftCornerY, 68, 0, 34, 256);
        }
        pGuiGraphics.blit(GuiTextures.Items.CANTRIPS, topLeftCornerX, topLeftCornerY, 0, 0, 194, 256);
        if (this.selectedCantrip != null && ((ContainerCantrips)this.menu).cantripHasSlot(this.selectedCantrip.getId())) {
            pGuiGraphics.blit(GuiTextures.Items.CANTRIPS, this.leftPos + 7, this.topPos + 31, 38, 70, 20, 20);
        }
        this.calculateCantripPositions();
        MutableInt index = new MutableInt(0);
        this.getCantripsForPage(this.page).stream().forEach(cantrip -> {
            Point pt = this.cantripPositions[index.getAndIncrement()];
            if (this.renderCantripBGAt((ICantrip)cantrip, pt.x, pt.y, mouseX, mouseY, pGuiGraphics, false)) {
                tt.setValue(cantrip);
                this.hoveredCantrip = cantrip;
            }
            this.drawManaweavePatternCombo(pGuiGraphics, pt.x + 29, pt.y + 3, this.cantripPatterns.get(cantrip.getId()));
        });
        if (this.selectedCantrip != null) {
            if (this.renderCantripBGAt(this.selectedCantrip, this.leftPos + 45, this.topPos + 8, mouseX, mouseY, pGuiGraphics, true)) {
                tt.setValue(this.selectedCantrip);
            }
            int prevSpacing = this.patternSpacing;
            this.patternSpacing = 36;
            this.drawManaweavePatternCombo(pGuiGraphics, this.leftPos + 110, this.topPos + 13, this.cantripPatterns.get(this.selectedCantrip.getId()), 0.155f);
            this.patternSpacing = prevSpacing;
            String selectedName = Component.translatable((String)("cantrip." + this.selectedCantrip.getId().toString().replace(":", "."))).withStyle(ChatFormatting.LIGHT_PURPLE).getString();
            pGuiGraphics.drawString(this.font, selectedName, this.leftPos + 40, this.topPos + 53, 0xFFFFFF, false);
        }
        if (tt.getValue() != null) {
            this.renderCantripTooltip(pGuiGraphics, (ICantrip)tt.getValue(), mouseX, mouseY);
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int p_230451_2_, int p_230451_3_) {
    }

    public boolean mouseClicked(double p_97748_, double p_97749_, int p_97750_) {
        if (this.hoveredCantrip != null) {
            this.selectedCantrip = this.hoveredCantrip;
            ((ContainerCantrips)this.menu).enableSlotsForCantrip(this.selectedCantrip.getId());
            this.setupPage(this.page);
        }
        return super.mouseClicked(p_97748_, p_97749_, p_97750_);
    }

    private void calculateCantripPositions() {
        int topLeftCornerY;
        int topLeftCornerX = (this.width - this.imageWidth) / 2;
        int pageY = topLeftCornerY = (this.height - this.imageHeight) / 2;
        this.cantripPositions = new Point[]{new Point(topLeftCornerX + 40, pageY + 72), new Point(topLeftCornerX + 40, pageY + 96), new Point(topLeftCornerX + 40, pageY + 120), new Point(topLeftCornerX + 40, pageY + 144), new Point(topLeftCornerX + 110, pageY + 72), new Point(topLeftCornerX + 110, pageY + 96), new Point(topLeftCornerX + 110, pageY + 120), new Point(topLeftCornerX + 110, pageY + 144)};
    }

    private boolean renderCantripBGAt(ICantrip cantrip, int x, int y, int mouseX, int mouseY, GuiGraphics pGuiGraphics, boolean large) {
        boolean hovered = false;
        if (this.progression.getTier() >= cantrip.getTier()) {
            int size = large ? 32 : 16;
            pGuiGraphics.blit(cantrip.getIcon(), x, y, 0, 0.0f, 0.0f, size, size, size, size);
            if (this.isMouseWithin(x, y, large ? x + size : x + size + 42, y + size, mouseX, mouseY)) {
                hovered = true;
            }
        }
        return hovered;
    }

    private boolean isMouseWithin(int x, int y, int x2, int y2, int mouseX, int mouseY) {
        return x <= mouseX && x2 >= mouseX && y <= mouseY && y2 >= mouseY;
    }

    private void renderCantripTooltip(GuiGraphics pGuiGraphics, ICantrip cantrip, int mouseX, int mouseY) {
        java.util.List<net.minecraft.network.chat.Component> ttLines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        ttLines.add(Component.translatable((String)("cantrip." + cantrip.getId().toString().replace(":", "."))).withStyle(ChatFormatting.LIGHT_PURPLE));
        ttLines.add(Component.translatable((String)"cantrip.mna.tier", (Object[])new Object[]{cantrip.getTier()}).withStyle(ChatFormatting.AQUA));
        ttLines.add(Component.translatable((String)("cantrip." + cantrip.getId().toString().replace(":", ".") + ".desc")).withStyle(ChatFormatting.ITALIC));
        pGuiGraphics.renderComponentTooltip(this.font, ttLines, mouseX, mouseY);
    }

    private void drawManaweavePatternCombo(GuiGraphics pGuiGraphics, int x, int y, Integer[] patterns) {
        this.drawManaweavePatternCombo(pGuiGraphics, x, y, patterns, 0.07f);
    }

    private void drawManaweavePatternCombo(GuiGraphics pGuiGraphics, int x, int y, Integer[] patterns, float scale) {
        if (patterns == null) {
            return;
        }
        for (int i = 0; i < patterns.length; ++i) {
            Integer index = patterns[i];
            if (index != null && index >= 0) {
                ManaweavingPattern p = this.playerKnownPatterns.get(index);
                GuiRenderUtils.renderManaweavePattern(pGuiGraphics, (int)((float)x / scale), (int)((float)y / scale), scale, p);
            }
            x += this.patternSpacing;
        }
    }

    public class PaperImageButton
    extends MNAImageButton {
        private Component tooltip;

        public PaperImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, Button.OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
        }

        public PaperImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int p_i51136_9_, int p_i51136_10_, Button.OnPress onPressIn, Component textIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, p_i51136_9_, p_i51136_10_, onPressIn, textIn);
        }

        public PaperImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int p_i51135_9_, int p_i51135_10_, Button.OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, p_i51135_9_, p_i51135_10_, onPressIn);
        }

        public PaperImageButton setTooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
            if (this.active && this.visible && this.isHoveredOrFocused() && this.tooltip != null) {
                GuiGuideBook.currentTooltip.add(this.tooltip);
            }
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
        }
    }
}

