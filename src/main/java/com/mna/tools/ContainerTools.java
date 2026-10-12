/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.TransientCraftingContainer
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.tools;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class ContainerTools {
    public static CraftingContainer createTemporaryContainer(ItemStack item) {
        return ContainerTools.createTemporaryContainer(1, 1, (NonNullList<ItemStack>)NonNullList.of(item, (Object[])new ItemStack[]{item}));
    }

    public static CraftingContainer createTemporaryContainer(int width, int height, NonNullList<ItemStack> items) {
        return new TransientCraftingContainer((AbstractContainerMenu)new TemporaryMenu(), width, height, items);
    }

    public static class TemporaryMenu
    extends AbstractContainerMenu {
        public TemporaryMenu() {
            super((MenuType)null, -1);
        }

        public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
            return ItemStack.EMPTY;
        }

        public boolean stillValid(Player pPlayer) {
            return false;
        }
    }
}

