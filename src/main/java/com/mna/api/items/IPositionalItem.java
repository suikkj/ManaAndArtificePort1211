/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.mna.api.items;

import com.mna.api.blocks.DirectionalPoint;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public interface IPositionalItem<T extends Item> {
    public void setLocation(ItemStack var1, DirectionalPoint var2);

    public void setLocation(ItemStack var1, BlockPos var2, Direction var3);

    public void setLocation(ItemStack var1, BlockPos var2, Direction var3, Level var4);

    public void copyPositionFrom(ItemStack var1, ItemStack var2);

    @Nullable
    public BlockPos getLocation(ItemStack var1);

    public Direction getFace(ItemStack var1);

    @Nullable
    public Component getBlockName(ItemStack var1);

    @Nullable
    public DirectionalPoint getDirectionalPoint(ItemStack var1);

    @Nullable
    public String getBlockTranslateKey(ItemStack var1);

    @Nullable
    public ResourceLocation getMarkedBlockId(ItemStack var1);

    @Nullable
    default public Block getMarkedBlock(ItemStack stack) {
        ResourceLocation rLoc = this.getMarkedBlockId(stack);
        if (rLoc != null) {
            return (Block)BuiltInRegistries.BLOCK.get(rLoc);
        }
        return null;
    }
}


