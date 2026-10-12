/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  javax.annotation.Nullable
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 */
package com.mna.guide.interfaces;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;

public interface IEntrySection {
    public Collection<IEntrySection> parse(JsonObject var1, int var2, int var3, int var4, int var5);

    public int getHeight(int var1);

    public int getWidth(int var1);

    public boolean canWrap();

    public boolean newPage();

    public int getPage();

    public void setPage(int var1);

    public void setPadding(int var1);

    public int getOverrideColor();

    public void setOverrideColor(int var1);

    public boolean isBaseMna();

    public void setNotBaseMna();

    public NonNullList<Component> getTooltip();

    public void setGuiPos(int var1, int var2);

    @Nullable
    public Collection<AbstractWidget> getWidgets(AbstractContainerScreen<?> var1, int var2, int var3, int var4, int var5, Consumer<List<Component>> var6, BiConsumer<String, Boolean> var7, BiConsumer<String, Boolean> var8);
}

