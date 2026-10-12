/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 *  org.joml.Vector3f
 */
package com.mna.api.items;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public interface IAreaItem<T extends Item> {
    public void setArea(ItemStack var1, AABB var2);

    public void setArea(ItemStack var1, BlockPos var2, int var3);

    public void setArea(ItemStack var1, BlockPos var2, BlockPos var3);

    public void copyAreaFrom(ItemStack var1, ItemStack var2);

    @Nullable
    public AABB getBounds(ItemStack var1);

    public void adjust(ItemStack var1, Vector3f var2, boolean var3);
}

