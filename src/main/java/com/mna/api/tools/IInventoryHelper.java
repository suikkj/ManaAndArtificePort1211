/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.IItemHandlerModifiable
 */
package com.mna.api.tools;

import com.mna.api.items.DynamicItemFilter;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public interface IInventoryHelper {
    public boolean hasRoomFor(IItemHandlerModifiable var1, ItemStack var2);

    public boolean mergeIntoInventory(IItemHandler var1, ItemStack var2);

    public boolean mergeIntoInventory(IItemHandler var1, ItemStack var2, int var3);

    public ItemStack mergeToPlayerInvPrioritizeOffhand(Player var1, ItemStack var2);

    public ItemStack getFirstItemFromContainer(List<Item> var1, int var2, IItemHandler var3, Direction var4);

    public ItemStack getFirstItemFromContainer(List<Item> var1, int var2, IItemHandler var3, Direction var4, boolean var5);

    public ItemStack getFirstItemFromContainer(DynamicItemFilter var1, int var2, IItemHandler var3, Direction var4);

    public ItemStack getFirstItemFromContainer(DynamicItemFilter var1, int var2, IItemHandler var3, Direction var4, boolean var5);

    public ItemStack getFirstItemFromContainer(DynamicItemFilter var1, int var2, IItemHandler var3, Direction var4, boolean var5, boolean var6);

    public boolean hasStackInInventory(DynamicItemFilter var1, IItemHandlerModifiable var2);

    public boolean hasStackInInventory(ItemStack var1, boolean var2, boolean var3, IItemHandlerModifiable var4);

    public boolean removeItemFromInventory(ItemStack var1, boolean var2, boolean var3, IItemHandlerModifiable var4);

    public boolean hasStackInInventory(ItemStack var1, boolean var2, boolean var3, IItemHandler var4, Direction var5);

    public boolean removeItemFromInventory(ItemStack var1, boolean var2, boolean var3, IItemHandler var4, Direction var5);

    public boolean consumeAcrossInventories(ItemStack var1, boolean var2, boolean var3, boolean var4, List<Pair<IItemHandler, Direction>> var5);

    public boolean consumeAcrossInventories(List<Item> var1, int var2, boolean var3, boolean var4, boolean var5, List<Pair<IItemHandler, Direction>> var6);

    public int countItem(ItemStack var1, IItemHandler var2, Direction var3, boolean var4, boolean var5);

    public ItemStack removeSingleItemFromInventory(ResourceLocation var1, Container var2);

    public void redirectCaptureOrDrop(Player var1, Level var2, List<ItemStack> var3, boolean var4);

    public Pair<Boolean, Boolean> getCaptureAndRedirect(Player var1);
}

