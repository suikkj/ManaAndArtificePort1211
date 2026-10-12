package com.mna.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Compat: no 1.20.1 o {@link ImageButton} vanilla aceitava
 * (x, y, w, h, u, v, hoverOffsetV, texture, texW, texH, onPress, message).
 * Esse construtor foi REMOVIDO em 1.21.1 (agora é WidgetSprites + blitSprite).
 *
 * Esta subclasse preserva a API antiga do MNA desenhando por blit com UV manual,
 * de modo que os 30+ call-sites do mod não precisem ser reescritos.
 */
@OnlyIn(Dist.CLIENT)
public class MNAImageButton extends ImageButton {
    protected final ResourceLocation resourceLocation;
    private final int texWidth;
    private final int texHeight;
    private final int u;
    private final int v;
    private final int hoverOffsetV;

    public MNAImageButton(int x, int y, int width, int height,
                          int u, int v, int hoverOffsetV,
                          ResourceLocation texture, int texWidth, int texHeight,
                          Button.OnPress onPress, Component message) {
        super(x, y, width, height,
                new WidgetSprites(texture, texture, texture, texture),
                onPress, message);
        this.resourceLocation = texture;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.u = u;
        this.v = v;
        this.hoverOffsetV = hoverOffsetV;
    }

    /** Variante antiga sem texWidth/texHeight (assume 256x256). */
    public MNAImageButton(int x, int y, int width, int height,
                          int u, int v, int hoverOffsetV,
                          ResourceLocation texture, Button.OnPress onPress) {
        this(x, y, width, height, u, v, hoverOffsetV, texture, 256, 256, onPress, Component.empty());
    }

    public MNAImageButton(int x, int y, int width, int height,
                          int u, int v, int hoverOffsetV,
                          ResourceLocation texture, int texWidth, int texHeight,
                          Button.OnPress onPress) {
        this(x, y, width, height, u, v, hoverOffsetV, texture, texWidth, texHeight, onPress, Component.empty());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int vOffset = this.isHoveredOrFocused() ? this.hoverOffsetV : 0;
        guiGraphics.blit(this.resourceLocation, this.getX(), this.getY(), 0,
                (float) this.u, (float) (this.v + vOffset),
                this.width, this.height, this.texWidth, this.texHeight);
    }
}
