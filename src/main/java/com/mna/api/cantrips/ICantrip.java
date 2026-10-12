/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.cantrips;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ICantrip {
    public ICantrip dynamicItem(@Nullable Item var1);

    public ICantrip setDelay(int var1);

    public ICantrip setSound(SoundEvent var1);

    public ICantrip setIcon(ResourceLocation var1);

    public ICantrip setRequiredAdvancement(ResourceLocation var1);

    public int getDelay();

    public SoundEvent getSound();

    public int getTier();

    @Nullable
    public ResourceLocation getRequiredAdvancement();

    public ResourceLocation getId();

    public ResourceLocation getIcon();

    public boolean isStackLocked();

    public ItemStack getDefaultStack();

    @Nonnull
    public Item getValidDynamicItem();

    public List<ResourceLocation> getDefaultCombination();
}

