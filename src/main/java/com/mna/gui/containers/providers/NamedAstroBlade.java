/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.containers.providers;

import com.mna.gui.containers.item.ContainerAstroBlade;
import com.mna.inventory.ItemInventoryBase;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class NamedAstroBlade
implements MenuProvider {
    private final ItemStack stack;

    public NamedAstroBlade(ItemStack stack) {
        this.stack = stack;
    }

    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ContainerAstroBlade(i, inventory, new ItemInventoryBase(this.stack, 6));
    }

    public Component getDisplayName() {
        return Component.literal((String)"");
    }
}

