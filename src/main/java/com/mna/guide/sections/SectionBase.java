/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.guide.sections;

import com.mna.guide.interfaces.IEntrySection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public abstract class SectionBase
implements IEntrySection {
    private int page;
    private boolean baseMna = true;
    protected final NonNullList<Component> tooltip = NonNullList.create();
    protected int overrideColor = -1;
    private int guiLeft = 0;
    private int guiTop = 0;

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public boolean canWrap() {
        return false;
    }

    @Override
    public NonNullList<Component> getTooltip() {
        return this.tooltip;
    }

    @Override
    public void setOverrideColor(int color) {
        this.overrideColor = color;
    }

    @Override
    public int getOverrideColor() {
        return this.overrideColor;
    }

    @Override
    public boolean isBaseMna() {
        return this.baseMna;
    }

    @Override
    public void setNotBaseMna() {
        this.baseMna = false;
    }

    @Override
    public void setGuiPos(int left, int top) {
        this.guiLeft = left;
        this.guiTop = top;
    }

    @Override
    @Nonnull
    public Collection<AbstractWidget> getWidgets(AbstractContainerScreen<?> screen, int sectionX, int sectionY, int maxWidth, int maxHeight, Consumer<List<Component>> tooltipFunction, BiConsumer<String, Boolean> showRecipe, BiConsumer<String, Boolean> showEntry) {
        return new ArrayList<AbstractWidget>();
    }

    protected boolean isMouseWithin(float x, float y, float width, float height, float mouseX, float mouseY) {
        return x + (float)this.guiLeft >= mouseX && x + (float)this.guiLeft + width <= mouseX && y + (float)this.guiTop >= mouseY && y + (float)this.guiTop + height <= mouseY;
    }
}


