/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.guide.recipe;

import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public interface ICyclingRecipeRenderer<T extends RecipeRendererBase> {
    public void init_cycling(ResourceLocation[] var1);

    public int countRecipes();

    default public int getIndex() {
        return ((RecipeRendererBase)(this)).lockIndex == -1 ? (int)(Minecraft.getInstance().level.getGameTime() / 20L) % Math.max(this.countRecipes(), 1) : ((RecipeRendererBase)(this)).lockIndex;
    }
}


