/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.inventory.CraftingMenu
 *  net.minecraft.world.inventory.RecipeBookType
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.gui.containers.block;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ContainerSpectralCraftingTable
extends CraftingMenu {
    public ContainerSpectralCraftingTable(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public ContainerSpectralCraftingTable(int id, Inventory playerInventory, ContainerLevelAccess p_i50090_3_) {
        super(id, playerInventory, p_i50090_3_);
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    @OnlyIn(value=Dist.CLIENT)
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }
}


