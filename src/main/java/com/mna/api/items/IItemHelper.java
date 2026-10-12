/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.items;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IItemHelper {
    public Item CreateStaff(Item.Properties var1, float var2, float var3);

    public Item CreateSpellBook(Item.Properties var1, @Nullable ResourceLocation var2, @Nullable ResourceLocation var3, boolean var4);

    public Item CreateGrimoire(Item.Properties var1, @Nullable ResourceLocation var2, @Nullable ResourceLocation var3, @Nullable ResourceLocation var4, boolean var5);

    public boolean AreTagsEqual(ItemStack var1, ItemStack var2);
}

