/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.capabilities;

import com.mna.recipes.manaweaving.ManaweavingPattern;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IPlayerCantrip {
    public ResourceLocation getCantripID();

    public ItemStack getStack();

    public void readFromNBT(CompoundTag var1);

    public CompoundTag writeToNBT();

    public void setStack(ItemStack var1);

    public ResourceLocation getPattern(int var1);

    public void setPatterns(List<ResourceLocation> var1);

    public boolean matches(List<ManaweavingPattern> var1);
}

