/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IMARecipe {
    public ResourceLocation getRegistryId();

    public ItemStack getResultItem();

    public ItemStack getGuiRepresentationStack();

    public int getTier();
}

