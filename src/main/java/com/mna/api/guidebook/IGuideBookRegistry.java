/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.api.guidebook;

import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IGuideBookRegistry {
    @OnlyIn(value=Dist.CLIENT)
    public void addGuidebookPath(ResourceLocation var1);

    @OnlyIn(value=Dist.CLIENT)
    public void registerRecipeRenderer(String var1, Class<? extends RecipeRendererBase> var2);

    @OnlyIn(value=Dist.CLIENT)
    public void registerGuidebookCategory(String var1, ResourceLocation var2);
}


