/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 */
package com.mna.gui.widgets.lodestar;

import com.mna.gui.GuiTextures;
import com.mna.gui.block.GuiLodestarV2;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class LodestarGroup
extends AbstractWidget {
    private static final int border_thickness = 1;
    private final Minecraft mc = Minecraft.getInstance();
    private String desiredLabel;
    private String renderedLabel;
    private int labelWidth;
    private boolean labelTruncated = false;
    private final BiConsumer<LodestarGroup, Boolean> clickHandler;
    private double clickX;
    private double clickY;
    private double clickXOffset;
    private double clickYOffset;
    private boolean lowTier;
    public boolean scaling = false;

    public LodestarGroup(int pX, int pY, int pWidth, int pHeight, boolean lowTier, BiConsumer<LodestarGroup, Boolean> clickHandler) {
        super(pX, pY, pWidth, pHeight, (Component)Component.empty());
        this.clickHandler = clickHandler;
        this.lowTier = lowTier;
        this.setLabel("Group");
    }

    public void onClick(double mouse_x, double mouse_y) {
        this.clickX = mouse_x;
        this.clickY = mouse_y;
        this.clickXOffset = mouse_x - (double)this.getX();
        this.clickYOffset = mouse_y - (double)this.getY();
        if (this.clickXOffset > (double)(this.width - 4) && this.clickYOffset > (double)(this.height - 4)) {
            this.scaling = true;
        }
        boolean for_delete = false;
        if (mouse_x >= (double)(this.getX() + this.getWidth() - 8) && mouse_x <= (double)(this.getX() + this.getWidth()) && mouse_y >= (double)this.getY() && mouse_y <= (double)(this.getY() + 8)) {
            for_delete = true;
        }
        if (this.clickHandler != null) {
            this.clickHandler.accept(this, for_delete);
        }
    }

    public CompoundTag toCompoundTag(int guiTop, int guiLeft) {
        CompoundTag output = new CompoundTag();
        output.putInt("x", this.getX() - guiLeft);
        output.putInt("y", this.getY() - guiTop);
        output.putInt("w", this.getWidth());
        output.putInt("h", this.getHeight());
        output.putString("l", this.desiredLabel);
        return output;
    }

    public static LodestarGroup fromCompound(CompoundTag tag, int guiTop, int guiLeft, boolean lowTier, BiConsumer<LodestarGroup, Boolean> clickHandler) {
        if (!(tag.contains("x") && tag.contains("y") && tag.contains("w") && tag.contains("h") && tag.contains("l"))) {
            return null;
        }
        int x = tag.getInt("x") + guiLeft;
        int y = tag.getInt("y") + guiTop;
        int w = tag.getInt("w");
        int h = tag.getInt("h");
        String l = tag.getString("l");
        LodestarGroup group = new LodestarGroup(x, y, w, h, lowTier, clickHandler);
        group.setLabel(l);
        return group;
    }

    public void setLabel(String newLabel) {
        this.desiredLabel = newLabel;
        this.labelTruncated = false;
        int maxLabelWidth = this.width - 4;
        this.labelWidth = this.mc.font.width((String)newLabel);
        while (this.labelWidth > maxLabelWidth && ((String)newLabel).length() >= 5) {
            newLabel = ((String)newLabel).substring(0, ((String)newLabel).length() - 4) + "...";
            this.labelWidth = this.mc.font.width((String)newLabel);
            this.labelTruncated = true;
        }
        this.renderedLabel = newLabel;
    }

    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        pGuiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, this.lowTier ? -14477819 : -14935012);
        int n = this.getX() - 1;
        int n2 = this.getY();
        Objects.requireNonNull(this.mc.font);
        pGuiGraphics.fill(n, n2 - 9 - 1 - 1, this.getX() + this.labelWidth + 1 + 2, this.getY(), this.lowTier ? -14477819 : -14935012);
        pGuiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.lowTier ? -6780822 : -14013910);
        int n3 = this.getX() - 1 + 1;
        int n4 = this.getY();
        Objects.requireNonNull(this.mc.font);
        pGuiGraphics.fill(n3, n4 - 9 - 1, this.getX() + this.labelWidth + 1 + 1, this.getY(), this.lowTier ? -6780822 : -14013910);
        Font font = this.mc.font;
        int n5 = this.getX() + 1;
        int n6 = this.getY();
        Objects.requireNonNull(this.mc.font);
        pGuiGraphics.drawString(font, this.renderedLabel, n5, n6 - 9, -1);
        if (this.labelTruncated && this.isMouseOverLabel((float)pMouseX / GuiLodestarV2.Zoom, (float)pMouseY / GuiLodestarV2.Zoom)) {
            this.mc.screen.setTooltipForNextRenderPass(Tooltip.splitTooltip((Minecraft)this.mc, (Component)Component.literal((String)this.desiredLabel)));
        }
        if (this.isMouseOver((float)pMouseX / GuiLodestarV2.Zoom, (float)pMouseY / GuiLodestarV2.Zoom)) {
            pGuiGraphics.blit(GuiTextures.Blocks.LODESTAR_MAIN, this.getX() + this.getWidth() - 8, this.getY(), 0, 224, 8, 8);
            pGuiGraphics.fill(this.getX() + this.getWidth() - 4, this.getY() + this.getHeight() - 4, this.getX() + this.getWidth(), this.getY() + this.getHeight(), -14477819);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isMouseOverLabel(double pMouseX, double pMouseY) {
        if (!this.active) return false;
        if (!this.visible) return false;
        if (!(pMouseX >= (double)this.getX())) return false;
        if (!(pMouseX <= (double)(this.getX() + this.labelWidth))) return false;
        int n = this.getY();
        Objects.requireNonNull(this.mc.font);
        if (!(pMouseY >= (double)(n - 9))) return false;
        if (!(pMouseY <= (double)this.getY())) return false;
        return true;
    }

    protected boolean clicked(double pMouseX, double pMouseY) {
        return super.clicked(pMouseX, pMouseY) || this.isMouseOverLabel(pMouseX, pMouseY);
    }

    public void resize(int width, int height) {
        this.width = Math.max(width, 50);
        this.height = Math.max(height, 50);
        this.setLabel(this.desiredLabel);
    }

    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public double getClickX() {
        return this.clickX;
    }

    public double getClickY() {
        return this.clickY;
    }

    public double getClickXOffset() {
        return this.clickXOffset;
    }

    public double getClickYOffset() {
        return this.clickYOffset;
    }

    public String getLabel() {
        return this.desiredLabel;
    }
}

