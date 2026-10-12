/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.guide.sections;

import com.google.gson.JsonObject;
import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.guide.RelatedRecipe;
import com.mna.guide.interfaces.IEntrySection;
import com.mna.guide.sections.SectionBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class RecipeSection
extends SectionBase {
    private RelatedRecipe rr;

    @Override
    public Collection<IEntrySection> parse(JsonObject element, int startY, int maxHeight, int maxWidth, int page) {
        this.setPage(page + 1);
        String type = element.get("recipeType").getAsString();
        String location = element.get("location").getAsString();
        this.rr = new RelatedRecipe(type, ResourceLocation.parse(location));
        return Arrays.asList(this);
    }

    @Override
    public Collection<AbstractWidget> getWidgets(AbstractContainerScreen<?> screen, int sectionX, int sectionY, int maxWidth, int maxHeight, Consumer<List<Component>> tooltipFunction, BiConsumer<String, Boolean> showRecipe, BiConsumer<String, Boolean> showEntry) {
        ArrayList<AbstractWidget> widgets = new ArrayList<AbstractWidget>();
        RecipeRendererBase rrBase = this.rr.constructRenderer(sectionX, sectionY, tooltipFunction);
        rrBase.setScale(0.6f);
        rrBase.disablePaperBackground();
        widgets.add(rrBase);
        return widgets;
    }

    @Override
    public int getHeight(int maxHeight) {
        return 168;
    }

    @Override
    public int getWidth(int maxWidth) {
        return 108;
    }

    @Override
    public boolean newPage() {
        return true;
    }

    @Override
    public void setPadding(int i) {
    }
}


